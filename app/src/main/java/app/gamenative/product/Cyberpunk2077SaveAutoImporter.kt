package app.gamenative.product

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

class Cyberpunk2077SaveAutoImporter internal constructor(
    private val fileOps: FileOps = RealFileOps,
) {
    internal interface FileOps {
        fun copyNoReplaceAndSync(source: File, destination: File): Boolean
        fun listFiles(directory: File): Array<File>?
        fun sha256(file: File): String
    }

    internal object RealFileOps : FileOps {
        override fun copyNoReplaceAndSync(source: File, destination: File): Boolean {
            var destinationCreated = false
            return try {
                FileChannel.open(
                    destination.toPath(),
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE,
                ).use { output ->
                    destinationCreated = true
                    FileChannel.open(source.toPath(), StandardOpenOption.READ).use { input ->
                        var position = 0L
                        while (position < input.size()) {
                            val written = input.transferTo(position, input.size() - position, output)
                            check(written > 0) { "Unable to copy Cyberpunk 2077 save ${source.name}" }
                            position += written
                        }
                    }
                    output.force(true)
                }
                true
            } catch (_: FileAlreadyExistsException) {
                false
            } catch (error: Throwable) {
                if (destinationCreated) destination.delete()
                throw error
            }
        }

        override fun listFiles(directory: File): Array<File>? = directory.listFiles()

        override fun sha256(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    digest.update(buffer, 0, read)
                }
            }
            return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
        }
    }

    data class Request(
        val appId: String,
        val executablePath: String,
        val gameDirectory: File,
        val containerRoot: File,
        val localShell: Boolean,
        val customGame: Boolean,
        val bootToContainer: Boolean,
    )

    sealed interface Result {
        data object NotApplicable : Result
        data object NoSource : Result
        data object AlreadyHandled : Result
        data class Imported(val saveCount: Int) : Result
        data class PreservedExisting(val saveCount: Int) : Result
        data class Failed(val message: String) : Result
    }

    internal data class SaveDirectory(
        val directory: File,
        val files: List<File>,
    )

    fun importIfNeeded(request: Request): Result {
        if (!request.localShell || !request.customGame || request.bootToContainer) {
            return Result.NotApplicable
        }
        if (!GameFoldPcGameIdentity.isCyberpunk2077Executable(request.executablePath)) {
            return Result.NotApplicable
        }

        val decisionFile = File(request.containerRoot, "$STATE_RELATIVE/$DECISION_FILE")
        if (decisionFile.exists()) return readCompletedDecision(request, decisionFile)

        val sources = runCatching { discoverSourceSaves(request.gameDirectory) }
            .getOrElse { error ->
                return Result.Failed(error.message ?: "Unable to read Cyberpunk 2077 save source")
            }
        if (sources.isEmpty()) return Result.NoSource
        if (sources.map { it.directory.name.lowercase() }.distinct().size != sources.size) {
            return Result.Failed("Cyberpunk 2077 save source contains case-colliding directories")
        }

        val targetDirectory = File(request.containerRoot, TARGET_RELATIVE)
        val existingTargets = runCatching { discoverSaveDirectories(targetDirectory) }
            .getOrElse { error ->
                return Result.Failed(error.message ?: "Unable to read Cyberpunk 2077 save target")
            }
        if (existingTargets.isNotEmpty()) {
            return preserveExistingTarget(request, existingTargets.size)
        }

        return runCatching { importFreshTarget(request, sources) }
            .getOrElse { error ->
                Result.Failed(error.message ?: "Unknown Cyberpunk 2077 save import error")
            }
    }

    internal fun discoverSourceSaves(gameDirectory: File): List<SaveDirectory> =
        discoverSaveDirectories(File(gameDirectory, "saves"))

    private fun discoverSaveDirectories(directory: File): List<SaveDirectory> {
        if (!directory.exists()) return emptyList()
        check(directory.isDirectory) {
            "Cyberpunk 2077 save path is not a directory: ${directory.path}"
        }
        val children = fileOps.listFiles(directory)
            ?: error("Unable to enumerate Cyberpunk 2077 save directory: ${directory.path}")
        return children
            .asSequence()
            .filter { candidate ->
                candidate.isDirectory &&
                    !Files.isSymbolicLink(candidate.toPath()) &&
                    SAVE_DIRECTORY.matches(candidate.name)
            }
            .mapNotNull { saveDirectory ->
                val entries = fileOps.listFiles(saveDirectory)
                    ?: error("Unable to enumerate Cyberpunk 2077 save slot: ${saveDirectory.path}")
                val files = entries
                    .asSequence()
                    .filter { candidate ->
                        candidate.isFile &&
                            !Files.isSymbolicLink(candidate.toPath()) &&
                            SAVE_FILE.matches(candidate.name)
                    }
                    .sortedBy { it.name.lowercase() }
                    .toList()
                if (files.none { it.name.equals(PRIMARY_SAVE_FILE, ignoreCase = true) }) {
                    null
                } else {
                    check(files.map { it.name.lowercase() }.distinct().size == files.size) {
                        "Cyberpunk 2077 save slot contains case-colliding files"
                    }
                    SaveDirectory(saveDirectory, files)
                }
            }
            .sortedBy { it.directory.name.lowercase() }
            .toList()
    }

    private fun importFreshTarget(request: Request, sources: List<SaveDirectory>): Result {
        val targetDirectory = File(request.containerRoot, TARGET_RELATIVE)
        val targetCreated = !targetDirectory.exists()
        check(targetDirectory.mkdirs() || targetDirectory.isDirectory) {
            "Unable to create Cyberpunk 2077 save destination"
        }

        val createdDirectories = mutableListOf<File>()
        return try {
            val manifest = JSONArray()
            for (source in sources) {
                val destinationDirectory = File(targetDirectory, source.directory.name)
                if (!destinationDirectory.mkdir()) {
                    cleanupCreatedDirectories(createdDirectories, targetDirectory, targetCreated)
                    return preserveExistingTarget(request, 1)
                }
                createdDirectories += destinationDirectory

                val filesManifest = JSONArray()
                for (sourceFile in source.files) {
                    val destinationFile = File(destinationDirectory, sourceFile.name)
                    if (!fileOps.copyNoReplaceAndSync(sourceFile, destinationFile)) {
                        cleanupCreatedDirectories(createdDirectories, targetDirectory, targetCreated)
                        return preserveExistingTarget(request, 1)
                    }
                    val sourceHash = fileOps.sha256(sourceFile)
                    check(
                        sourceFile.length() == destinationFile.length() &&
                            sourceHash == fileOps.sha256(destinationFile),
                    ) { "Cyberpunk 2077 save verification failed for ${sourceFile.path}" }
                    filesManifest.put(
                        JSONObject()
                            .put("name", sourceFile.name)
                            .put("size", sourceFile.length())
                            .put("sha256", sourceHash),
                    )
                }
                manifest.put(
                    JSONObject()
                        .put("name", source.directory.name)
                        .put("files", filesManifest),
                )
            }

            writeAtomicJson(
                File(request.containerRoot, "$STATE_RELATIVE/$DECISION_FILE"),
                JSONObject()
                    .put("schemaVersion", SCHEMA_VERSION)
                    .put("status", "imported")
                    .put("appId", request.appId)
                    .put("sourcePath", File(request.gameDirectory, "saves").absolutePath)
                    .put("destinationPath", TARGET_RELATIVE)
                    .put("timestamp", System.currentTimeMillis())
                    .put("saves", manifest),
            )
            Result.Imported(sources.size)
        } catch (error: Throwable) {
            cleanupCreatedDirectories(createdDirectories, targetDirectory, targetCreated)
            throw error
        }
    }

    private fun cleanupCreatedDirectories(
        createdDirectories: List<File>,
        targetDirectory: File,
        targetCreated: Boolean,
    ) {
        createdDirectories.asReversed().forEach(File::deleteRecursively)
        if (targetCreated && targetDirectory.listFiles()?.isEmpty() == true) {
            targetDirectory.delete()
        }
    }

    private fun preserveExistingTarget(request: Request, saveCount: Int): Result {
        val decision = JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("status", "skipped_existing_target")
            .put("appId", request.appId)
            .put("sourcePath", File(request.gameDirectory, "saves").absolutePath)
            .put("destinationPath", TARGET_RELATIVE)
            .put("timestamp", System.currentTimeMillis())
        return runCatching {
            writeAtomicJson(
                File(request.containerRoot, "$STATE_RELATIVE/$DECISION_FILE"),
                decision,
            )
            Result.PreservedExisting(saveCount)
        }.getOrElse { error ->
            Result.Failed(error.message ?: "Unable to record preserved Cyberpunk 2077 save state")
        }
    }

    private fun readCompletedDecision(request: Request, decisionFile: File): Result {
        return runCatching {
            val decision = JSONObject(decisionFile.readText())
            check(decision.getInt("schemaVersion") == SCHEMA_VERSION)
            val status = decision.getString("status")
            check(status in FINAL_STATUSES)
            check(decision.getString("appId") == request.appId)
            check(
                decision.getString("sourcePath") ==
                    File(request.gameDirectory, "saves").absolutePath,
            )
            check(decision.getString("destinationPath") == TARGET_RELATIVE)
            check(decision.getLong("timestamp") >= 0)
            if (status == "imported") validateManifest(decision.getJSONArray("saves"))
            Result.AlreadyHandled
        }.getOrElse {
            Result.Failed("Cyberpunk 2077 save import state is invalid")
        }
    }

    private fun validateManifest(manifest: JSONArray) {
        check(manifest.length() > 0)
        val directoryNames = mutableSetOf<String>()
        for (index in 0 until manifest.length()) {
            val entry = manifest.getJSONObject(index)
            val directoryName = entry.getString("name")
            check(SAVE_DIRECTORY.matches(directoryName) && File(directoryName).name == directoryName)
            check(directoryNames.add(directoryName.lowercase()))
            val files = entry.getJSONArray("files")
            check(files.length() > 0)
            var hasPrimarySave = false
            val fileNames = mutableSetOf<String>()
            for (fileIndex in 0 until files.length()) {
                val file = files.getJSONObject(fileIndex)
                val name = file.getString("name")
                check(SAVE_FILE.matches(name) && File(name).name == name)
                check(fileNames.add(name.lowercase()))
                check(file.getLong("size") >= 0)
                check(SHA_256.matches(file.getString("sha256")))
                if (name.equals(PRIMARY_SAVE_FILE, ignoreCase = true)) hasPrimarySave = true
            }
            check(hasPrimarySave)
        }
    }

    private fun writeAtomicJson(destination: File, json: JSONObject) {
        check(destination.parentFile?.mkdirs() == true || destination.parentFile?.isDirectory == true) {
            "Unable to create Cyberpunk 2077 save state directory"
        }
        val temporary = File(destination.parentFile, "${destination.name}.tmp")
        FileOutputStream(temporary).use { output ->
            output.write(json.toString().toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        if (destination.exists()) {
            check(destination.delete()) { "Unable to replace Cyberpunk 2077 save state" }
        }
        check(temporary.renameTo(destination)) { "Unable to commit Cyberpunk 2077 save state" }
    }

    private companion object {
        const val SCHEMA_VERSION = 1
        const val PRIMARY_SAVE_FILE = "sav.dat"
        const val TARGET_RELATIVE =
            ".wine/drive_c/users/xuser/Saved Games/CD Projekt Red/Cyberpunk 2077"
        const val STATE_RELATIVE = ".gamefold/cyberpunk2077-save-import-v1"
        const val DECISION_FILE = "decision.json"
        val FINAL_STATUSES = setOf("imported", "skipped_existing_target")
        val SHA_256 = Regex("^[0-9a-f]{64}$")
        val SAVE_DIRECTORY = Regex(
            "^(?:ManualSave|AutoSave|QuickSave|EndGameSave|PointOfNoReturnSave)-[0-9]+$",
            RegexOption.IGNORE_CASE,
        )
        val SAVE_FILE = Regex(
            "^(?:sav\\.dat|screenshot\\.png|metadata\\.[0-9]+\\.json)$",
            RegexOption.IGNORE_CASE,
        )
    }
}

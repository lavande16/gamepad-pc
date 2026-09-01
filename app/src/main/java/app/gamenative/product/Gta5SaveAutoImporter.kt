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

class Gta5SaveAutoImporter internal constructor(
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
                            check(written > 0) { "Unable to copy GTA5 save ${source.name}" }
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
        data class Imported(val fileCount: Int) : Result
        data class PreservedExisting(val fileCount: Int) : Result
        data class Failed(val message: String) : Result
    }

    fun importIfNeeded(request: Request): Result {
        if (!request.localShell || !request.customGame || request.bootToContainer) {
            return Result.NotApplicable
        }
        if (!GameFoldPcGameIdentity.isGtaVExecutable(request.executablePath)) {
            return Result.NotApplicable
        }
        if (!isSupportedSocialClubBuild(File(request.gameDirectory, "socialclub.ini"))) {
            return Result.NotApplicable
        }

        runCatching { clearObsoleteTransactionFiles(request) }
            .getOrElse { error ->
                return Result.Failed(error.message ?: "Unable to clear obsolete GTA5 import files")
            }

        val decisionFile = File(request.containerRoot, "$STATE_RELATIVE/$DECISION_FILE")
        if (decisionFile.exists()) return readCompletedDecision(request, decisionFile)

        val sources = runCatching { discoverSourceFiles(request.gameDirectory) }
            .getOrElse { error ->
                return Result.Failed(error.message ?: "Unable to read GTA5 save source")
            }
        if (sources.isEmpty()) return Result.NoSource
        if (sources.map { it.name.lowercase() }.distinct().size != sources.size) {
            return Result.Failed("GTA5 save source contains case-colliding file names")
        }

        val existingTargets = runCatching {
            discoverAcceptedFiles(File(request.containerRoot, TARGET_RELATIVE))
        }.getOrElse { error ->
            return Result.Failed(error.message ?: "Unable to read GTA5 save target")
        }
        if (existingTargets.isNotEmpty()) {
            return preserveExistingTarget(request, existingTargets.size)
        }

        return runCatching { importFreshTarget(request, sources) }
            .getOrElse { error -> Result.Failed(error.message ?: "Unknown GTA5 save import error") }
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
            if (status == "imported") validateManifest(decision.getJSONArray("files"))
            Result.AlreadyHandled
        }.getOrElse {
            Result.Failed("GTA5 save import state is invalid")
        }
    }

    private fun clearObsoleteTransactionFiles(request: Request) {
        listOf(
            File(request.containerRoot, "$STATE_RELATIVE/$LEGACY_STAGING_DIR"),
            File(request.containerRoot, "$TARGET_RELATIVE/$LEGACY_TARGET_STAGING_DIR"),
        ).forEach { staging ->
            check(staging.deleteRecursively()) { "Unable to clear obsolete GTA5 save staging" }
        }
        val journal = File(request.containerRoot, "$STATE_RELATIVE/$LEGACY_JOURNAL_FILE")
        if (journal.exists()) {
            check(journal.delete()) { "Unable to clear obsolete GTA5 save journal" }
        }
    }

    private fun validateManifest(manifest: JSONArray) {
        check(manifest.length() > 0)
        val names = mutableSetOf<String>()
        for (index in 0 until manifest.length()) {
            val entry = manifest.getJSONObject(index)
            val name = entry.getString("name")
            check(GTA_SAVE_FILE.matches(name) && File(name).name == name)
            check(names.add(name.lowercase()))
            check(entry.getLong("size") >= 0)
            check(SHA_256.matches(entry.getString("sha256")))
        }
    }

    internal fun discoverSourceFiles(gameDirectory: File): List<File> {
        return discoverAcceptedFiles(File(gameDirectory, "saves"))
    }

    private fun discoverAcceptedFiles(directory: File): List<File> {
        if (!directory.exists()) return emptyList()
        check(directory.isDirectory) { "GTA5 save path is not a directory: ${directory.path}" }
        val children = fileOps.listFiles(directory)
            ?: error("Unable to enumerate GTA5 save directory: ${directory.path}")
        return children
            .asSequence()
            .filter { candidate ->
                candidate.isFile &&
                    !Files.isSymbolicLink(candidate.toPath()) &&
                    GTA_SAVE_FILE.matches(candidate.name)
            }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    private fun preserveExistingTarget(request: Request, fileCount: Int): Result {
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
            Result.PreservedExisting(fileCount)
        }.getOrElse { error ->
            Result.Failed(error.message ?: "Unable to record preserved GTA5 save state")
        }
    }

    private fun isSupportedSocialClubBuild(iniFile: File): Boolean {
        if (!iniFile.isFile) return false

        var section = ""
        var userName: String? = null
        var appId: String? = null
        iniFile.forEachLine { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith('[') && line.endsWith(']') -> {
                    section = line.substring(1, line.length - 1).trim().lowercase()
                }

                '=' in line -> {
                    val key = line.substringBefore('=').trim().lowercase()
                    val value = line.substringAfter('=').trim()
                    if (section == "settings" && key == "username") userName = value
                    if (section == "socialclub" && key == "appid") appId = value
                }
            }
        }
        return userName == "RLD!" && appId == "271590"
    }

    private fun importFreshTarget(request: Request, sources: List<File>): Result {
        val destinationDirectory = File(request.containerRoot, TARGET_RELATIVE)
        check(destinationDirectory.mkdirs() || destinationDirectory.isDirectory) {
            "Unable to create GTA5 save destination"
        }

        val manifest = JSONArray()
        for (source in sources) {
            val destination = File(destinationDirectory, source.name)
            if (!fileOps.copyNoReplaceAndSync(source, destination)) {
                val existingCount = discoverAcceptedFiles(destinationDirectory).size
                return preserveExistingTarget(request, existingCount.coerceAtLeast(1))
            }
            val sourceHash = fileOps.sha256(source)
            check(
                source.length() == destination.length() &&
                    sourceHash == fileOps.sha256(destination),
            ) { "GTA5 save verification failed for ${source.name}" }
            manifest.put(
                JSONObject()
                    .put("name", source.name)
                    .put("size", source.length())
                    .put("sha256", sourceHash),
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
                .put("files", manifest),
        )
        return Result.Imported(sources.size)
    }

    private fun writeAtomicJson(destination: File, json: JSONObject) {
        check(destination.parentFile?.mkdirs() == true || destination.parentFile?.isDirectory == true) {
            "Unable to create GTA5 save state directory"
        }
        val temporary = File(destination.parentFile, "${destination.name}.tmp")
        FileOutputStream(temporary).use { output ->
            output.write(json.toString().toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        if (destination.exists()) check(destination.delete()) { "Unable to replace GTA5 save state" }
        check(temporary.renameTo(destination)) { "Unable to commit GTA5 save state" }
    }

    private companion object {
        const val SCHEMA_VERSION = 1
        const val TARGET_RELATIVE = ".wine/drive_c/ProgramData/Socialclub/RLD!/271590"
        const val STATE_RELATIVE = ".gamefold/gta5-save-import-v1"
        const val DECISION_FILE = "decision.json"
        const val LEGACY_JOURNAL_FILE = "transaction.json"
        const val LEGACY_STAGING_DIR = "staging"
        const val LEGACY_TARGET_STAGING_DIR = ".gamefold-import-v1-staging"
        val FINAL_STATUSES = setOf("imported", "skipped_existing_target")
        val SHA_256 = Regex("^[0-9a-f]{64}$")
        val GTA_SAVE_FILE = Regex("^SGTA5[0-9]+(?:\\.bak)?$", RegexOption.IGNORE_CASE)
    }
}

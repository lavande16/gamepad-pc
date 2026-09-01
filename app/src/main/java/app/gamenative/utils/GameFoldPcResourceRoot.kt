package app.gamenative.utils

import android.os.Environment
import timber.log.Timber
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Resolves GamePad PC runtime archives from the user-managed external payload.
 *
 * The callers still use the logical asset names from GameNative (for example
 * `imagefs_bionic.txz` or `dxwrapper/dxvk-2.6.1-gplasync.tzst`). This keeps the
 * existing launch pipeline intact while allowing the large archives to live in
 * /storage/emulated/0/GamePadPC instead of inside the APK.
 */
object GameFoldPcResourceRoot {
    const val ROOT_NAME = "GamePadPC"
    const val MANIFEST_NAME = "manifest.json"

    data class ManifestValidationResult(
        val manifestPresent: Boolean,
        val valid: Boolean,
        val missing: List<String> = emptyList(),
        val invalid: List<String> = emptyList(),
        val manifestSha256: String? = null,
    ) {
        fun describe(): String = buildList {
            if (!manifestPresent) add("manifest.json")
            addAll(missing.map { "缺少：$it" })
            addAll(invalid.map { "校验失败：$it" })
        }.joinToString("\n")
    }

    @JvmStatic
    fun root(): File = File(Environment.getExternalStorageDirectory(), ROOT_NAME)

    @JvmStatic
    fun gameRoot(): File = File(root(), "game")

    @JvmStatic
    fun manifestFile(): File = File(root(), MANIFEST_NAME)

    @JvmStatic
    fun isReady(): Boolean = manifestFile().isFile

    /**
     * Validates the external resource manifest without ever contacting the network.
     * The manifest is deliberately checked from the public payload so clearing the
     * APK's private data cannot turn into a hidden re-download.
     */
    @JvmStatic
    fun validateManifest(verifyHashes: Boolean = true): ManifestValidationResult {
        return validateManifestAt(root(), verifyHashes)
    }

    internal fun validateManifestAt(payloadRoot: File, verifyHashes: Boolean): ManifestValidationResult {
        val root = payloadRoot.canonicalFile
        val file = File(root, MANIFEST_NAME)
        if (!file.isFile) return ManifestValidationResult(manifestPresent = false, valid = false)

        return try {
            val rawManifest = file.readBytes()
            val manifestSha256 = sha256(rawManifest)
            val json = JSONObject(rawManifest.toString(Charsets.UTF_8))
            val entries = json.optJSONArray("required")
                ?: json.optJSONArray("files")
                ?: return ManifestValidationResult(
                    manifestPresent = true,
                    valid = false,
                    invalid = listOf("manifest.json（缺少 required 文件清单）"),
                    manifestSha256 = manifestSha256,
                )

            val missing = mutableListOf<String>()
            val invalid = mutableListOf<String>()
            for (index in 0 until entries.length()) {
                val entry = entries.optJSONObject(index)
                if (entry == null) {
                    invalid += "manifest.json（第 ${index + 1} 项不是对象）"
                    continue
                }
                val relative = entry.optString("path").trim().replace('\\', '/').trimStart('/')
                val segments = relative.split('/')
                val resource = File(root, relative).canonicalFile
                if (relative.isEmpty() || segments.any { it == ".." } ||
                    !resource.path.startsWith(root.path + File.separator)
                ) {
                    invalid += if (relative.isEmpty()) "manifest.json（空路径）" else relative
                    continue
                }
                if (!resource.isFile || resource.length() <= 0L) {
                    missing += relative
                    continue
                }

                val expectedSize = entry.optLong("size", -1L)
                if (expectedSize >= 0L && resource.length() != expectedSize) {
                    invalid += "$relative（大小 ${resource.length()}，应为 $expectedSize）"
                    continue
                }

                val expectedHash = entry.optString("sha256").trim().lowercase()
                if (verifyHashes && expectedHash.isNotEmpty()) {
                    val actualHash = sha256(resource)
                    if (actualHash != expectedHash) {
                        invalid += "$relative（SHA-256 不匹配）"
                    }
                }
            }
            ManifestValidationResult(
                manifestPresent = true,
                valid = missing.isEmpty() && invalid.isEmpty(),
                missing = missing.sorted(),
                invalid = invalid.sorted(),
                manifestSha256 = manifestSha256,
            )
        } catch (e: Exception) {
            Timber.e(e, "GamePadPC manifest parse/validation failed")
            ManifestValidationResult(
                manifestPresent = true,
                valid = false,
                invalid = listOf("manifest.json（无法解析）"),
                manifestSha256 = runCatching { sha256(file.readBytes()) }.getOrNull(),
            )
        }
    }

    /**
     * Resolve either a logical GameNative asset path or a path relative to the
     * external GamePadPC root. Returns null when the external payload does not
     * contain the requested file.
     */
    @JvmStatic
    fun resolve(logicalPath: String?): File? {
        val path = logicalPath?.trim()?.replace('\\', '/')?.trimStart('/')
            ?: return null
        if (path.isEmpty()) return null

        resolveManifestAlias(path)?.let { alias ->
            val aliased = File(root(), alias)
            if (aliased.isFile && aliased.length() > 0L) return aliased
        }

        val candidates = LinkedHashSet<String>()
        fun add(relative: String) {
            if (relative.isNotBlank()) candidates.add(relative.trimStart('/'))
        }

        add(path)
        val basename = path.substringAfterLast('/')
        when {
            path.startsWith("graphics_driver/") -> add("graphics/$basename")
            path.startsWith("dxwrapper/") -> {
                when {
                    basename.startsWith("vkd3d-", ignoreCase = true) -> add("vkd3d/$basename")
                    basename.startsWith("d8vk-", ignoreCase = true) ||
                        basename.startsWith("dxvk-", ignoreCase = true) -> add("dxvk/$basename")
                    else -> add("dxvk/$basename")
                }
            }
            path.startsWith("container_files/") -> add("containers/$basename")
            path.startsWith("wincomponents/") -> add("wincomponents/$basename")
            path.startsWith("box86_64/") || path.startsWith("fexcore/") ||
                path.startsWith("wowbox64/") || path.startsWith("steampipe/") ||
                path.startsWith("steaminput/") || path.startsWith("inputcontrols/") ||
                path.startsWith("lsfg_vk/") || path.startsWith("bionic_fg/") ->
                add("containers/$path")
            basename.startsWith("imagefs", ignoreCase = true) -> add("imagefs/$basename")
            basename.endsWith(".wcp", ignoreCase = true) ||
                basename.startsWith("proton-", ignoreCase = true) -> add("proton/$basename")
            basename == "extras.tzst" || basename.startsWith("wincomponents", ignoreCase = true) ->
                add("wincomponents/$basename")
            basename.startsWith("container_pattern_", ignoreCase = true) ||
                basename.contains("container_pattern", ignoreCase = true) ||
                basename.startsWith("experimental-drm", ignoreCase = true) ->
                add("containers/$basename")
            basename.startsWith("pulseaudio-", ignoreCase = true) ||
                basename.endsWith("_input_dlls.tzst", ignoreCase = true) ->
                add("containers/runtime/$basename")
            basename == "redirect.tzst" -> add("containers/runtime/$basename")
        }

        for (relative in candidates) {
            val file = File(root(), relative)
            if (file.isFile && file.length() > 0L) {
                Timber.tag("GameFoldPcResources").d("Resolved $logicalPath -> ${file.absolutePath}")
                return file
            }
        }
        return null
    }

    /**
     * Resolve an Adreno/Turnip or wrapper driver archive from the external
     * graphics directory. Driver manifests have historically used several
     * naming schemes (for example `Turnip_v26.0.0_R4` versus
     * `adrenotools-turnip26.0.0_R4.tzst`). Exact semantic matches win. If
     * the requested Turnip build is not present, the newest local Turnip
     * archive is used as an explicit compatibility fallback, so a bundled
     * runtime does not silently trigger a network download.
     */
    @JvmStatic
    fun resolveGraphicsDriver(vararg logicalNames: String?): File? {
        logicalNames.asSequence()
            .filterNotNull()
            .mapNotNull { resolveManifestAlias(it) }
            .map { File(root(), it) }
            .firstOrNull { it.isFile && it.length() > 0L }
            ?.also { Timber.tag("GameFoldPcResources").i("Matched manifest graphics driver: ${it.absolutePath}") }
            ?.let { return it }

        logicalNames.asSequence()
            .filterNotNull()
            .mapNotNull { resolve(it) }
            .firstOrNull()
            ?.let { return it }

        val graphicsDir = File(root(), "graphics")
        val files = graphicsDir.listFiles()?.filter { file ->
            file.isFile && file.length() > 0L &&
                (file.name.endsWith(".tzst", ignoreCase = true) ||
                    file.name.endsWith(".tar.xz", ignoreCase = true) ||
                    file.name.endsWith(".zip", ignoreCase = true))
        } ?: return null

        val wantedKeys = logicalNames.asSequence()
            .filterNotNull()
            .map { driverKey(it) }
            .filter { it.isNotEmpty() }
            .toSet()
        if (wantedKeys.isEmpty()) return null

        files.firstOrNull { driverKey(it.name) in wantedKeys }
            ?.also { Timber.tag("GameFoldPcResources").i("Matched external graphics driver: ${it.absolutePath}") }
            ?.let { return it }

        val wantsTurnip = wantedKeys.any { it.contains("turnip") }
        if (wantsTurnip) {
            return files
                .filter { driverKey(it.name).contains("turnip") }
                .maxWithOrNull(
                    compareBy<File> { versionScore(it.name) }
                        .thenBy { if (it.name.contains("r4", ignoreCase = true)) 1 else 0 },
                )
                ?.also {
                    Timber.tag("GameFoldPcResources").w(
                        "Exact graphics driver is unavailable; using local Turnip fallback: ${it.absolutePath}",
                    )
                }
        }
        return null
    }

    /**
     * Resolves only an exact public graphics-driver archive. Unlike
     * [resolveGraphicsDriver], this never substitutes another Turnip build.
     */
    @JvmStatic
    fun resolveGraphicsDriverExact(vararg logicalNames: String?): File? =
        resolveGraphicsDriverExactAt(root(), *logicalNames)

    internal fun resolveGraphicsDriverExactAt(payloadRoot: File, vararg logicalNames: String?): File? {
        val wantedKeys = logicalNames.asSequence()
            .filterNotNull()
            .map { driverKey(it) }
            .filter { it.isNotEmpty() }
            .toSet()
        if (wantedKeys.isEmpty()) return null

        val files = File(payloadRoot, "graphics").listFiles()?.filter { file ->
            file.isFile && file.length() > 0L &&
                file.name.endsWith(".tzst", ignoreCase = true)
        }.orEmpty()

        return files.firstOrNull { driverKey(it.name) in wantedKeys }
    }

    /**
     * Resolve a DXVK/VKD3D archive. Exact names win; when the external
     * payload has a different patch-level archive, use the newest archive of
     * the same family already on disk instead of downloading another copy.
     */
    @JvmStatic
    fun resolveDxWrapper(vararg logicalNames: String?): File? {
        logicalNames.asSequence()
            .filterNotNull()
            .mapNotNull { resolve("dxwrapper/$it.tzst") ?: resolve(it) }
            .firstOrNull()
            ?.let { return it }

        val requested = logicalNames.asSequence()
            .filterNotNull()
            .map { it.substringAfterLast('/').lowercase() }
            .firstOrNull { it.startsWith("dxvk-") || it.startsWith("vkd3d-") || it.startsWith("d8vk-") }
            ?: return null
        val family = requested.substringBefore('-')
        // The external layout keeps all DirectX wrappers together under dxvk/;
        // the logical family name is still used to filter the archive names.
        val directory = File(root(), if (family == "d8vk") "dxvk" else family)
        val candidates = directory.listFiles()?.filter { file ->
            file.isFile && file.length() > 0L &&
                file.name.lowercase().startsWith("$family-") &&
                file.name.endsWith(".tzst", ignoreCase = true)
        } ?: return null
        return candidates
            .maxWithOrNull(compareBy<File> { versionScore(it.name) })
            ?.also {
                Timber.tag("GameFoldPcResources").w(
                    "Exact $family archive is unavailable; using local fallback: ${it.absolutePath}",
                )
            }
    }

    private fun driverKey(value: String): String {
        var key = value.substringAfterLast('/')
            .lowercase()
            .removeSuffix(".tar.xz")
            .removeSuffix(".tzst")
            .removeSuffix(".zip")
            .replace(Regex("^adrenotools[-_]"), "")
            .replace(Regex("^graphicsdriver[-_]"), "")
            .replace(Regex("v(?=\\d)"), "")
            .replace(Regex("[^a-z0-9]+"), "")
        return key
    }

    private fun resolveManifestAlias(logicalPath: String): String? {
        val file = manifestFile()
        if (!file.isFile) return null
        return try {
            val aliases = JSONObject(file.readText()).optJSONObject("aliases") ?: return null
            val normalized = logicalPath.trim().replace('\\', '/').trimStart('/')
            aliases.optString(normalized).trim().takeIf { it.isNotEmpty() }
                ?: aliases.optString(logicalPath.substringAfterLast('/')).trim().takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        BufferedInputStream(FileInputStream(file), 1024 * 1024).use { input ->
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count <= 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().toHex()
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun versionScore(value: String): Long {
        val match = Regex("(\\d+)\\.(\\d+)(?:\\.(\\d+))?").find(value) ?: return 0L
        val major = match.groupValues[1].toLongOrNull() ?: 0L
        val minor = match.groupValues[2].toLongOrNull() ?: 0L
        val patch = match.groupValues.getOrNull(3)?.toLongOrNull() ?: 0L
        return major * 1_000_000L + minor * 1_000L + patch
    }
}

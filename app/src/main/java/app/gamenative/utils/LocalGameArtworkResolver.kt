package app.gamenative.utils

import java.io.File
import java.util.Locale

/**
 * Resolves artwork using only files already present beside a local Windows game.
 *
 * The resolver intentionally has no Android, network, or metadata-service dependency so it
 * can be used from the existing IO scan path without changing the launch/runtime pipeline.
 */
object LocalGameArtworkResolver {
    private val imageExtensions = listOf("png", "jpg", "jpeg", "webp", "ico")

    private val namedCandidates = listOf(
        listOf("cover_square", "cover"),
        listOf("coverv", "poster", "artwork"),
        listOf("steamgriddb_grid_capsule", "grid_capsule"),
        listOf("icon", "logo", "steamgriddb_logo"),
    )

    /**
     * Finds one deterministic local image for [folder]. Only the folder root and one level of
     * child directories are inspected. [preferredExecutableRelativePath] is the executable
     * configured for the game container, when one is available.
     */
    fun resolve(folder: File, preferredExecutableRelativePath: String? = null): File? {
        if (!folder.isDirectory) return null

        namedCandidates.flatten().forEach { baseName ->
            findNamedFile(folder, baseName)?.let { return it }
        }

        if (!preferredExecutableRelativePath.isNullOrBlank()) {
            findExecutableIcon(folder, preferredExecutableRelativePath)?.let { return it }
        }

        findUniqueExecutable(folder)?.let { executable ->
            findExecutableIcon(folder, executable)?.let { return it }
        }

        return findUnambiguousNearbyImage(folder)
    }

    private fun findNamedFile(folder: File, baseName: String): File? {
        val files = folder.listFiles { file -> file.isFile } ?: return null
        return files
            .filter { file ->
                file.nameWithoutExtension.equals(baseName, ignoreCase = true) &&
                    isSupportedImage(file)
            }
            .minByOrNull { extensionRank(it) }
    }

    private fun findExecutableIcon(folder: File, relativePath: String): File? {
        val normalized = relativePath.replace('\\', File.separatorChar).replace('/', File.separatorChar)
        val executable = File(folder, normalized)
        if (!executable.isFile || !executable.name.endsWith(".exe", ignoreCase = true)) return null

        val cached = File(executable.parentFile ?: folder, executable.nameWithoutExtension + ".extracted.ico")
        if (cached.isFile && cached.lastModified() >= executable.lastModified()) return cached

        return runCatching {
            if (ExeIconExtractor.tryExtractMainIcon(executable, cached)) cached else null
        }.getOrNull()
    }

    private fun findUniqueExecutable(folder: File): String? {
        fun File.isCandidate(): Boolean = isFile &&
            name.endsWith(".exe", ignoreCase = true) &&
            !name.startsWith("unins", ignoreCase = true) &&
            listOf("launcher", "crash", "report", "setup", "updater").none {
                name.contains(it, ignoreCase = true)
            }

        val candidates = buildList<File> {
            fun visit(directory: File, depth: Int) {
                directory.listFiles()?.sortedBy { it.name.lowercase(Locale.ROOT) }?.forEach { file ->
                    when {
                        file.isCandidate() -> add(file)
                        file.isDirectory && depth < MAX_EXECUTABLE_SEARCH_DEPTH -> visit(file, depth + 1)
                    }
                }
            }
            visit(folder, 0)
        }.distinctBy { it.absolutePath }

        val normalizedFolderName = normalizeIdentity(folder.name)
        val gameNamed = candidates.filter { executable ->
            normalizeIdentity(executable.nameWithoutExtension) == normalizedFolderName
        }
        val selected = gameNamed.singleOrNull() ?: candidates.singleOrNull() ?: return null
        return selected.relativeTo(folder).path.replace(File.separatorChar, '/')
    }

    private fun normalizeIdentity(value: String): String =
        value.lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)

    private fun findUnambiguousNearbyImage(folder: File): File? {
        val candidates = buildList {
            folder.listFiles()?.filter { it.isFile && isSupportedImage(it) }?.forEach { add(it) }
            folder.listFiles { file -> file.isDirectory }?.forEach { child ->
                child.listFiles()?.filter { it.isFile && isSupportedImage(it) }?.forEach { add(it) }
            }
        }.distinctBy { it.absolutePath }

        if (candidates.size == 1) return candidates.single()

        // A single icon/logo-like file is still unambiguous when other nearby images are
        // screenshots or unrelated assets. Multiple icon-like files remain ambiguous.
        return candidates
            .filter { it.name.contains("icon", ignoreCase = true) || it.name.contains("logo", ignoreCase = true) }
            .singleOrNull()
    }

    private fun isSupportedImage(file: File): Boolean =
        imageExtensions.contains(file.extension.lowercase(Locale.ROOT))

    private fun extensionRank(file: File): Int {
        val rank = imageExtensions.indexOf(file.extension.lowercase(Locale.ROOT))
        return if (rank >= 0) rank else Int.MAX_VALUE
    }

    private const val MAX_EXECUTABLE_SEARCH_DEPTH = 3
}

package app.gamenative.utils

import java.io.File
import java.util.Locale

/** Resolves a launchable Windows executable strictly inside one local game folder. */
object LocalGameExecutableResolver {
    private const val MAX_SCAN_DEPTH = 4

    private val ignoredExactNames = setOf(
        "setup.exe",
        "install.exe",
        "installer.exe",
        "redist.exe",
        "dxsetup.exe",
    )

    private val ignoredNameFragments = listOf(
        "crashreport",
        "crashpad",
        "crashhandler",
        "benchmark",
        "vcredist",
        "redistributable",
        "helper",
        "updater",
        "bootstrapper",
        "patcher",
    )

    fun resolve(gameFolder: File, persistedRelativePath: String? = null): String? {
        val root = runCatching { gameFolder.canonicalFile }.getOrNull() ?: return null
        if (!root.isDirectory) return null

        validatePersisted(root, persistedRelativePath)?.let { return it }

        val candidates = mutableListOf<Candidate>()
        scan(root = root, directory = root, depth = 0, output = candidates)
        return candidates
            .sortedWith(
                compareByDescending<Candidate> { it.score }
                    .thenBy { it.depth }
                    .thenComparator { left, right ->
                        compareRelativePaths(left.relativePath, right.relativePath)
                    },
            )
            .firstOrNull()
            ?.relativePath
    }

    fun resolveForMappedGame(
        registeredGameFolder: File?,
        mappedGameFolder: File?,
        persistedRelativePath: String? = null,
    ): String? {
        val registered = runCatching { registeredGameFolder?.canonicalFile }.getOrNull() ?: return null
        val mapped = runCatching { mappedGameFolder?.canonicalFile }.getOrNull() ?: return null
        if (registered != mapped) return null
        return resolve(registered, persistedRelativePath)
    }

    internal fun compareRelativePaths(left: String, right: String): Int {
        val caseInsensitive = left.lowercase(Locale.ROOT).compareTo(right.lowercase(Locale.ROOT))
        return if (caseInsensitive != 0) caseInsensitive else left.compareTo(right)
    }

    private fun validatePersisted(root: File, persistedRelativePath: String?): String? {
        val normalized = persistedRelativePath
            ?.trim()
            ?.replace('\\', '/')
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        if (File(normalized).isAbsolute) return null

        val candidate = runCatching { File(root, normalized).canonicalFile }.getOrNull() ?: return null
        if (!isInside(root, candidate) || !candidate.isFile || !candidate.name.endsWith(".exe", true)) {
            return null
        }
        return candidate.relativeTo(root).invariantSeparatorsPath
    }

    private fun scan(root: File, directory: File, depth: Int, output: MutableList<Candidate>) {
        if (depth > MAX_SCAN_DEPTH) return
        val children = directory.listFiles()?.sortedBy { it.name.lowercase(Locale.ROOT) } ?: return
        for (child in children) {
            val canonicalChild = runCatching { child.canonicalFile }.getOrNull() ?: continue
            if (!isInside(root, canonicalChild)) continue
            when {
                canonicalChild.isFile && isCandidate(canonicalChild) -> {
                    val relativePath = canonicalChild.relativeTo(root).invariantSeparatorsPath
                    output += Candidate(
                        relativePath = relativePath,
                        depth = relativePath.count { it == '/' },
                        score = score(root, canonicalChild, relativePath),
                    )
                }

                canonicalChild.isDirectory && depth < MAX_SCAN_DEPTH -> {
                    scan(root, canonicalChild, depth + 1, output)
                }
            }
        }
    }

    private fun isCandidate(file: File): Boolean {
        if (!file.name.endsWith(".exe", ignoreCase = true)) return false
        val name = file.name.lowercase(Locale.ROOT)
        if (name.startsWith("unins") || name.startsWith("uninstall")) return false
        if (name in ignoredExactNames) return false
        return ignoredNameFragments.none(name::contains)
    }

    private fun score(root: File, file: File, relativePath: String): Int {
        val baseName = file.name.substringBeforeLast('.').lowercase(Locale.ROOT)
        val normalizedBase = normalizeName(baseName)
        val normalizedFolder = normalizeName(root.name)
        var score = 0

        if (baseName.contains("launcher")) score += 1_000
        if (baseName.startsWith("play") || baseName.contains("playgame")) score += 700
        if (baseName.startsWith("start")) score += 650
        if (baseName.endsWith("win64-shipping")) score += 600
        if (normalizedFolder.length >= 4 && normalizedBase == normalizedFolder) score += 500
        if (normalizedFolder.length >= 4 && normalizedBase.contains(normalizedFolder)) score += 400

        score -= relativePath.count { it == '/' } * 5
        return score
    }

    private fun normalizeName(value: String): String = buildString(value.length) {
        value.lowercase(Locale.ROOT).forEach { char ->
            if (char.isLetterOrDigit()) append(char)
        }
    }

    private fun isInside(root: File, candidate: File): Boolean {
        if (candidate == root) return false
        return candidate.path.startsWith(root.path + File.separator)
    }

    private data class Candidate(
        val relativePath: String,
        val depth: Int,
        val score: Int,
    )
}

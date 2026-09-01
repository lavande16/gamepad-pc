package app.gamenative.utils

import java.io.File
import java.util.Locale
import kotlin.math.ln

/** Offline, deterministic executable and DirectX-wrapper selection for one game folder. */
object LocalGameLaunchAnalyzer {
    private const val MAX_SCAN_DEPTH = 5
    private const val MAX_EXECUTABLE_CANDIDATES = 24

    data class Decision(
        val launchExecutable: String,
        val runtimeExecutable: String,
        val dxWrapper: String,
        val executableConfidence: Double,
        val wrapperConfidence: Double,
        val evidence: List<String>,
    )

    private data class Candidate(
        val file: File,
        val relativePath: String,
        val info: PortableExecutableInspector.Info?,
        val score: Int,
        val evidence: List<String>,
    )

    private val excludedDirectories = setOf(
        "_commonredist", "commonredist", "redist", "redistributable", "crashreporter",
        "tools", "redmod", "installer", "support",
    )
    private val excludedNameParts = listOf(
        "crashreport", "crashhandler", "errorreporter", "benchmark", "vcredist",
        "dxsetup", "uninstall", "updater", "patcher",
    )
    private val excludedExactNames = setOf(
        "setup.exe", "install.exe", "installer.exe", "redist.exe", "dxsetup.exe", "rapidcrc.exe",
    )

    fun analyze(gameFolder: File, persistedRelativePath: String? = null): Decision? {
        val root = runCatching { gameFolder.canonicalFile }.getOrNull() ?: return null
        if (!root.isDirectory) return null

        val directories = linkedSetOf<String>()
        val files = mutableListOf<Pair<File, String>>()
        scan(root, root, 0, directories, files)

        val executableFiles = files
            .filter { (_, path) -> isExecutableCandidate(path) }
            .sortedWith(compareBy<Pair<File, String>> { it.second.lowercase(Locale.ROOT) }.thenBy { it.second })
            .take(MAX_EXECUTABLE_CANDIDATES)
        if (executableFiles.isEmpty()) return null

        val peByPath = mutableMapOf<String, PortableExecutableInspector.Info>()
        val candidates = executableFiles.map { (file, relativePath) ->
            val info = inspect(file)?.also { peByPath[relativePath] = it }
            scoreCandidate(root.name, file, relativePath, directories, info)
        }.sortedWith(
            compareByDescending<Candidate> { it.score }
                .thenBy { it.relativePath.lowercase(Locale.ROOT) }
                .thenBy { it.relativePath },
        )

        val persisted = validatePersisted(root, persistedRelativePath)
        val runtime = if (persisted != null) {
            candidates.firstOrNull { it.relativePath == persisted } ?: candidates.first()
        } else {
            candidates.first()
        }
        val (launch, chainEvidence) = selectLaunchEntry(runtime, candidates)
        val (wrapper, wrapperConfidence, wrapperEvidence) = selectWrapper(
            root = root,
            runtime = runtime,
            directories = directories,
            files = files,
            peByPath = peByPath,
        )
        val secondScore = candidates.firstOrNull { it !== runtime }?.score
        return Decision(
            launchExecutable = if (persisted != null) persisted else launch.relativePath,
            runtimeExecutable = runtime.relativePath,
            dxWrapper = wrapper,
            executableConfidence = confidence(runtime.score, secondScore),
            wrapperConfidence = wrapperConfidence,
            evidence = buildList {
                addAll(runtime.evidence)
                chainEvidence?.let(::add)
                addAll(wrapperEvidence)
            },
        )
    }

    private fun scan(
        root: File,
        directory: File,
        depth: Int,
        directories: MutableSet<String>,
        files: MutableList<Pair<File, String>>,
    ) {
        if (depth > MAX_SCAN_DEPTH) return
        val children = directory.listFiles()?.sortedWith(
            compareBy<File> { it.name.lowercase(Locale.ROOT) }.thenBy { it.name },
        ) ?: return
        for (child in children) {
            val canonical = runCatching { child.canonicalFile }.getOrNull() ?: continue
            if (!isInside(root, canonical)) continue
            val relative = canonical.relativeTo(root).invariantSeparatorsPath
            when {
                canonical.isDirectory -> {
                    directories += relative.lowercase(Locale.ROOT)
                    if (depth < MAX_SCAN_DEPTH) scan(root, canonical, depth + 1, directories, files)
                }

                canonical.isFile && (
                    canonical.name.endsWith(".exe", true) || canonical.name.endsWith(".dll", true)
                ) -> files += canonical to relative
            }
        }
    }

    private fun isExecutableCandidate(relativePath: String): Boolean {
        val normalized = relativePath.replace('\\', '/')
        if (!normalized.endsWith(".exe", true)) return false
        val parts = normalized.split('/')
        if (parts.dropLast(1).any { it.lowercase(Locale.ROOT) in excludedDirectories }) return false
        val name = parts.last().lowercase(Locale.ROOT)
        if (name in excludedExactNames || name.startsWith("unins") || name.startsWith("setup_") || name.startsWith("setup-")) {
            return false
        }
        return excludedNameParts.none(name::contains)
    }

    private fun scoreCandidate(
        gameName: String,
        file: File,
        relativePath: String,
        directories: Set<String>,
        info: PortableExecutableInspector.Info?,
    ): Candidate {
        val normalizedPath = relativePath.replace('\\', '/')
        val lowerPath = normalizedPath.lowercase(Locale.ROOT)
        val name = file.name
        val base = file.nameWithoutExtension
        val normalizedBase = normalize(base)
        val normalizedGame = normalize(gameName)
        val evidence = mutableListOf("payload size ${file.length()} bytes")
        var score = sizeScore(file.length())

        if (info?.is64Bit == true) {
            score += 140
            evidence += "PE32+ x86-64"
        }
        if (info?.subsystem == 2) score += 20
        if ("/bin/x64/" in "/$lowerPath/" || lowerPath.startsWith("bin/x64/")) {
            score += 450
            evidence += "main-runtime directory bin/x64"
        }
        if ("binaries/win64/" in lowerPath) {
            score += 450
            evidence += "main-runtime directory Binaries/Win64"
        }
        if (normalizedBase.endsWith("win64shipping")) {
            score += 550
            evidence += "Unreal Win64-Shipping executable"
        }
        if (normalizedGame.isNotEmpty() && normalizedBase == normalizedGame) {
            score += 450
            evidence += "executable name matches game folder"
        } else if (
            normalizedGame.isNotEmpty() &&
            (normalizedBase in normalizedGame || normalizedGame in normalizedBase) &&
            minOf(normalizedBase.length, normalizedGame.length) >= 5
        ) {
            score += 240
            evidence += "executable name resembles game folder"
        }
        if (normalize(file.parentFile?.name.orEmpty()) == normalizedGame && normalizedGame.isNotEmpty()) {
            score += 150
            evidence += "located in game-named payload directory"
        }
        if (hasUnityDataDirectory(normalizedPath, directories)) {
            score += 550
            evidence += "matching Unity _Data directory"
        }

        val imports = graphicsImports(info)
        if (imports.any { "d3d12" in it || "dx12" in it }) {
            score += 240
            evidence += "direct DX12 module evidence"
        } else if (imports.any(::isLegacyDirectXModule)) {
            score += 200
            evidence += "direct Direct3D/DXGI module evidence"
        }
        if (imports.any { it.contains("unityplayer") }) {
            score += 150
            evidence += "UnityPlayer dependency"
        }
        if (imports.any { it.startsWith("qt5") }) {
            score -= 250
            evidence += "Qt utility/launcher signature"
        }

        val lowerName = name.lowercase(Locale.ROOT)
        when {
            "prelauncher" in lowerName -> {
                score -= 450
                evidence += "prelauncher penalty"
            }

            "launcher" in lowerName -> {
                score -= 220
                evidence += "launcher is not treated as runtime"
            }

            lowerName.startsWith("start") || lowerName.startsWith("play") -> {
                score -= 100
                evidence += "generic start/play helper penalty"
            }
        }
        return Candidate(file, normalizedPath, info, score, evidence)
    }

    private fun selectLaunchEntry(runtime: Candidate, candidates: List<Candidate>): Pair<Candidate, String?> {
        val runtimeName = runtime.file.name
        val runtimeParent = runtime.file.parentFile?.canonicalPath.orEmpty()
        val runtimeStem = normalize(runtime.file.nameWithoutExtension)
        for (candidate in candidates) {
            if (candidate === runtime || candidate.info == null) continue
            val lowerName = candidate.file.name.lowercase(Locale.ROOT)
            if ("launcher" !in lowerName || "prelauncher" in lowerName) continue
            if (candidate.file.parentFile?.canonicalPath.orEmpty() != runtimeParent) continue
            if (runtime.info != null && candidate.info.machine != runtime.info.machine) continue
            val references = candidate.info.referencedExecutables.map { it.lowercase(Locale.ROOT) }.toSet()
            if (runtimeName.lowercase(Locale.ROOT) !in references) continue
            val launcherStem = normalize(candidate.file.nameWithoutExtension).replace("launcher", "")
            if (commonPrefixLength(launcherStem, runtimeStem) < 3) continue
            return candidate to "launcher ${candidate.file.name} explicitly references runtime $runtimeName"
        }
        return runtime to null
    }

    private fun selectWrapper(
        root: File,
        runtime: Candidate,
        directories: Set<String>,
        files: List<Pair<File, String>>,
        peByPath: MutableMap<String, PortableExecutableInspector.Info>,
    ): Triple<String, Double, List<String>> {
        val imports = graphicsImports(runtime.info).toMutableSet()
        val evidence = mutableListOf<String>()
        val byBaseName = files
            .filter { (file, _) -> file.name.endsWith(".dll", true) }
            .groupBy { (file, _) -> file.name.lowercase(Locale.ROOT) }

        imports.sortedWith(stableTextComparator()).forEach { dependency ->
            byBaseName[File(dependency).name.lowercase(Locale.ROOT)]
                .orEmpty()
                .sortedWith(compareBy<Pair<File, String>> { it.second.lowercase(Locale.ROOT) }.thenBy { it.second })
                .forEach { (file, path) ->
                    val info = peByPath[path] ?: inspect(file)?.also { peByPath[path] = it } ?: return@forEach
                    val nested = graphicsImports(info)
                    if (nested.isNotEmpty()) {
                        imports += nested
                        evidence += "inspected dependency $path"
                    }
                }
        }

        val dx12 = imports.filter { "d3d12" in it || "dx12" in it }.sortedWith(stableTextComparator())
        if (dx12.isNotEmpty()) {
            evidence += "DX12 evidence: ${dx12.take(4).joinToString()}"
            return Triple("vkd3d", 0.96, evidence)
        }
        val legacy = imports.filter(::isLegacyDirectXModule).sortedWith(stableTextComparator())
        if (legacy.isNotEmpty()) {
            evidence += "DX9/10/11 evidence: ${legacy.take(4).joinToString()}"
            return Triple("dxvk", 0.92, evidence)
        }
        if (hasUnityDataDirectory(runtime.relativePath, directories)) {
            evidence += "Unity fallback defaults to DXVK"
            return Triple("dxvk", 0.72, evidence)
        }
        evidence += "no decisive graphics import; safe default DXVK"
        return Triple("dxvk", 0.45, evidence)
    }

    private fun validatePersisted(root: File, path: String?): String? {
        val normalized = path?.trim()?.replace('\\', '/')?.takeIf { it.isNotEmpty() } ?: return null
        if (File(normalized).isAbsolute) return null
        val candidate = runCatching { File(root, normalized).canonicalFile }.getOrNull() ?: return null
        if (!isInside(root, candidate) || !candidate.isFile || !candidate.name.endsWith(".exe", true)) return null
        return candidate.relativeTo(root).invariantSeparatorsPath
    }

    private fun inspect(file: File): PortableExecutableInspector.Info? =
        runCatching { PortableExecutableInspector.inspect(file) }.getOrNull()

    private fun graphicsImports(info: PortableExecutableInspector.Info?): Set<String> =
        info?.let { (it.imports + it.delayImports).mapTo(linkedSetOf()) { name -> name.lowercase(Locale.ROOT) } }
            ?: emptySet()

    private fun isLegacyDirectXModule(value: String): Boolean {
        val name = File(value).name.lowercase(Locale.ROOT)
        return name == "dxgi.dll" || name.matches(Regex("d3d(9|10|11)(_[0-9]+)?\\.dll"))
    }

    private fun hasUnityDataDirectory(relativePath: String, directories: Set<String>): Boolean {
        val normalized = relativePath.replace('\\', '/')
        val parent = normalized.substringBeforeLast('/', "")
        val base = normalized.substringAfterLast('/').substringBeforeLast('.')
        val expected = listOf(parent, "${base}_Data").filter { it.isNotEmpty() }.joinToString("/")
        return expected.lowercase(Locale.ROOT) in directories
    }

    private fun sizeScore(size: Long): Int {
        if (size <= 64L * 1024L) return 0
        val log2 = ln(size.toDouble() / (64.0 * 1024.0)) / ln(2.0)
        return minOf(350, maxOf(0, (log2 * 40).toInt()))
    }

    private fun confidence(topScore: Int, secondScore: Int?): Double {
        if (secondScore == null) return 0.93
        val margin = maxOf(0, topScore - secondScore)
        return minOf(0.97, 0.68 + margin / 2400.0).let { kotlin.math.round(it * 100) / 100 }
    }

    private fun normalize(value: String): String = buildString(value.length) {
        value.lowercase(Locale.ROOT).forEach { if (it.isLetterOrDigit()) append(it) }
    }

    private fun commonPrefixLength(left: String, right: String): Int =
        left.zip(right).takeWhile { (a, b) -> a == b }.size

    private fun stableTextComparator(): Comparator<String> =
        compareBy<String> { it.lowercase(Locale.ROOT) }.thenBy { it }

    private fun isInside(root: File, candidate: File): Boolean =
        candidate != root && candidate.path.startsWith(root.path + File.separator)
}

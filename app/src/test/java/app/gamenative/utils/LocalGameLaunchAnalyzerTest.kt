package app.gamenative.utils

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalGameLaunchAnalyzerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun analyze_selectsCyberpunkRuntimeAndVkd3dFromLocalDependency() {
        val root = temporaryFolder.newFolder("Cyberpunk 2077")
        writePe(root, "REDprelauncher.exe", machine = 0x14c, imports = listOf("Qt5Core.dll"))
        writePe(root, "开始游戏11.exe", machine = 0x14c, refs = listOf("Cyberpunk2077.exe"))
        writePe(root, "bin/x64/Cyberpunk2077.exe", imports = listOf("ffx_backend_dx12_x64.dll"))
        writePe(root, "bin/x64/ffx_backend_dx12_x64.dll", imports = listOf("d3d12.dll", "dxgi.dll"))

        val decision = LocalGameLaunchAnalyzer.analyze(root)!!

        assertEquals("bin/x64/Cyberpunk2077.exe", decision.launchExecutable)
        assertEquals("bin/x64/Cyberpunk2077.exe", decision.runtimeExecutable)
        assertEquals("vkd3d", decision.dxWrapper)
        assertTrue(decision.wrapperConfidence >= 0.85)
    }

    @Test
    fun analyze_selectsGtaLauncherChainAndDxvk() {
        val root = temporaryFolder.newFolder("Grand Theft Auto V")
        writePe(root, "Grand Theft Auto V/GTA5.exe", imports = listOf("d3d9.dll"), padding = 200_000)
        writePe(
            root,
            "Grand Theft Auto V/GTAVLauncher.exe",
            refs = listOf("GTA5.exe"),
            padding = 100_000,
        )
        writePe(root, "Grand Theft Auto V/sc.exe", imports = listOf("Qt5Core.dll"), padding = 150_000)

        val decision = LocalGameLaunchAnalyzer.analyze(root)!!

        assertEquals("Grand Theft Auto V/GTAVLauncher.exe", decision.launchExecutable)
        assertEquals("Grand Theft Auto V/GTA5.exe", decision.runtimeExecutable)
        assertEquals("dxvk", decision.dxWrapper)
        assertTrue(decision.evidence.any { it.contains("explicitly references") })
    }

    @Test
    fun analyze_selectsUnityExecutableAndDxvk() {
        val root = temporaryFolder.newFolder("The Ramp")
        File(root, "The Ramp_Data").mkdirs()
        writePe(root, "The Ramp.exe", imports = listOf("UnityPlayer.dll"))
        writePe(root, "UnityPlayer.dll", imports = listOf("d3d11.dll", "dxgi.dll"))
        writePe(root, "UnityCrashHandler64.exe")

        val decision = LocalGameLaunchAnalyzer.analyze(root)!!

        assertEquals("The Ramp.exe", decision.launchExecutable)
        assertEquals("dxvk", decision.dxWrapper)
    }

    @Test
    fun analyze_selectsSingleUnityExecutableWithNonMatchingFolderName() {
        val root = temporaryFolder.newFolder("黑神话像素版")
        File(root, "BlackMythPixel_Data").mkdirs()
        writePe(root, "BlackMythPixel.exe", imports = listOf("UnityPlayer.dll"))
        writePe(root, "UnityPlayer.dll")

        val decision = LocalGameLaunchAnalyzer.analyze(root)!!

        assertEquals("BlackMythPixel.exe", decision.launchExecutable)
        assertEquals("dxvk", decision.dxWrapper)
    }

    @Test
    fun analyze_isDeterministicAndRejectsEscapingPersistedOverride() {
        val root = temporaryFolder.newFolder("Stable")
        writePe(root, "Stable.exe", imports = listOf("middle.dll", "Alpha.dll"))
        writePe(root, "middle.dll", imports = listOf("d3d11.dll"))
        writePe(root, "Alpha.dll")

        val first = LocalGameLaunchAnalyzer.analyze(root, "../Other.exe")!!
        val second = LocalGameLaunchAnalyzer.analyze(root, "../Other.exe")!!

        assertEquals(first, second)
        assertEquals("Stable.exe", first.launchExecutable)
        assertEquals("dxvk", first.dxWrapper)
    }

    private fun writePe(
        root: File,
        relativePath: String,
        machine: Int = 0x8664,
        imports: List<String> = emptyList(),
        refs: List<String> = emptyList(),
        padding: Int = 0,
    ) {
        val file = File(root, relativePath).apply { parentFile?.mkdirs() }
        PortableExecutableInspectorTest.writeMinimalPe(
            file,
            machine = machine,
            imports = imports,
            executableReferences = refs,
        )
        if (padding > 0) file.appendBytes(ByteArray(padding))
    }
}

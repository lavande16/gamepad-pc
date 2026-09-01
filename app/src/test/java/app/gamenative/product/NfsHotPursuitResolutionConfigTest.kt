package app.gamenative.product

import com.winlator.container.Container
import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NfsHotPursuitResolutionConfigTest {
    @Test
    fun originalNfs11ConfigSynchronizesBothDirectionsWithoutChangingOtherSettings() {
        val root = createTempDirectory("nfs-config-").toFile()
        val container = Container("NFS").apply {
            executablePath = "NFS11.exe"
            rootDir = root
        }
        val config = configFile(root).apply {
            parentFile.mkdirs()
            writeText(
                "[Display]\r\nDisplayMode=0\r\nWidth=1920\r\nHeight=1080\r\n" +
                    "VSync=1\r\n[Graphics]\r\nPreset=-1\r\n",
            )
        }

        assertTrue(NfsHotPursuitResolutionConfig.synchronize(container, "2304x1440"))
        assertEquals(
            "[Display]\r\nDisplayMode=0\r\nWidth=2304\r\nHeight=1440\r\n" +
                "VSync=1\r\n[Graphics]\r\nPreset=-1\r\n",
            config.readText(),
        )
        assertTrue(NfsHotPursuitResolutionConfig.synchronize(container, "1920x1080"))
        assertTrue(config.readText().contains("Width=1920\r\nHeight=1080"))
    }

    @Test
    fun remasteredConfigIsOutsideTheSupportedProductScope() {
        val root = createTempDirectory("other-config-").toFile()
        val container = Container("OTHER").apply {
            executablePath = "NFS11Remastered.exe"
            rootDir = root
        }
        val config = remasteredConfigFile(root).apply {
            parentFile.mkdirs()
            writeText("[Display]\r\nWidth=1920\r\nHeight=1080\r\n")
        }

        assertFalse(NfsHotPursuitResolutionConfig.synchronize(container, "2304x1440"))
        assertEquals("[Display]\r\nWidth=1920\r\nHeight=1080\r\n", config.readText())
    }

    private fun configFile(root: File): File = File(
        root,
        ".wine/drive_c/users/xuser/Documents/Criterion Games/" +
            "Need for Speed(TM) Hot Pursuit/config.NFS11Save",
    )

    private fun remasteredConfigFile(root: File): File = File(
        root,
        ".wine/drive_c/users/xuser/Documents/Criterion Games/" +
            "Need for Speed(TM) Hot Pursuit Remastered/config.NFS11Save",
    )
}

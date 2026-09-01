package app.gamenative.gamepad

import app.gamenative.displayresize.DisplayDimensions
import app.gamenative.displayresize.DisplaySizeSnapshot
import com.winlator.container.Container
import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePadDisplayProfileTest {
    @Test
    fun forced16x9DisplaySynchronizesGlobalAndEveryGameTo2304x1296() {
        val first = Container("GAME_ONE").apply { screenSize = "2304x1440" }
        val second = Container("GAME_TWO").apply { screenSize = "1280x720" }
        var globalScreenSize = ""
        val persisted = mutableListOf<String>()

        GamePadDisplayProfile.synchronize(
            snapshot = DisplaySizeSnapshot(
                DisplayDimensions(1440, 2304),
                DisplayDimensions(1296, 2304),
            ),
            containers = listOf(first, second),
            writeGlobal = { globalScreenSize = it },
            persist = { persisted += it.id },
        )

        assertEquals("2304x1296", globalScreenSize)
        assertEquals("2304x1296", first.screenSize)
        assertEquals("2304x1296", second.screenSize)
        assertEquals(listOf("GAME_ONE", "GAME_TWO"), persisted)
    }

    @Test
    fun forced16x9DisplayAlsoSynchronizesOriginalNfsConfigTo2304x1296() {
        val root = createTempDirectory("nfs-16x9-profile-").toFile()
        val configFile = File(
            root,
            ".wine/drive_c/users/xuser/Documents/Criterion Games/" +
                "Need for Speed(TM) Hot Pursuit/config.NFS11Save",
        ).apply {
            parentFile.mkdirs()
            writeText("[Display]\r\nWidth=1920\r\nHeight=1080\r\nVSync=1\r\n")
        }
        val game = Container("NFS11").apply {
            screenSize = "1920x1080"
            executablePath = "NFS11.exe"
            rootDir = root
        }

        GamePadDisplayProfile.synchronize(
            snapshot = DisplaySizeSnapshot(
                DisplayDimensions(1440, 2304),
                DisplayDimensions(1296, 2304),
            ),
            containers = listOf(game),
            writeGlobal = {},
            persist = {},
        )

        assertEquals("2304x1296", game.screenSize)
        assertEquals(
            "[Display]\r\nWidth=2304\r\nHeight=1296\r\nVSync=1\r\n",
            configFile.readText(),
        )
    }

    @Test
    fun restoredPhysicalDisplaySynchronizesGlobalAndEveryGameTo2304x1440() {
        val root = createTempDirectory("nfs-profile-").toFile()
        val configFile = File(
            root,
            ".wine/drive_c/users/xuser/Documents/Criterion Games/" +
                "Need for Speed(TM) Hot Pursuit/config.NFS11Save",
        ).apply {
            parentFile.mkdirs()
            writeText("[Display]\r\nWidth=1920\r\nHeight=1080\r\nVSync=1\r\n")
        }
        val game = Container("GAME_ONE").apply {
            screenSize = "1920x1080"
            executablePath = "NFS11.exe"
            rootDir = root
        }
        var globalScreenSize = ""

        GamePadDisplayProfile.synchronize(
            snapshot = DisplaySizeSnapshot(DisplayDimensions(1440, 2304), null),
            containers = listOf(game),
            writeGlobal = { globalScreenSize = it },
            persist = {},
        )

        assertEquals("2304x1440", globalScreenSize)
        assertEquals("2304x1440", game.screenSize)
        assertEquals(
            "[Display]\r\nWidth=2304\r\nHeight=1440\r\nVSync=1\r\n",
            configFile.readText(),
        )
    }
}

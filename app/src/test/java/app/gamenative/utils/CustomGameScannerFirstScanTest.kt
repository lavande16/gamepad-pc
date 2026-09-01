package app.gamenative.utils

import app.gamenative.PrefManager
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CustomGameScannerFirstScanTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun firstLazyScan_registersFolderForImmediateArtworkLookup() {
        val root = temporaryFolder.newFolder("game-root")
        val game = File(root, "Test Game").also { it.mkdirs() }
        File(game, "cover_square.png").writeText("cover")
        File(game, ".gamenative").writeText("{\"appId\":12345}")

        mockkObject(PrefManager)
        mockkStatic(GameFoldPcResourceRoot::class)
        every { PrefManager.customGameManualFolders } returns emptySet()
        every { PrefManager.importCustomGameAsSteamGame } returns false
        every { GameFoldPcResourceRoot.gameRoot() } returns root
        CustomGameScanner.invalidateCache()

        try {
            val item = CustomGameScanner.scanAsLibraryItems().single()

            assertEquals(game.absolutePath, CustomGameScanner.getFolderPathFromAppId(item.appId))
        } finally {
            CustomGameScanner.invalidateCache()
            unmockkStatic(GameFoldPcResourceRoot::class)
            unmockkObject(PrefManager)
        }
    }

    @Test
    fun defaultExecutable_resolvesNonGtaGameInsideItsOwnFolder() {
        val game = temporaryFolder.newFolder("黑神话像素版")
        File(game, "黑神话像素版1.0 电脑版.exe").writeText("game")

        assertEquals(
            "黑神话像素版1.0 电脑版.exe",
            CustomGameScanner.findGameFoldPcDefaultExecutable(game.absolutePath),
        )
    }
}

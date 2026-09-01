package app.gamenative.utils

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalGameExecutableResolverTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun resolve_selectsGtaLauncherInsideGtaFolder() {
        val gtaFolder = temporaryFolder.newFolder("Grand Theft Auto V")
        val payload = File(gtaFolder, "Grand Theft Auto V").also { it.mkdirs() }
        File(payload, "GTA5.exe").writeText("game")
        File(payload, "GTAVLauncher.exe").writeText("launcher")
        File(payload, "sc.exe").writeText("social club")
        File(payload, "unins000.exe").writeText("uninstaller")

        assertEquals(
            "Grand Theft Auto V/GTAVLauncher.exe",
            LocalGameExecutableResolver.resolve(gtaFolder),
        )
    }

    @Test
    fun resolve_selectsOnlyExecutableInsideBlackMythFolder() {
        val gameFolder = temporaryFolder.newFolder("黑神话像素版")
        File(gameFolder, "黑神话像素版1.0 电脑版.exe").writeText("game")

        assertEquals(
            "黑神话像素版1.0 电脑版.exe",
            LocalGameExecutableResolver.resolve(gameFolder),
        )
    }

    @Test
    fun resolve_repairsPersistedExecutableFromSiblingGame() {
        val libraryRoot = temporaryFolder.newFolder("game")
        val gtaFolder = File(libraryRoot, "Grand Theft Auto V").also { it.mkdirs() }
        val gtaPayload = File(gtaFolder, "Grand Theft Auto V").also { it.mkdirs() }
        File(gtaPayload, "GTAVLauncher.exe").writeText("launcher")
        val blackMythFolder = File(libraryRoot, "黑神话像素版").also { it.mkdirs() }
        File(blackMythFolder, "黑神话像素版1.0 电脑版.exe").writeText("game")

        assertEquals(
            "黑神话像素版1.0 电脑版.exe",
            LocalGameExecutableResolver.resolve(
                blackMythFolder,
                "../Grand Theft Auto V/Grand Theft Auto V/GTAVLauncher.exe",
            ),
        )
    }

    @Test
    fun resolve_preservesValidPersistedSelectionForCurrentGame() {
        val gameFolder = temporaryFolder.newFolder("multi")
        File(gameFolder, "Custom.exe").writeText("selected")
        File(gameFolder, "GameLauncher.exe").writeText("automatic candidate")

        assertEquals(
            "Custom.exe",
            LocalGameExecutableResolver.resolve(gameFolder, "Custom.exe"),
        )
    }

    @Test
    fun resolve_rejectsAbsoluteOrEscapingPersistedPath() {
        val libraryRoot = temporaryFolder.newFolder("library")
        val gameFolder = File(libraryRoot, "current").also { it.mkdirs() }
        File(gameFolder, "Current.exe").writeText("current")
        val outside = File(libraryRoot, "Outside.exe").also { it.writeText("outside") }

        assertEquals(
            "Current.exe",
            LocalGameExecutableResolver.resolve(gameFolder, outside.absolutePath),
        )
        assertEquals(
            "Current.exe",
            LocalGameExecutableResolver.resolve(gameFolder, "../Outside.exe"),
        )
    }

    @Test
    fun resolve_ignoresKnownNonGameExecutables() {
        val gameFolder = temporaryFolder.newFolder("tools-only")
        File(gameFolder, "unins000.exe").writeText("uninstaller")
        File(gameFolder, "setup.exe").writeText("installer")
        File(gameFolder, "CrashReporter.exe").writeText("reporter")
        File(gameFolder, "redist.exe").writeText("redistributable")

        assertNull(LocalGameExecutableResolver.resolve(gameFolder))
    }

    @Test
    fun resolve_ignoresLauncherHelperAndUnityCrashHandler() {
        val gameFolder = temporaryFolder.newFolder("Actual Game")
        File(gameFolder, "ActualGame.exe").writeText("game")
        File(gameFolder, "GameLauncherHelper.exe").writeText("helper")
        File(gameFolder, "UnityCrashHandler64.exe").writeText("crash handler")

        assertEquals(
            "ActualGame.exe",
            LocalGameExecutableResolver.resolve(gameFolder),
        )
    }

    @Test
    fun compareRelativePaths_usesCaseSensitivePathAsFinalStableTieBreaker() {
        assertTrue(LocalGameExecutableResolver.compareRelativePaths("Game.exe", "game.exe") < 0)
        assertTrue(LocalGameExecutableResolver.compareRelativePaths("game.exe", "Game.exe") > 0)
    }

    @Test
    fun resolveForMappedGame_rejectsSharedLibraryRootMapping() {
        val libraryRoot = temporaryFolder.newFolder("library-root")
        val gtaFolder = File(libraryRoot, "Grand Theft Auto V").also { it.mkdirs() }
        File(gtaFolder, "GTAVLauncher.exe").writeText("launcher")
        val blackMythFolder = File(libraryRoot, "黑神话像素版").also { it.mkdirs() }
        File(blackMythFolder, "黑神话像素版.exe").writeText("game")

        assertNull(
            LocalGameExecutableResolver.resolveForMappedGame(
                registeredGameFolder = blackMythFolder,
                mappedGameFolder = libraryRoot,
                persistedRelativePath = "Grand Theft Auto V/GTAVLauncher.exe",
            ),
        )
    }
}

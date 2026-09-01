package app.gamenative.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GtaFullscreenConfigTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun sync_writesFullscreenDimensionsNextToGtaLauncher() {
        val gameDir = temporaryFolder.newFolder("Grand Theft Auto V")
        val launcher = File(gameDir, "GTAVLauncher.exe").apply { createNewFile() }

        assertTrue(GtaFullscreenConfig.sync(launcher, "1067x600"))
        assertEquals(
            "-fullscreen\n-width 1067\n-height 600\n",
            File(gameDir, "commandline.txt").readText(),
        )
    }

    @Test
    fun sync_replacesConflictingDisplayFlagsButPreservesOtherArguments() {
        val gameDir = temporaryFolder.newFolder("GTA5")
        val executable = File(gameDir, "gta5.EXE").apply { createNewFile() }
        File(gameDir, "commandline.txt").writeText(
            "-scOfflineOnly\n-windowed\n-width=1280\n-height 720\n-ignoreDifferentVideoCard\n",
        )

        assertTrue(GtaFullscreenConfig.sync(executable, "1920x1080"))
        assertEquals(
            "-scOfflineOnly\n-ignoreDifferentVideoCard\n-fullscreen\n-width 1920\n-height 1080\n",
            File(gameDir, "commandline.txt").readText(),
        )
    }

    @Test
    fun sync_replacesUnsupportedGtaResolutionWithSafeDefault() {
        val gameDir = temporaryFolder.newFolder("GTA-small-resolution")
        val executable = File(gameDir, "GTA5.exe").apply { createNewFile() }

        assertTrue(GtaFullscreenConfig.sync(executable, "854x480"))
        assertEquals(
            "-fullscreen\n-width 1067\n-height 600\n",
            File(gameDir, "commandline.txt").readText(),
        )
    }

    @Test
    fun sync_rejectsInvalidScreenSizeWithoutChangingExistingFile() {
        val gameDir = temporaryFolder.newFolder("GTA-invalid")
        val launcher = File(gameDir, "GTAVLauncher.exe").apply { createNewFile() }
        val commandLine = File(gameDir, "commandline.txt").apply { writeText("-scOfflineOnly\n") }

        assertFalse(GtaFullscreenConfig.sync(launcher, "automatic"))
        assertEquals("-scOfflineOnly\n", commandLine.readText())
    }

    @Test
    fun sync_ignoresExecutablesFromOtherGames() {
        val gameDir = temporaryFolder.newFolder("Other-game")
        val executable = File(gameDir, "Game.exe").apply { createNewFile() }

        assertFalse(GtaFullscreenConfig.sync(executable, "854x480"))
        assertFalse(File(gameDir, "commandline.txt").exists())
    }

    @Test
    fun compatibleScreenSize_enforcesGtaMinimumButKeepsSupportedResolutions() {
        val gameDir = temporaryFolder.newFolder("GTA-resolution")
        val launcher = File(gameDir, "GTAVLauncher.exe").apply { createNewFile() }

        assertEquals("1067x600", GtaFullscreenConfig.compatibleScreenSize(launcher, "854x480"))
        assertEquals("1067x600", GtaFullscreenConfig.compatibleScreenSize(launcher, "640x480"))
        assertEquals("800x600", GtaFullscreenConfig.compatibleScreenSize(launcher, "800x600"))
        assertEquals("1280x720", GtaFullscreenConfig.compatibleScreenSize(launcher, "1280x720"))
        assertEquals("1920x1080", GtaFullscreenConfig.compatibleScreenSize(launcher, "1920x1080"))
    }

    @Test
    fun compatibleScreenSize_doesNotChangeOtherGamesOrInvalidValues() {
        val gameDir = temporaryFolder.newFolder("Other-resolution")
        val otherGame = File(gameDir, "Game.exe").apply { createNewFile() }
        val gta = File(gameDir, "GTA5.exe").apply { createNewFile() }

        assertEquals("854x480", GtaFullscreenConfig.compatibleScreenSize(otherGame, "854x480"))
        assertEquals("automatic", GtaFullscreenConfig.compatibleScreenSize(gta, "automatic"))
    }
}

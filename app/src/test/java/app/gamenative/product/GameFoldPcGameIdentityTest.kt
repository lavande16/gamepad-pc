package app.gamenative.product

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcGameIdentityTest {
    @Test
    fun gtaVRecognizesLauncherAndRuntimeCaseInsensitively() {
        assertTrue(
            GameFoldPcGameIdentity.isGtaVExecutable(
                "Grand Theft Auto V/GTAVLauncher.exe",
            ),
        )
        assertTrue(GameFoldPcGameIdentity.isGtaVExecutable("gta5.EXE"))
    }

    @Test
    fun gtaVRejectsOtherExecutables() {
        assertFalse(GameFoldPcGameIdentity.isGtaVExecutable("PlayGTAV.exe"))
        assertFalse(GameFoldPcGameIdentity.isGtaVExecutable("Cyberpunk2077.exe"))
        assertFalse(GameFoldPcGameIdentity.isGtaVExecutable(""))
    }

    @Test
    fun cyberpunkRecognizesOnlyTheGameExecutableCaseInsensitively() {
        assertTrue(
            GameFoldPcGameIdentity.isCyberpunk2077Executable(
                "A:\\bin\\x64\\Cyberpunk2077.exe",
            ),
        )
        assertTrue(GameFoldPcGameIdentity.isCyberpunk2077Executable("cyberpunk2077.EXE"))
        assertFalse(GameFoldPcGameIdentity.isCyberpunk2077Executable("REDlauncher.exe"))
        assertFalse(GameFoldPcGameIdentity.isCyberpunk2077Executable("GTAVLauncher.exe"))
        assertFalse(GameFoldPcGameIdentity.isCyberpunk2077Executable(""))
    }
}

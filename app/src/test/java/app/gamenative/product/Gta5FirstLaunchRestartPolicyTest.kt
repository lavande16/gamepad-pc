package app.gamenative.product

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gta5FirstLaunchRestartPolicyTest {
    @Test
    fun restartsGta5AfterFirstLaunchPrerequisitesComplete() {
        assertTrue(
            Gta5FirstLaunchRestartPolicy.shouldRestartAfterPreInstall(
                localShell = true,
                customGame = true,
                executablePath = "Grand Theft Auto V/GTAVLauncher.exe",
                preInstallStepCount = 1,
            ),
        )
    }

    @Test
    fun doesNotRestartNormalLaunchOrOtherGames() {
        assertFalse(
            Gta5FirstLaunchRestartPolicy.shouldRestartAfterPreInstall(
                localShell = true,
                customGame = true,
                executablePath = "Grand Theft Auto V/GTAVLauncher.exe",
                preInstallStepCount = 0,
            ),
        )
        assertFalse(
            Gta5FirstLaunchRestartPolicy.shouldRestartAfterPreInstall(
                localShell = true,
                customGame = true,
                executablePath = "Cyberpunk2077.exe",
                preInstallStepCount = 1,
            ),
        )
        assertFalse(
            Gta5FirstLaunchRestartPolicy.shouldRestartAfterPreInstall(
                localShell = false,
                customGame = true,
                executablePath = "GTA5.exe",
                preInstallStepCount = 1,
            ),
        )
    }
}

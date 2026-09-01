package app.gamenative.gamefold.gyro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GyroAimSessionPolicyTest {
    @Test
    fun localGtaExecutablesEnableGyroAim() {
        assertTrue(
            GyroAimSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "D:\\Games\\Grand Theft Auto V\\GTAVLauncher.exe",
            ),
        )
        assertTrue(
            GyroAimSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "/storage/emulated/0/GTA V/GTA5.exe",
            ),
        )
    }

    @Test
    fun nonLocalShellNeverEnablesGyroAim() {
        assertFalse(
            GyroAimSessionPolicy.shouldEnable(
                localShell = false,
                executablePath = "GTA5.exe",
            ),
        )
    }

    @Test
    fun otherExecutablesNeverEnableGyroAim() {
        assertFalse(
            GyroAimSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "Cyberpunk2077.exe",
            ),
        )
        assertFalse(
            GyroAimSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "",
            ),
        )
    }
}

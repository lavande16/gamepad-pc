package app.gamenative.gamefold.gyro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GyroSteeringSessionPolicyTest {
    @Test
    fun onlyOriginalNfs11InLocalShellEnablesGyroSteering() {
        assertTrue(
            GyroSteeringSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "A:\\NFS11.exe",
            ),
        )
        assertTrue(
            GyroSteeringSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "/games/nfs11.EXE",
            ),
        )
        assertFalse(
            GyroSteeringSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "NFS11Remastered.exe",
            ),
        )
        assertFalse(
            GyroSteeringSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "GTA5.exe",
            ),
        )
        assertFalse(
            GyroSteeringSessionPolicy.shouldEnable(
                localShell = false,
                executablePath = "NFS11.exe",
            ),
        )
    }

    @Test
    fun disabledContainerSettingPreventsNfs11GyroSteering() {
        assertFalse(
            GyroSteeringSessionPolicy.shouldEnable(
                localShell = true,
                executablePath = "NFS11.exe",
                userEnabled = false,
            ),
        )
    }
}

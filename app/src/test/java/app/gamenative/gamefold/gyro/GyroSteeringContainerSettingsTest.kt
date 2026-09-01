package app.gamenative.gamefold.gyro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GyroSteeringContainerSettingsTest {
    @Test
    fun settingIsAvailableOnlyForOriginalNfs11() {
        assertTrue(GyroSteeringContainerSettings.isAvailable("A:\\NFS11.exe"))
        assertTrue(GyroSteeringContainerSettings.isAvailable("/games/nfs11.EXE"))
        assertFalse(GyroSteeringContainerSettings.isAvailable("NFS11Remastered.exe"))
        assertFalse(GyroSteeringContainerSettings.isAvailable("GTA5.exe"))
    }

    @Test
    fun missingOrInvalidStoredValueDefaultsToEnabled() {
        assertTrue(GyroSteeringContainerSettings.decodeEnabled(null))
        assertTrue(GyroSteeringContainerSettings.decodeEnabled(""))
        assertTrue(GyroSteeringContainerSettings.decodeEnabled("unexpected"))
    }

    @Test
    fun explicitStoredValueControlsGyroSteering() {
        assertTrue(GyroSteeringContainerSettings.decodeEnabled("true"))
        assertFalse(GyroSteeringContainerSettings.decodeEnabled("false"))
    }
}

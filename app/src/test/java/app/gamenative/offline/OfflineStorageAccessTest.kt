package app.gamenative.offline

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineStorageAccessTest {
    @Test
    fun android11OrNewerRequiresAllFilesAccess() {
        assertFalse(OfflineStorageAccess.hasRequiredAccess(30, allFilesAccess = false, readWriteAccess = true))
        assertTrue(OfflineStorageAccess.hasRequiredAccess(30, allFilesAccess = true, readWriteAccess = false))
    }

    @Test
    fun preAndroid11UsesReadWriteStoragePermission() {
        assertFalse(OfflineStorageAccess.hasRequiredAccess(29, allFilesAccess = false, readWriteAccess = false))
        assertTrue(OfflineStorageAccess.hasRequiredAccess(29, allFilesAccess = false, readWriteAccess = true))
    }
}

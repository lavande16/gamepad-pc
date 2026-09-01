package app.gamenative.product

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductModePolicyTest {
    @Test
    fun localShell_exposesOnlyLocalLibraryAndBlocksOnlineProductFeatures() {
        val capabilities = ProductModePolicy.capabilities(localShell = true)

        assertTrue(capabilities.localLibraryOnly)
        assertFalse(capabilities.platformAccounts)
        assertFalse(capabilities.onlineUpdates)
        assertFalse(capabilities.analytics)
        assertFalse(capabilities.onlineRuntimeDownloads)
        assertFalse(capabilities.informationSection)
    }

    @Test
    fun standardBuild_preservesExistingOnlineProductFeatures() {
        val capabilities = ProductModePolicy.capabilities(localShell = false)

        assertFalse(capabilities.localLibraryOnly)
        assertTrue(capabilities.platformAccounts)
        assertTrue(capabilities.onlineUpdates)
        assertTrue(capabilities.analytics)
        assertTrue(capabilities.onlineRuntimeDownloads)
        assertTrue(capabilities.informationSection)
    }
}

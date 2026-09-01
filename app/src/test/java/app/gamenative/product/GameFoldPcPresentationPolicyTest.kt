package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcPresentationPolicyTest {
    @Test
    fun localShell_usesGameFoldBrandWithoutTipsOrIconPicker() {
        val presentation = GameFoldPcPresentationPolicy.forProduct(localShell = true)

        assertEquals("GameFold", presentation.bootSplashBrand)
        assertFalse(presentation.showBootSplashFixedTips)
        assertFalse(presentation.showIconStylePicker)
    }

    @Test
    fun upstreamBuild_keepsGameNativePresentation() {
        val presentation = GameFoldPcPresentationPolicy.forProduct(localShell = false)

        assertEquals("GameNative", presentation.bootSplashBrand)
        assertTrue(presentation.showBootSplashFixedTips)
        assertTrue(presentation.showIconStylePicker)
    }
}

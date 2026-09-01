package app.gamenative.ui.screen.settings

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageButton
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import app.gamenative.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.yuzu.yuzu_emu.views.PixelWordmarkView
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GameFoldAboutPageInflationTest {
    @Test
    fun nsAboutLayout_inflatesWithThePcHostContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val view = inflateGameFoldNsAbout(context)

        assertNotNull(view)

        val xiaohongshu = view.findViewById<ImageButton>(R.id.button_xiaohongshu)
        val dribbble = view.findViewById<ImageButton>(R.id.button_dribbble)
        assertEquals(View.VISIBLE, (xiaohongshu.parent as View).visibility)
        assertNotNull(xiaohongshu.drawable)
        assertNotNull(dribbble.drawable)
    }

    @Test
    fun aboutContent_usesGamePadBrandAndCurrentVersionWithoutAnEmbeddedToolbar() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val view = inflateGameFoldNsAbout(context) as ViewGroup
        val productWordmark = view.findViewWithTag<PixelWordmarkView>("about_product_brand")
        val currentVersion = view.findViewById<TextView>(R.id.text_version_name)

        assertEquals(1, view.childCount)
        assertEquals("GAMEPAD", productWordmark.contentDescription)
        assertEquals("GamePad", currentVersion.text.toString())
    }

    @Test
    @Config(sdk = [35], qualifiers = "land")
    fun landscapeBrandBlock_usesTwentyEightDpTopMarginToMatchVersionCard() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = inflateGameFoldNsAbout(context)
        val brandBlock = view.findViewById<View>(R.id.brand_block)
        val expectedTopMargin = (28f * context.resources.displayMetrics.density).roundToInt()

        assertEquals(expectedTopMargin, (brandBlock.layoutParams as MarginLayoutParams).topMargin)
    }

    @Test
    @Config(sdk = [35], qualifiers = "port")
    fun portraitBrandBlock_keepsTheExistingFortyTwoDpTopMargin() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view = inflateGameFoldNsAbout(context)
        val brandBlock = view.findViewById<View>(R.id.brand_block)
        val expectedTopMargin = (42f * context.resources.displayMetrics.density).roundToInt()

        assertEquals(expectedTopMargin, (brandBlock.layoutParams as MarginLayoutParams).topMargin)
    }
}

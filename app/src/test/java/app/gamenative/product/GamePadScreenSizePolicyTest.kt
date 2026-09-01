package app.gamenative.product

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.gamenative.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePadScreenSizePolicyTest {
    @Test
    fun screenSizeEntries_place2304x1440FirstAndCustomSecond() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val entries = context.resources.getStringArray(R.array.screen_size_entries)

        assertEquals("2304x1440 (16:10)", entries[0])
        assertEquals("Custom", entries[1])
        assertTrue("2304x1296 (16:9)" in entries)
    }

    @Test
    fun selectionPolicy_distinguishesFirstPresetFromSecondCustomEntry() {
        val entries = listOf(
            "2304x1440 (16:10)",
            "Custom",
            "1920x1080 (16:9)",
        )

        assertEquals(0, GamePadScreenSizePolicy.selectedIndex(entries, "2304x1440"))
        assertFalse(GamePadScreenSizePolicy.isCustom(entries, 0))
        assertTrue(GamePadScreenSizePolicy.isCustom(entries, 1))
        assertEquals(
            "2304x1440",
            GamePadScreenSizePolicy.resolveSelection(
                entries = entries,
                selectedIndex = 0,
                customWidth = "1600",
                customHeight = "1000",
                currentValue = "1920x1080",
            ),
        )
        assertEquals(
            "1600x1000",
            GamePadScreenSizePolicy.resolveSelection(
                entries = entries,
                selectedIndex = 1,
                customWidth = "1600",
                customHeight = "1000",
                currentValue = "1920x1080",
            ),
        )
    }

    @Test
    fun selectionPolicy_usesCustomEntryForUnlistedResolution() {
        val entries = listOf(
            "2304x1440 (16:10)",
            "Custom",
            "1920x1080 (16:9)",
        )

        assertEquals(1, GamePadScreenSizePolicy.selectedIndex(entries, "1600x1000"))
    }
}

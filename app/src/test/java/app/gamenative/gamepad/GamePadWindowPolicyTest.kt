package app.gamenative.gamepad

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.test.core.app.ApplicationProvider
import app.gamenative.MainActivity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePadWindowPolicyTest {
    @Test
    fun exactTargetLandscapeAt576Dp_isSupported() {
        assertTrue(GamePadWindowPolicy.isSupported(state()))
    }

    @Test
    fun exact1080pLandscapeAtTheDevice400DpiScale_isSupported() {
        assertTrue(
            GamePadWindowPolicy.isSupported(
                state(widthPx = 1920, heightPx = 1080, smallestWidthDp = 432),
            ),
        )
    }

    @Test
    fun exact2304x1296Landscape_isSupported() {
        assertTrue(
            GamePadWindowPolicy.isSupported(
                state(widthPx = 2304, heightPx = 1296, smallestWidthDp = 518),
            ),
        )
    }

    @Test
    fun anyPixelDriftOrPortraitMode_failsClosed() {
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 2303)))
        assertFalse(GamePadWindowPolicy.isSupported(state(heightPx = 1439)))
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 1440, heightPx = 2304)))
        assertFalse(GamePadWindowPolicy.isSupported(state(smallestWidthDp = 575)))
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 1919, heightPx = 1080, smallestWidthDp = 432)))
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 1920, heightPx = 1079, smallestWidthDp = 432)))
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 1080, heightPx = 1920, smallestWidthDp = 432)))
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 2303, heightPx = 1296, smallestWidthDp = 518)))
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 2304, heightPx = 1295, smallestWidthDp = 518)))
        assertFalse(GamePadWindowPolicy.isSupported(state(widthPx = 1296, heightPx = 2304, smallestWidthDp = 518)))
    }

    @Test
    fun splitScreenAndPictureInPictureRemainBlockedAtBothSupportedSizes() {
        assertFalse(GamePadWindowPolicy.isSupported(state(inMultiWindow = true)))
        assertFalse(GamePadWindowPolicy.isSupported(state(inPictureInPicture = true)))
        assertFalse(
            GamePadWindowPolicy.isSupported(
                state(widthPx = 1920, heightPx = 1080, smallestWidthDp = 432, inMultiWindow = true),
            ),
        )
        assertFalse(
            GamePadWindowPolicy.isSupported(
                state(widthPx = 1920, heightPx = 1080, smallestWidthDp = 432, inPictureInPicture = true),
            ),
        )
        assertFalse(
            GamePadWindowPolicy.isSupported(
                state(widthPx = 2304, heightPx = 1296, smallestWidthDp = 518, inMultiWindow = true),
            ),
        )
    }

    @Test
    fun productSurface_hasNoLogicalBorderOrCornerRadius() {
        assertTrue(GamePadWindowPolicy.BORDER_WIDTH_PX == 0)
        assertTrue(GamePadWindowPolicy.CORNER_RADIUS_PX == 0)
    }

    @Test
    fun packagedMainActivity_isLandscapeOnly() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val info = context.packageManager.getActivityInfo(
            ComponentName(context, MainActivity::class.java),
            0,
        )

        assertEquals(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, info.screenOrientation)
    }

    private fun state(
        widthPx: Int = 2304,
        heightPx: Int = 1440,
        smallestWidthDp: Int = 576,
        inMultiWindow: Boolean = false,
        inPictureInPicture: Boolean = false,
    ) = GamePadWindowState(
        widthPx = widthPx,
        heightPx = heightPx,
        smallestWidthDp = smallestWidthDp,
        inMultiWindow = inMultiWindow,
        inPictureInPicture = inPictureInPicture,
    )
}

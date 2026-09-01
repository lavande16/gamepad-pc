package app.gamenative.gamefold.ui

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameFoldPauseOverlayBitmapTest {
    @Test
    fun pauseOverlay_canBeConstructedWhenVectorAssetsHaveNoIntrinsicBounds() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertNotNull(GameFoldPauseOverlayView(context))
    }
}

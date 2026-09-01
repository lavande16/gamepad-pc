package app.gamenative.gamefold.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameFoldGameLoadingOverlayViewAndroidTest {
    @Test
    fun drawsBeforeAsyncGameCoverIsAvailable() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val view = GameFoldGameLoadingOverlayView(instrumentation.targetContext)
            view.layout(0, 0, 1_200, 600)
            val output = Bitmap.createBitmap(1_200, 600, Bitmap.Config.ARGB_8888)

            view.draw(Canvas(output))
        }
    }
}

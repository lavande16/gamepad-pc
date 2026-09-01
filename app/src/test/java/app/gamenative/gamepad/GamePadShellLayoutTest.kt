package app.gamenative.gamepad

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Looper
import android.view.View
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View.MeasureSpec
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import app.gamenative.R
import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldControllerListener
import app.gamenative.gamefold.ui.GameFoldStick
import app.gamenative.gamefold.ui.GameFoldShellLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import java.security.MessageDigest
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GamePadShellLayoutTest {
    @Test
    fun transparentV2DpadAndAbxyAssets_matchApprovedFiles() {
        val context = targetContext()
        val expected = linkedMapOf(
            R.drawable.controller_dpad_default to "d226d3808d6b3a3bdcd40f5f634cbdc1d56aa94ef0ab5426bdeb206300e89419",
            R.drawable.controller_dpad_up_pressed to "4b031594ef3eee508c92cd3ae3dea09339f73a2f055fd859283c8f364de533fa",
            R.drawable.controller_dpad_right_pressed to "d34b5701be75102af892f768de6f9ab9af98fe331338339ce79b2e4b8f219c42",
            R.drawable.controller_dpad_down_pressed to "fbf39332e81ec50f457389221d714df462a1fe227a89ca5bd6f088000087ddd2",
            R.drawable.controller_dpad_left_pressed to "91bd6c02e95990af3f1417917756389cda2d826f70a00d84a685908d0a233184",
            R.drawable.controller_abxy_default to "138d2bbddc6fe1b4560835f1d03143e10654afb240301c720599edafaaf69b51",
            R.drawable.controller_abxy_y_pressed to "913d81eef4b55ffc597358eb23e7f865858aa8d474b2995238ee90d269745434",
            R.drawable.controller_abxy_b_pressed to "5647a85583d662e243f05e812af499dd132fc168b16ccaa8b93a4b1efe6cad81",
            R.drawable.controller_abxy_a_pressed to "a51484e69bd66a7d3a44432fd8e1e1168eb212a7ad5806502b30ece583d902cd",
            R.drawable.controller_abxy_x_pressed to "803118bf89a87d10e0848d2fff144058fdddc0b8b78db3e5dd5f7f807bf2201c",
        )

        expected.forEach { (resourceId, expectedSha256) ->
            val bytes = context.resources.openRawResource(resourceId).use { it.readBytes() }
            val actualSha256 = MessageDigest.getInstance("SHA-256")
                .digest(bytes)
                .joinToString("") { byte -> "%02x".format(byte) }
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            assertEquals(expectedSha256, actualSha256)
            assertEquals(706, bitmap.width)
            assertEquals(706, bitmap.height)
            assertTrue(bitmap.hasAlpha())
        }
    }

    @Test
    fun transparentDefaultControlAssets_useLowAlphaAcrossTheirMainBodies() {
        val context = targetContext()
        val dpad = BitmapFactory.decodeResource(context.resources, R.drawable.controller_dpad_default)
        val abxy = BitmapFactory.decodeResource(context.resources, R.drawable.controller_abxy_default)

        assertTrue(Color.alpha(dpad.getPixel(162, 353)) < 16)
        assertTrue(Color.alpha(abxy.getPixel(162, 353)) < 16)
    }

    @Test
    fun hiddenStickWell_doesNotAlterGamePixelOutsideTheStickCap() {
        val gameColor = Color.rgb(51, 102, 153)
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        val bitmap = renderShellOver(shell, gameColor)

        assertColorNear(gameColor, bitmap.getPixel(460, 1083), tolerance = 15)
    }

    @Test
    fun ownedPageContentAndOverlay_fillTheWhole2304By1440Window() {
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        val content = View(context)
        val overlay = View(context)
        shell.addContentView(content)
        shell.addOverlayView(overlay)

        measureAndLayout(shell)

        assertBounds(content, 0, 0, 2304, 1440)
        assertBounds(overlay, 0, 0, 2304, 1440)
        assertTrue(shell.isPointInDisplay(0f, 0f))
        assertTrue(shell.isPointInDisplay(2304f, 1440f))
    }

    @Test
    fun sixteenByNineRuntimeContent_letterboxesWhileOverlayRemainsFullWindow() {
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        val content = View(context)
        val overlay = View(context)
        shell.addContentView(content)
        shell.addOverlayView(overlay)
        shell.setContentSourceSize(1920, 1080)

        measureAndLayout(shell)

        assertBounds(content, 0, 72, 2304, 1368)
        assertBounds(overlay, 0, 0, 2304, 1440)
    }

    @Test
    fun fullHdControllerArtworkLeavesTheBottomEdgeUnclipped() {
        val gameColor = Color.rgb(51, 102, 153)
        val context = targetContext(smallestWidthDp = 432)
        val shell = GameFoldShellLayout(context)
        shell.addContentView(View(context).apply { setBackgroundColor(gameColor) })
        shell.setControllerInputListener(recordingListener(mutableListOf()))
        measureAndLayout(shell, width = 1920, height = 1080)

        val bitmap = renderMeasuredShell(shell)

        assertEquals(0, countPixelsDifferentFrom(bitmap, gameColor, top = 1072))
    }

    @Test
    fun fullHdControllerTouchUsesTheSameInverseTransformAsItsArtwork() {
        val context = targetContext(smallestWidthDp = 432)
        val shell = GameFoldShellLayout(context)
        val buttons = mutableListOf<Pair<GameFoldButton, Boolean>>()
        shell.addContentView(View(context))
        shell.setControllerInputListener(recordingListener(buttons))
        measureAndLayout(shell, width = 1920, height = 1080)
        val canonicalDpadUp = GamePadControllerGeometry.LEFT_TRANSFORM.toScreen(
            GamePadPoint(483f, 1314f),
        )
        val fullHdDpadUp = GamePadControllerViewportPolicy
            .fit(windowWidth = 1920, windowHeight = 1080)
            .toScreen(canonicalDpadUp)

        shell.dispatchTouchEvent(
            event(MotionEvent.ACTION_DOWN, pointer(0, fullHdDpadUp.x, fullHdDpadUp.y)),
        )
        shell.dispatchTouchEvent(
            event(MotionEvent.ACTION_UP, pointer(0, fullHdDpadUp.x, fullHdDpadUp.y)),
        )

        assertEquals(
            listOf(GameFoldButton.DPAD_UP to true, GameFoldButton.DPAD_UP to false),
            buttons,
        )
    }

    @Test
    fun fullHdHomeTouchUsesItsIndependentRightShiftedTransform() {
        val context = targetContext(smallestWidthDp = 432)
        val shell = GameFoldShellLayout(context)
        val buttons = mutableListOf<Pair<GameFoldButton, Boolean>>()
        shell.addContentView(View(context))
        shell.setControllerInputListener(recordingListener(buttons))
        measureAndLayout(shell, width = 1920, height = 1080)
        val home = GamePadControllerGeometry.HOME_BOUNDS
        val fullHdHome = GamePadControllerViewportPolicy
            .homeFit(windowWidth = 1920, windowHeight = 1080)
            .toScreen(
                GamePadPoint(
                    x = (home.left + home.right) / 2f,
                    y = (home.top + home.bottom) / 2f,
                ),
            )

        shell.dispatchTouchEvent(
            event(MotionEvent.ACTION_DOWN, pointer(0, fullHdHome.x, fullHdHome.y)),
        )
        shell.dispatchTouchEvent(
            event(MotionEvent.ACTION_UP, pointer(0, fullHdHome.x, fullHdHome.y)),
        )

        assertEquals(
            listOf(GameFoldButton.HOME to true, GameFoldButton.HOME to false),
            buttons,
        )
    }

    @Test
    fun idleGameControllerFadesLowerControlsButKeepsHomeVisible() {
        val gameColor = Color.rgb(51, 102, 153)
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        shell.addContentView(View(context).apply { setBackgroundColor(gameColor) })
        shell.setControllerInputListener(recordingListener(mutableListOf()))
        measureAndLayout(shell)
        shell.setControllerAutoHideEnabled(true)

        val visible = renderMeasuredShell(shell)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10_300L))
        val hidden = renderMeasuredShell(shell)

        val visibleLowerPixels = countPixelsDifferentFrom(visible, gameColor, top = 700)
        val hiddenLowerPixels = countPixelsDifferentFrom(hidden, gameColor, top = 700)
        assertTrue(visibleLowerPixels > 1_000)
        assertTrue(hiddenLowerPixels < visibleLowerPixels / 10)
        assertRegionEquals(
            visible,
            hidden,
            GamePadControllerGeometry.HOME_BOUNDS.left.toInt(),
            GamePadControllerGeometry.HOME_BOUNDS.top.toInt(),
            GamePadControllerGeometry.HOME_BOUNDS.right.toInt(),
            GamePadControllerGeometry.HOME_BOUNDS.bottom.toInt(),
        )
    }

    @Test
    fun firstNonHomeTouchAfterHideOnlyWakesControls() {
        val gameColor = Color.rgb(51, 102, 153)
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        val buttons = mutableListOf<Pair<GameFoldButton, Boolean>>()
        shell.addContentView(View(context).apply { setBackgroundColor(gameColor) })
        shell.setControllerInputListener(recordingListener(buttons))
        measureAndLayout(shell)
        shell.setControllerAutoHideEnabled(true)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10_300L))

        val dpadUp = GamePadControllerGeometry.LEFT_TRANSFORM.toScreen(GamePadPoint(483f, 1314f))
        shell.dispatchTouchEvent(event(MotionEvent.ACTION_DOWN, pointer(0, dpadUp.x, dpadUp.y)))
        shell.dispatchTouchEvent(event(MotionEvent.ACTION_UP, pointer(0, dpadUp.x, dpadUp.y)))
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300L))

        assertTrue(buttons.isEmpty())
        assertTrue(countPixelsDifferentFrom(renderMeasuredShell(shell), gameColor, top = 700) > 1_000)
    }

    @Test
    fun visibleHomeStillDispatchesWhileLowerControlsAreHidden() {
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        val buttons = mutableListOf<Pair<GameFoldButton, Boolean>>()
        shell.addContentView(View(context))
        shell.setControllerInputListener(recordingListener(buttons))
        measureAndLayout(shell)
        shell.setControllerAutoHideEnabled(true)
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(10_300L))

        val home = GamePadControllerGeometry.HOME_BOUNDS
        val x = (home.left + home.right) / 2f
        val y = (home.top + home.bottom) / 2f
        shell.dispatchTouchEvent(event(MotionEvent.ACTION_DOWN, pointer(0, x, y)))
        shell.dispatchTouchEvent(event(MotionEvent.ACTION_UP, pointer(0, x, y)))

        assertEquals(
            listOf(GameFoldButton.HOME to true, GameFoldButton.HOME to false),
            buttons,
        )
    }

    @Test
    fun contentDownThenControllerPointer_routesEachPointerIndependently() {
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        val content = RecordingTouchView(context)
        val buttons = mutableListOf<Pair<GameFoldButton, Boolean>>()
        shell.addContentView(content)
        shell.setControllerInputListener(recordingListener(buttons))
        measureAndLayout(shell)

        shell.dispatchTouchEvent(event(MotionEvent.ACTION_DOWN, pointer(0, 1152f, 500f)))
        shell.dispatchTouchEvent(
            event(
                MotionEvent.ACTION_POINTER_DOWN or
                    (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                pointer(0, 1152f, 500f),
                pointer(1, 2113f, 94f),
            ),
        )

        assertEquals(listOf(GameFoldButton.HOME to true), buttons)
        assertEquals(MotionEvent.ACTION_DOWN, content.events.first().action)
        assertTrue(content.events.all { it.pointerIds == listOf(0) })
    }

    @Test
    fun controllerDownThenContentPointer_startsContentStreamAndCancelReleasesBoth() {
        val context = targetContext()
        val shell = GameFoldShellLayout(context)
        val content = RecordingTouchView(context)
        val buttons = mutableListOf<Pair<GameFoldButton, Boolean>>()
        shell.addContentView(content)
        shell.setControllerInputListener(recordingListener(buttons))
        measureAndLayout(shell)

        shell.dispatchTouchEvent(event(MotionEvent.ACTION_DOWN, pointer(0, 2113f, 94f)))
        shell.dispatchTouchEvent(
            event(
                MotionEvent.ACTION_POINTER_DOWN or
                    (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT),
                pointer(0, 2113f, 94f),
                pointer(1, 1152f, 500f),
            ),
        )
        shell.dispatchTouchEvent(
            event(
                MotionEvent.ACTION_CANCEL,
                pointer(0, 2113f, 94f),
                pointer(1, 1152f, 500f),
            ),
        )

        assertEquals(MotionEvent.ACTION_DOWN, content.events.first().action)
        assertEquals(listOf(1), content.events.first().pointerIds)
        assertEquals(MotionEvent.ACTION_CANCEL, content.events.last().action)
        assertEquals(
            listOf(GameFoldButton.HOME to true, GameFoldButton.HOME to false),
            buttons,
        )
    }

    private fun measureAndLayout(
        shell: GameFoldShellLayout,
        width: Int = 2304,
        height: Int = 1440,
    ) {
        shell.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
        shell.layout(0, 0, width, height)
    }

    private fun renderShellOver(shell: GameFoldShellLayout, gameColor: Int): Bitmap {
        val content = View(shell.context).apply { setBackgroundColor(gameColor) }
        shell.addContentView(content)
        measureAndLayout(shell)
        return Bitmap.createBitmap(2304, 1440, Bitmap.Config.ARGB_8888).also { bitmap ->
            shell.draw(Canvas(bitmap))
        }
    }

    private fun renderMeasuredShell(shell: GameFoldShellLayout): Bitmap =
        Bitmap.createBitmap(shell.width, shell.height, Bitmap.Config.ARGB_8888).also { bitmap ->
            shell.draw(Canvas(bitmap))
        }

    private fun countPixelsDifferentFrom(bitmap: Bitmap, color: Int, top: Int): Int {
        var count = 0
        for (y in top until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitmap.getPixel(x, y) != color) count += 1
            }
        }
        return count
    }

    private fun assertRegionEquals(
        expected: Bitmap,
        actual: Bitmap,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        for (y in top until bottom) {
            for (x in left until right) {
                assertEquals("pixel ($x,$y)", expected.getPixel(x, y), actual.getPixel(x, y))
            }
        }
    }

    private fun assertColorNear(expected: Int, actual: Int, tolerance: Int) {
        val message = "expected=${colorChannels(expected)} actual=${colorChannels(actual)}"
        assertEquals(message, Color.alpha(expected), Color.alpha(actual))
        assertTrue(message, kotlin.math.abs(Color.red(expected) - Color.red(actual)) <= tolerance)
        assertTrue(message, kotlin.math.abs(Color.green(expected) - Color.green(actual)) <= tolerance)
        assertTrue(message, kotlin.math.abs(Color.blue(expected) - Color.blue(actual)) <= tolerance)
    }

    private fun colorChannels(color: Int): String =
        "a=${Color.alpha(color)},r=${Color.red(color)},g=${Color.green(color)},b=${Color.blue(color)}"

    private fun targetContext(smallestWidthDp: Int = 576): android.content.Context {
        val base = ApplicationProvider.getApplicationContext<android.content.Context>()
        val configuration = Configuration(base.resources.configuration).apply {
            this.smallestScreenWidthDp = smallestWidthDp
            orientation = Configuration.ORIENTATION_LANDSCAPE
        }
        return base.createConfigurationContext(configuration)
    }

    private fun assertBounds(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        assertEquals(left, view.left)
        assertEquals(top, view.top)
        assertEquals(right, view.right)
        assertEquals(bottom, view.bottom)
    }

    private fun recordingListener(
        buttons: MutableList<Pair<GameFoldButton, Boolean>>,
    ) = object : GameFoldControllerListener {
        override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
            buttons += button to pressed
        }

        override fun onStickChanged(stick: GameFoldStick, x: Float, y: Float) = Unit
    }

    private data class Pointer(val id: Int, val x: Float, val y: Float)

    private fun pointer(id: Int, x: Float, y: Float) = Pointer(id, x, y)

    private fun event(action: Int, vararg pointers: Pointer): MotionEvent {
        val properties = Array(pointers.size) { index ->
            MotionEvent.PointerProperties().apply {
                id = pointers[index].id
                toolType = MotionEvent.TOOL_TYPE_FINGER
            }
        }
        val coordinates = Array(pointers.size) { index ->
            MotionEvent.PointerCoords().apply {
                x = pointers[index].x
                y = pointers[index].y
                pressure = 1f
                size = 1f
            }
        }
        return MotionEvent.obtain(
            0L,
            0L,
            action,
            pointers.size,
            properties,
            coordinates,
            0,
            0,
            1f,
            1f,
            0,
            0,
            InputDevice.SOURCE_TOUCHSCREEN,
            0,
        )
    }

    private class RecordingTouchView(context: android.content.Context) : View(context) {
        data class Event(val action: Int, val pointerIds: List<Int>)

        val events = mutableListOf<Event>()

        override fun onTouchEvent(event: MotionEvent): Boolean {
            events += Event(
                action = event.actionMasked,
                pointerIds = List(event.pointerCount, event::getPointerId),
            )
            return true
        }
    }
}

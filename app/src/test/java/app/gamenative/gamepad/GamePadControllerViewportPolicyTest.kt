package app.gamenative.gamepad

import org.junit.Assert.assertEquals
import org.junit.Test

class GamePadControllerViewportPolicyTest {
    @Test
    fun nativeWindowKeepsTheApprovedControllerCoordinatesUnchanged() {
        assertEquals(
            GamePadTransform(scale = 1f, translateX = 0f, translateY = 0f),
            GamePadControllerViewportPolicy.fit(windowWidth = 2304, windowHeight = 1440),
        )
        assertEquals(
            GamePadTransform(scale = 1f, translateX = 0f, translateY = 0f),
            GamePadControllerViewportPolicy.homeFit(windowWidth = 2304, windowHeight = 1440),
        )
    }

    @Test
    fun sixteenNineTargetKeepsNativeControllerScaleAndHorizontalOrigin() {
        val native = GamePadControllerViewportPolicy.fit(
            windowWidth = 2304,
            windowHeight = 1440,
        )
        val target = GamePadControllerViewportPolicy.fit(
            windowWidth = 2304,
            windowHeight = 1296,
        )

        assertEquals(1f, target.scale, 0.0001f)
        assertEquals(native.scale, target.scale, 0.0001f)
        assertEquals(native.translateX, target.translateX, 0.0001f)
        assertEquals(-72f, target.translateY, 0.0001f)
        assertEquals(
            native.toScreen(GamePadPoint(483f, 1314f)).x,
            target.toScreen(GamePadPoint(483f, 1314f)).x,
            0.0001f,
        )
        assertEquals(
            native.toScreen(GamePadPoint(483f, 1314f)).y - 72f,
            target.toScreen(GamePadPoint(483f, 1314f)).y,
            0.0001f,
        )
    }

    @Test
    fun sixteenNineHomeTargetMovesLogoDownWithoutMovingLowerControls() {
        assertEquals(
            GamePadTransform(scale = 1f, translateX = 0f, translateY = 0f),
            GamePadControllerViewportPolicy.homeFit(
                windowWidth = 2304,
                windowHeight = 1296,
            ),
        )
        assertEquals(-72f, GamePadControllerViewportPolicy.fit(2304, 1296).translateY, 0.0001f)
    }

    @Test
    fun fullHdLowerControlsAreLargerFurtherOutAndLowerWithoutClipping() {
        val transform = GamePadControllerViewportPolicy.fit(
            windowWidth = 1920,
            windowHeight = 1080,
        )

        assertEquals(0.82f, transform.scale, 0.0001f)
        assertEquals(16f, transform.translateX, 0.0001f)
        assertEquals(-44f, transform.translateY, 0.0001f)
        val screenPoint = transform.toScreen(GamePadPoint(112.64f, 828.92f))
        assertEquals(108.3648f, screenPoint.x, 0.0001f)
        assertEquals(635.7144f, screenPoint.y, 0.0001f)
        val sourcePoint = transform.toSource(GamePadPoint(108.3648f, 635.7144f))
        assertEquals(112.64f, sourcePoint.x, 0.0001f)
        assertEquals(828.92f, sourcePoint.y, 0.0001f)
    }

    @Test
    fun fullHdHomeKeepsItsSizeAndMovesRightByFortyEightPixels() {
        val transform = GamePadControllerViewportPolicy.homeFit(
            windowWidth = 1920,
            windowHeight = 1080,
        )

        assertEquals(0.75f, transform.scale, 0.0001f)
        assertEquals(144f, transform.translateX, 0.0001f)
        assertEquals(0f, transform.translateY, 0.0001f)
        assertEquals(
            GamePadPoint(1830f, 99.75f),
            transform.toScreen(GamePadPoint(2248f, 133f)),
        )
    }
}

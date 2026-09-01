package app.gamenative.gamepad

import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldPoint
import app.gamenative.gamefold.ui.button
import app.gamenative.gamefold.ui.classifyGameFoldControl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePadControllerGeometryTest {
    @Test
    fun leftAndRightVisualBounds_landOnApprovedAnchors() {
        assertPoint(GamePadPoint(86f, 804.44f), GamePadControllerGeometry.LEFT_TRANSFORM.toScreen(GamePadPoint(93f, 1277f)))
        assertPoint(GamePadPoint(646.88f, 1361f), GamePadControllerGeometry.LEFT_TRANSFORM.toScreen(GamePadPoint(872f, 2050f)))
        assertPoint(GamePadPoint(1657.12f, 804.44f), GamePadControllerGeometry.RIGHT_TRANSFORM.toScreen(GamePadPoint(1608f, 1277f)))
        assertPoint(GamePadPoint(2218f, 1361f), GamePadControllerGeometry.RIGHT_TRANSFORM.toScreen(GamePadPoint(2387f, 2050f)))
    }

    @Test
    fun inverseTransform_recoversTheBaselineTouchPoint() {
        val source = GamePadPoint(483f, 1664f)
        val screen = GamePadControllerGeometry.LEFT_TRANSFORM.toScreen(source)
        assertPoint(source, GamePadControllerGeometry.LEFT_TRANSFORM.toSource(screen))
    }

    @Test
    fun homeUsesTheExactApprovedVisualAndHitRectangle() {
        val home = GamePadControllerGeometry.HOME_BOUNDS
        assertEquals(GamePadRect(1978f, 56f, 2248f, 133f), home)
        assertEquals(270f, home.width, 0f)
        assertEquals(77f, home.height, 0f)
        assertTrue(home.contains(GamePadPoint(1978f, 56f)))
        assertTrue(home.contains(GamePadPoint(2248f, 133f)))
        assertFalse(home.contains(GamePadPoint(1977.99f, 100f)))
        assertFalse(home.contains(GamePadPoint(2100f, 133.01f)))
    }

    @Test
    fun approvedLeftGroupVisualBounds_doNotIntersectTheMeasuredCutout() {
        val cutout = GamePadRect(0f, 694f, 82f, 746f)
        val leftVisual = GamePadControllerGeometry.LEFT_TRANSFORM.mapRect(
            GamePadRect(93f, 1277f, 872f, 2050f),
        )
        assertFalse(leftVisual.intersects(cutout))
    }

    @Test
    fun transformedScreenPoints_reuseBaselineSemanticClassification() {
        val leftUp = GamePadControllerGeometry.LEFT_TRANSFORM.toScreen(GamePadPoint(483f, 1314f))
        val rightY = GamePadControllerGeometry.RIGHT_TRANSFORM.toScreen(GamePadPoint(1997f, 1314f))

        assertEquals(GameFoldButton.DPAD_UP, classifyGamePadControl(leftUp)?.control?.button())
        assertEquals(GameFoldButton.Y, classifyGamePadControl(rightY)?.control?.button())
        assertEquals(
            GameFoldButton.HOME,
            classifyGamePadControl(GamePadPoint(2100f, 90f))?.control?.button(),
        )
        assertEquals(null, classifyGamePadControl(GamePadPoint(1152f, 720f)))
    }

    @Test
    fun smallButtonHitZones_acceptTwoPointFourNineVisibleRadiiAtViewportEdges() {
        val probes = listOf(
            GamePadPoint(11.9768f, 854.12f) to GameFoldButton.ZL,
            GamePadPoint(597.2f, 730.4168f) to GameFoldButton.L,
            GamePadPoint(1706.8f, 730.4168f) to GameFoldButton.R,
            GamePadPoint(2292.0232f, 854.12f) to GameFoldButton.ZR,
            GamePadPoint(597.2f, 1435.0232f) to GameFoldButton.MINUS,
            GamePadPoint(1706.8f, 1435.0232f) to GameFoldButton.PLUS,
        )

        probes.forEach { (screenPoint, expectedButton) ->
            assertEquals(expectedButton, classifyGamePadControl(screenPoint)?.control?.button())
        }
    }

    @Test
    fun smallButtonHitZones_rejectTwoPointFiveOneVisibleRadii() {
        val probes = listOf(
            GamePadPoint(10.9832f, 854.12f),
            GamePadPoint(597.2f, 729.4232f),
            GamePadPoint(1706.8f, 729.4232f),
            GamePadPoint(2293.0168f, 854.12f),
            GamePadPoint(597.2f, 1436.0168f),
            GamePadPoint(1706.8f, 1436.0168f),
        )

        probes.forEach { screenPoint ->
            assertEquals(null, classifyGamePadControl(screenPoint))
        }
    }

    @Test
    fun gamePadExpansion_doesNotExpandSharedGameFoldSmallButtons() {
        val sourcePoint = GameFoldPoint(803f, 1174.19f)
        val screenPoint = GamePadControllerGeometry.LEFT_TRANSFORM.toScreen(
            GamePadPoint(sourcePoint.x, sourcePoint.y),
        )

        assertEquals(GameFoldButton.L, classifyGamePadControl(screenPoint)?.control?.button())
        assertEquals(null, classifyGameFoldControl(sourcePoint))
    }

    private fun assertPoint(expected: GamePadPoint, actual: GamePadPoint) {
        assertEquals(expected.x, actual.x, 0.01f)
        assertEquals(expected.y, actual.y, 0.01f)
    }
}

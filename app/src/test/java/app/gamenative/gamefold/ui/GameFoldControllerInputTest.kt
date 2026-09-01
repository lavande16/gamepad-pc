package app.gamenative.gamefold.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldControllerInputTest {
    @Test
    fun pcFaceLayout_mapsTopYRightBBottomAAndLeftX() {
        assertEquals(
            GameFoldButton.DPAD_UP,
            classifyGameFoldControl(GameFoldPoint(483f, 1314f))?.button(),
        )
        assertEquals(
            GameFoldButton.Y,
            classifyGameFoldControl(GameFoldPoint(1997f, 1314f))?.button(),
        )
        assertEquals(
            GameFoldButton.B,
            classifyGameFoldControl(GameFoldPoint(2297f, 1664f))?.button(),
        )
        assertEquals(
            GameFoldButton.A,
            classifyGameFoldControl(GameFoldPoint(1997f, 2014f))?.button(),
        )
        assertEquals(
            GameFoldButton.X,
            classifyGameFoldControl(GameFoldPoint(1697f, 1664f))?.button(),
        )
    }

    @Test
    fun officialReferenceGeometry_mapsShouldersTriggersAndMenuButtons() {
        assertEquals(
            GameFoldButton.ZL,
            classifyGameFoldControl(GameFoldPoint(162f, 1346f))?.button(),
        )
        assertEquals(
            GameFoldButton.R,
            classifyGameFoldControl(GameFoldPoint(1677f, 1346f))?.button(),
        )
        assertEquals(
            GameFoldButton.MINUS,
            classifyGameFoldControl(GameFoldPoint(803f, 1981f))?.button(),
        )
        assertEquals(
            GameFoldButton.PLUS,
            classifyGameFoldControl(GameFoldPoint(1677f, 1981f))?.button(),
        )
        assertEquals(
            GameFoldButton.HOME,
            classifyGameFoldControl(GameFoldPoint(1240f, 1350f))?.button(),
        )
    }

    @Test
    fun stickHitAndNormalization_preserveDeadZoneAndUnitRange() {
        val center = gameFoldStickCenter(GameFoldPadSide.LEFT)
        val control = classifyGameFoldControl(center)
        assertNotNull(control)
        assertTrue(control is GameFoldActiveControl.Stick)

        assertEquals(GameFoldPoint(0f, 0f), gameFoldNormalizedStickValue(GameFoldPoint(1f, 1f)))
        val fullRight = gameFoldNormalizedStickValue(
            GameFoldPoint(GameFoldControllerGeometry.STICK_MAX_TRAVEL, 0f),
        )
        assertEquals(1f, fullRight.x, 0.0001f)
        assertEquals(0f, fullRight.y, 0.0001f)
    }

    @Test
    fun displayAperture_doesNotClassifyTouchesInsideStickAsButtons() {
        val leftCenter = gameFoldStickCenter(GameFoldPadSide.LEFT)
        val rightCenter = gameFoldStickCenter(GameFoldPadSide.RIGHT)
        assertNull(classifyGameFoldControl(leftCenter)?.button())
        assertNull(classifyGameFoldControl(rightCenter)?.button())
    }

    @Test
    fun groupClassifier_neverLeaksControlsFromTheOppositeSideOrLegacyHome() {
        assertEquals(
            GameFoldButton.DPAD_UP,
            classifyGameFoldControlInGroup(
                GameFoldPoint(483f, 1314f),
                GameFoldControlGroup.LEFT,
            )?.button(),
        )
        assertNull(
            classifyGameFoldControlInGroup(
                GameFoldPoint(1997f, 1314f),
                GameFoldControlGroup.LEFT,
            ),
        )
        assertEquals(
            GameFoldButton.Y,
            classifyGameFoldControlInGroup(
                GameFoldPoint(1997f, 1314f),
                GameFoldControlGroup.RIGHT,
            )?.button(),
        )
        assertNull(
            classifyGameFoldControlInGroup(
                GameFoldPoint(1240f, 1350f),
                GameFoldControlGroup.RIGHT,
            ),
        )
    }
}

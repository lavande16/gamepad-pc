package app.gamenative.gamepad

import org.junit.Assert.assertEquals
import org.junit.Test

class GamePadHomeHeaderPolicyTest {
    @Test
    fun withoutDualScreen_resizeIsImmediatelyBeforeSettings() {
        assertEquals(
            listOf(
                GamePadHomeHeaderAction.RESIZE,
                GamePadHomeHeaderAction.SETTINGS,
            ),
            GamePadHomeHeaderPolicy.visibleActions(dualScreenAvailable = false),
        )
    }

    @Test
    fun withDualScreen_resizeRemainsImmediatelyBeforeSettings() {
        assertEquals(
            listOf(
                GamePadHomeHeaderAction.DUAL_SCREEN,
                GamePadHomeHeaderAction.RESIZE,
                GamePadHomeHeaderAction.SETTINGS,
            ),
            GamePadHomeHeaderPolicy.visibleActions(dualScreenAvailable = true),
        )
    }
}

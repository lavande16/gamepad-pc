package app.gamenative.gamepad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePadControllerVisibilityStateTest {
    @Test
    fun tenSecondsWithoutTouchTargetsHiddenWithA300msTransition() {
        val state = GamePadControllerVisibilityState()
        state.setEnabled(true, nowMs = 0L)

        assertFalse(state.update(nowMs = 9_999L))
        assertTrue(state.controlsVisible)
        assertTrue(state.update(nowMs = 10_000L))
        assertFalse(state.controlsVisible)
        assertEquals(300L, state.transitionDurationMs)
    }

    @Test
    fun activeTouchPreventsHideUntilTenSecondsAfterRelease() {
        val state = GamePadControllerVisibilityState()
        state.setEnabled(true, nowMs = 0L)

        state.onTouchDown(nowMs = 4_500L, hitsHome = false)
        assertFalse(state.update(nowMs = 10_000L))
        assertTrue(state.controlsVisible)

        state.onTouchUp(nowMs = 10_000L)
        assertFalse(state.update(nowMs = 19_999L))
        assertTrue(state.controlsVisible)
        assertTrue(state.update(nowMs = 20_000L))
        assertFalse(state.controlsVisible)
    }

    @Test
    fun hiddenNonHomeTouchOnlyWakesWhileVisibleHomeStillDispatches() {
        val state = GamePadControllerVisibilityState()
        state.setEnabled(true, nowMs = 0L)
        state.update(nowMs = 10_000L)

        assertEquals(
            GamePadWakeTouchDisposition.WAKE_ONLY,
            state.onTouchDown(nowMs = 10_001L, hitsHome = false),
        )
        assertTrue(state.controlsVisible)
        state.onTouchUp(nowMs = 10_001L)
        state.update(nowMs = 20_001L)

        assertEquals(
            GamePadWakeTouchDisposition.DISPATCH,
            state.onTouchDown(nowMs = 20_002L, hitsHome = true),
        )
        assertTrue(state.controlsVisible)
    }

    @Test
    fun cancelledGestureRestartsIdleCountdown() {
        val state = GamePadControllerVisibilityState()
        state.setEnabled(true, nowMs = 0L)

        state.onTouchDown(nowMs = 4_500L, hitsHome = false)
        state.onTouchCancel(nowMs = 10_000L)

        assertFalse(state.update(nowMs = 19_999L))
        assertTrue(state.update(nowMs = 20_000L))
    }
}

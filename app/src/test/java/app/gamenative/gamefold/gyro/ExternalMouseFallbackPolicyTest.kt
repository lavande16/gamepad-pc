package app.gamenative.gamefold.gyro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalMouseFallbackPolicyTest {
    @Test
    fun forwardsNonzeroExternalRelativeMouseMoveWhenPointerCaptureIsMissing() {
        assertTrue(
            ExternalMouseFallbackPolicy.shouldForward(
                hasPointerCapture = false,
                isExternal = true,
                supportsRelativeMouse = true,
                isMove = true,
                dx = 12,
                dy = -7,
            ),
        )
    }

    @Test
    fun doesNotDoubleForwardWhenPointerCaptureAlreadyOwnsTheMouse() {
        assertFalse(
            ExternalMouseFallbackPolicy.shouldForward(
                hasPointerCapture = true,
                isExternal = true,
                supportsRelativeMouse = true,
                isMove = true,
                dx = 12,
                dy = -7,
            ),
        )
    }

    @Test
    fun rejectsVirtualAbsoluteStationaryAndNonMoveEvents() {
        assertFalse(ExternalMouseFallbackPolicy.shouldForward(false, false, true, true, 12, 0))
        assertFalse(ExternalMouseFallbackPolicy.shouldForward(false, true, false, true, 12, 0))
        assertFalse(ExternalMouseFallbackPolicy.shouldForward(false, true, true, false, 12, 0))
        assertFalse(ExternalMouseFallbackPolicy.shouldForward(false, true, true, true, 0, 0))
    }
}

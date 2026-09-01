package com.winlator.xserver.extensions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XInput2RawMotionRoutingPolicyTest {
    @Test
    fun wineMasterPointerInitializationReceivesDedicatedRawMotionFallback() {
        assertTrue(
            XInput2RawMotionRoutingPolicy.shouldUseWineFallback(1, true, 2),
        )
    }

    @Test
    fun unrelatedTouchSelectionDoesNotReceiveDedicatedRawMotionFallback() {
        assertFalse(
            XInput2RawMotionRoutingPolicy.shouldUseWineFallback(1, false, 2),
        )
    }

    @Test
    fun masterKeyboardDoesNotMatchPointerMotionFallback() {
        assertFalse(
            XInput2RawMotionRoutingPolicy.shouldUseWineFallback(3, true, 2),
        )
    }
}

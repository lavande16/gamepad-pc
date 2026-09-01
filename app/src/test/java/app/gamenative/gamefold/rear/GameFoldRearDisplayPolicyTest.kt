package app.gamenative.gamefold.rear

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldRearDisplayPolicyTest {
    @Test
    fun rearSession_requiresVivoLandscapeFrontAndExplicitEnable() {
        assertTrue(GameFoldRearDisplayPolicy.canEnable(isVivo = true, isPortrait = false))
        assertFalse(GameFoldRearDisplayPolicy.canEnable(isVivo = false, isPortrait = false))
        assertFalse(GameFoldRearDisplayPolicy.canEnable(isVivo = true, isPortrait = true))
    }

    @Test
    fun toggle_isHiddenWhenUnsupportedOrPortrait() {
        assertTrue(GameFoldRearDisplayPolicy.shouldShowToggle(isSupported = true, isPortrait = false))
        assertFalse(GameFoldRearDisplayPolicy.shouldShowToggle(isSupported = true, isPortrait = true))
        assertFalse(GameFoldRearDisplayPolicy.shouldShowToggle(isSupported = false, isPortrait = false))
    }

    @Test
    fun rearDisplayIds_andDeviceState_matchOfficialGameFoldContract() {
        assertTrue(GameFoldRearDisplayPolicy.REAR_DISPLAY_ID == 3)
        assertTrue(GameFoldRearDisplayPolicy.DUAL_STATE == 100)
        assertTrue(GameFoldRearDisplayPolicy.REAR_DISPLAY_ID_BEFORE_STATE == 2)
    }

    @Test
    fun userLeave_isDeferredOnlyDuringAnExplicitInternalRearTransition() {
        assertTrue(GameFoldRearDisplayPolicy.shouldDeferUserLeave(internalRearTransition = true))
        assertFalse(GameFoldRearDisplayPolicy.shouldDeferUserLeave(internalRearTransition = false))
    }

    @Test
    fun deferredUserLeave_isResolvedFromTheFinalFrontAppState() {
        assertEquals(
            GameFoldRearDisplayPolicy.DeferredLeaveDecision.NONE,
            GameFoldRearDisplayPolicy.resolveDeferredUserLeave(
                pendingUserLeave = false,
                frontForeground = false,
            ),
        )
        assertEquals(
            GameFoldRearDisplayPolicy.DeferredLeaveDecision.KEEP_SESSION,
            GameFoldRearDisplayPolicy.resolveDeferredUserLeave(
                pendingUserLeave = true,
                frontForeground = true,
            ),
        )
        assertEquals(
            GameFoldRearDisplayPolicy.DeferredLeaveDecision.CLOSE_SESSION,
            GameFoldRearDisplayPolicy.resolveDeferredUserLeave(
                pendingUserLeave = true,
                frontForeground = false,
            ),
        )
    }

    @Test
    fun sustainedFocusLoss_closesAfterInternalTransitionWithoutUserLeaveGate() {
        assertFalse(
            GameFoldRearDisplayPolicy.shouldCloseAfterFocusLoss(
                sessionEnabled = true,
                internalRearTransition = true,
                focusLossArmed = true,
                hasFocusedFront = false,
            ),
        )
        assertTrue(
            GameFoldRearDisplayPolicy.shouldCloseAfterFocusLoss(
                sessionEnabled = true,
                internalRearTransition = false,
                focusLossArmed = true,
                hasFocusedFront = false,
            ),
        )
        assertFalse(
            GameFoldRearDisplayPolicy.shouldCloseAfterFocusLoss(
                sessionEnabled = true,
                internalRearTransition = false,
                focusLossArmed = true,
                hasFocusedFront = true,
            ),
        )
        assertFalse(
            GameFoldRearDisplayPolicy.shouldCloseAfterFocusLoss(
                sessionEnabled = true,
                internalRearTransition = false,
                focusLossArmed = false,
                hasFocusedFront = false,
            ),
        )
    }

    @Test
    fun explicitUserLeave_closesOnceTheInternalRearTransitionHasSettled() {
        assertTrue(
            GameFoldRearDisplayPolicy.shouldCloseAfterUserLeave(
                sessionEnabled = true,
                internalRearTransition = false,
            ),
        )
        assertFalse(
            GameFoldRearDisplayPolicy.shouldCloseAfterUserLeave(
                sessionEnabled = true,
                internalRearTransition = true,
            ),
        )
    }

    @Test
    fun zeroStartedFrontActivities_closesAfterInternalTransition() {
        assertTrue(
            GameFoldRearDisplayPolicy.shouldCloseAfterFrontStopped(
                sessionEnabled = true,
                internalRearTransition = false,
                frontStartedCount = 0,
            ),
        )
        assertFalse(
            GameFoldRearDisplayPolicy.shouldCloseAfterFrontStopped(
                sessionEnabled = true,
                internalRearTransition = true,
                frontStartedCount = 0,
            ),
        )
        assertFalse(
            GameFoldRearDisplayPolicy.shouldCloseAfterFrontStopped(
                sessionEnabled = true,
                internalRearTransition = false,
                frontStartedCount = 1,
            ),
        )
    }

    @Test
    fun rearTouchLayout_normalizesPortraitDisplayToLandscapeControls() {
        assertEquals(
            GameFoldRearDisplayPolicy.RearTouchLayout(
                width = 2748,
                height = 1172,
                rotationDegrees = 0f,
            ),
            GameFoldRearDisplayPolicy.rearTouchLayout(
                displayWidth = 2748,
                displayHeight = 1172,
            ),
        )
        assertEquals(
            GameFoldRearDisplayPolicy.RearTouchLayout(
                width = 2748,
                height = 1172,
                rotationDegrees = 270f,
            ),
            GameFoldRearDisplayPolicy.rearTouchLayout(
                displayWidth = 1172,
                displayHeight = 2748,
            ),
        )
        assertEquals(null, GameFoldRearDisplayPolicy.rearTouchLayout(0, 1172))
    }

    @Test
    fun rearTouchLayout_isNotReappliedWhenGeometryAndRotationAlreadyMatch() {
        val target = GameFoldRearDisplayPolicy.RearTouchLayout(
            width = 2748,
            height = 1172,
            rotationDegrees = 270f,
        )
        assertFalse(
            GameFoldRearDisplayPolicy.shouldUpdateRearTouchLayout(
                currentWidth = 2748,
                currentHeight = 1172,
                currentRotationDegrees = 270f,
                target = target,
            ),
        )
        assertTrue(
            GameFoldRearDisplayPolicy.shouldUpdateRearTouchLayout(
                currentWidth = 1172,
                currentHeight = 2748,
                currentRotationDegrees = 0f,
                target = target,
            ),
        )
    }

    @Test
    fun realActivityDestroyClosesSession_butConfigurationReplacementDoesNot() {
        assertTrue(GameFoldRearDisplayPolicy.shouldCloseOnActivityDestroy(changingConfigurations = false))
        assertFalse(GameFoldRearDisplayPolicy.shouldCloseOnActivityDestroy(changingConfigurations = true))
    }
}

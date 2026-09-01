package app.gamenative.gamefold.rear

/** Constants and eligibility rules for the official vivo rear-display session. */
object GameFoldRearDisplayPolicy {
    const val FRONT_DISPLAY_ID = 0
    const val REAR_DISPLAY_ID_BEFORE_STATE = 2
    const val REAR_DISPLAY_ID = 3
    const val DUAL_STATE = 100

    fun canEnable(isVivo: Boolean, isPortrait: Boolean): Boolean = isVivo && !isPortrait

    fun shouldShowToggle(isSupported: Boolean, isPortrait: Boolean): Boolean =
        isSupported && !isPortrait

    enum class DeferredLeaveDecision {
        NONE,
        KEEP_SESSION,
        CLOSE_SESSION,
    }

    data class RearTouchLayout(
        val width: Int,
        val height: Int,
        val rotationDegrees: Float,
    )

    fun shouldDeferUserLeave(internalRearTransition: Boolean): Boolean =
        internalRearTransition

    fun resolveDeferredUserLeave(
        pendingUserLeave: Boolean,
        frontForeground: Boolean,
    ): DeferredLeaveDecision = when {
        !pendingUserLeave -> DeferredLeaveDecision.NONE
        frontForeground -> DeferredLeaveDecision.KEEP_SESSION
        else -> DeferredLeaveDecision.CLOSE_SESSION
    }

    fun shouldCloseAfterFocusLoss(
        sessionEnabled: Boolean,
        internalRearTransition: Boolean,
        focusLossArmed: Boolean,
        hasFocusedFront: Boolean,
    ): Boolean =
        sessionEnabled && !internalRearTransition && focusLossArmed && !hasFocusedFront

    fun shouldCloseAfterUserLeave(
        sessionEnabled: Boolean,
        internalRearTransition: Boolean,
    ): Boolean = sessionEnabled && !internalRearTransition

    fun shouldCloseAfterFrontStopped(
        sessionEnabled: Boolean,
        internalRearTransition: Boolean,
        frontStartedCount: Int,
    ): Boolean = sessionEnabled && !internalRearTransition && frontStartedCount == 0

    fun rearTouchLayout(displayWidth: Int, displayHeight: Int): RearTouchLayout? {
        if (displayWidth <= 0 || displayHeight <= 0) return null
        val portraitDisplay = displayHeight > displayWidth
        return RearTouchLayout(
            width = if (portraitDisplay) displayHeight else displayWidth,
            height = if (portraitDisplay) displayWidth else displayHeight,
            rotationDegrees = if (portraitDisplay) 270f else 0f,
        )
    }

    fun shouldUpdateRearTouchLayout(
        currentWidth: Int,
        currentHeight: Int,
        currentRotationDegrees: Float,
        target: RearTouchLayout,
    ): Boolean =
        currentWidth != target.width ||
            currentHeight != target.height ||
            currentRotationDegrees != target.rotationDegrees

    fun shouldCloseOnActivityDestroy(changingConfigurations: Boolean): Boolean =
        !changingConfigurations
}

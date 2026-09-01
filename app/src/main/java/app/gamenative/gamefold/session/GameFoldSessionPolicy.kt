package app.gamenative.gamefold.session

/**
 * Decides how the local shell should handle a game selected from Home while a runtime may still
 * be preserved behind the launcher. Keeping this decision pure makes the Home/Back state machine
 * auditable without requiring a running Wine environment.
 */
enum class GameFoldSessionLaunchAction {
    RESUME_PRESERVED,
    REPLACE_PRESERVED,
    COLD_START,
}

enum class GameFoldRuntimeInputAttachmentAction {
    CREATE_NEW,
    REATTACH_EXISTING,
}

enum class GameFoldRootBackAction {
    CANCEL_PENDING_LAUNCH,
    DISPATCH_TO_GAME,
    POP_NAVIGATION,
    IGNORE,
}

/**
 * Immutable decision for one navigation into XServer.
 *
 * The global paused-at-home flag is consumed while the renderer is reattached. Keeping that
 * mutable flag as a Compose input would reclassify the same navigation as a cold/replacement
 * launch on the next recomposition and make the XServer loading overlay visible forever.
 */
data class GameFoldSessionLaunchIntent(
    val action: GameFoldSessionLaunchAction,
) {
    val resumePreserved: Boolean
        get() = action == GameFoldSessionLaunchAction.RESUME_PRESERVED

    val rendererAlreadyStarted: Boolean
        get() = resumePreserved

    companion object {
        fun capture(
            localShell: Boolean,
            environmentPresent: Boolean,
            activeAppId: String?,
            selectedAppId: String,
            pausedAtHome: Boolean,
        ): GameFoldSessionLaunchIntent = GameFoldSessionLaunchIntent(
            GameFoldSessionPolicy.launchAction(
                localShell = localShell,
                environmentPresent = environmentPresent,
                activeAppId = activeAppId,
                selectedAppId = selectedAppId,
                pausedAtHome = pausedAtHome,
            ),
        )
    }
}

object GameFoldSessionPolicy {
    fun rootBackAction(
        localShell: Boolean,
        atHome: Boolean,
        launchPreparing: Boolean,
        environmentPresent: Boolean,
        gameBackRegistered: Boolean,
        hasBack: Boolean,
    ): GameFoldRootBackAction = when {
        localShell && atHome && launchPreparing ->
            GameFoldRootBackAction.CANCEL_PENDING_LAUNCH
        environmentPresent && gameBackRegistered ->
            GameFoldRootBackAction.DISPATCH_TO_GAME
        hasBack -> GameFoldRootBackAction.POP_NAVIGATION
        else -> GameFoldRootBackAction.IGNORE
    }

    fun runtimeInputAttachmentAction(
        reusedXServer: Boolean,
        existingHandlerPresent: Boolean,
    ): GameFoldRuntimeInputAttachmentAction =
        if (reusedXServer && existingHandlerPresent) {
            GameFoldRuntimeInputAttachmentAction.REATTACH_EXISTING
        } else {
            GameFoldRuntimeInputAttachmentAction.CREATE_NEW
        }

    fun launchAction(
        localShell: Boolean,
        environmentPresent: Boolean,
        activeAppId: String?,
        selectedAppId: String,
        pausedAtHome: Boolean,
    ): GameFoldSessionLaunchAction {
        if (!localShell || !environmentPresent) return GameFoldSessionLaunchAction.COLD_START
        if (pausedAtHome && activeAppId == selectedAppId) {
            return GameFoldSessionLaunchAction.RESUME_PRESERVED
        }
        return GameFoldSessionLaunchAction.REPLACE_PRESERVED
    }

    /**
     * A preserved session is only resumable while all three identity signals still agree.
     * Keeping this guard separate from launch routing makes stale process/app state explicit and
     * prevents a later Home launch from treating a half-torn-down environment as a live session.
     */
    fun canResumePreserved(
        localShell: Boolean,
        environmentPresent: Boolean,
        activeAppId: String?,
        selectedAppId: String,
        pausedAtHome: Boolean,
    ): Boolean = launchAction(
        localShell = localShell,
        environmentPresent = environmentPresent,
        activeAppId = activeAppId,
        selectedAppId = selectedAppId,
        pausedAtHome = pausedAtHome,
    ) == GameFoldSessionLaunchAction.RESUME_PRESERVED
}

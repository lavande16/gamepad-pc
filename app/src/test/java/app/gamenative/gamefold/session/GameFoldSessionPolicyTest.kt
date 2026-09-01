package app.gamenative.gamefold.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldSessionPolicyTest {
    @Test
    fun rootBackCancelsHomeLaunchBeforeDispatchingToPreservedRuntime() {
        assertEquals(
            GameFoldRootBackAction.CANCEL_PENDING_LAUNCH,
            GameFoldSessionPolicy.rootBackAction(
                localShell = true,
                atHome = true,
                launchPreparing = true,
                environmentPresent = true,
                gameBackRegistered = false,
                hasBack = false,
            ),
        )
    }

    @Test
    fun rootBackDispatchesToLiveGameThenFallsBackToNavigationOrIgnore() {
        assertEquals(
            GameFoldRootBackAction.DISPATCH_TO_GAME,
            GameFoldSessionPolicy.rootBackAction(
                localShell = true,
                atHome = false,
                launchPreparing = true,
                environmentPresent = true,
                gameBackRegistered = true,
                hasBack = true,
            ),
        )
        assertEquals(
            GameFoldRootBackAction.POP_NAVIGATION,
            GameFoldSessionPolicy.rootBackAction(
                localShell = true,
                atHome = true,
                launchPreparing = false,
                environmentPresent = true,
                gameBackRegistered = false,
                hasBack = true,
            ),
        )
        assertEquals(
            GameFoldRootBackAction.IGNORE,
            GameFoldSessionPolicy.rootBackAction(
                localShell = true,
                atHome = true,
                launchPreparing = false,
                environmentPresent = true,
                gameBackRegistered = false,
                hasBack = false,
            ),
        )
    }
    @Test
    fun reusedXServerKeepsItsStartedInputHandler() {
        assertEquals(
            GameFoldRuntimeInputAttachmentAction.REATTACH_EXISTING,
            GameFoldSessionPolicy.runtimeInputAttachmentAction(
                reusedXServer = true,
                existingHandlerPresent = true,
            ),
        )
    }

    @Test
    fun coldOrIncompleteXServerCreatesAnInputHandler() {
        assertEquals(
            GameFoldRuntimeInputAttachmentAction.CREATE_NEW,
            GameFoldSessionPolicy.runtimeInputAttachmentAction(
                reusedXServer = false,
                existingHandlerPresent = false,
            ),
        )
        assertEquals(
            GameFoldRuntimeInputAttachmentAction.CREATE_NEW,
            GameFoldSessionPolicy.runtimeInputAttachmentAction(
                reusedXServer = true,
                existingHandlerPresent = false,
            ),
        )
    }

    @Test
    fun samePausedGameReusesTheExistingRuntime() {
        assertEquals(
            GameFoldSessionLaunchAction.RESUME_PRESERVED,
            GameFoldSessionPolicy.launchAction(
                localShell = true,
                environmentPresent = true,
                activeAppId = "custom:cyberpunk",
                selectedAppId = "custom:cyberpunk",
                pausedAtHome = true,
            ),
        )
    }

    @Test
    fun differentGameReplacesThePreservedRuntime() {
        assertEquals(
            GameFoldSessionLaunchAction.REPLACE_PRESERVED,
            GameFoldSessionPolicy.launchAction(
                localShell = true,
                environmentPresent = true,
                activeAppId = "custom:cyberpunk",
                selectedAppId = "custom:gta5",
                pausedAtHome = true,
            ),
        )
    }

    @Test
    fun noEnvironmentOrNonLocalShellAlwaysColdStarts() {
        assertEquals(
            GameFoldSessionLaunchAction.COLD_START,
            GameFoldSessionPolicy.launchAction(
                localShell = true,
                environmentPresent = false,
                activeAppId = null,
                selectedAppId = "custom:gta5",
                pausedAtHome = false,
            ),
        )
        assertEquals(
            GameFoldSessionLaunchAction.COLD_START,
            GameFoldSessionPolicy.launchAction(
                localShell = false,
                environmentPresent = true,
                activeAppId = "custom:cyberpunk",
                selectedAppId = "custom:cyberpunk",
                pausedAtHome = true,
            ),
        )
    }

    @Test
    fun staleIdentityCannotResumePreservedRuntime() {
        assertEquals(
            false,
            GameFoldSessionPolicy.canResumePreserved(
                localShell = true,
                environmentPresent = true,
                activeAppId = "custom:cyberpunk",
                selectedAppId = "custom:cyberpunk",
                pausedAtHome = false,
            ),
        )
        assertEquals(
            false,
            GameFoldSessionPolicy.canResumePreserved(
                localShell = true,
                environmentPresent = true,
                activeAppId = null,
                selectedAppId = "custom:cyberpunk",
                pausedAtHome = true,
            ),
        )
    }

    @Test
    fun capturedResumeIntent_remainsStableAfterPausedFlagIsConsumed() {
        val intent = GameFoldSessionLaunchIntent.capture(
            localShell = true,
            environmentPresent = true,
            activeAppId = "custom:cyberpunk",
            selectedAppId = "custom:cyberpunk",
            pausedAtHome = true,
        )

        // Resuming consumes the mutable global pausedAtHome flag. The navigation that was
        // already classified as a resume must not be reclassified during recomposition.
        val recomputedPolicyAfterConsume = GameFoldSessionPolicy.canResumePreserved(
            localShell = true,
            environmentPresent = true,
            activeAppId = "custom:cyberpunk",
            selectedAppId = "custom:cyberpunk",
            pausedAtHome = false,
        )

        assertEquals(false, recomputedPolicyAfterConsume)
        assertTrue(intent.resumePreserved)
        assertTrue(intent.rendererAlreadyStarted)
        assertEquals(GameFoldSessionLaunchAction.RESUME_PRESERVED, intent.action)
    }
}

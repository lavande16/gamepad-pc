package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalRuntimeBootstrapPolicyTest {
    @Test
    fun firstLocalShellLaunch_bootstrapsImageFsBeforeCreatingContainer() {
        assertEquals(
            listOf(
                LocalRuntimeLaunchStep.BOOTSTRAP_IMAGE_FS,
                LocalRuntimeLaunchStep.CREATE_CONTAINER,
            ),
            LocalRuntimeBootstrapPolicy.launchSteps(
                localShell = true,
                imageFsValid = false,
            ),
        )
        assertEquals(
            "bionic",
            LocalRuntimeBootstrapPolicy.bootstrapContainerVariant(localShell = true),
        )
    }

    @Test
    fun laterLocalShellLaunch_skipsBootstrapAndCreatesContainerDirectly() {
        assertEquals(
            listOf(LocalRuntimeLaunchStep.CREATE_CONTAINER),
            LocalRuntimeBootstrapPolicy.launchSteps(
                localShell = true,
                imageFsValid = true,
            ),
        )
    }

    @Test
    fun standardBuild_preservesItsExistingLaunchPath() {
        assertEquals(
            listOf(LocalRuntimeLaunchStep.CREATE_CONTAINER),
            LocalRuntimeBootstrapPolicy.launchSteps(
                localShell = false,
                imageFsValid = false,
            ),
        )
    }
}

package app.gamenative.ui.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Documents the single-card loading contract used by the local GameFold PC shell.
 * The state carries one message/progress pair for both the pre-launch and legacy pipeline.
 */
class MainStateLoadingContractTest {
    @Test
    fun defaultLoadingStateStartsWithUsefulStageCopyAndIndeterminateProgress() {
        val state = MainState()

        assertEquals("正在准备运行环境", state.loadingDialogMessage)
        assertEquals("正在准备运行环境", state.bootingSplashText)
        assertEquals(0f, state.loadingDialogProgress)
        assertEquals(-1f, state.bootingSplashProgress)
    }
}

package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Test

class InGameQuickMenuPolicyTest {
    @Test
    fun visibleMenu_backAlwaysDismisses() {
        assertEquals(
            InGameQuickMenuPolicy.BackAction.DISMISS,
            InGameQuickMenuPolicy.backAction(enabled = false, visible = true),
        )
        assertEquals(
            InGameQuickMenuPolicy.BackAction.DISMISS,
            InGameQuickMenuPolicy.backAction(enabled = true, visible = true),
        )
    }

    @Test
    fun enabledHiddenMenu_backOpensMenu() {
        assertEquals(
            InGameQuickMenuPolicy.BackAction.OPEN,
            InGameQuickMenuPolicy.backAction(enabled = true, visible = false),
        )
    }

    @Test
    fun disabledHiddenMenu_backRequestsGameExitWithoutOpening() {
        assertEquals(
            InGameQuickMenuPolicy.BackAction.REQUEST_EXIT,
            InGameQuickMenuPolicy.backAction(enabled = false, visible = false),
        )
    }
}

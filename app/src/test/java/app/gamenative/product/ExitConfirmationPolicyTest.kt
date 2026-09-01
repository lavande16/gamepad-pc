package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Test

class ExitConfirmationPolicyTest {
    @Test
    fun warningDisabled_exitsOnFirstRequest() {
        assertEquals(
            ExitConfirmationPolicy.Action.EXIT,
            ExitConfirmationPolicy.action(
                warnBeforeExit = false,
                warningVisible = false,
            ),
        )
    }

    @Test
    fun warningEnabled_firstRequestWarns() {
        assertEquals(
            ExitConfirmationPolicy.Action.WARN,
            ExitConfirmationPolicy.action(
                warnBeforeExit = true,
                warningVisible = false,
            ),
        )
    }

    @Test
    fun warningEnabled_secondRequestExits() {
        assertEquals(
            ExitConfirmationPolicy.Action.EXIT,
            ExitConfirmationPolicy.action(
                warnBeforeExit = true,
                warningVisible = true,
            ),
        )
    }
}

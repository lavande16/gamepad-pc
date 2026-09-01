package app.gamenative.gamefold.gyro

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeMouseTransportPolicyTest {
    @Test
    fun relativeMouseUsesWinHandlerWhenGuestTransportIsReady() {
        assertEquals(
            RelativeMouseTransportRoute.WIN_HANDLER,
            RelativeMouseTransportPolicy.route(relativeMouseMode = true, winHandlerReady = true),
        )
    }

    @Test
    fun absoluteMouseUsesXServerPointerPath() {
        assertEquals(
            RelativeMouseTransportRoute.XSERVER_POINTER,
            RelativeMouseTransportPolicy.route(relativeMouseMode = false, winHandlerReady = false),
        )
    }

    @Test
    fun relativeMouseDoesNotFallBackToTheWrongPointerPathBeforeGuestIsReady() {
        assertEquals(
            RelativeMouseTransportRoute.DROP,
            RelativeMouseTransportPolicy.route(relativeMouseMode = true, winHandlerReady = false),
        )
    }
}

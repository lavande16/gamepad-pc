package app.gamenative.gamefold.gyro

import org.junit.Assert.assertEquals
import org.junit.Test

class GyroMouseOutputDispatcherTest {
    @Test
    fun zeroDeltaDoesNotReachWineRelativeMouseTransport() {
        val moves = mutableListOf<IntegerMouseDelta>()
        val dispatcher = GyroMouseOutputDispatcher { dx, dy ->
            moves += IntegerMouseDelta(dx, dy)
            1
        }

        val recipients = dispatcher.dispatch(0, 0)

        assertEquals(emptyList<IntegerMouseDelta>(), moves)
        assertEquals(0, recipients)
    }

    @Test
    fun nonzeroDeltaReachesWineRelativeMouseTransportUnchangedOnce() {
        val moves = mutableListOf<IntegerMouseDelta>()
        val dispatcher = GyroMouseOutputDispatcher { dx, dy ->
            moves += IntegerMouseDelta(dx, dy)
            2
        }

        val recipients = dispatcher.dispatch(137, -91)

        assertEquals(listOf(IntegerMouseDelta(137, -91)), moves)
        assertEquals(2, recipients)
    }
}

package app.gamenative.gamefold.gyro

import org.junit.Assert.assertEquals
import org.junit.Test

class GameFoldGyroOrientationPolicyTest {
    @Test
    fun portraitInvertsBothMouseAxes() {
        assertEquals(
            GyroMouseDelta(-12.5f, 7.25f),
            GameFoldGyroOrientationPolicy.apply(
                delta = GyroMouseDelta(12.5f, -7.25f),
                portraitMode = true,
            ),
        )
    }

    @Test
    fun landscapePreservesBothMouseAxes() {
        val delta = GyroMouseDelta(12.5f, -7.25f)

        assertEquals(
            delta,
            GameFoldGyroOrientationPolicy.apply(delta = delta, portraitMode = false),
        )
    }

    @Test
    fun portraitKeepsZeroMouseDeltaNeutral() {
        assertEquals(
            GyroMouseDelta.ZERO,
            GameFoldGyroOrientationPolicy.apply(
                delta = GyroMouseDelta.ZERO,
                portraitMode = true,
            ),
        )
    }
}

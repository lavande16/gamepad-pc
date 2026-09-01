package app.gamenative.gamefold.gyro

import org.junit.Assert.assertEquals
import org.junit.Test

class GyroSteeringProcessorTest {
    @Test
    fun processorDefaultReachesFullSteeringAtTwentySixDegrees() {
        val processor = GyroSteeringProcessor()

        assertEquals(1f, processor.onTiltSample(26f, 1_000_000_000L), 0f)
    }

    @Test
    fun tiltCurveUsesContinuousSymmetricLinearRangeOutsideDeadzone() {
        val tuning = GyroSteeringTuning(
            deadzoneDegrees = 2.5f,
            fullScaleDegrees = 25f,
        )

        assertEquals(0f, GyroSteeringCurve.mapTiltToStick(0f, tuning), 0f)
        assertEquals(0f, GyroSteeringCurve.mapTiltToStick(2.5f, tuning), 0f)
        assertEquals(0.111111f, GyroSteeringCurve.mapTiltToStick(5f, tuning), 0.00001f)
        assertEquals(-0.111111f, GyroSteeringCurve.mapTiltToStick(-5f, tuning), 0.00001f)
        assertEquals(0.555556f, GyroSteeringCurve.mapTiltToStick(15f, tuning), 0.00001f)
        assertEquals(1f, GyroSteeringCurve.mapTiltToStick(25f, tuning), 0f)
        assertEquals(-1f, GyroSteeringCurve.mapTiltToStick(-40f, tuning), 0f)
    }

    @Test
    fun processorDefaultDoesNotAddAnotherSteeringLagLayer() {
        val processor = GyroSteeringProcessor(
            GyroSteeringTuning(
                deadzoneDegrees = 1f,
                fullScaleDegrees = 32f,
                smoothingTimeConstantSeconds = 0f,
            ),
        )

        assertEquals(1f, processor.onTiltSample(32f, 1_000_000_000L), 0f)
        assertEquals(0f, processor.onTiltSample(0f, 1_010_000_000L), 0f)
    }
}

package app.gamenative.gamefold.gyro

import org.junit.Assert.assertEquals
import org.junit.Test

class GamepadMotionStillnessCalibratorTest {
    @Test
    fun lessThanCollectionGateCannotLearnBias() {
        val calibrator = GamepadMotionStillnessCalibrator()

        repeat(40) {
            calibrator.addSample(
                gyroDps = Vector3f(0.8f, -0.4f, 0.2f),
                accelG = Vector3f(0f, 0f, 1f),
                deltaSeconds = 0.01f,
            )
        }

        assertEquals(GyroBias.ZERO, calibrator.bias)
    }

    @Test
    fun twoSecondsOfStableSamplesLearnThreeAxisMeanBias() {
        val calibrator = GamepadMotionStillnessCalibrator()

        repeat(210) {
            calibrator.addSample(
                gyroDps = Vector3f(0.8f, -0.4f, 0.2f),
                accelG = Vector3f(0f, 0f, 1f),
                deltaSeconds = 0.01f,
            )
        }

        assertEquals(0.8f, calibrator.bias.xDps, 0.0001f)
        assertEquals(-0.4f, calibrator.bias.yDps, 0.0001f)
        assertEquals(0.2f, calibrator.bias.zDps, 0.0001f)
    }

    @Test
    fun slightNoiseRangeGrowthAfterCollectionGateStillCalibrates() {
        val calibrator = GamepadMotionStillnessCalibrator()
        repeat(60) { index ->
            val noise = if (index % 2 == 0) -0.01f else 0.01f
            calibrator.addSample(
                gyroDps = STABLE_GYRO.copy(x = STABLE_GYRO.x + noise),
                accelG = STABLE_ACCEL,
                deltaSeconds = 0.01f,
            )
        }
        repeat(160) { index ->
            val noise = if (index % 2 == 0) -0.0104f else 0.0104f
            calibrator.addSample(
                gyroDps = STABLE_GYRO.copy(x = STABLE_GYRO.x + noise),
                accelG = STABLE_ACCEL,
                deltaSeconds = 0.01f,
            )
        }

        assertEquals(STABLE_GYRO.x, calibrator.bias.xDps, 0.001f)
    }

    @Test
    fun unstableMotionResetsCandidateWindow() {
        val calibrator = GamepadMotionStillnessCalibrator()
        repeat(150) {
            calibrator.addSample(STABLE_GYRO, STABLE_ACCEL, 0.01f)
        }
        calibrator.addSample(
            gyroDps = Vector3f(8f, -6f, 4f),
            accelG = Vector3f(0.5f, -0.4f, 0.75f),
            deltaSeconds = 0.01f,
        )
        repeat(60) {
            calibrator.addSample(STABLE_GYRO, STABLE_ACCEL, 0.01f)
        }

        assertEquals(GyroBias.ZERO, calibrator.bias)
    }

    @Test
    fun frozenCalibratorCannotChangeBiasDuringAimSession() {
        val calibrator = GamepadMotionStillnessCalibrator()
        repeat(210) {
            calibrator.addSample(STABLE_GYRO, STABLE_ACCEL, 0.01f)
        }
        val learned = calibrator.bias

        calibrator.setFrozen(true)
        repeat(300) {
            calibrator.addSample(
                gyroDps = Vector3f(-3f, 2f, -1f),
                accelG = STABLE_ACCEL,
                deltaSeconds = 0.01f,
            )
        }

        assertEquals(learned, calibrator.bias)
    }

    private companion object {
        val STABLE_GYRO = Vector3f(0.8f, -0.4f, 0.2f)
        val STABLE_ACCEL = Vector3f(0f, 0f, 1f)
    }
}

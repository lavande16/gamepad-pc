package app.gamenative.gamefold.gyro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GyroSteeringOrientationTest {
    @Test
    fun diagnosticModeCycleVisitsAutoWheelYawThenReturnsToAuto() {
        assertEquals(GyroSteeringMode.WHEEL, GyroSteeringMode.AUTO.next())
        assertEquals(GyroSteeringMode.YAW, GyroSteeringMode.WHEEL.next())
        assertEquals(GyroSteeringMode.AUTO, GyroSteeringMode.YAW.next())
    }

    @Test
    fun displayRotationNinetyDoesNotDoubleInvertScreenYaw() {
        val fifteenDegreesAroundNaturalX = SteeringQuaternion(
            w = 0.9914449f,
            x = 0.1305262f,
            y = 0f,
            z = 0f,
        )

        val natural = GyroSteeringOrientation.relativeSteeringComponents(
            neutral = SteeringQuaternion.IDENTITY,
            current = fifteenDegreesAroundNaturalX,
            displayRotation = GyroSteeringDisplayRotation.ROTATION_0,
        )
        val rotated = GyroSteeringOrientation.relativeSteeringComponents(
            neutral = SteeringQuaternion.IDENTITY,
            current = fifteenDegreesAroundNaturalX,
            displayRotation = GyroSteeringDisplayRotation.ROTATION_90,
        )

        assertEquals(0f, natural.yawDegrees, 0.01f)
        assertEquals(-15f, rotated.yawDegrees, 0.01f)
    }

    @Test
    fun autoModeLatchesFirstIntentAxisUntilActiveAxisReturnsToCenter() {
        val selector = GyroSteeringAxisSelector(
            activationDegrees = 3f,
            releaseDegrees = 1f,
        )

        assertEquals(
            GyroSteeringSelection(GyroSteeringAxis.WHEEL, 8f),
            selector.select(GyroSteeringComponents(8f, 3f), GyroSteeringMode.AUTO),
        )
        assertEquals(
            GyroSteeringSelection(GyroSteeringAxis.WHEEL, 4f),
            selector.select(GyroSteeringComponents(4f, -10f), GyroSteeringMode.AUTO),
        )
        assertEquals(
            GyroSteeringSelection(GyroSteeringAxis.NONE, 0f),
            selector.select(GyroSteeringComponents(0.5f, -0.5f), GyroSteeringMode.AUTO),
        )
        assertEquals(
            GyroSteeringSelection(GyroSteeringAxis.YAW, -7f),
            selector.select(GyroSteeringComponents(1f, -7f), GyroSteeringMode.AUTO),
        )
    }

    @Test
    fun autoModeSwitchesFromCenteredActiveAxisToSignificantOtherAxis() {
        val selector = GyroSteeringAxisSelector(
            activationDegrees = 3f,
            releaseDegrees = 1f,
        )

        assertEquals(
            GyroSteeringSelection(GyroSteeringAxis.YAW, -7f),
            selector.select(GyroSteeringComponents(1f, -7f), GyroSteeringMode.AUTO),
        )
        assertEquals(
            GyroSteeringSelection(GyroSteeringAxis.WHEEL, 8.73f),
            selector.select(GyroSteeringComponents(8.73f, -0.12f), GyroSteeringMode.AUTO),
        )
    }

    @Test
    fun screenNormalTiltIsMeasuredRelativeToArbitraryNeutralPose() {
        val neutral = SteeringQuaternion(
            w = 0.9238795f,
            x = 0.3826834f,
            y = 0f,
            z = 0f,
        )
        val neutralThenFifteenDegreesClockwise = SteeringQuaternion(
            w = 0.9159756f,
            x = 0.3794095f,
            y = -0.0499502f,
            z = 0.1205905f,
        )

        assertEquals(
            15f,
            GyroSteeringOrientation.relativeScreenNormalDegrees(
                neutral = neutral,
                current = neutralThenFifteenDegreesClockwise,
            ),
            0.01f,
        )
    }

    @Test
    fun turningWholeScreenPlaneLeftOrRightProducesSteering() {
        val fifteenDegreesAroundNaturalPositiveY = SteeringQuaternion(
            w = 0.9914449f,
            x = 0f,
            y = 0.1305262f,
            z = 0f,
        )

        assertEquals(
            -15f,
            GyroSteeringOrientation.relativeCombinedSteeringDegrees(
                neutral = SteeringQuaternion.IDENTITY,
                current = fifteenDegreesAroundNaturalPositiveY,
            ),
            0.01f,
        )
    }

    @Test
    fun pitchingTopOrBottomEdgeDoesNotProduceSteering() {
        val twentyDegreesOfPitch = SteeringQuaternion(
            w = 0.9848078f,
            x = 0.1736482f,
            y = 0f,
            z = 0f,
        )

        assertEquals(
            0f,
            GyroSteeringOrientation.relativeCombinedSteeringDegrees(
                neutral = SteeringQuaternion.IDENTITY,
                current = twentyDegreesOfPitch,
            ),
            0.01f,
        )
    }

    @Test
    fun worldVerticalTurnKeepsFullStrengthAtFortyFiveDegreeHoldingPitch() {
        val neutral = SteeringQuaternion(
            w = 0.9238795f,
            x = 0.3826834f,
            y = 0f,
            z = 0f,
        )
        val fifteenDegreeWorldVerticalTurn = SteeringQuaternion(
            w = 0.9159756f,
            x = 0.3794095f,
            y = 0.1205905f,
            z = -0.0499502f,
        )

        assertEquals(
            -15f,
            GyroSteeringOrientation.relativeCombinedSteeringDegrees(
                neutral = neutral,
                current = fifteenDegreeWorldVerticalTurn,
            ),
            0.01f,
        )
    }

    @Test
    fun neutralIsCapturedOnlyAfterThreeHundredMillisecondsOfStablePose() {
        val calibrator = GyroSteeringNeutralCalibrator(
            stableDurationNs = 300_000_000L,
            maximumStableAngularSpeedDegreesPerSecond = 5f,
        )
        val pose = SteeringQuaternion.IDENTITY

        assertNull(calibrator.observe(pose, 1_000_000_000L))
        assertNull(calibrator.observe(pose, 1_100_000_000L))
        assertNull(calibrator.observe(pose, 1_299_000_000L))
        assertEquals(pose, calibrator.observe(pose, 1_300_000_000L))
    }

    @Test
    fun neutralUsesMeanOfStableWindowInsteadOfLastSample() {
        val calibrator = GyroSteeringNeutralCalibrator(
            stableDurationNs = 300_000_000L,
            maximumStableAngularSpeedDegreesPerSecond = 5f,
        )
        fun zRotation(degrees: Float): SteeringQuaternion {
            val radians = Math.toRadians(degrees.toDouble()) / 2.0
            return SteeringQuaternion(
                w = kotlin.math.cos(radians).toFloat(),
                x = 0f,
                y = 0f,
                z = kotlin.math.sin(radians).toFloat(),
            )
        }

        assertNull(calibrator.observe(zRotation(-0.3f), 1_000_000_000L))
        assertNull(calibrator.observe(zRotation(-0.1f), 1_100_000_000L))
        assertNull(calibrator.observe(zRotation(0.1f), 1_200_000_000L))
        val neutral = calibrator.observe(zRotation(0.3f), 1_300_000_000L)

        assertEquals(
            0f,
            GyroSteeringOrientation.relativeScreenNormalDegrees(
                neutral = requireNotNull(neutral),
                current = SteeringQuaternion.IDENTITY,
            ),
            0.01f,
        )
    }

    @Test
    fun sessionCalibratesThenConvertsRelativeRotationToFilteredStick() {
        val session = GyroSteeringSession()
        val neutral = SteeringQuaternion.IDENTITY

        assertFalse(session.onRotationSample(neutral, 1_000_000_000L).calibrated)
        assertFalse(session.onRotationSample(neutral, 1_100_000_000L).calibrated)
        assertFalse(session.onRotationSample(neutral, 1_200_000_000L).calibrated)
        val calibrated = session.onRotationSample(neutral, 1_300_000_000L)
        assertTrue(calibrated.calibrated)
        assertEquals(0f, calibrated.stickX, 0f)

        val twentyFiveDegrees = SteeringQuaternion(
            w = 0.976296f,
            x = 0f,
            y = 0f,
            z = 0.216440f,
        )
        assertEquals(
            0.96f,
            session.onRotationSample(twentyFiveDegrees, 1_350_000_000L).stickX,
            0.00001f,
        )
    }

    @Test
    fun sessionMapsNaturalPositiveYawToCorrectedNegativeLeftStickX() {
        val session = GyroSteeringSession(direction = -1f)
        val neutral = SteeringQuaternion.IDENTITY
        session.onRotationSample(neutral, 1_000_000_000L)
        session.onRotationSample(neutral, 1_100_000_000L)
        session.onRotationSample(neutral, 1_200_000_000L)
        session.onRotationSample(neutral, 1_300_000_000L)

        val fifteenDegreesAroundNaturalPositiveY = SteeringQuaternion(
            w = 0.9914449f,
            x = 0f,
            y = 0.1305262f,
            z = 0f,
        )
        assertEquals(
            -0.56f,
            session.onRotationSample(fifteenDegreesAroundNaturalPositiveY, 1_350_000_000L).stickX,
            0.00001f,
        )
    }
}

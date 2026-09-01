package app.gamenative.gamefold.gyro

import kotlin.math.PI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GyroMouseProcessorTest {
    @Test
    fun zlOffProducesNoMouseMovement() {
        val processor = GyroMouseProcessor()

        val output = processor.onGyroSample(sample(yDps = 30f, timestampNs = 10_000_000L))

        assertEquals(0f, output.dxCounts, 0.0001f)
        assertEquals(0f, output.dyCounts, 0.0001f)
    }

    @Test
    fun firstAimedSampleOnlyEstablishesTimingReference() {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }

        val output = processor.onGyroSample(sample(yDps = 20f, timestampNs = 10_000_000L))

        assertEquals(0f, output.dxCounts, 0.0001f)
        assertEquals(0f, output.dyCounts, 0.0001f)
    }

    @Test
    fun twentyDegreesPerSecondForFiftyMillisecondsProducesTwentyFourPointFiveCounts() {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }
        processor.onGyroSample(sample(yDps = 20f, timestampNs = 10_000_000L))

        val output = processor.onGyroSample(sample(yDps = 20f, timestampNs = 60_000_000L))

        assertEquals(24.5f, output.dxCounts, 0.0001f)
        assertEquals(0f, output.dyCounts, 0.0001f)
    }

    @Test
    fun equalOppositeAngularMotionHasZeroNetMouseDelta() {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }
        processor.onGyroSample(sample(yDps = 0f, timestampNs = 10_000_000L))

        val forward = processor.onGyroSample(sample(yDps = 35f, timestampNs = 30_000_000L))
        val backward = processor.onGyroSample(sample(yDps = -35f, timestampNs = 50_000_000L))

        assertEquals(0f, forward.dxCounts + backward.dxCounts, 0.0001f)
        assertEquals(0f, forward.dyCounts + backward.dyCounts, 0.0001f)
    }

    @Test
    fun learnedBiasIsSubtractedBeforeMouseIntegration() {
        val processor = GyroMouseProcessor().apply {
            setBias(GyroBias(xDps = 0f, yDps = 1.25f, zDps = 0f))
            setAimHeld(true)
        }
        processor.onGyroSample(sample(yDps = 1.25f, timestampNs = 10_000_000L))

        val output = processor.onGyroSample(sample(yDps = 1.25f, timestampNs = 30_000_000L))

        assertEquals(0f, output.dxCounts, 0.0001f)
        assertEquals(0f, output.dyCounts, 0.0001f)
    }

    @Test
    fun landscapeDisplayMapsRc3DirectionsToMouseAxes() {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }
        processor.onGyroSample(
            sample(timestampNs = 10_000_000L, rotation = DisplayQuarterTurn.ROTATION_90),
        )

        val output = processor.onGyroSample(
            sample(
                xDps = -20f,
                yDps = -20f,
                timestampNs = 30_000_000L,
                rotation = DisplayQuarterTurn.ROTATION_90,
            ),
        )

        assertTrue("negative raw X must turn camera right", output.dxCounts > 0f)
        assertTrue("negative raw Y must move camera up in GTA5 Windows mouse mode", output.dyCounts < 0f)
    }

    @Test
    fun horizontalAndVerticalUseEqualSensitivity() {
        val yaw = aimedDelta(yDps = 15f)
        val pitch = aimedDelta(xDps = 15f)

        assertEquals(kotlin.math.abs(yaw.dxCounts), kotlin.math.abs(pitch.dyCounts), 0.0001f)
    }

    @Test
    fun samePhysicalAngleProducesSameCountsAtHalfSecondAndTwoSeconds() {
        val fastTurn = integratedYawCounts(degreesPerSecond = 80f, durationMs = 500)
        val slowTurn = integratedYawCounts(degreesPerSecond = 20f, durationMs = 2_000)

        assertEquals(24.5f * 40f, fastTurn, 0.01f)
        assertEquals(fastTurn, slowTurn, 0.01f)
    }

    @Test
    fun sampleGapOverFiftyMillisecondsDoesNotCreateJump() {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }
        processor.onGyroSample(sample(yDps = 20f, timestampNs = 10_000_000L))

        val output = processor.onGyroSample(sample(yDps = 20f, timestampNs = 70_000_001L))

        assertEquals(GyroMouseDelta.ZERO, output)
    }

    @Test
    fun releasingAndRepressingZlClearsTimingReference() {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }
        processor.onGyroSample(sample(yDps = 20f, timestampNs = 10_000_000L))
        processor.setAimHeld(false)
        processor.setAimHeld(true)

        val output = processor.onGyroSample(sample(yDps = 20f, timestampNs = 40_000_000L))

        assertEquals(GyroMouseDelta.ZERO, output)
    }

    @Test
    fun fractionalMouseCountsAreConservedAcrossSamples() {
        val accumulator = FractionalMouseDeltaAccumulator()

        assertEquals(IntegerMouseDelta.ZERO, accumulator.add(GyroMouseDelta(0.75f, -0.75f)))
        assertEquals(IntegerMouseDelta(1, -1), accumulator.add(GyroMouseDelta(0.75f, -0.75f)))
        assertEquals(IntegerMouseDelta.ZERO, accumulator.add(GyroMouseDelta(-0.5f, 0.5f)))
    }

    @Test
    fun accumulatorResetDropsSubPixelRemainderAtZlRelease() {
        val accumulator = FractionalMouseDeltaAccumulator()
        accumulator.add(GyroMouseDelta(0.75f, -0.75f))

        accumulator.reset()

        assertEquals(IntegerMouseDelta.ZERO, accumulator.add(GyroMouseDelta(0.25f, -0.25f)))
    }

    private fun aimedDelta(xDps: Float = 0f, yDps: Float = 0f): GyroMouseDelta {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }
        processor.onGyroSample(sample(timestampNs = 10_000_000L))
        return processor.onGyroSample(
            sample(xDps = xDps, yDps = yDps, timestampNs = 30_000_000L),
        )
    }

    private fun integratedYawCounts(degreesPerSecond: Float, durationMs: Int): Float {
        val processor = GyroMouseProcessor().apply { setAimHeld(true) }
        var timestampNs = 10_000_000L
        processor.onGyroSample(sample(yDps = degreesPerSecond, timestampNs = timestampNs))
        var total = 0f
        repeat(durationMs / 10) {
            timestampNs += 10_000_000L
            total += processor.onGyroSample(
                sample(yDps = degreesPerSecond, timestampNs = timestampNs),
            ).dxCounts
        }
        return total
    }

    private fun sample(
        xDps: Float = 0f,
        yDps: Float = 0f,
        zDps: Float = 0f,
        timestampNs: Long,
        rotation: DisplayQuarterTurn = DisplayQuarterTurn.ROTATION_0,
    ) = GyroMouseSample(
        xRadPerSec = dpsToRadians(xDps),
        yRadPerSec = dpsToRadians(yDps),
        zRadPerSec = dpsToRadians(zDps),
        timestampNs = timestampNs,
        rotation = rotation,
    )

    private fun dpsToRadians(value: Float): Float = (value * PI / 180.0).toFloat()
}

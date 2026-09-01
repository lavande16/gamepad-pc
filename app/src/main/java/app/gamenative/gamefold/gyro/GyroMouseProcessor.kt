package app.gamenative.gamefold.gyro

import kotlin.math.PI

internal enum class DisplayQuarterTurn {
    ROTATION_0,
    ROTATION_90,
    ROTATION_180,
    ROTATION_270,
}

internal data class GyroMouseSample(
    val xRadPerSec: Float,
    val yRadPerSec: Float,
    val zRadPerSec: Float,
    val timestampNs: Long,
    val rotation: DisplayQuarterTurn,
)

internal data class GyroMouseDelta(
    val dxCounts: Float,
    val dyCounts: Float,
) {
    companion object {
        val ZERO = GyroMouseDelta(0f, 0f)
    }
}

internal data class IntegerMouseDelta(
    val dx: Int,
    val dy: Int,
) {
    companion object {
        val ZERO = IntegerMouseDelta(0, 0)
    }
}

internal data class GyroMouseTuning(
    val realWorldCalibrationCountsPerDegree: Float = 19.6f,
    val horizontalSensitivity: Float = 1.25f,
    val verticalSensitivity: Float = 1.25f,
    val maximumSampleDeltaSeconds: Float = 0.05f,
)

/**
 * Mouse-style gyro control law following JoyShockMapper's angular-velocity path.
 * It intentionally does not integrate an absolute orientation and never emits stick input.
 */
internal class GyroMouseProcessor(
    private val tuning: GyroMouseTuning = GyroMouseTuning(),
) {
    private var aimHeld = false
    private var lastTimestampNs = 0L
    private var lastRotation: DisplayQuarterTurn? = null
    private var biasXRadPerSec = 0f
    private var biasYRadPerSec = 0f
    private var biasZRadPerSec = 0f

    fun setAimHeld(held: Boolean) {
        if (aimHeld == held) return
        aimHeld = held
        resetTimingReference()
    }

    fun setBias(bias: GyroBias) {
        if (aimHeld) return
        biasXRadPerSec = bias.xDps * DEGREES_TO_RADIANS
        biasYRadPerSec = bias.yDps * DEGREES_TO_RADIANS
        biasZRadPerSec = bias.zDps * DEGREES_TO_RADIANS
    }

    fun onGyroSample(sample: GyroMouseSample): GyroMouseDelta {
        if (!aimHeld) return GyroMouseDelta.ZERO

        if (lastTimestampNs == 0L || lastRotation != sample.rotation) {
            establishReference(sample)
            return GyroMouseDelta.ZERO
        }

        val deltaSeconds = (sample.timestampNs - lastTimestampNs) * NS_TO_SECONDS
        establishReference(sample)
        if (
            !deltaSeconds.isFinite() ||
            deltaSeconds <= 0f ||
            deltaSeconds > tuning.maximumSampleDeltaSeconds
        ) {
            return GyroMouseDelta.ZERO
        }

        val (pitchRadPerSec, yawRadPerSec) = mapToDisplayAxes(
            rawX = sample.xRadPerSec - biasXRadPerSec,
            rawY = sample.yRadPerSec - biasYRadPerSec,
            rotation = sample.rotation,
        )
        val countsPerRadian =
            tuning.realWorldCalibrationCountsPerDegree * RADIANS_TO_DEGREES

        return GyroMouseDelta(
            dxCounts = yawRadPerSec * deltaSeconds * countsPerRadian *
                tuning.horizontalSensitivity,
            dyCounts = pitchRadPerSec * deltaSeconds * countsPerRadian *
                tuning.verticalSensitivity,
        )
    }

    fun reset() {
        aimHeld = false
        biasXRadPerSec = 0f
        biasYRadPerSec = 0f
        biasZRadPerSec = 0f
        resetTimingReference()
    }

    private fun establishReference(sample: GyroMouseSample) {
        lastTimestampNs = sample.timestampNs
        lastRotation = sample.rotation
    }

    private fun resetTimingReference() {
        lastTimestampNs = 0L
        lastRotation = null
    }

    private fun mapToDisplayAxes(
        rawX: Float,
        rawY: Float,
        rotation: DisplayQuarterTurn,
    ): Pair<Float, Float> = when (rotation) {
        DisplayQuarterTurn.ROTATION_0 -> rawX to rawY
        DisplayQuarterTurn.ROTATION_90 -> rawY to -rawX
        DisplayQuarterTurn.ROTATION_180 -> -rawX to -rawY
        DisplayQuarterTurn.ROTATION_270 -> -rawY to rawX
    }

    private companion object {
        const val NS_TO_SECONDS = 1e-9f
        val RADIANS_TO_DEGREES = (180.0 / PI).toFloat()
        val DEGREES_TO_RADIANS = (PI / 180.0).toFloat()
    }
}

/** Ports JoyShockMapper's float-to-integer mouse remainder accumulation. */
internal class FractionalMouseDeltaAccumulator {
    private var pendingX = 0f
    private var pendingY = 0f

    fun add(delta: GyroMouseDelta): IntegerMouseDelta {
        pendingX += delta.dxCounts
        pendingY += delta.dyCounts

        val applicableX = pendingX.toInt()
        val applicableY = pendingY.toInt()
        pendingX -= applicableX
        pendingY -= applicableY
        return IntegerMouseDelta(applicableX, applicableY)
    }

    fun reset() {
        pendingX = 0f
        pendingY = 0f
    }
}

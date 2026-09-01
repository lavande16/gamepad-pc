package app.gamenative.gamefold.gyro

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign

internal data class GyroSteeringTuning(
    val deadzoneDegrees: Float = 1f,
    val fullScaleDegrees: Float = 26f,
    val smoothingTimeConstantSeconds: Float = 0f,
    val maximumSampleDeltaSeconds: Float = 0.10f,
)

internal object GyroSteeringCurve {
    fun mapTiltToStick(tiltDegrees: Float, tuning: GyroSteeringTuning): Float {
        val magnitude = abs(tiltDegrees)
        if (magnitude <= tuning.deadzoneDegrees) return 0f
        val usableRange = tuning.fullScaleDegrees - tuning.deadzoneDegrees
        val normalized = ((magnitude - tuning.deadzoneDegrees) / usableRange).coerceIn(0f, 1f)
        return normalized * tiltDegrees.sign
    }
}

internal class GyroSteeringProcessor(
    private val tuning: GyroSteeringTuning = GyroSteeringTuning(),
) {
    private var filteredStick = 0f
    private var lastTimestampNs = 0L

    fun onTiltSample(tiltDegrees: Float, timestampNs: Long): Float {
        if (lastTimestampNs == 0L) {
            lastTimestampNs = timestampNs
            filteredStick = GyroSteeringCurve.mapTiltToStick(tiltDegrees, tuning)
            return filteredStick
        }

        val deltaSeconds = (timestampNs - lastTimestampNs) * NS_TO_SECONDS
        lastTimestampNs = timestampNs
        if (
            !deltaSeconds.isFinite() ||
            deltaSeconds <= 0f ||
            deltaSeconds > tuning.maximumSampleDeltaSeconds
        ) {
            return filteredStick
        }

        val target = GyroSteeringCurve.mapTiltToStick(tiltDegrees, tuning)
        val alpha = if (tuning.smoothingTimeConstantSeconds <= 0f) {
            1f
        } else {
            1f - exp(-deltaSeconds / tuning.smoothingTimeConstantSeconds)
        }
        filteredStick += (target - filteredStick) * alpha
        return filteredStick
    }

    fun reset() {
        filteredStick = 0f
        lastTimestampNs = 0L
    }

    private companion object {
        const val NS_TO_SECONDS = 1e-9f
    }
}

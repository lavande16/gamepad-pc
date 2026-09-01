package app.gamenative.gamefold.gyro

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sign
import kotlin.math.sqrt

internal data class SteeringQuaternion(
    val w: Float,
    val x: Float,
    val y: Float,
    val z: Float,
) {
    fun normalized(): SteeringQuaternion {
        val magnitude = sqrt(w * w + x * x + y * y + z * z)
        if (!magnitude.isFinite() || magnitude <= MIN_MAGNITUDE) return IDENTITY
        return SteeringQuaternion(w / magnitude, x / magnitude, y / magnitude, z / magnitude)
    }

    fun conjugate() = SteeringQuaternion(w, -x, -y, -z)

    operator fun times(other: SteeringQuaternion) = SteeringQuaternion(
        w = w * other.w - x * other.x - y * other.y - z * other.z,
        x = w * other.x + x * other.w + y * other.z - z * other.y,
        y = w * other.y - x * other.z + y * other.w + z * other.x,
        z = w * other.z + x * other.y - y * other.x + z * other.w,
    )

    companion object {
        val IDENTITY = SteeringQuaternion(1f, 0f, 0f, 0f)
        private const val MIN_MAGNITUDE = 1e-6f
    }
}

internal enum class GyroSteeringDisplayRotation {
    ROTATION_0,
    ROTATION_90,
    ROTATION_180,
    ROTATION_270,
}

internal enum class GyroSteeringMode {
    AUTO,
    WHEEL,
    YAW,

    ;

    fun next(): GyroSteeringMode = when (this) {
        AUTO -> WHEEL
        WHEEL -> YAW
        YAW -> AUTO
    }
}

internal enum class GyroSteeringAxis {
    NONE,
    WHEEL,
    YAW,
}

internal data class GyroSteeringComponents(
    val wheelDegrees: Float,
    val yawDegrees: Float,
)

internal data class GyroSteeringSelection(
    val axis: GyroSteeringAxis,
    val degrees: Float,
)

internal class GyroSteeringAxisSelector(
    private val activationDegrees: Float = 3f,
    private val releaseDegrees: Float = 1f,
) {
    private var activeAxis = GyroSteeringAxis.NONE

    fun select(
        components: GyroSteeringComponents,
        mode: GyroSteeringMode,
    ): GyroSteeringSelection {
        if (mode != GyroSteeringMode.AUTO) {
            activeAxis = GyroSteeringAxis.NONE
            return when (mode) {
                GyroSteeringMode.WHEEL -> GyroSteeringSelection(
                    GyroSteeringAxis.WHEEL,
                    components.wheelDegrees,
                )
                GyroSteeringMode.YAW -> GyroSteeringSelection(
                    GyroSteeringAxis.YAW,
                    components.yawDegrees,
                )
                GyroSteeringMode.AUTO -> error("AUTO handled above")
            }
        }

        val activeMagnitude = when (activeAxis) {
            GyroSteeringAxis.WHEEL -> abs(components.wheelDegrees)
            GyroSteeringAxis.YAW -> abs(components.yawDegrees)
            GyroSteeringAxis.NONE -> 0f
        }
        if (activeAxis != GyroSteeringAxis.NONE && activeMagnitude <= releaseDegrees) {
            activeAxis = GyroSteeringAxis.NONE
        }

        if (activeAxis == GyroSteeringAxis.NONE) {
            val wheelMagnitude = abs(components.wheelDegrees)
            val yawMagnitude = abs(components.yawDegrees)
            if (maxOf(wheelMagnitude, yawMagnitude) < activationDegrees) {
                return GyroSteeringSelection(GyroSteeringAxis.NONE, 0f)
            }
            activeAxis = if (wheelMagnitude >= yawMagnitude) {
                GyroSteeringAxis.WHEEL
            } else {
                GyroSteeringAxis.YAW
            }
        }

        return when (activeAxis) {
            GyroSteeringAxis.WHEEL -> GyroSteeringSelection(activeAxis, components.wheelDegrees)
            GyroSteeringAxis.YAW -> GyroSteeringSelection(activeAxis, components.yawDegrees)
            GyroSteeringAxis.NONE -> GyroSteeringSelection(activeAxis, 0f)
        }
    }

    fun reset() {
        activeAxis = GyroSteeringAxis.NONE
    }
}

internal object GyroSteeringOrientation {
    fun relativeScreenNormalDegrees(
        neutral: SteeringQuaternion,
        current: SteeringQuaternion,
    ): Float {
        val relative = neutral.normalized().conjugate() * current.normalized()
        val twistMagnitude = sqrt(relative.w * relative.w + relative.z * relative.z)
        if (!twistMagnitude.isFinite() || twistMagnitude <= MIN_TWIST_MAGNITUDE) return 0f
        val radians = 2f * atan2(relative.z / twistMagnitude, relative.w / twistMagnitude)
        return radians * RADIANS_TO_DEGREES
    }

    fun relativeCombinedSteeringDegrees(
        neutral: SteeringQuaternion,
        current: SteeringQuaternion,
    ): Float {
        val rotation = relativeRotationVectorDegrees(neutral, current)
        val screenNormalContribution = rotation.z
        val screenPlaneYawContribution = -rotation.y
        if (abs(screenNormalContribution) <= MIN_ROTATION_DEGREES) {
            return screenPlaneYawContribution
        }
        if (abs(screenPlaneYawContribution) <= MIN_ROTATION_DEGREES) {
            return screenNormalContribution
        }
        return if (sign(screenNormalContribution) == sign(screenPlaneYawContribution)) {
            sign(screenNormalContribution) * sqrt(
                screenNormalContribution * screenNormalContribution +
                    screenPlaneYawContribution * screenPlaneYawContribution,
            )
        } else {
            screenNormalContribution + screenPlaneYawContribution
        }
    }

    fun relativeSteeringComponents(
        neutral: SteeringQuaternion,
        current: SteeringQuaternion,
        displayRotation: GyroSteeringDisplayRotation,
    ): GyroSteeringComponents {
        val screenVerticalAxis = when (displayRotation) {
            GyroSteeringDisplayRotation.ROTATION_0 -> SteeringAxis(0f, 1f, 0f)
            GyroSteeringDisplayRotation.ROTATION_90 -> SteeringAxis(-1f, 0f, 0f)
            GyroSteeringDisplayRotation.ROTATION_180 -> SteeringAxis(0f, -1f, 0f)
            GyroSteeringDisplayRotation.ROTATION_270 -> SteeringAxis(1f, 0f, 0f)
        }
        return GyroSteeringComponents(
            wheelDegrees = relativeTwistDegrees(
                neutral = neutral,
                current = current,
                axis = SteeringAxis(0f, 0f, 1f),
            ),
            yawDegrees = relativeTwistDegrees(
                neutral = neutral,
                current = current,
                axis = screenVerticalAxis,
            ),
        )
    }

    private fun relativeTwistDegrees(
        neutral: SteeringQuaternion,
        current: SteeringQuaternion,
        axis: SteeringAxis,
    ): Float {
        val relative = neutral.normalized().conjugate() * current.normalized()
        val projected = relative.x * axis.x + relative.y * axis.y + relative.z * axis.z
        val twistMagnitude = sqrt(relative.w * relative.w + projected * projected)
        if (!twistMagnitude.isFinite() || twistMagnitude <= MIN_TWIST_MAGNITUDE) return 0f
        return 2f * atan2(projected / twistMagnitude, relative.w / twistMagnitude) *
            RADIANS_TO_DEGREES
    }

    private fun relativeRotationVectorDegrees(
        neutral: SteeringQuaternion,
        current: SteeringQuaternion,
    ): SteeringRotationVector {
        val relative = (neutral.normalized().conjugate() * current.normalized()).normalized()
        val canonical = if (relative.w < 0f) {
            SteeringQuaternion(-relative.w, -relative.x, -relative.y, -relative.z)
        } else {
            relative
        }
        val vectorMagnitude = sqrt(
            canonical.x * canonical.x + canonical.y * canonical.y + canonical.z * canonical.z,
        )
        if (!vectorMagnitude.isFinite() || vectorMagnitude <= MIN_TWIST_MAGNITUDE) {
            return SteeringRotationVector.ZERO
        }
        val angleDegrees = 2f * atan2(vectorMagnitude, canonical.w) * RADIANS_TO_DEGREES
        val scale = angleDegrees / vectorMagnitude
        return SteeringRotationVector(
            x = canonical.x * scale,
            y = canonical.y * scale,
            z = canonical.z * scale,
        )
    }

    private const val MIN_TWIST_MAGNITUDE = 1e-6f
    private const val MIN_ROTATION_DEGREES = 1e-4f
    private val RADIANS_TO_DEGREES = (180.0 / PI).toFloat()
}

private data class SteeringAxis(
    val x: Float,
    val y: Float,
    val z: Float,
)

private data class SteeringRotationVector(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    companion object {
        val ZERO = SteeringRotationVector(0f, 0f, 0f)
    }
}

internal class GyroSteeringNeutralCalibrator(
    private val stableDurationNs: Long = 300_000_000L,
    private val maximumStableAngularSpeedDegreesPerSecond: Float = 5f,
) {
    private var previous: SteeringQuaternion? = null
    private var previousTimestampNs = 0L
    private var stableSinceTimestampNs = 0L
    private var sumW = 0f
    private var sumX = 0f
    private var sumY = 0f
    private var sumZ = 0f
    private var stableSampleCount = 0

    fun observe(sample: SteeringQuaternion, timestampNs: Long): SteeringQuaternion? {
        val current = sample.normalized()
        val prior = previous
        if (prior == null) {
            previous = current
            previousTimestampNs = timestampNs
            resetStableWindow(current, timestampNs)
            return null
        }

        val deltaSeconds = (timestampNs - previousTimestampNs) * NS_TO_SECONDS
        val angularSpeed = if (!deltaSeconds.isFinite() || deltaSeconds <= 0f) {
            Float.POSITIVE_INFINITY
        } else {
            angularDistanceDegrees(prior, current) / deltaSeconds
        }
        if (angularSpeed > maximumStableAngularSpeedDegreesPerSecond) {
            resetStableWindow(current, timestampNs)
        } else {
            addStableSample(current)
        }
        previous = current
        previousTimestampNs = timestampNs

        return if (timestampNs - stableSinceTimestampNs >= stableDurationNs) {
            SteeringQuaternion(sumW, sumX, sumY, sumZ).normalized()
        } else {
            null
        }
    }

    fun reset() {
        previous = null
        previousTimestampNs = 0L
        stableSinceTimestampNs = 0L
        clearStableSamples()
    }

    private fun resetStableWindow(sample: SteeringQuaternion, timestampNs: Long) {
        stableSinceTimestampNs = timestampNs
        clearStableSamples()
        addStableSample(sample)
    }

    private fun addStableSample(sample: SteeringQuaternion) {
        val alignment = if (
            stableSampleCount > 0 &&
            sample.w * sumW + sample.x * sumX + sample.y * sumY + sample.z * sumZ < 0f
        ) {
            -1f
        } else {
            1f
        }
        sumW += sample.w * alignment
        sumX += sample.x * alignment
        sumY += sample.y * alignment
        sumZ += sample.z * alignment
        stableSampleCount += 1
    }

    private fun clearStableSamples() {
        sumW = 0f
        sumX = 0f
        sumY = 0f
        sumZ = 0f
        stableSampleCount = 0
    }

    private fun angularDistanceDegrees(
        first: SteeringQuaternion,
        second: SteeringQuaternion,
    ): Float {
        val dot = abs(
            first.w * second.w + first.x * second.x +
                first.y * second.y + first.z * second.z,
        ).coerceIn(0f, 1f)
        return 2f * acos(dot) * RADIANS_TO_DEGREES
    }

    private companion object {
        const val NS_TO_SECONDS = 1e-9f
        val RADIANS_TO_DEGREES = (180.0 / PI).toFloat()
    }
}

internal data class GyroSteeringOutput(
    val calibrated: Boolean,
    val relativeTiltDegrees: Float,
    val stickX: Float,
    val wheelDegrees: Float = 0f,
    val yawDegrees: Float = 0f,
    val activeAxis: GyroSteeringAxis = GyroSteeringAxis.NONE,
    val mode: GyroSteeringMode = GyroSteeringMode.AUTO,
)

internal class GyroSteeringSession(
    private val calibrator: GyroSteeringNeutralCalibrator = GyroSteeringNeutralCalibrator(),
    private val processor: GyroSteeringProcessor = GyroSteeringProcessor(),
    private val direction: Float = 1f,
    private val axisSelector: GyroSteeringAxisSelector = GyroSteeringAxisSelector(),
    initialMode: GyroSteeringMode = GyroSteeringMode.AUTO,
) {
    private var neutral: SteeringQuaternion? = null
    private var mode = initialMode

    fun onRotationSample(
        sample: SteeringQuaternion,
        timestampNs: Long,
        displayRotation: GyroSteeringDisplayRotation = GyroSteeringDisplayRotation.ROTATION_0,
    ): GyroSteeringOutput {
        val reference = neutral
        if (reference == null) {
            val calibratedPose = calibrator.observe(sample, timestampNs)
            if (calibratedPose != null) {
                neutral = calibratedPose
                processor.reset()
                return GyroSteeringOutput(true, 0f, 0f, mode = mode)
            }
            return GyroSteeringOutput(false, 0f, 0f, mode = mode)
        }

        val components = GyroSteeringOrientation.relativeSteeringComponents(
            neutral = reference,
            current = sample,
            displayRotation = displayRotation,
        )
        val selection = axisSelector.select(components, mode)
        val relativeTilt = selection.degrees * direction
        return GyroSteeringOutput(
            calibrated = true,
            relativeTiltDegrees = relativeTilt,
            stickX = processor.onTiltSample(relativeTilt, timestampNs),
            wheelDegrees = components.wheelDegrees * direction,
            yawDegrees = components.yawDegrees * direction,
            activeAxis = selection.axis,
            mode = mode,
        )
    }

    fun setMode(newMode: GyroSteeringMode) {
        if (mode == newMode) return
        mode = newMode
        axisSelector.reset()
        processor.reset()
    }

    fun recenter() {
        reset()
    }

    fun resetOutputFilter() {
        processor.reset()
    }

    fun reset() {
        neutral = null
        calibrator.reset()
        axisSelector.reset()
        processor.reset()
    }
}

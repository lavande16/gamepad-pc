package app.gamenative.gamefold.gyro

import kotlin.math.max
import kotlin.math.min

// Focused Kotlin port of the stillness calibration window from
// JibbSmart/GamepadMotionHelpers GamepadMotion.hpp Version 10.
// Upstream commit: f1a76007ec058122a4bb6f19c816c6c0c7b7f12a (MIT).
internal data class Vector3f(
    val x: Float,
    val y: Float,
    val z: Float,
) {
    constructor(value: Float) : this(value, value, value)

    fun min(other: Vector3f) = Vector3f(
        min(x, other.x),
        min(y, other.y),
        min(z, other.z),
    )

    fun max(other: Vector3f) = Vector3f(
        max(x, other.x),
        max(y, other.y),
        max(z, other.z),
    )

    operator fun minus(other: Vector3f) = Vector3f(
        x - other.x,
        y - other.y,
        z - other.z,
    )

    operator fun plus(other: Vector3f) = Vector3f(
        x + other.x,
        y + other.y,
        z + other.z,
    )

    operator fun times(scale: Float) = Vector3f(x * scale, y * scale, z * scale)

    companion object {
        val ZERO = Vector3f(0f, 0f, 0f)
    }
}

internal data class GyroBias(
    val xDps: Float,
    val yDps: Float,
    val zDps: Float,
) {
    companion object {
        val ZERO = GyroBias(0f, 0f, 0f)
    }
}

internal class GamepadMotionStillnessCalibrator {
    var bias: GyroBias = GyroBias.ZERO
        private set

    private val window = SensorMinMaxWindow()
    private var frozen = false
    private var minimumGyroDelta = Vector3f(INITIAL_GYRO_DELTA_DPS)
    private var minimumAccelDelta = Vector3f(INITIAL_ACCEL_DELTA_G)
    private var recalibrateThreshold = 1f
    private var confidence = 0f
    private var timeSteady = 0f

    fun addSample(gyroDps: Vector3f, accelG: Vector3f, deltaSeconds: Float) {
        if (frozen || !deltaSeconds.isFinite() || deltaSeconds <= 0f) return
        if (gyroDps == Vector3f.ZERO && accelG == Vector3f.ZERO) return

        window.addSample(gyroDps, accelG, deltaSeconds)
        if (confidence < 1f) {
            val deterioration = Vector3f(STILLNESS_SAMPLE_DETERIORATION_RATE * deltaSeconds)
            minimumGyroDelta += deterioration
            minimumAccelDelta += deterioration
        }
        if (window.sampleCount < MIN_STILLNESS_SAMPLES ||
            window.timeSampled < MIN_STILLNESS_COLLECTION_SECONDS
        ) {
            climbRecalibrateThreshold(deltaSeconds)
            return
        }

        val gyroDelta = window.maximumGyro - window.minimumGyro
        val accelDelta = window.maximumAccel - window.minimumAccel
        minimumGyroDelta = minimumGyroDelta.min(gyroDelta)
        minimumAccelDelta = minimumAccelDelta.min(accelDelta)

        if (!isWithin(gyroDelta, minimumGyroDelta * recalibrateThreshold) ||
            !isWithin(accelDelta, minimumAccelDelta * recalibrateThreshold)
        ) {
            if (timeSteady > 0f) {
                recalibrateThreshold =
                    max(1f, recalibrateThreshold - STILLNESS_ERROR_DROP_ON_RECALIBRATE)
                timeSteady = 0f
            } else {
                climbRecalibrateThreshold(deltaSeconds)
            }
            resetCandidateWindow()
            return
        }

        if (window.timeSampled >= MIN_STILLNESS_CORRECTION_SECONDS) {
            timeSteady = min(timeSteady + deltaSeconds, STILLNESS_CALIBRATION_EASE_IN_SECONDS)
            bias = GyroBias(
                xDps = window.meanGyro.x,
                yDps = window.meanGyro.y,
                zDps = window.meanGyro.z,
            )
            confidence = min(confidence + deltaSeconds * STILLNESS_CONFIDENCE_RATE, 1f)
        } else {
            climbRecalibrateThreshold(deltaSeconds)
        }
    }

    fun setFrozen(frozen: Boolean) {
        if (this.frozen == frozen) return
        this.frozen = frozen
        resetCandidateWindow()
    }

    fun resetCandidateWindow() {
        window.reset()
    }

    fun reset() {
        frozen = false
        bias = GyroBias.ZERO
        minimumGyroDelta = Vector3f(INITIAL_GYRO_DELTA_DPS)
        minimumAccelDelta = Vector3f(INITIAL_ACCEL_DELTA_G)
        recalibrateThreshold = 1f
        confidence = 0f
        timeSteady = 0f
        resetCandidateWindow()
    }

    private fun climbRecalibrateThreshold(deltaSeconds: Float) {
        recalibrateThreshold = min(
            recalibrateThreshold + STILLNESS_ERROR_CLIMB_RATE * deltaSeconds,
            MAX_STILLNESS_ERROR,
        )
    }

    private fun isWithin(delta: Vector3f, threshold: Vector3f): Boolean =
        delta.x <= threshold.x && delta.y <= threshold.y && delta.z <= threshold.z

    private class SensorMinMaxWindow {
        var minimumGyro = Vector3f.ZERO
            private set
        var maximumGyro = Vector3f.ZERO
            private set
        var meanGyro = Vector3f.ZERO
            private set
        var minimumAccel = Vector3f.ZERO
            private set
        var maximumAccel = Vector3f.ZERO
            private set
        var meanAccel = Vector3f.ZERO
            private set
        var sampleCount = 0
            private set
        var timeSampled = 0f
            private set

        fun addSample(gyro: Vector3f, accel: Vector3f, deltaSeconds: Float) {
            if (sampleCount == 0) {
                minimumGyro = gyro
                maximumGyro = gyro
                meanGyro = gyro
                minimumAccel = accel
                maximumAccel = accel
                meanAccel = accel
                sampleCount = 1
                timeSampled = deltaSeconds
                return
            }

            minimumGyro = minimumGyro.min(gyro)
            maximumGyro = maximumGyro.max(gyro)
            minimumAccel = minimumAccel.min(accel)
            maximumAccel = maximumAccel.max(accel)
            sampleCount += 1
            timeSampled += deltaSeconds
            val sampleWeight = 1f / sampleCount
            meanGyro = meanGyro + (gyro - meanGyro) * sampleWeight
            meanAccel = meanAccel + (accel - meanAccel) * sampleWeight
        }

        fun reset() {
            minimumGyro = Vector3f.ZERO
            maximumGyro = Vector3f.ZERO
            meanGyro = Vector3f.ZERO
            minimumAccel = Vector3f.ZERO
            maximumAccel = Vector3f.ZERO
            meanAccel = Vector3f.ZERO
            sampleCount = 0
            timeSampled = 0f
        }
    }

    private companion object {
        const val MIN_STILLNESS_SAMPLES = 10
        const val MIN_STILLNESS_COLLECTION_SECONDS = 0.5f
        const val MIN_STILLNESS_CORRECTION_SECONDS = 2f
        const val INITIAL_GYRO_DELTA_DPS = 1f
        const val INITIAL_ACCEL_DELTA_G = 0.25f
        const val STILLNESS_SAMPLE_DETERIORATION_RATE = 0.2f
        const val STILLNESS_ERROR_CLIMB_RATE = 0.1f
        const val STILLNESS_ERROR_DROP_ON_RECALIBRATE = 0.1f
        const val MAX_STILLNESS_ERROR = 2f
        const val STILLNESS_CALIBRATION_EASE_IN_SECONDS = 3f
        const val STILLNESS_CONFIDENCE_RATE = 1f
    }
}

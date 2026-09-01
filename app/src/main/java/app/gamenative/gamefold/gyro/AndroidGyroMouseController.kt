package app.gamenative.gamefold.gyro

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import kotlin.math.PI
import kotlin.math.abs
import timber.log.Timber

internal class AndroidGyroMouseController(
    context: Context,
    private val rotationProvider: () -> Int,
    private val portraitMode: Boolean,
    private val onMouseDelta: (dx: Int, dy: Int) -> Int,
    private val processor: GyroMouseProcessor = GyroMouseProcessor(),
    private val calibrator: GamepadMotionStillnessCalibrator =
        GamepadMotionStillnessCalibrator(),
) : SensorEventListener {
    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val accumulator = FractionalMouseDeltaAccumulator()

    private var started = false
    private var aimHeld = false
    private var latestAccelerationG: Vector3f? = null
    private var lastGyroTimestampNs = 0L
    private var lastDisplayRotation: DisplayQuarterTurn? = null
    private var diagnosticWindowStartMs = 0L
    private var diagnosticMoveCount = 0
    private var diagnosticDxTotal = 0L
    private var diagnosticDyTotal = 0L
    private var diagnosticRecipientMax = 0
    private var diagnosticSampleCount = 0
    private var diagnosticProcessorNonZeroCount = 0
    private var diagnosticIntegerNonZeroCount = 0
    private var diagnosticMinDt = Float.POSITIVE_INFINITY
    private var diagnosticMaxDt = 0f
    private var diagnosticMaxRawDps = 0f

    fun start(): Boolean {
        if (started) return true
        val gyroSensor = gyroscope ?: run {
            Timber.w("[GameFoldGyroMouse] TYPE_GYROSCOPE unavailable; gyro aim disabled")
            return false
        }
        started = sensorManager.registerListener(
            this,
            gyroSensor,
            SENSOR_PERIOD_US,
            0,
            mainHandler,
        )
        if (!started) {
            Timber.w("[GameFoldGyroMouse] Failed to register gyroscope listener")
            return false
        }

        val accelRegistered = accelerometer?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SENSOR_PERIOD_US,
                0,
                mainHandler,
            )
        } ?: false
        processor.setBias(calibrator.bias)
        Timber.i(
            "[GameFoldGyroMouse] GTA5 angle-integrated gyro-to-raw-mouse started; accelCalibration=%s",
            accelRegistered,
        )
        return true
    }

    fun setAimHeld(held: Boolean) {
        if (aimHeld == held) return
        aimHeld = held
        lastGyroTimestampNs = 0L
        resetDiagnostics()
        Log.i(TRACE_TAG, "stage=gyro_gate aimHeld=$held")
        if (held) {
            accumulator.reset()
            calibrator.setFrozen(true)
            processor.setBias(calibrator.bias)
            processor.setAimHeld(true)
        } else {
            processor.setAimHeld(false)
            accumulator.reset()
            calibrator.setFrozen(false)
        }
    }

    fun stop() {
        if (started) {
            sensorManager.unregisterListener(this)
            Timber.i("[GameFoldGyroMouse] GTA5 angle-integrated gyro-to-raw-mouse stopped")
        }
        started = false
        aimHeld = false
        latestAccelerationG = null
        lastGyroTimestampNs = 0L
        lastDisplayRotation = null
        resetDiagnostics()
        processor.setAimHeld(false)
        accumulator.reset()
        calibrator.setFrozen(false)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!started || event.values.size < 3) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> updateAcceleration(event)
            Sensor.TYPE_GYROSCOPE -> processGyroscope(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun updateAcceleration(event: SensorEvent) {
        latestAccelerationG = Vector3f(
            x = event.values[0] / SensorManager.GRAVITY_EARTH,
            y = event.values[1] / SensorManager.GRAVITY_EARTH,
            z = event.values[2] / SensorManager.GRAVITY_EARTH,
        )
    }

    private fun processGyroscope(event: SensorEvent) {
        val rotation = rotationProvider().toDisplayQuarterTurn()
        if (lastDisplayRotation != null && lastDisplayRotation != rotation) {
            lastGyroTimestampNs = 0L
        }
        lastDisplayRotation = rotation

        val deltaSeconds = if (lastGyroTimestampNs == 0L) {
            0f
        } else {
            (event.timestamp - lastGyroTimestampNs) * NS_TO_SECONDS
        }
        lastGyroTimestampNs = event.timestamp
        if (!deltaSeconds.isFinite() || deltaSeconds <= 0f || deltaSeconds > MAX_SAMPLE_SECONDS) {
            calibrator.resetCandidateWindow()
        } else if (!aimHeld) {
            latestAccelerationG?.let { accelG ->
                calibrator.addSample(
                    gyroDps = Vector3f(
                        event.values[0] * RADIANS_TO_DEGREES,
                        event.values[1] * RADIANS_TO_DEGREES,
                        event.values[2] * RADIANS_TO_DEGREES,
                    ),
                    accelG = accelG,
                    deltaSeconds = deltaSeconds,
                )
                processor.setBias(calibrator.bias)
            }
        }

        val mouseDelta = GameFoldGyroOrientationPolicy.apply(
            delta = processor.onGyroSample(
                GyroMouseSample(
                    xRadPerSec = event.values[0],
                    yRadPerSec = event.values[1],
                    zRadPerSec = event.values[2],
                    timestampNs = event.timestamp,
                    rotation = rotation,
                ),
            ),
            portraitMode = portraitMode,
        )
        if (aimHeld) {
            val integerDelta = accumulator.add(mouseDelta)
            recordDiagnosticSample(event, deltaSeconds, mouseDelta, integerDelta)
            if (integerDelta != IntegerMouseDelta.ZERO) {
                val recipientCount = onMouseDelta(integerDelta.dx, integerDelta.dy)
                recordDiagnosticDelta(integerDelta, recipientCount)
            }
        }
    }

    private fun recordDiagnosticSample(
        event: SensorEvent,
        deltaSeconds: Float,
        mouseDelta: GyroMouseDelta,
        integerDelta: IntegerMouseDelta,
    ) {
        diagnosticSampleCount += 1
        if (mouseDelta != GyroMouseDelta.ZERO) diagnosticProcessorNonZeroCount += 1
        if (integerDelta != IntegerMouseDelta.ZERO) diagnosticIntegerNonZeroCount += 1
        if (deltaSeconds.isFinite() && deltaSeconds > 0f) {
            diagnosticMinDt = minOf(diagnosticMinDt, deltaSeconds)
            diagnosticMaxDt = maxOf(diagnosticMaxDt, deltaSeconds)
        }
        diagnosticMaxRawDps = maxOf(
            diagnosticMaxRawDps,
            abs(event.values[0] * RADIANS_TO_DEGREES),
            abs(event.values[1] * RADIANS_TO_DEGREES),
            abs(event.values[2] * RADIANS_TO_DEGREES),
        )

        val nowMs = SystemClock.elapsedRealtime()
        if (diagnosticWindowStartMs == 0L) diagnosticWindowStartMs = nowMs
        if (nowMs - diagnosticWindowStartMs < DIAGNOSTIC_PERIOD_MS) return
        val minDtMs = if (diagnosticMinDt.isFinite()) diagnosticMinDt * 1_000f else 0f
        Log.i(
            TRACE_TAG,
            "stage=gyro_process aimHeld=$aimHeld samples=$diagnosticSampleCount " +
                "processorNonZero=$diagnosticProcessorNonZeroCount " +
                "integerNonZero=$diagnosticIntegerNonZeroCount packets=$diagnosticMoveCount " +
                "sumDx=$diagnosticDxTotal sumDy=$diagnosticDyTotal " +
                "routeAcceptedMax=$diagnosticRecipientMax maxRawDps=$diagnosticMaxRawDps " +
                "dtMsMin=$minDtMs dtMsMax=${diagnosticMaxDt * 1_000f}",
        )
        resetDiagnostics(nowMs)
    }

    private fun recordDiagnosticDelta(delta: IntegerMouseDelta, recipientCount: Int) {
        diagnosticMoveCount += 1
        diagnosticDxTotal += delta.dx
        diagnosticDyTotal += delta.dy
        diagnosticRecipientMax = maxOf(diagnosticRecipientMax, recipientCount)
    }

    private fun resetDiagnostics(windowStartMs: Long = 0L) {
        diagnosticWindowStartMs = windowStartMs
        diagnosticMoveCount = 0
        diagnosticDxTotal = 0L
        diagnosticDyTotal = 0L
        diagnosticRecipientMax = 0
        diagnosticSampleCount = 0
        diagnosticProcessorNonZeroCount = 0
        diagnosticIntegerNonZeroCount = 0
        diagnosticMinDt = Float.POSITIVE_INFINITY
        diagnosticMaxDt = 0f
        diagnosticMaxRawDps = 0f
    }

    private fun Int.toDisplayQuarterTurn(): DisplayQuarterTurn = when (this) {
        Surface.ROTATION_90 -> DisplayQuarterTurn.ROTATION_90
        Surface.ROTATION_180 -> DisplayQuarterTurn.ROTATION_180
        Surface.ROTATION_270 -> DisplayQuarterTurn.ROTATION_270
        else -> DisplayQuarterTurn.ROTATION_0
    }

    private companion object {
        const val SENSOR_PERIOD_US = 10_000
        const val NS_TO_SECONDS = 1e-9f
        const val MAX_SAMPLE_SECONDS = 0.05f
        const val DIAGNOSTIC_PERIOD_MS = 1_000L
        const val TRACE_TAG = "GameFoldMouseTrace"
        val RADIANS_TO_DEGREES = (180.0 / PI).toFloat()
    }
}

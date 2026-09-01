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
import timber.log.Timber

internal class AndroidGyroSteeringController(
    context: Context,
    private val onSteering: (Float) -> Unit,
    private val rotationProvider: () -> Int = { Surface.ROTATION_0 },
    private val onDiagnostics: (GyroSteeringOutput) -> Unit = {},
    private val session: GyroSteeringSession = GyroSteeringSession(
        // Android's positive screen-normal rotation is counter-clockwise as viewed by the user.
        // XInput's positive left-stick X steers right, so invert it for steering-wheel motion.
        direction = -1f,
    ),
) : SensorEventListener {
    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gameRotationVector =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val quaternionValues = FloatArray(4)

    private var started = false
    private var manualOverride = false
    private var calibrationLogged = false
    private var diagnosticWindowStartMs = 0L
    private var diagnosticSampleCount = 0
    private var diagnosticLatestAngleDegrees = 0f
    private var diagnosticLatestStickX = 0f
    private var diagnosticLastDispatchMs = 0L

    fun start(): Boolean {
        if (started) return true
        val sensor = gameRotationVector ?: run {
            Timber.w("[GameFoldGyroSteering] TYPE_GAME_ROTATION_VECTOR unavailable")
            Log.i(TRACE_TAG, "stage=sensor_unavailable type=game_rotation_vector")
            onSteering(0f)
            return false
        }
        session.reset()
        resetDiagnostics()
        calibrationLogged = false
        started = sensorManager.registerListener(
            this,
            sensor,
            SENSOR_PERIOD_US,
            0,
            mainHandler,
        )
        if (!started) {
            Timber.w("[GameFoldGyroSteering] Failed to register Game Rotation Vector")
            Log.i(TRACE_TAG, "stage=sensor_registration registered=false")
            onSteering(0f)
            return false
        }
        Log.i(
            TRACE_TAG,
            "stage=sensor_registration registered=true sensor=${sensor.name} " +
                "periodUs=$SENSOR_PERIOD_US",
        )
        return true
    }

    fun stop() {
        if (started) {
            sensorManager.unregisterListener(this)
            Log.i(TRACE_TAG, "stage=sensor_stopped")
        }
        started = false
        session.reset()
        resetDiagnostics()
        calibrationLogged = false
        onSteering(0f)
    }

    fun setManualOverride(active: Boolean) {
        if (manualOverride == active) return
        manualOverride = active
        session.resetOutputFilter()
        onSteering(0f)
        Log.i(TRACE_TAG, "stage=manual_override active=$active")
    }

    fun setMode(mode: GyroSteeringMode) {
        session.setMode(mode)
        onSteering(0f)
        Log.i(TRACE_TAG, "stage=mode_changed mode=$mode")
    }

    fun recenter() {
        session.recenter()
        calibrationLogged = false
        onSteering(0f)
        Log.i(TRACE_TAG, "stage=recenter_requested")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!started || event.sensor.type != Sensor.TYPE_GAME_ROTATION_VECTOR) return
        SensorManager.getQuaternionFromVector(quaternionValues, event.values)
        val output = session.onRotationSample(
            sample = SteeringQuaternion(
                w = quaternionValues[0],
                x = quaternionValues[1],
                y = quaternionValues[2],
                z = quaternionValues[3],
            ),
            timestampNs = event.timestamp,
            displayRotation = displayRotation(rotationProvider()),
        )
        if (output.calibrated && !calibrationLogged) {
            calibrationLogged = true
            Log.i(TRACE_TAG, "stage=neutral_calibrated stableMs=300")
        }
        val emittedStickX = if (manualOverride || !output.calibrated) 0f else output.stickX
        onSteering(emittedStickX)
        recordDiagnostics(output.copy(stickX = emittedStickX), emittedStickX)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun recordDiagnostics(output: GyroSteeringOutput, emittedStickX: Float) {
        diagnosticSampleCount += 1
        diagnosticLatestAngleDegrees = output.relativeTiltDegrees
        diagnosticLatestStickX = emittedStickX
        val nowMs = SystemClock.elapsedRealtime()
        if (nowMs - diagnosticLastDispatchMs >= DIAGNOSTIC_UI_PERIOD_MS) {
            diagnosticLastDispatchMs = nowMs
            onDiagnostics(output)
        }
        if (diagnosticWindowStartMs == 0L) diagnosticWindowStartMs = nowMs
        if (nowMs - diagnosticWindowStartMs < DIAGNOSTIC_PERIOD_MS) return
        Log.i(
            TRACE_TAG,
            "stage=steering_sample calibrated=${output.calibrated} " +
                "manualOverride=$manualOverride samples=$diagnosticSampleCount " +
                "mode=${output.mode} axis=${output.activeAxis} " +
                "rotation=${displayRotation(rotationProvider())} " +
                "wheelDeg=${output.wheelDegrees} yawDeg=${output.yawDegrees} " +
                "angleDeg=$diagnosticLatestAngleDegrees stickX=$diagnosticLatestStickX",
        )
        resetDiagnostics(nowMs)
    }

    private fun resetDiagnostics(windowStartMs: Long = 0L) {
        diagnosticWindowStartMs = windowStartMs
        diagnosticSampleCount = 0
        diagnosticLatestAngleDegrees = 0f
        diagnosticLatestStickX = 0f
        diagnosticLastDispatchMs = 0L
    }

    private fun displayRotation(surfaceRotation: Int): GyroSteeringDisplayRotation =
        when (surfaceRotation) {
            Surface.ROTATION_90 -> GyroSteeringDisplayRotation.ROTATION_90
            Surface.ROTATION_180 -> GyroSteeringDisplayRotation.ROTATION_180
            Surface.ROTATION_270 -> GyroSteeringDisplayRotation.ROTATION_270
            else -> GyroSteeringDisplayRotation.ROTATION_0
        }

    private companion object {
        const val SENSOR_PERIOD_US = 10_000
        const val DIAGNOSTIC_PERIOD_MS = 1_000L
        const val DIAGNOSTIC_UI_PERIOD_MS = 50L
        const val TRACE_TAG = "GameFoldSteeringTrace"
    }
}

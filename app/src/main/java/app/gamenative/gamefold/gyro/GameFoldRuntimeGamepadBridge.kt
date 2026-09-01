package app.gamenative.gamefold.gyro

import android.util.Log
import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldControllerListener
import app.gamenative.gamefold.ui.GameFoldStick
import com.winlator.inputcontrols.GamepadState

internal class GameFoldRuntimeGamepadBridge(
    private val publish: (GamepadState) -> Unit,
    private val onAimHeldChanged: (Boolean) -> Unit,
    private val onHomePressed: () -> Unit,
    private val interceptButton: (GameFoldButton, Boolean) -> Boolean,
    private val onManualSteeringOverrideChanged: (Boolean) -> Unit = {},
) : GameFoldControllerListener {
    private val state = GamepadState()
    private var manualLeftX = 0f
    private var manualLeftY = 0f
    private var manualRightX = 0f
    private var manualRightY = 0f
    private var manualLeftActive = false
    private var gyroSteeringEnabled = false
    private var gyroSteeringX = 0f

    override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
        if (interceptButton(button, pressed)) return

        when (button) {
            GameFoldButton.DPAD_UP -> state.dpad[0] = pressed
            GameFoldButton.DPAD_RIGHT -> state.dpad[1] = pressed
            GameFoldButton.DPAD_DOWN -> state.dpad[2] = pressed
            GameFoldButton.DPAD_LEFT -> state.dpad[3] = pressed
            GameFoldButton.A -> state.setPressed(0, pressed)
            GameFoldButton.B -> state.setPressed(1, pressed)
            GameFoldButton.X -> state.setPressed(2, pressed)
            GameFoldButton.Y -> state.setPressed(3, pressed)
            GameFoldButton.L -> state.setPressed(4, pressed)
            GameFoldButton.R -> state.setPressed(5, pressed)
            GameFoldButton.MINUS -> state.setPressed(6, pressed)
            GameFoldButton.PLUS -> state.setPressed(7, pressed)
            GameFoldButton.L3 -> state.setPressed(8, pressed)
            GameFoldButton.R3 -> state.setPressed(9, pressed)
            GameFoldButton.ZL -> updateAimGate(pressed)
            GameFoldButton.ZR -> state.triggerR = if (pressed) 1f else 0f
            GameFoldButton.HOME -> {
                if (pressed) onHomePressed()
                return
            }
        }
        publishState()
    }

    override fun onStickChanged(stick: GameFoldStick, x: Float, y: Float) {
        if (stick == GameFoldStick.LEFT) {
            manualLeftX = x
            manualLeftY = y
        } else {
            manualRightX = x
            manualRightY = y
        }
        publishState()
    }

    override fun onStickInteractionChanged(stick: GameFoldStick, active: Boolean) {
        if (stick != GameFoldStick.LEFT || manualLeftActive == active) return
        manualLeftActive = active
        if (!active) gyroSteeringX = 0f
        onManualSteeringOverrideChanged(active)
        publishState()
    }

    fun setGyroSteeringEnabled(enabled: Boolean) {
        gyroSteeringEnabled = enabled
        if (!enabled) gyroSteeringX = 0f
        publishState()
    }

    fun updateGyroSteering(value: Float) {
        if (!gyroSteeringEnabled) return
        gyroSteeringX = value.coerceIn(-1f, 1f)
        publishState()
    }

    fun releaseGyroSteering() {
        gyroSteeringX = 0f
        publishState()
    }

    fun releaseGyro() {
        state.triggerL = 0f
        trace("stage=zl_gate source=releaseGyro pressed=false triggerL=0.0")
        onAimHeldChanged(false)
        publishState()
    }

    private fun updateAimGate(pressed: Boolean) {
        state.triggerL = if (pressed) 1f else 0f
        trace("stage=zl_gate source=button pressed=$pressed triggerL=${state.triggerL}")
        onAimHeldChanged(pressed)
    }

    private fun publishState() {
        state.thumbLX = if (gyroSteeringEnabled && !manualLeftActive) {
            gyroSteeringX
        } else {
            manualLeftX
        }
        state.thumbLY = manualLeftY
        state.thumbRX = manualRightX
        state.thumbRY = manualRightY
        publish(state)
    }

    private fun trace(message: String) {
        try {
            Log.i(TRACE_TAG, message)
        } catch (_: RuntimeException) {
            // android.util.Log is a stub in the bridge's pure JVM unit tests.
        }
    }

    private companion object {
        const val TRACE_TAG = "GameFoldMouseTrace"
    }
}

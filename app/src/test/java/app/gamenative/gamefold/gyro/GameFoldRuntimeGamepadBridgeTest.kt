package app.gamenative.gamefold.gyro

import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldStick
import com.winlator.inputcontrols.GamepadState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldRuntimeGamepadBridgeTest {
    @Test
    fun zlControlsTriggerAndGyroGate() {
        val states = mutableListOf<GamepadState>()
        val gates = mutableListOf<Boolean>()
        val bridge = createBridge(states = states, gates = gates)

        bridge.onButtonChanged(GameFoldButton.ZL, true)
        assertEquals(1f, states.last().triggerL, 0f)
        assertEquals(true, gates.last())

        bridge.onButtonChanged(GameFoldButton.ZL, false)
        assertEquals(0f, states.last().triggerL, 0f)
        assertEquals(false, gates.last())
    }

    @Test
    fun zrControlsTriggerWithoutOpeningGyroGate() {
        val states = mutableListOf<GamepadState>()
        val gates = mutableListOf<Boolean>()
        val bridge = createBridge(states = states, gates = gates)

        bridge.onButtonChanged(GameFoldButton.ZR, true)
        assertEquals(1f, states.last().triggerR, 0f)
        assertTrue(gates.isEmpty())

        bridge.onButtonChanged(GameFoldButton.ZR, false)
        assertEquals(0f, states.last().triggerR, 0f)
        assertTrue(gates.isEmpty())
    }

    @Test
    fun releasingZlStopsGyroEvenWhileZrRemainsHeld() {
        val states = mutableListOf<GamepadState>()
        val gates = mutableListOf<Boolean>()
        val bridge = createBridge(states = states, gates = gates)

        bridge.onButtonChanged(GameFoldButton.ZL, true)
        bridge.onButtonChanged(GameFoldButton.ZR, true)
        bridge.onButtonChanged(GameFoldButton.ZL, false)

        assertEquals(0f, states.last().triggerL, 0f)
        assertEquals(1f, states.last().triggerR, 0f)
        assertEquals(listOf(true, false), gates)

        bridge.onButtonChanged(GameFoldButton.ZR, false)

        assertEquals(0f, states.last().triggerR, 0f)
        assertEquals(listOf(true, false), gates)
    }

    @Test
    fun zlGateNeverChangesManualRightStick() {
        val states = mutableListOf<GamepadState>()
        val bridge = createBridge(states = states)
        bridge.onStickChanged(GameFoldStick.RIGHT, 0.40f, -0.20f)

        bridge.onButtonChanged(GameFoldButton.ZL, true)
        assertEquals(0.40f, states.last().thumbRX, 0.001f)
        assertEquals(-0.20f, states.last().thumbRY, 0.001f)

        bridge.onButtonChanged(GameFoldButton.ZL, false)
        assertEquals(0.40f, states.last().thumbRX, 0.001f)
        assertEquals(-0.20f, states.last().thumbRY, 0.001f)
    }

    @Test
    fun lifecycleReleaseClearsZlWithoutChangingManualRightStick() {
        val states = mutableListOf<GamepadState>()
        val gates = mutableListOf<Boolean>()
        val bridge = createBridge(states = states, gates = gates)
        bridge.onStickChanged(GameFoldStick.RIGHT, 0.25f, -0.15f)
        bridge.onButtonChanged(GameFoldButton.ZL, true)

        bridge.releaseGyro()

        assertEquals(0f, states.last().triggerL, 0f)
        assertEquals(0.25f, states.last().thumbRX, 0.001f)
        assertEquals(-0.15f, states.last().thumbRY, 0.001f)
        assertEquals(false, gates.last())
    }

    @Test
    fun existingButtonsDpadTriggersAndLeftStickKeepTheirMappings() {
        val states = mutableListOf<GamepadState>()
        val bridge = createBridge(states = states)

        bridge.onButtonChanged(GameFoldButton.A, true)
        bridge.onButtonChanged(GameFoldButton.Y, true)
        bridge.onButtonChanged(GameFoldButton.L, true)
        bridge.onButtonChanged(GameFoldButton.R3, true)
        bridge.onButtonChanged(GameFoldButton.DPAD_LEFT, true)
        bridge.onButtonChanged(GameFoldButton.ZR, true)
        bridge.onStickChanged(GameFoldStick.LEFT, -0.25f, 0.75f)

        val state = states.last()
        assertTrue(state.isPressed(0))
        assertTrue(state.isPressed(3))
        assertTrue(state.isPressed(4))
        assertTrue(state.isPressed(9))
        assertTrue(state.dpad[3])
        assertEquals(1f, state.triggerR, 0f)
        assertEquals(-0.25f, state.thumbLX, 0f)
        assertEquals(0.75f, state.thumbLY, 0f)
    }

    @Test
    fun touchedLeftStickOverridesGyroAndReleaseClearsStaleGyroValue() {
        val states = mutableListOf<GamepadState>()
        val manualOverrideChanges = mutableListOf<Boolean>()
        val bridge = createBridge(
            states = states,
            manualOverrideChanges = manualOverrideChanges,
        )
        bridge.setGyroSteeringEnabled(true)
        bridge.updateGyroSteering(0.60f)
        assertEquals(0.60f, states.last().thumbLX, 0f)

        bridge.onStickInteractionChanged(GameFoldStick.LEFT, true)
        bridge.onStickChanged(GameFoldStick.LEFT, -0.25f, 0.75f)
        bridge.updateGyroSteering(0.80f)
        assertEquals(-0.25f, states.last().thumbLX, 0f)
        assertEquals(0.75f, states.last().thumbLY, 0f)

        bridge.onStickChanged(GameFoldStick.LEFT, 0f, 0f)
        bridge.onStickInteractionChanged(GameFoldStick.LEFT, false)
        assertEquals(0f, states.last().thumbLX, 0f)
        assertEquals(listOf(true, false), manualOverrideChanges)

        bridge.updateGyroSteering(0.20f)
        assertEquals(0.20f, states.last().thumbLX, 0f)
    }

    @Test
    fun homeIsHandledOutsideGamepadState() {
        val states = mutableListOf<GamepadState>()
        var homePresses = 0
        val bridge = createBridge(
            states = states,
            onHomePressed = { homePresses += 1 },
        )

        bridge.onButtonChanged(GameFoldButton.HOME, true)
        bridge.onButtonChanged(GameFoldButton.HOME, false)

        assertEquals(1, homePresses)
        assertTrue(states.isEmpty())
    }

    @Test
    fun interceptedDialogButtonsDoNotReachGamepadState() {
        val states = mutableListOf<GamepadState>()
        val bridge = createBridge(
            states = states,
            interceptButton = { button, _ -> button == GameFoldButton.B },
        )

        bridge.onButtonChanged(GameFoldButton.B, true)

        assertTrue(states.isEmpty())
    }

    private fun createBridge(
        states: MutableList<GamepadState>,
        gates: MutableList<Boolean> = mutableListOf(),
        manualOverrideChanges: MutableList<Boolean> = mutableListOf(),
        onHomePressed: () -> Unit = {},
        interceptButton: (GameFoldButton, Boolean) -> Boolean = { _, _ -> false },
    ) = GameFoldRuntimeGamepadBridge(
        publish = { state -> states += snapshot(state) },
        onAimHeldChanged = gates::add,
        onManualSteeringOverrideChanged = manualOverrideChanges::add,
        onHomePressed = onHomePressed,
        interceptButton = interceptButton,
    )

    private fun snapshot(state: GamepadState): GamepadState =
        GamepadState().also { it.copy(state) }
}

package app.gamenative.gamefold.rear

import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldControllerListener

/**
 * Process-local bridge from the vivo rear-display activity to the active PC game shell.
 *
 * The rear activity has its own Android window, but it must publish the same semantic buttons as
 * the front shell. Keeping the bridge independent from WinHandler means the rear lifecycle can
 * be stopped and restarted without owning emulator state or a second controller profile.
 */
object RearTouchInputRouter {
    private val lock = Any()
    private var listener: GameFoldControllerListener? = null
    private val pressedButtons = linkedSetOf<GameFoldButton>()

    fun register(next: GameFoldControllerListener) {
        val previous: GameFoldControllerListener?
        synchronized(lock) {
            previous = listener
            listener = next
            pressedButtons.clear()
        }
        previous?.let { old ->
            old.onButtonChanged(GameFoldButton.ZL, false)
            old.onButtonChanged(GameFoldButton.ZR, false)
        }
    }

    fun unregister(expected: GameFoldControllerListener) {
        val toRelease: List<GameFoldButton>
        synchronized(lock) {
            if (listener !== expected) return
            toRelease = pressedButtons.toList()
            pressedButtons.clear()
            listener = null
        }
        toRelease.forEach { expected.onButtonChanged(it, false) }
    }

    fun setButton(button: GameFoldButton, pressed: Boolean) {
        val target: GameFoldControllerListener
        synchronized(lock) {
            target = listener ?: return
            if (pressed) pressedButtons += button else pressedButtons -= button
        }
        target.onButtonChanged(button, pressed)
    }

    fun releaseAll() {
        val target: GameFoldControllerListener
        val toRelease: List<GameFoldButton>
        synchronized(lock) {
            target = listener ?: return
            toRelease = pressedButtons.toList()
            pressedButtons.clear()
        }
        toRelease.forEach { target.onButtonChanged(it, false) }
    }
}

package app.gamenative.gamefold.rear

import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldControllerListener
import org.junit.Assert.assertEquals
import org.junit.Test

class RearTouchInputRouterTest {
    @Test
    fun registerAndSetButton_forwardsEventsToActiveGameBridge() {
        val events = mutableListOf<Pair<GameFoldButton, Boolean>>()
        val listener = object : GameFoldControllerListener {
            override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
                events += button to pressed
            }

            override fun onStickChanged(
                stick: app.gamenative.gamefold.ui.GameFoldStick,
                x: Float,
                y: Float,
            ) = Unit
        }

        RearTouchInputRouter.register(listener)
        RearTouchInputRouter.setButton(GameFoldButton.ZL, true)
        RearTouchInputRouter.setButton(GameFoldButton.ZL, false)
        RearTouchInputRouter.unregister(listener)

        assertEquals(
            listOf(GameFoldButton.ZL to true, GameFoldButton.ZL to false),
            events,
        )
    }

    @Test
    fun unregister_releasesButtonsThatRemainPressed() {
        val events = mutableListOf<Pair<GameFoldButton, Boolean>>()
        val listener = object : GameFoldControllerListener {
            override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
                events += button to pressed
            }

            override fun onStickChanged(
                stick: app.gamenative.gamefold.ui.GameFoldStick,
                x: Float,
                y: Float,
            ) = Unit
        }

        RearTouchInputRouter.register(listener)
        RearTouchInputRouter.setButton(GameFoldButton.ZR, true)
        RearTouchInputRouter.unregister(listener)

        assertEquals(
            listOf(GameFoldButton.ZR to true, GameFoldButton.ZR to false),
            events,
        )
    }
}

package app.gamenative.gamefold.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class GameFoldPcControllerBackRoutingTest {

    @Test
    fun modalHandlerConsumesBothBAndYBeforePageListener() {
        val registry = GameFoldPcControllerBackRegistry()
        val owner = Any()
        var modalBackCount = 0
        var pageBackCount = 0
        registry.register(owner) { modalBackCount++ }

        listOf(GameFoldButton.B, GameFoldButton.Y).forEach { button ->
            routeGameFoldControllerButton(
                button = button,
                pressed = true,
                backRegistry = registry,
                dispatchToPage = { pageBackCount++ },
            )
        }

        assertEquals(2, modalBackCount)
        assertEquals(0, pageBackCount)
    }

    @Test
    fun pageListenerReceivesBackAfterModalUnregisters() {
        val registry = GameFoldPcControllerBackRegistry()
        val owner = Any()
        var pageBackCount = 0
        registry.register(owner) { }
        registry.unregister(owner)

        routeGameFoldControllerButton(
            button = GameFoldButton.B,
            pressed = true,
            backRegistry = registry,
            dispatchToPage = { pageBackCount++ },
        )

        assertEquals(1, pageBackCount)
    }
}

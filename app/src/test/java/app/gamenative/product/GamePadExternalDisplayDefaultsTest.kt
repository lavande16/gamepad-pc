package app.gamenative.product

import com.winlator.container.Container
import com.winlator.container.ContainerData
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GamePadExternalDisplayDefaultsTest {
    @Test
    fun newGameTemplates_defaultExternalDisplaySwapOn() {
        assertTrue(Container("NEW_GAME").isExternalDisplaySwap)
        assertTrue(ContainerData(name = "New Game").externalDisplaySwap)
    }
}

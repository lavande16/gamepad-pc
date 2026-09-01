package app.gamenative.product

import app.gamenative.ui.util.ScreenEffectsConfig
import app.gamenative.utils.LsfgVkManager
import com.winlator.container.Container
import com.winlator.winhandler.WinHandler.PreferredInputApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GameFoldPcPerGameDefaultsTest {
    @Test
    fun versionOneContainer_migratesToSimplifiedChineseLsfgAndFpsExactlyOnce() {
        val container = Container("CUSTOM_GAME_TEST").apply {
            containerVariant = "bionic"
            language = "english"
            screenSize = "1280x720"
            setLC_ALL("en_US.UTF-8")
            isShowFPS = false
            putExtra(LsfgVkManager.EXTRA_ARMED, "false")
            putExtra("gamefoldPcDefaults", "1")
        }

        assertTrue(GameFoldPcPerGameDefaults.applyTo(container))
        assertEquals(GameFoldPcDefaults.DEFAULTS_VERSION, container.getExtra("gamefoldPcDefaults", ""))
        assertEquals("schinese", container.language)
        assertEquals("zh_CN.UTF-8", container.lC_ALL)
        assertEquals("2304x1440", container.screenSize)
        assertTrue(container.isShowFPS)
        assertEquals("true", container.getExtra(LsfgVkManager.EXTRA_ARMED, ""))

        container.language = "english"
        assertFalse(GameFoldPcPerGameDefaults.applyTo(container))
        assertEquals("english", container.language)
    }

    @Test
    fun newGameContainer_receivesGamePadDefaultScreenSize() {
        val container = Container("CUSTOM_GAME_NEW")

        assertTrue(GameFoldPcPerGameDefaults.applyTo(container))
        assertEquals("2304x1440", container.screenSize)
    }

    @Test
    fun alreadyConfiguredContainer_keepsUserSelectedScreenSize() {
        val container = Container("CUSTOM_GAME_EXISTING").apply {
            screenSize = "1920x1080"
            putExtra("gamefoldPcDefaults", GameFoldPcDefaults.DEFAULTS_VERSION)
        }

        assertFalse(GameFoldPcPerGameDefaults.applyTo(container))
        assertEquals("1920x1080", container.screenSize)
    }

    @Test
    fun versionTwoContainer_migratesOnlyV066OldDefaults() {
        val container = Container("CUSTOM_GAME_V2").apply {
            screenSize = "1920x1080"
            language = "english"
            setLC_ALL("en_US.UTF-8")
            isShowFPS = false
            inputType = PreferredInputApi.AUTO.ordinal
            dinputMapperType = 2
            putExtra(LsfgVkManager.EXTRA_ARMED, "false")
            putExtra(LsfgVkManager.EXTRA_FLOW_SCALE, "0.80")
            putExtra(ScreenEffectsConfig.KEY_SCALING_MODE, ScreenEffectsConfig.SCALING_MODE_NONE)
            putExtra("useSteamInput", "true")
            putExtra("gamefoldPcDefaults", "2")
        }

        assertTrue(GameFoldPcPerGameDefaults.applyTo(container))
        assertEquals(GameFoldPcDefaults.DEFAULTS_VERSION, container.getExtra("gamefoldPcDefaults", ""))
        assertEquals("0.50", container.getExtra(LsfgVkManager.EXTRA_FLOW_SCALE, ""))
        assertEquals(
            ScreenEffectsConfig.SCALING_MODE_FSR_ASPECT.toString(),
            container.getExtra(ScreenEffectsConfig.KEY_SCALING_MODE, ""),
        )
        assertEquals("false", container.getExtra("useSteamInput", ""))
        assertEquals(PreferredInputApi.BOTH.ordinal, container.inputType)
        assertEquals(1.toByte(), container.dinputMapperType)

        assertEquals("false", container.getExtra(LsfgVkManager.EXTRA_ARMED, ""))
        assertEquals("english", container.language)
        assertEquals("en_US.UTF-8", container.lC_ALL)
        assertEquals("1920x1080", container.screenSize)
        assertFalse(container.isShowFPS)
    }

    @Test
    fun versionTwoContainer_preservesNonDefaultFlowScaleAndScreenEffect() {
        val container = Container("CUSTOM_GAME_V2_CUSTOM").apply {
            putExtra(LsfgVkManager.EXTRA_FLOW_SCALE, "0.65")
            putExtra(ScreenEffectsConfig.KEY_SCALING_MODE, ScreenEffectsConfig.SCALING_MODE_DLS)
            putExtra("gamefoldPcDefaults", "2")
        }

        assertTrue(GameFoldPcPerGameDefaults.applyTo(container))
        assertEquals("0.65", container.getExtra(LsfgVkManager.EXTRA_FLOW_SCALE, ""))
        assertEquals(
            ScreenEffectsConfig.SCALING_MODE_DLS.toString(),
            container.getExtra(ScreenEffectsConfig.KEY_SCALING_MODE, ""),
        )
    }
}

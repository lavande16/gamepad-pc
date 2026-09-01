package app.gamenative.product

import app.gamenative.ui.util.ScreenEffectsConfig
import com.winlator.winhandler.WinHandler.PreferredInputApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcDefaultsTest {
    @Test
    fun globalScreenDefault_missingOrLegacyValue_migratesTo2304x1440() {
        assertEquals(
            "2304x1440",
            GameFoldPcDefaults.migrateGlobalScreenSize(
                currentScreenSize = null,
                appliedVersion = null,
            ),
        )
        assertEquals(
            "2304x1440",
            GameFoldPcDefaults.migrateGlobalScreenSize(
                currentScreenSize = "1280x720",
                appliedVersion = null,
            ),
        )
    }

    @Test
    fun globalScreenDefault_customValue_survivesFirstMigration() {
        assertEquals(
            "1920x1080",
            GameFoldPcDefaults.migrateGlobalScreenSize(
                currentScreenSize = "1920x1080",
                appliedVersion = null,
            ),
        )
    }

    @Test
    fun globalScreenDefault_completedMigration_doesNotOverrideLaterUserChoice() {
        assertEquals(
            null,
            GameFoldPcDefaults.migrateGlobalScreenSize(
                currentScreenSize = "1280x720",
                appliedVersion = GameFoldPcDefaults.GLOBAL_SCREEN_SIZE_DEFAULTS_VERSION,
            ),
        )
    }

    @Test
    fun localContainerDefaults_useProton10SimplifiedChineseLsfgAndFps() {
        assertEquals("bionic", GameFoldPcDefaults.CONTAINER_VARIANT)
        assertEquals("proton-10.0-arm64ec-2", GameFoldPcDefaults.WINE_VERSION)
        assertEquals("schinese", GameFoldPcDefaults.GAME_LANGUAGE)
        assertTrue(GameFoldPcDefaults.CONTAINER_LSFG_ENABLED)
        assertTrue(GameFoldPcDefaults.CONTAINER_SHOW_FPS)
        assertEquals(0.50f, GameFoldPcDefaults.LSFG_FLOW_SCALE)
        assertEquals(
            ScreenEffectsConfig.SCALING_MODE_FSR_ASPECT,
            GameFoldPcDefaults.SCREEN_EFFECT_SCALING_MODE,
        )
    }

    @Test
    fun localBionicWineOptions_includeConfiguredProton10InsteadOfFallingBackToProton9() {
        assertEquals(
            listOf(
                "proton-10.0-arm64ec-2",
                "proton-9.0-arm64ec",
                "proton-9.0-x86_64",
            ),
            GameFoldPcDefaults.localBionicWineOptions(
                listOf(
                    "proton-9.0-arm64ec",
                    "proton-9.0-x86_64",
                ),
            ),
        )
    }

    @Test
    fun localControllerDefaults_supportOfflineModernAndLegacyGames() {
        assertFalse(GameFoldPcDefaults.USE_STEAM_INPUT)
        assertEquals(PreferredInputApi.BOTH.ordinal, GameFoldPcDefaults.CONTROLLER_INPUT_TYPE)
        assertEquals(1.toByte(), GameFoldPcDefaults.DINPUT_MAPPER_TYPE)
    }

    @Test
    fun gamePadGlobalTemplate_defaultsExternalDisplaySwapOn() {
        assertTrue(GameFoldPcDefaults.EXTERNAL_DISPLAY_SWAP)
    }

    @Test
    fun localShellUiDefaults_warnBeforeExitAndHideQuickMenuTipsAndIconPicker() {
        assertTrue(GameFoldPcDefaults.WARN_BEFORE_EXIT)
        assertFalse(GameFoldPcDefaults.SHOW_IN_GAME_QUICK_MENU)
        assertEquals("GameFold", GameFoldPcDefaults.BOOT_SPLASH_BRAND)
        assertFalse(GameFoldPcDefaults.SHOW_BOOT_SPLASH_FIXED_TIPS)
        assertFalse(GameFoldPcDefaults.SHOW_ICON_STYLE_PICKER)
    }

    @Test
    fun performanceHudDefaults_toEnabledFpsOnly() {
        assertTrue(GameFoldPcDefaults.PERFORMANCE_HUD_ENABLED)
        assertEquals(
            setOf(PerformanceHudMetric.FRAME_RATE),
            GameFoldPcDefaults.DEFAULT_PERFORMANCE_HUD_METRICS,
        )
    }

    @Test
    fun existingGame_receivesRequestedPerGameDefaultsExactlyOnce() {
        assertTrue(GameFoldPcDefaults.needsPerGameDefaults(null))
        assertTrue(GameFoldPcDefaults.needsPerGameDefaults("0"))
        assertTrue(GameFoldPcDefaults.needsPerGameDefaults("1"))
        assertTrue(GameFoldPcDefaults.needsPerGameDefaults("2"))
        assertEquals(false, GameFoldPcDefaults.needsPerGameDefaults(GameFoldPcDefaults.DEFAULTS_VERSION))
    }
}

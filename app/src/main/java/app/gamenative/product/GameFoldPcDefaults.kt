package app.gamenative.product

import app.gamenative.ui.util.ScreenEffectsConfig
import com.winlator.winhandler.WinHandler.PreferredInputApi

enum class PerformanceHudMetric {
    FRAME_RATE,
    CPU_USAGE,
    GPU_USAGE,
    RAM_USAGE,
    BATTERY_LEVEL,
    POWER_DRAW,
    BATTERY_RUNTIME,
    BATTERY_TEMPERATURE,
    CLOCK_TIME,
    CPU_TEMPERATURE,
    GPU_TEMPERATURE,
    FRAME_RATE_GRAPH,
    CPU_USAGE_GRAPH,
    GPU_USAGE_GRAPH,
}

object GameFoldPcDefaults {
    const val DEFAULTS_VERSION = "4"
    const val GLOBAL_SCREEN_SIZE_DEFAULTS_VERSION = "2"
    const val CONTAINER_VARIANT = "bionic"
    const val WINE_VERSION = "proton-10.0-arm64ec-2"
    const val GAME_LANGUAGE = "schinese"
    const val CONTAINER_SCREEN_SIZE = "2304x1440"
    const val CONTAINER_LSFG_ENABLED = true
    const val CONTAINER_SHOW_FPS = true
    const val PERFORMANCE_HUD_ENABLED = true
    const val LSFG_FLOW_SCALE = 0.50f
    const val LSFG_FLOW_SCALE_VALUE = "0.50"
    const val SCREEN_EFFECT_SCALING_MODE = ScreenEffectsConfig.SCALING_MODE_FSR_ASPECT

    const val USE_STEAM_INPUT = false
    const val XINPUT_ENABLED = true
    const val DINPUT_ENABLED = true
    const val DINPUT_MAPPER_TYPE: Byte = 1
    const val EXTERNAL_DISPLAY_SWAP = true
    val CONTROLLER_INPUT_TYPE = PreferredInputApi.BOTH.ordinal

    const val WARN_BEFORE_EXIT = true
    const val SHOW_IN_GAME_QUICK_MENU = false
    const val BOOT_SPLASH_BRAND = "GameFold"
    const val SHOW_BOOT_SPLASH_FIXED_TIPS = false
    const val SHOW_ICON_STYLE_PICKER = false

    val DEFAULT_PERFORMANCE_HUD_METRICS: Set<PerformanceHudMetric> =
        setOf(PerformanceHudMetric.FRAME_RATE)

    fun migrateGlobalScreenSize(
        currentScreenSize: String?,
        appliedVersion: String?,
    ): String? {
        if (appliedVersion == GLOBAL_SCREEN_SIZE_DEFAULTS_VERSION) return null

        return migrateContainerScreenSize(currentScreenSize)
    }

    fun migrateContainerScreenSize(currentScreenSize: String?): String =
        if (
            currentScreenSize.isNullOrBlank() ||
            currentScreenSize == "1280x720" ||
            currentScreenSize == "1067x600"
        ) {
            CONTAINER_SCREEN_SIZE
        } else {
            currentScreenSize
        }

    fun localBionicWineOptions(resourceOptions: List<String>): List<String> =
        (listOf(WINE_VERSION) + resourceOptions).distinct()

    fun needsPerGameDefaults(appliedVersion: String?): Boolean =
        appliedVersion != DEFAULTS_VERSION
}

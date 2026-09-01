package app.gamenative.product

import app.gamenative.ui.util.ScreenEffectsConfig
import app.gamenative.utils.LsfgVkManager
import com.winlator.container.Container

object GameFoldPcPerGameDefaults {
    private const val DEFAULT_LOCALE = "zh_CN.UTF-8"
    private const val VERSION_EXTRA = "gamefoldPcDefaults"

    fun applyTo(container: Container): Boolean {
        val appliedVersion = container.getExtra(VERSION_EXTRA, "")
        if (!GameFoldPcDefaults.needsPerGameDefaults(appliedVersion)) return false

        val appliedVersionNumber = appliedVersion.toIntOrNull() ?: 0
        container.screenSize = GameFoldPcDefaults.migrateContainerScreenSize(container.screenSize)
        if (appliedVersionNumber < 2) {
            container.isShowFPS = GameFoldPcDefaults.CONTAINER_SHOW_FPS
            container.putExtra(
                LsfgVkManager.EXTRA_ARMED,
                GameFoldPcDefaults.CONTAINER_LSFG_ENABLED.toString(),
            )
            container.language = GameFoldPcDefaults.GAME_LANGUAGE
            container.setLC_ALL(DEFAULT_LOCALE)
        }

        val flowScale = container.getExtra(LsfgVkManager.EXTRA_FLOW_SCALE)
            ?.toFloatOrNull()
        if (flowScale == null || flowScale == 0.80f) {
            container.putExtra(
                LsfgVkManager.EXTRA_FLOW_SCALE,
                GameFoldPcDefaults.LSFG_FLOW_SCALE_VALUE,
            )
        }

        val scalingMode = container.getExtra(ScreenEffectsConfig.KEY_SCALING_MODE)
            ?.toIntOrNull()
        if (scalingMode == null || scalingMode == ScreenEffectsConfig.SCALING_MODE_NONE) {
            container.putExtra(
                ScreenEffectsConfig.KEY_SCALING_MODE,
                GameFoldPcDefaults.SCREEN_EFFECT_SCALING_MODE,
            )
        }

        container.putExtra("useSteamInput", GameFoldPcDefaults.USE_STEAM_INPUT.toString())
        container.inputType = GameFoldPcDefaults.CONTROLLER_INPUT_TYPE
        container.dinputMapperType = GameFoldPcDefaults.DINPUT_MAPPER_TYPE
        container.putExtra(VERSION_EXTRA, GameFoldPcDefaults.DEFAULTS_VERSION)
        return true
    }
}

package app.gamenative.ui.data

import app.gamenative.enums.AppTheme
import app.gamenative.ui.enums.ConnectionState
import app.gamenative.ui.screen.PluviaScreen
import com.materialkolor.PaletteStyle

data class MainState(
    val appTheme: AppTheme = AppTheme.NIGHT,
    val paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    val resettedScreen: PluviaScreen? = null,
    val currentScreen: PluviaScreen? = PluviaScreen.LoginUser,
    val hasLaunched: Boolean = false,
    val loadingDialogVisible: Boolean = false,
    val loadingDialogProgress: Float = 0F,
    // The PC shell renders this value inside the NS-style loading card. Keep a useful
    // stage description even during the first frame before the launch pipeline reports one.
    val loadingDialogMessage: String = "正在准备运行环境",
    val annoyingDialogShown: Boolean = false,
    val hasCrashedLastStart: Boolean = false,
    val isSteamConnected: Boolean = false,
    val launchedAppId: String = "",
    val bootToContainer: Boolean = false,
    val testGraphics: Boolean = false,
    val diagnostics: Boolean = false,
    val showBootingSplash: Boolean = false,
    val bootingSplashText: String = "正在准备运行环境",
    val bootingSplashProgress: Float = -1f,
    val bootingSplashHeroImageUrl: String = "",
    val bootingSplashTitle: String = "GamePad PC",

    // Connection state for background reconnection
    // Default to DISCONNECTED - service will start and set to CONNECTING
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val connectionMessage: String? = null,
    val connectionTimeoutSeconds: Int = 0,
)

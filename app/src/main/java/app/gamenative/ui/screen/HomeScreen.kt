package app.gamenative.ui.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import app.gamenative.BuildConfig
import app.gamenative.ui.screen.library.OfflineLibraryScreen
import app.gamenative.ui.components.GameFoldRetroLoadingCard
import app.gamenative.ui.theme.PluviaTheme
import app.gamenative.gamefold.ui.GameFoldPcShellHost
import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldControllerListener
import app.gamenative.gamefold.ui.GameFoldPcFrontendPolicy
import app.gamenative.gamefold.ui.GameFoldStick

@Composable
fun HomeScreen(
    onChat: (Long) -> Unit,
    onClickExit: () -> Unit,
    onClickPlay: (String, Boolean) -> Unit,
    onTestGraphics: (String) -> Unit,
    onPlayWithDiagnostics: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    onGoOnline: () -> Unit,
    onCancelLaunch: () -> Unit = {},
    isOffline: Boolean = false,
    isSteamConnected: Boolean = false,
    loadingVisible: Boolean = false,
    loadingProgress: Float = -1f,
    loadingMessage: String = "正在准备运行环境",
    bootingVisible: Boolean = false,
    bootingProgress: Float = -1f,
    bootingMessage: String = "正在准备运行环境",
    bootingArtworkUrl: String = "",
    bootingTitle: String = "GamePad PC",
) {
    var otherControllerModalVisible by remember { androidx.compose.runtime.mutableStateOf(false) }
    var gameDetailsVisible by remember { androidx.compose.runtime.mutableStateOf(false) }
    var controllerBackRequests by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var controllerNavigationRequests by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var controllerAnyButtonRequests by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var controllerNavigationButton by remember { androidx.compose.runtime.mutableStateOf<GameFoldButton?>(null) }
    val dismissibleControllerModalVisible = otherControllerModalVisible
    val launchPreparing = GameFoldPcFrontendPolicy.showHomeLaunchPreparingSurface(
        localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
        loadingVisible = loadingVisible,
        bootingVisible = bootingVisible,
    )
    val homeControllerListener = remember(
        onClickExit,
        onCancelLaunch,
        gameDetailsVisible,
        dismissibleControllerModalVisible,
        launchPreparing,
    ) {
        object : GameFoldControllerListener {
            // The library owns explicit semantic selection for Home. A game details page,
            // however, is the real settings form, so its A/X and DPAD events must reach the
            // focused Compose control instead of being consumed by the library rail.
            override fun dispatchConfirmToCompose(): Boolean = gameDetailsVisible

            override fun dispatchNavigationToCompose(): Boolean = gameDetailsVisible

            override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
                if (pressed) {
                    if (
                        GameFoldPcFrontendPolicy.homeLaunchControlAction(
                            localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                            launchPreparing = launchPreparing,
                            button = button,
                        ) == GameFoldPcFrontendPolicy.HomeLaunchControlAction.CANCEL_LAUNCH
                    ) {
                        onCancelLaunch()
                        return
                    }
                    controllerAnyButtonRequests++
                    // Add Game consumes the whole physical button event so A/X is not replayed
                    // into the Home navigation rail while the dialog closes.
                    if (dismissibleControllerModalVisible) return
                    when (button) {
                        GameFoldButton.DPAD_UP,
                        GameFoldButton.DPAD_RIGHT,
                        GameFoldButton.DPAD_DOWN,
                        GameFoldButton.DPAD_LEFT,
                        GameFoldButton.A,
                        GameFoldButton.X,
                        -> {
                            controllerNavigationButton = button
                            controllerNavigationRequests++
                        }
                        GameFoldButton.B,
                        GameFoldButton.Y,
                        -> {
                            if (gameDetailsVisible) {
                                controllerBackRequests++
                            }
                        }
                        // NS treats HOME as "go to home". It is intentionally a no-op when the
                        // user is already on Home, and it only closes a nested Home surface.
                        GameFoldButton.HOME -> if (gameDetailsVisible) {
                            controllerBackRequests++
                        }
                        else -> Unit
                    }
                }
            }

            override fun onStickChanged(stick: GameFoldStick, x: Float, y: Float) = Unit
        }
    }
    GameFoldPcShellHost(controllerListener = homeControllerListener) {
        Box(Modifier.fillMaxSize()) {
            if (launchPreparing) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                ) {
                    GameFoldRetroLoadingCard(
                        text = if (loadingVisible) loadingMessage else bootingMessage,
                        progress = if (loadingVisible) loadingProgress else bootingProgress,
                        heroImageUrl = bootingArtworkUrl,
                        title = bootingTitle,
                    )
                }
            } else OfflineLibraryScreen(
                onClickPlay = onClickPlay,
                onTestGraphics = onTestGraphics,
                onPlayWithDiagnostics = onPlayWithDiagnostics,
                onClickExit = onClickExit,
                onNavigateRoute = onNavigateRoute,
                onControllerModalVisibilityChanged = { otherControllerModalVisible = it },
                onGameDetailsVisibilityChanged = { gameDetailsVisible = it },
                controllerBackRequests = controllerBackRequests,
                controllerNavigationRequests = controllerNavigationRequests,
                controllerNavigationButton = controllerNavigationButton,
                controllerAnyButtonRequests = controllerAnyButtonRequests,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=1920px,dpi=440,orientation=landscape",
)
@Composable
private fun Preview_HomeScreenContent() {
    PluviaTheme {
        HomeScreen(
            onChat = {},
            onClickPlay = { _, _ -> },
            onTestGraphics = { },
            onPlayWithDiagnostics = { },
            onLogout = {},
            onNavigateRoute = {},
            onClickExit = {},
            onGoOnline = {},
        )
    }
}

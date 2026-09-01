package app.gamenative.ui.model

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gamenative.PluviaApp
import app.gamenative.PrefManager
import app.gamenative.BuildConfig
import app.gamenative.data.GameSource
import app.gamenative.data.LibraryPlayHistory
import app.gamenative.db.dao.LibraryPlayHistoryDao
import app.gamenative.di.IAppTheme
import app.gamenative.enums.AppTheme
import app.gamenative.enums.LoginResult
import app.gamenative.events.AndroidEvent
import app.gamenative.ui.enums.Orientation
import java.util.EnumSet
import app.gamenative.service.ActiveGameRegistry
import app.gamenative.utils.CustomGameScanner
import app.gamenative.ui.data.MainState
import app.gamenative.ui.enums.ConnectionState
import app.gamenative.ui.screen.PluviaScreen
import app.gamenative.utils.ContainerUtils
import app.gamenative.utils.IntentLaunchManager
import app.gamenative.utils.UpdateInfo
import app.gamenative.gamefold.ui.GameFoldPcFrontendPolicy
import app.gamenative.gamefold.startup.GameFoldPcPreLaunchCoordinator
import app.gamenative.gamefold.startup.GameFoldPcPreLaunchTicket
import com.materialkolor.PaletteStyle
import com.winlator.xserver.Window
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.io.path.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val appTheme: IAppTheme,
    private val libraryPlayHistoryDao: LibraryPlayHistoryDao,
) : ViewModel() {

    companion object {
        private const val KEY_CURRENT_SCREEN_ROUTE = "current_screen_route"
    }

    sealed class MainUiEvent {
        data object OnBackPressed : MainUiEvent()
        data object OnLoggedOut : MainUiEvent()
        data object LaunchApp : MainUiEvent()
        data class ExternalGameLaunch(val appId: String) : MainUiEvent()
        data class OnLogonEnded(val result: LoginResult) : MainUiEvent()
        data class SteamDisconnected(val isTerminal: Boolean) : MainUiEvent()
        data object ShowDiscordSupportDialog : MainUiEvent()
        data class ShowGameFeedbackDialog(val appId: String) : MainUiEvent()
        data object ServiceReady : MainUiEvent()
    }

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _uiEvent = Channel<MainUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _offline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> get() = _offline

    fun setOffline(value: Boolean) {
        _offline.value = value
    }

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    fun setUpdateInfo(info: UpdateInfo?) {
        _updateInfo.value = info
    }

    private val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
        viewModelScope.launch {
            _uiEvent.send(MainUiEvent.OnBackPressed)
        }
    }

    private val onExternalGameLaunch: (AndroidEvent.ExternalGameLaunch) -> Unit = {
        Timber.tag("MainViewModel").i("Received external game launch event for app ${it.appId}")
        viewModelScope.launch {
            Timber.tag("MainViewModel").i("Sending ExternalGameLaunch UI event for app ${it.appId}")
            _uiEvent.send(MainUiEvent.ExternalGameLaunch(it.appId))
        }
    }

    private val onSetBootingSplashText: (AndroidEvent.SetBootingSplashText) -> Unit = bootProgress@{
        if (
            !GameFoldPcFrontendPolicy.shouldApplyBootingUpdate(
                localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                pausedAtHome = PluviaApp.gamePausedAtHome,
            )
        ) {
            Timber.tag("MainViewModel").d("Ignoring late boot progress while game is paused at Home")
            return@bootProgress
        }
        setBootingSplashText(
            if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
                GameFoldPcFrontendPolicy.localizeLoadingText(it.text)
            } else {
                it.text
            },
        )
        setShowBootingSplash(true)
    }

    private var bootingSplashTimeoutJob: Job? = null
    private var connectionTimeoutJob: Job? = null

    init {
        // Restore persisted screen from SavedStateHandle if available
        val persistedRoute = savedStateHandle.get<String>(KEY_CURRENT_SCREEN_ROUTE)
        val restoredScreen = when (persistedRoute) {
            PluviaScreen.Home.route -> PluviaScreen.Home
            PluviaScreen.XServer.route -> PluviaScreen.XServer
            PluviaScreen.Settings.route -> PluviaScreen.Settings
            PluviaScreen.Chat.route -> PluviaScreen.Chat
            else -> null
        }

        _state.update {
            it.copy(
                isSteamConnected = false,
                hasCrashedLastStart = PrefManager.recentlyCrashed,
                launchedAppId = "",
                currentScreen = restoredScreen,
                connectionState = ConnectionState.OFFLINE_MODE,
            )
        }

        // Register event handlers
        PluviaApp.events.on<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.on<AndroidEvent.ExternalGameLaunch, Unit>(onExternalGameLaunch)
        PluviaApp.events.on<AndroidEvent.SetBootingSplashText, Unit>(onSetBootingSplashText)

        // Collect theme preferences
        viewModelScope.launch {
            appTheme.themeFlow.collect { value ->
                _state.update { it.copy(appTheme = value) }
            }
        }

        viewModelScope.launch {
            appTheme.paletteFlow.collect { value ->
                _state.update { it.copy(paletteStyle = value) }
            }
        }
    }

    override fun onCleared() {
        PluviaApp.events.off<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.off<AndroidEvent.ExternalGameLaunch, Unit>(onExternalGameLaunch)
        PluviaApp.events.off<AndroidEvent.SetBootingSplashText, Unit>(onSetBootingSplashText)
        connectionTimeoutJob?.cancel()
    }

    fun setTheme(value: AppTheme) {
        appTheme.currentTheme = value
    }

    fun setPalette(value: PaletteStyle) {
        appTheme.currentPalette = value
    }

    fun setAnnoyingDialogShown(value: Boolean) {
        _state.update { it.copy(annoyingDialogShown = value) }
    }

    fun setLoadingDialogVisible(value: Boolean) {
        _state.update { it.copy(loadingDialogVisible = value) }
    }

    fun setLoadingDialogProgress(value: Float) {
        _state.update {
            it.copy(
                loadingDialogProgress = value,
                // The local PC shell renders one card owned by HomeScreen. Keep the
                // pre-launch progress on that same card instead of leaving the booting
                // layer indeterminate while the hidden legacy loading state advances.
                bootingSplashProgress = if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL && it.showBootingSplash) {
                    value
                } else {
                    it.bootingSplashProgress
                },
            )
        }
    }

    fun setLoadingDialogMessage(value: String) {
        _state.update {
            it.copy(
                loadingDialogMessage = value,
                // Use the same stage source for the visible NS loading card.
                bootingSplashText = if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL && it.showBootingSplash) {
                    value
                } else {
                    it.bootingSplashText
                },
            )
        }
    }

    fun setHasLaunched(value: Boolean) {
        _state.update { it.copy(hasLaunched = value) }
    }

    fun setShowBootingSplash(value: Boolean) {
        _state.update {
            it.copy(
                showBootingSplash = value,
                bootingSplashProgress = if (value) it.bootingSplashProgress else -1f,
            )
        }
    }

    fun setBootingSplashText(value: String) {
        _state.update {
            it.copy(
                bootingSplashText = value,
                loadingDialogMessage = if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL && it.showBootingSplash) {
                    value
                } else {
                    it.loadingDialogMessage
                },
            )
        }
    }

    fun setBootingSplashProgress(value: Float) {
        _state.update { it.copy(bootingSplashProgress = value) }
    }

    fun setBootingSplashHeroImageUrl(url: String) {
        _state.update { it.copy(bootingSplashHeroImageUrl = url) }
    }

    fun setBootingSplashTitle(title: String) {
        _state.update { it.copy(bootingSplashTitle = title.ifBlank { "GamePad PC" }) }
    }

    /**
     * Publishes the selected game's NS-style loading identity before the pre-launch pipeline
     * starts installing/validating runtime files.  This keeps the permission/resource stage and
     * the actual container stage as one visual card instead of briefly showing a generic PC icon.
     */
    fun prepareBootingSplash(context: Context, appId: String) {
        setShowBootingSplash(true)
        setBootingSplashTitle(ContainerUtils.resolveGameName(appId))
        setBootingSplashText("正在准备运行环境")
        setBootingSplashProgress(-1f)
        setBootingSplashHeroImageUrl("")
        viewModelScope.launch(Dispatchers.IO) {
            val heroUrl = runCatching {
                val folderPath = CustomGameScanner.getFolderPathFromAppId(appId)
                    ?: return@runCatching ""
                val folder = java.io.File(folderPath)
                CustomGameScanner.findCapsuleCoverForCustomGame(appId)
                    ?: CustomGameScanner.findLocalArtworkForCustomGame(context, appId)
                    ?: folder.listFiles()?.firstOrNull { file ->
                        file.isFile && file.name.startsWith("steamgriddb_hero", ignoreCase = true) &&
                            !file.name.contains("grid_", ignoreCase = true) &&
                            file.extension.lowercase() in setOf("png", "jpg", "jpeg", "webp")
                    }?.let { android.net.Uri.fromFile(it).toString() }
                    ?: ""
            }.getOrDefault("")
            setBootingSplashHeroImageUrl(heroUrl)
        }
    }

    // Connection state management

    /**
     * Called when starting a reconnection attempt.
     * Sets state to CONNECTING and starts a timeout counter.
     */
    fun startConnecting(message: String? = null) {
        connectionTimeoutJob?.cancel()
        _state.update {
            it.copy(
                connectionState = ConnectionState.CONNECTING,
                connectionMessage = message,
                connectionTimeoutSeconds = 0,
            )
        }

        // Start timeout counter
        connectionTimeoutJob = viewModelScope.launch {
            var seconds = 0
            while (seconds < 30 && _state.value.connectionState == ConnectionState.CONNECTING) {
                delay(1000)
                seconds++
                _state.update { it.copy(connectionTimeoutSeconds = seconds) }
            }
        }
    }

    /**
     * Called when user chooses to continue in offline mode.
     * Stops reconnection attempts and allows app to function offline.
     */
    fun continueOffline() {
        connectionTimeoutJob?.cancel()
        _state.update {
            it.copy(
                connectionState = ConnectionState.OFFLINE_MODE,
                connectionMessage = null,
                connectionTimeoutSeconds = 0,
            )
        }
    }

    /**
     * Called when user wants to retry connection.
     * Resets offline mode and triggers reconnection.
     */
    fun retryConnection() {
        if (_state.value.connectionState == ConnectionState.OFFLINE_MODE ||
            _state.value.connectionState == ConnectionState.DISCONNECTED
        ) {
            startConnecting()
        }
    }

    fun setCurrentScreen(currentScreen: String?) {
        // Route matching accounts for query params and path params in templates
        // e.g., "home?offline={offline}" should match Home, "chat/{id}" should match Chat
        val screen = when {
            currentScreen == null -> PluviaScreen.LoginUser
            currentScreen == PluviaScreen.LoginUser.route -> PluviaScreen.LoginUser
            currentScreen.startsWith(PluviaScreen.Home.route) -> PluviaScreen.Home
            currentScreen == PluviaScreen.XServer.route -> PluviaScreen.XServer
            currentScreen == PluviaScreen.Settings.route -> PluviaScreen.Settings
            currentScreen.startsWith("chat") -> PluviaScreen.Chat
            else -> PluviaScreen.LoginUser
        }

        setCurrentScreen(screen)
    }

    fun setCurrentScreen(value: PluviaScreen) {
        _state.update { it.copy(currentScreen = value) }
        savedStateHandle[KEY_CURRENT_SCREEN_ROUTE] = value.route
    }

    /**
     * Gets the persisted route from SavedStateHandle
     *
     * Returns the route the user was on before process death, or null if:
     * - No route was persisted
     * - The persisted route is LoginUser (not meaningful to restore)
     * - The persisted route is XServer (game session is gone after process death)
     * - The persisted route is Chat (dynamic IDs require special handling)
     *
     * Navigation decisions should be made by the caller based on the current
     * NavController destination, not by tracking internal flags.
     *
     * TODO: reconsider this approach when merging GOG and Epic
     */
    fun getPersistedRoute(): String? {
        val persistedRoute = savedStateHandle.get<String>(KEY_CURRENT_SCREEN_ROUTE)
        return when {
            persistedRoute == null -> null
            persistedRoute == PluviaScreen.LoginUser.route -> null
            persistedRoute == PluviaScreen.XServer.route -> null
            persistedRoute.startsWith("chat") -> null
            else -> persistedRoute
        }
    }

    fun clearPersistedRoute() {
        savedStateHandle[KEY_CURRENT_SCREEN_ROUTE] = PluviaScreen.LoginUser.route
    }

    fun setHasCrashedLastStart(value: Boolean) {
        if (value.not()) {
            PrefManager.recentlyCrashed = false
        }
        _state.update { it.copy(hasCrashedLastStart = value) }
    }

    fun setScreen() {
        _state.update { it.copy(resettedScreen = it.currentScreen) }
    }

    fun setLaunchedAppId(value: String) {
        _state.update { it.copy(launchedAppId = value) }
    }

    fun setBootToContainer(value: Boolean) {
        _state.update { it.copy(bootToContainer = value) }
    }

    fun setTestGraphics(value: Boolean) {
        _state.update { it.copy(testGraphics = value) }
    }

    fun setDiagnostics(value: Boolean) {
        _state.update { it.copy(diagnostics = value) }
    }

    fun launchApp(
        context: Context,
        appId: String,
        preLaunchTicket: GameFoldPcPreLaunchTicket?,
    ) {
        // Show booting splash before launching the app
        viewModelScope.launch {
            try {
                if (preLaunchTicket != null && !GameFoldPcPreLaunchCoordinator.singleFlight.isActive(preLaunchTicket)) {
                    return@launch
                }
                viewModelScope.launch(Dispatchers.IO) {
                    libraryPlayHistoryDao.upsert(
                        LibraryPlayHistory(
                            appId = appId,
                            lastPlayed = System.currentTimeMillis(),
                        ),
                    )
                }

                prepareBootingSplash(context, appId)
                PluviaApp.events.emit(AndroidEvent.SetAllowedOrientation(PrefManager.allowedOrientation))

                val heroUrl = withContext(Dispatchers.IO) {
                    val folderPath = CustomGameScanner.getFolderPathFromAppId(appId) ?: return@withContext ""
                    val folder = java.io.File(folderPath)
                    // The NS loading card uses the square cartridge cover. Keep the old hero
                    // fallback for games without a local capsule/artwork asset.
                    CustomGameScanner.findCapsuleCoverForCustomGame(appId)
                        ?: CustomGameScanner.findLocalArtworkForCustomGame(context, appId)
                        ?: folder.listFiles()?.firstOrNull { file ->
                            file.isFile &&
                                file.name.startsWith("steamgriddb_hero", ignoreCase = true) &&
                                !file.name.contains("grid_", ignoreCase = true) &&
                                (file.name.endsWith(".png", ignoreCase = true) ||
                                    file.name.endsWith(".jpg", ignoreCase = true) ||
                                    file.name.endsWith(".webp", ignoreCase = true))
                        }?.let { android.net.Uri.fromFile(it).toString() }
                        ?: ""
                }
                if (preLaunchTicket != null && !GameFoldPcPreLaunchCoordinator.singleFlight.isActive(preLaunchTicket)) {
                    return@launch
                }
                setBootingSplashHeroImageUrl(heroUrl)

                // Small delay to ensure the splash screen is visible before proceeding
                delay(100)

                if (preLaunchTicket != null && !GameFoldPcPreLaunchCoordinator.singleFlight.isActive(preLaunchTicket)) {
                    return@launch
                }
                _uiEvent.send(MainUiEvent.LaunchApp)
            } finally {
                preLaunchTicket?.let(GameFoldPcPreLaunchCoordinator.singleFlight::finish)
            }
        }
    }

    fun exitSteamApp(context: Context, appId: String, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                Timber.tag("Exit").i("Exiting, getting feedback for appId: $appId")
                bootingSplashTimeoutJob?.cancel()
                bootingSplashTimeoutJob = null
                setShowBootingSplash(false)
                // Check if we have a temporary override before doing anything
                val hadTemporaryOverride = IntentLaunchManager.hasTemporaryOverride(appId)

                val gameId = ContainerUtils.extractGameIdFromContainerId(appId)
                ActiveGameRegistry.clearIfMatches(gameId)

                // Prompt user to save temporary container configuration if one was applied
                if (hadTemporaryOverride) {
                    PluviaApp.events.emit(AndroidEvent.PromptSaveContainerConfig(appId))
                    // Dialog handler in PluviaMain manages the save/discard logic
                }

                // After app closes, check if we need to show the feedback dialog
                // Show feedback if: first time running this game OR config was changed
                try {
                    // Show feedback for all stores except custom games.
                    val feedbackGameSource = ContainerUtils.extractGameSourceFromContainerId(appId)
                    if (feedbackGameSource != GameSource.CUSTOM_GAME) {
                        val container = ContainerUtils.getContainer(context, appId)

                        val shown = container.getExtra("discord_support_prompt_shown", "false") == "true"
                        val configChanged = container.getExtra("config_changed", "false") == "true"
                        if (!shown) {
                            container.putExtra("discord_support_prompt_shown", "true")
                            container.saveData()
                            _uiEvent.send(MainUiEvent.ShowGameFeedbackDialog(appId))
                        }

                        // Only show feedback if container config was changed before this game run
                        if (configChanged) {
                            // Clear the flag
                            container.putExtra("config_changed", "false")
                            container.saveData()
                            // Show the feedback dialog
                            _uiEvent.send(MainUiEvent.ShowGameFeedbackDialog(appId))
                        }
                    } else {
                        Timber.d("Custom game detected, not showing feedback")
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to check/update feedback dialog state for $appId")
                }
            } finally {
                onComplete?.invoke()
            }
        }
    }

    fun onWindowMapped(context: Context, window: Window, appId: String) {
        viewModelScope.launch {
            // Hide the booting splash when a window is mapped
            bootingSplashTimeoutJob?.cancel()
            bootingSplashTimeoutJob = null
            setShowBootingSplash(false)

            Timber.tag("MainViewModel").d("Local game window mapped: ${window.className} ($appId)")
        }
    }

    fun onGameLaunchError(error: String) {
        viewModelScope.launch {
            // Hide the splash screen if it's still showing
            bootingSplashTimeoutJob?.cancel()
            bootingSplashTimeoutJob = null
            setShowBootingSplash(false)

            // You could also show an error dialog here if needed
            Timber.tag("MainViewModel").e("Game launch error: $error")
        }
    }

}

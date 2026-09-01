package app.gamenative.ui

import android.content.Context
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.gamenative.BuildConfig
import app.gamenative.Constants
import app.gamenative.MainActivity
import app.gamenative.NetworkMonitor
import app.gamenative.PluviaApp
import app.gamenative.PrefManager
import app.gamenative.R
import app.gamenative.data.GameSource
import app.gamenative.enums.AppTheme
import app.gamenative.enums.PathType
import app.gamenative.enums.SaveLocation
import app.gamenative.enums.SyncResult
import app.gamenative.events.AndroidEvent
import app.gamenative.product.Cyberpunk2077SaveAutoImporter
import app.gamenative.product.Cyberpunk2077SaveImportLaunchPolicy
import app.gamenative.product.ExitConfirmationPolicy
import app.gamenative.product.Gta5SaveAutoImporter
import app.gamenative.product.Gta5SaveImportLaunchPolicy
import app.gamenative.product.ProductModePolicy
import app.gamenative.product.LocalRuntimeBootstrapPolicy
import app.gamenative.service.ActiveGameRegistry
import app.gamenative.service.SteamService
import app.gamenative.service.amazon.AmazonService
import app.gamenative.ui.component.AchievementOverlay
import app.gamenative.service.epic.EpicService
import app.gamenative.service.gog.GOGService
import app.gamenative.ui.component.dialog.ContainerConfigDialog
import app.gamenative.ui.component.dialog.GameFeedbackDialog
import app.gamenative.ui.component.dialog.LoadingDialog
import app.gamenative.ui.component.dialog.MessageDialog
import app.gamenative.ui.component.dialog.state.GameFeedbackDialogState
import app.gamenative.ui.component.dialog.state.MessageDialogState
import app.gamenative.ui.components.BootingSplash
import app.gamenative.gamefold.ui.GameFoldPcShellHost
import app.gamenative.gamefold.session.GameFoldSessionLaunchAction
import app.gamenative.gamefold.session.GameFoldSessionPolicy
import app.gamenative.gamefold.session.GameFoldRootBackAction
import app.gamenative.gamefold.startup.GameFoldPcPreLaunchCoordinator
import app.gamenative.gamefold.startup.GameFoldPcPreLaunchTicket
import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldControllerListener
import app.gamenative.gamefold.ui.GameFoldStick
import app.gamenative.gamefold.ui.GameFoldPcFrontendPolicy
import app.gamenative.ui.enums.AppOptionMenuType
import app.gamenative.launch.LaunchReadiness
import app.gamenative.ui.enums.DialogType
import app.gamenative.ui.enums.Orientation
import app.gamenative.ui.model.MainViewModel
import app.gamenative.ui.screen.HomeScreen
import app.gamenative.ui.screen.PluviaScreen
import app.gamenative.ui.screen.settings.SettingsScreen
import app.gamenative.ui.screen.xserver.XServerScreen
import app.gamenative.ui.theme.PluviaTheme
import app.gamenative.ui.util.LocalSnackbarHostController
import app.gamenative.ui.util.SnackbarManager
import app.gamenative.utils.BestConfigService
import app.gamenative.utils.ContainerUtils
import app.gamenative.utils.CustomGameScanner
import app.gamenative.utils.ManifestInstaller
import app.gamenative.utils.GameFoldPcResourceRoot
import app.gamenative.utils.GameFeedbackUtils
import app.gamenative.utils.IntentLaunchManager
import app.gamenative.utils.SteamUtils
import app.gamenative.utils.UpdateChecker
import app.gamenative.utils.UpdateInfo
import app.gamenative.utils.UpdateInstaller
import app.gamenative.utils.LaunchDependencies
import app.gamenative.workshop.WorkshopManager
import app.gamenative.workshop.compatibility.SlayTheSpireModTheSpireCompatibility
import com.google.android.play.core.splitcompat.SplitCompat
import com.winlator.container.Container
import com.winlator.container.ContainerData
import com.winlator.container.ContainerManager
import com.winlator.core.StringUtils
import com.winlator.core.TarCompressorUtils
import com.winlator.xenvironment.ImageFSLegacyMigrator
import com.winlator.xenvironment.ImageFs
import com.winlator.xenvironment.ImageFsInstaller
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientObjects.ECloudPendingRemoteOperation
import java.io.File
import java.util.Locale
import java.util.Date
import java.util.EnumSet
import kotlin.reflect.KFunction3
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

private const val PENDING_LAUNCH_TIMEOUT_MS = 10_000L
private const val SNACKBAR_SHOW_TIMEOUT_MS = 15_000L

/** Used to suspend preLaunchApp while the user decides on large workshop updates. */
private var workshopUpdateDeferred: CompletableDeferred<Boolean>? = null

private sealed class GameResolutionResult {
    data class Success(
        val finalAppId: String,
        val gameId: Int,
        val isSteamInstalled: Boolean,
        val isCustomGame: Boolean,
    ) : GameResolutionResult()
    data class NotFound(
        val gameId: Int,
        val originalAppId: String,
    ) : GameResolutionResult()
}

private fun resolveGameAppId(context: Context, appId: String): GameResolutionResult {
    val gameSource = ContainerUtils.extractGameSourceFromContainerId(appId)
    val gameId = ContainerUtils.extractGameIdFromContainerId(appId)
    val isInstalled = gameSource == GameSource.CUSTOM_GAME && CustomGameScanner.isGameInstalled(gameId)

    if (!isInstalled) {
        return GameResolutionResult.NotFound(
            gameId = gameId,
            originalAppId = appId,
        )
    }

    val isCustomGame = gameSource == GameSource.CUSTOM_GAME

    return GameResolutionResult.Success(
        finalAppId = appId,
        gameId = gameId,
        isSteamInstalled = false,
        isCustomGame = isCustomGame,
    )
}


/** Check if launch should be deferred — Steam needs login, GOG/Epic/Amazon need service startup */
private fun needsDeferLaunch(context: Context, appId: String): Boolean {
    return false
}

/** Show snackbar for a deferred launch based on the game's source. Returns true if shown. */
private fun showDeferredLaunchSnackbar(context: Context, appId: String): Boolean {
    return false
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PluviaMain(
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    val context = LocalContext.current
    val productCapabilities = remember {
        ProductModePolicy.capabilities(BuildConfig.GAMEFOLD_PC_LOCAL_SHELL)
    }
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()

    var msgDialogState by rememberSaveable(stateSaver = MessageDialogState.Saver) {
        mutableStateOf(MessageDialogState(false))
    }
    val setMessageDialogState: (MessageDialogState) -> Unit = { msgDialogState = it }

    var gameFeedbackState by rememberSaveable(stateSaver = GameFeedbackDialogState.Saver) {
        mutableStateOf(GameFeedbackDialogState(false))
    }

    var hasBack by rememberSaveable { mutableStateOf(navController.previousBackStackEntry?.destination?.route != null) }

    var isConnecting by rememberSaveable { mutableStateOf(false) }
    var shownPendingLaunchSnackbar by rememberSaveable { mutableStateOf(false) }
    // incremented each time a launch is deferred, so the timeout restarts per request
    var pendingLaunchGeneration by rememberSaveable { mutableIntStateOf(0) }

    var gameBackAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val cancelPendingLocalLaunch: () -> Unit = {
        val appId = state.launchedAppId
        val cancelled = if (appId.isNotBlank()) {
            GameFoldPcPreLaunchCoordinator.singleFlight.cancel(appId)
        } else {
            false
        }
        viewModel.setLoadingDialogVisible(false)
        viewModel.setShowBootingSplash(false)
        Timber.i(
            "[GameFoldLaunch] Returned Home during startup appId=%s transactionCancelled=%s",
            appId,
            cancelled,
        )
    }

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    var openContainerConfigForAppId by rememberSaveable { mutableStateOf<String?>(null) }

    // Check for updates on app start
    LaunchedEffect(Unit) {
        if (BuildConfig.MODERN_ANDROID || !productCapabilities.onlineUpdates) return@LaunchedEffect
        val checkedUpdateInfo = UpdateChecker.checkForUpdate(context)
        if (checkedUpdateInfo != null) {
            val appVersionCode = BuildConfig.VERSION_CODE
            val serverVersionCode = checkedUpdateInfo.versionCode
            Timber.i("Update check: app versionCode=$appVersionCode, server versionCode=$serverVersionCode")
            if (appVersionCode < serverVersionCode) {
                updateInfo = checkedUpdateInfo
                viewModel.setUpdateInfo(checkedUpdateInfo)
            }
        }
    }

    // shared intent-launch path. resolves isOffline at the call site because intent launches can
    // arrive pre-login (cold-boot via stored creds) and downstream cloud-sync needs a settled answer.
    val launchIntentApp: (resolvedAppId: String, hasTemporaryOverride: Boolean) -> Unit = { resolvedAppId, hasTemporaryOverride ->
        if (PluviaApp.xEnvironment != null) {
            Timber.i("[PluviaMain]: Game $resolvedAppId already running; bringing XServer screen forward")
            if (navController.currentDestination?.route != PluviaScreen.XServer.route) {
                navController.navigate(PluviaScreen.XServer.route)
            }
        } else {
            MainActivity.wasLaunchedViaExternalIntent = true
            viewModel.setLaunchedAppId(resolvedAppId)
            viewModel.setBootToContainer(false)
            scope.launch(Dispatchers.IO) {
                val isOffline = true
                // sync viewModel — dialog retries + replaceSteamApi read isOffline.value
                viewModel.setOffline(isOffline)
                preLaunchApp(
                    context = context,
                    appId = resolvedAppId,
                    useTemporaryOverride = hasTemporaryOverride,
                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                    setMessageDialogState = setMessageDialogState,
                    onSuccess = viewModel::launchApp,
                    isOffline = isOffline,
                )
            }
        }
    }

    // process pending launch request from cold start (event bus has no replay)
    LaunchedEffect(Unit) {
        MainActivity.consumePendingLaunchRequest()?.let { launchRequest ->
            Timber.i("[PluviaMain]: Processing pending launch request for app ${launchRequest.appId}")
            // defer if service isn't ready yet — will be processed on ServiceReady/OnLogonEnded
            // consume+requeue is safe: both calls are non-suspending, so no other coroutine
            // can interleave between them on the main dispatcher (cooperative scheduling).
            if (needsDeferLaunch(context, launchRequest.appId)) {
                MainActivity.setPendingLaunchRequest(launchRequest)
                pendingLaunchGeneration++
                shownPendingLaunchSnackbar = showDeferredLaunchSnackbar(context, launchRequest.appId)
                return@let
            }
            when (val resolution = resolveGameAppId(context, launchRequest.appId)) {
                is GameResolutionResult.Success -> {
                    if (launchRequest.containerConfig != null) {
                        IntentLaunchManager.applyTemporaryConfigOverride(
                            context, launchRequest.appId, launchRequest.containerConfig,
                        )
                    }
                    launchIntentApp(resolution.finalAppId, launchRequest.containerConfig != null)
                }

                is GameResolutionResult.NotFound -> {
                    val appName = ContainerUtils.resolveGameName(resolution.originalAppId)
                    Timber.w("[PluviaMain]: Game not installed: $appName (${launchRequest.appId})")
                    msgDialogState = MessageDialogState(
                        visible = true,
                        type = DialogType.SYNC_FAIL,
                        title = context.getString(R.string.game_not_installed_title),
                        message = context.getString(R.string.game_not_installed_message, appName),
                        dismissBtnText = context.getString(R.string.ok),
                    )
                }
            }
        }
    }

    // timeout stale pending requests — if service never starts, don't hang indefinitely
    // keyed on generation so timeout restarts each time a new request is deferred
    LaunchedEffect(pendingLaunchGeneration) {
        if (pendingLaunchGeneration == 0) return@LaunchedEffect
        delay(PENDING_LAUNCH_TIMEOUT_MS)
        MainActivity.peekPendingLaunchRequest()?.let { request ->
            if (needsDeferLaunch(context, request.appId)) {
                Timber.tag("IntentLaunch").w("Pending launch timed out for ${request.appId}")
                MainActivity.consumePendingLaunchRequest()
                SnackbarManager.show(context.getString(R.string.intent_launch_service_timeout))
            }
        }
    }

    // shared handler for deferred intent launches (Steam login, GOG/Epic/Amazon service startup)
    val processPendingLaunch: (String) -> Unit = { reason ->
        MainActivity.consumePendingLaunchRequest()?.let { launchRequest ->
            Timber.tag("IntentLaunch")
                .i("Processing pending launch for ${launchRequest.appId} ($reason)")
            when (val resolution = resolveGameAppId(context, launchRequest.appId)) {
                is GameResolutionResult.NotFound -> {
                    val appName = ContainerUtils.resolveGameName(resolution.originalAppId)
                    Timber.tag("IntentLaunch").w("Game not installed: $appName (${launchRequest.appId})")
                    msgDialogState = MessageDialogState(
                        visible = true,
                        type = DialogType.SYNC_FAIL,
                        title = context.getString(R.string.game_not_installed_title),
                        message = context.getString(R.string.game_not_installed_message, appName),
                        dismissBtnText = context.getString(R.string.ok),
                    )
                }

                is GameResolutionResult.Success -> {
                    if (launchRequest.containerConfig != null) {
                        IntentLaunchManager.applyTemporaryConfigOverride(
                            context, launchRequest.appId, launchRequest.containerConfig,
                        )
                    }
                    val homeRoute = PluviaScreen.Home.route + "?offline={offline}"
                    if (navController.currentDestination?.route != homeRoute) {
                        navController.navigate(PluviaScreen.Home.route + "?offline=false") {
                            popUpTo(navController.graph.startDestinationId) { saveState = false }
                        }
                    }
                    // finalAppId — track what actually launched (may differ from launchRequest.appId after resolution)
                    launchIntentApp(resolution.finalAppId, launchRequest.containerConfig != null)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                MainViewModel.MainUiEvent.LaunchApp -> {
                    navController.navigate(PluviaScreen.XServer.route)
                }

                is MainViewModel.MainUiEvent.ExternalGameLaunch -> {
                    Timber.i("[PluviaMain]: Received ExternalGameLaunch UI event for app ${event.appId}")

                    // defer if service isn't ready yet
                    if (needsDeferLaunch(context, event.appId)) {
                        // preserve any container config override already applied by handleLaunchIntent
                        MainActivity.setPendingLaunchRequest(
                            IntentLaunchManager.LaunchRequest(
                                appId = event.appId,
                                containerConfig = IntentLaunchManager.getTemporaryOverride(event.appId),
                            )
                        )
                        pendingLaunchGeneration++
                        shownPendingLaunchSnackbar = showDeferredLaunchSnackbar(context, event.appId)
                        return@collect
                    }

                    when (val resolution = resolveGameAppId(context, event.appId)) {
                        is GameResolutionResult.Success -> {
                            Timber.i("[PluviaMain]: Using appId: ${resolution.finalAppId} (original: ${event.appId}, isSteamInstalled: ${resolution.isSteamInstalled}, isCustomGame: ${resolution.isCustomGame})")
                            launchIntentApp(
                                resolution.finalAppId,
                                IntentLaunchManager.hasTemporaryOverride(resolution.finalAppId),
                            )
                        }

                        is GameResolutionResult.NotFound -> {
                            val appName = ContainerUtils.resolveGameName(resolution.originalAppId)
                            Timber.w("[PluviaMain]: Game not installed: $appName (${event.appId})")
                            msgDialogState = MessageDialogState(
                                visible = true,
                                type = DialogType.SYNC_FAIL,
                                title = context.getString(R.string.game_not_installed_title),
                                message = context.getString(R.string.game_not_installed_message, appName),
                                dismissBtnText = context.getString(R.string.ok),
                            )
                        }
                    }
                }

                MainViewModel.MainUiEvent.OnBackPressed -> {
                    when (
                        GameFoldSessionPolicy.rootBackAction(
                            localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                            atHome = state.currentScreen == PluviaScreen.Home,
                            launchPreparing = state.loadingDialogVisible || state.showBootingSplash,
                            environmentPresent = PluviaApp.xEnvironment != null,
                            gameBackRegistered = gameBackAction != null,
                            hasBack = hasBack,
                        )
                    ) {
                        GameFoldRootBackAction.CANCEL_PENDING_LAUNCH -> cancelPendingLocalLaunch()
                        GameFoldRootBackAction.DISPATCH_TO_GAME -> gameBackAction?.invoke()
                        GameFoldRootBackAction.POP_NAVIGATION -> navController.popBackStack()
                        GameFoldRootBackAction.IGNORE -> Unit
                    }
                }

                MainViewModel.MainUiEvent.OnLoggedOut -> {
                    Timber.i("Ignoring platform logout event in offline build")
                }

                is MainViewModel.MainUiEvent.OnLogonEnded -> {
                    Timber.i("Ignoring platform logon event in offline build: ${event.result}")
                }

                is MainViewModel.MainUiEvent.SteamDisconnected -> {
                    Timber.i("Ignoring removed Steam service event in local shell")
                }

                MainViewModel.MainUiEvent.ServiceReady -> {
                    Timber.i("Ignoring removed online service event in local shell")
                }

                MainViewModel.MainUiEvent.ShowDiscordSupportDialog -> {
                    msgDialogState = MessageDialogState(
                        visible = true,
                        type = DialogType.DISCORD,
                        title = context.getString(R.string.main_discord_support_title),
                        message = context.getString(R.string.main_discord_support_message),
                        confirmBtnText = context.getString(R.string.main_open_discord),
                        dismissBtnText = context.getString(R.string.close),
                    )
                }

                is MainViewModel.MainUiEvent.ShowGameFeedbackDialog -> {
                    gameFeedbackState = GameFeedbackDialogState(
                        visible = true,
                        appId = event.appId,
                    )
                }
            }
        }
    }

    LaunchedEffect(navController) {
        Timber.i("navController changed")

        if (!state.hasLaunched) {
            viewModel.setHasLaunched(true)

            Timber.i("Creating on destination changed listener")

            PluviaApp.onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
                Timber.i("onDestinationChanged to ${destination.route}")
                // in order not to trigger the screen changed launch effect
                viewModel.setCurrentScreen(destination.route)
            }
            PluviaApp.events.emit(AndroidEvent.StartOrientator)
        } else {
            PluviaApp.onDestinationChangedListener?.let {
                navController.removeOnDestinationChangedListener(it)
            }
        }

        PluviaApp.onDestinationChangedListener?.let {
            navController.addOnDestinationChangedListener(it)
        }
    }

    // TODO merge to VM?
    LaunchedEffect(state.currentScreen) {
        // do the following each time we navigate to a new screen
        if (state.resettedScreen != state.currentScreen) {
            viewModel.setScreen()
            // Log.d("PluviaMain", "Screen changed to $currentScreen, resetting some values")
            // TODO: remove this if statement once XServerScreen orientation change bug is fixed
            if (state.currentScreen != PluviaScreen.XServer) {
                // Hide or show status bar based on if in game or not
                val shouldShowStatusBar = !BuildConfig.GAMEFOLD_PC_LOCAL_SHELL &&
                    !PrefManager.hideStatusBarWhenNotInGame
                PluviaApp.events.emit(AndroidEvent.SetSystemUIVisibility(shouldShowStatusBar))

                // reset system ui visibility based on user preference
                // TODO: add option for user to set
                // reset available orientations
                PluviaApp.events.emit(AndroidEvent.SetAllowedOrientation(EnumSet.of(Orientation.UNSPECIFIED)))
            }
            // find out if back is available
            hasBack = navController.previousBackStackEntry?.destination?.route != null
        }
    }

    // Listen for save container config prompt
    var pendingSaveAppId by rememberSaveable { mutableStateOf<String?>(null) }
    val onPromptSaveConfig: (AndroidEvent.PromptSaveContainerConfig) -> Unit = { event ->
        pendingSaveAppId = event.appId
        msgDialogState = MessageDialogState(
            visible = true,
            type = DialogType.SAVE_CONTAINER_CONFIG,
            title = context.getString(R.string.save_container_settings_title),
            message = context.getString(R.string.save_container_settings_message),
            confirmBtnText = context.getString(R.string.save),
            dismissBtnText = context.getString(R.string.discard),
        )
    }

    // Listen for game feedback request
    val onShowGameFeedback: (AndroidEvent.ShowGameFeedback) -> Unit = { event ->
        gameFeedbackState = GameFeedbackDialogState(
            visible = true,
            appId = event.appId,
        )
    }

    LaunchedEffect(Unit) {
        PluviaApp.events.on<AndroidEvent.PromptSaveContainerConfig, Unit>(onPromptSaveConfig)
        PluviaApp.events.on<AndroidEvent.ShowGameFeedback, Unit>(onShowGameFeedback)
    }

    DisposableEffect(Unit) {
        onDispose {
            PluviaApp.events.off<AndroidEvent.PromptSaveContainerConfig, Unit>(onPromptSaveConfig)
            PluviaApp.events.off<AndroidEvent.ShowGameFeedback, Unit>(onShowGameFeedback)
        }
    }

    val onDismissRequest: (() -> Unit)?
    val onDismissClick: (() -> Unit)?
    val onConfirmClick: (() -> Unit)?
    var onActionClick: (() -> Unit)? = null
    when (msgDialogState.type) {
        DialogType.DISCORD -> {
            onConfirmClick = {
                setMessageDialogState(MessageDialogState(false))
                uriHandler.openUri("https://discord.gg/2hKv4VfZfE")
            }
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.SUPPORT -> {
            onConfirmClick = {
                uriHandler.openUri(Constants.Misc.KO_FI_LINK)
                PrefManager.tipped = true
                msgDialogState = MessageDialogState(visible = false)
            }
            onDismissRequest = {
                msgDialogState = MessageDialogState(visible = false)
            }
            onDismissClick = {
                msgDialogState = MessageDialogState(visible = false)
            }
            onActionClick = {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.main_share_text))
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.main_share)))
            }
        }

        DialogType.SYNC_CONFLICT -> {
            onConfirmClick = {
                preLaunchApp(
                    context = context,
                    appId = state.launchedAppId,
                    preferredSave = SaveLocation.Remote,
                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                    setMessageDialogState = setMessageDialogState,
                    onSuccess = viewModel::launchApp,
                )
                msgDialogState = MessageDialogState(false)
            }
            onDismissClick = {
                preLaunchApp(
                    context = context,
                    appId = state.launchedAppId,
                    preferredSave = SaveLocation.Local,
                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                    setMessageDialogState = setMessageDialogState,
                    onSuccess = viewModel::launchApp,
                )
                msgDialogState = MessageDialogState(false)
            }
            onDismissRequest = {
                msgDialogState = MessageDialogState(false)
            }
        }

        DialogType.SYNC_FAIL -> {
            onConfirmClick = null
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }


        DialogType.EXECUTABLE_NOT_FOUND -> {
            onConfirmClick = null
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
            onActionClick = {
                setMessageDialogState(MessageDialogState(false))
                openContainerConfigForAppId = state.launchedAppId
            }
        }

        DialogType.SYNC_IN_PROGRESS -> {
            onConfirmClick = {
                setMessageDialogState(MessageDialogState(false))
                preLaunchApp(
                    context = context,
                    appId = state.launchedAppId,
                    skipCloudSync = true,
                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                    setMessageDialogState = setMessageDialogState,
                    onSuccess = viewModel::launchApp,
                    isOffline = viewModel.isOffline.value,
                )
            }
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.PENDING_UPLOAD_IN_PROGRESS -> {
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
            onConfirmClick = null
        }

        DialogType.PENDING_UPLOAD -> {
            onConfirmClick = {
                setMessageDialogState(MessageDialogState(false))
                preLaunchApp(
                    context = context,
                    appId = state.launchedAppId,
                    ignorePendingOperations = true,
                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                    setMessageDialogState = setMessageDialogState,
                    onSuccess = viewModel::launchApp,
                    isOffline = viewModel.isOffline.value,
                    bootToContainer = state.bootToContainer,
                )
            }
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.APP_SESSION_ACTIVE -> {
            onConfirmClick = {
                setMessageDialogState(MessageDialogState(false))
                preLaunchApp(
                    context = context,
                    appId = state.launchedAppId,
                    ignorePendingOperations = true,
                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                    setMessageDialogState = setMessageDialogState,
                    onSuccess = viewModel::launchApp,
                )
            }
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.ACCOUNT_SESSION_ACTIVE -> {
            onConfirmClick = {
                setMessageDialogState(MessageDialogState(false))
                viewModel.viewModelScope.launch {
                    preLaunchApp(
                        context = context,
                        appId = state.launchedAppId,
                        setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                        setLoadingProgress = viewModel::setLoadingDialogProgress,
                        setLoadingMessage = viewModel::setLoadingDialogMessage,
                        setMessageDialogState = setMessageDialogState,
                        onSuccess = viewModel::launchApp,
                        isOffline = viewModel.isOffline.value,
                    )
                }
            }
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.APP_SESSION_SUSPENDED -> {
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
            onConfirmClick = null
        }

        DialogType.PENDING_OPERATION_NONE -> {
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
            onConfirmClick = null
        }

        DialogType.MULTIPLE_PENDING_OPERATIONS -> {
            onConfirmClick = {
                setMessageDialogState(MessageDialogState(false))
                preLaunchApp(
                    context = context,
                    appId = state.launchedAppId,
                    ignorePendingOperations = true,
                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                    setMessageDialogState = setMessageDialogState,
                    onSuccess = viewModel::launchApp,
                    isOffline = viewModel.isOffline.value,
                    bootToContainer = state.bootToContainer,
                )
            }
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.CRASH -> {
            onDismissClick = null
            onDismissRequest = {
                viewModel.setHasCrashedLastStart(false)
                setMessageDialogState(MessageDialogState(false))
            }
            onConfirmClick = {
                viewModel.setHasCrashedLastStart(false)
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.SAVE_CONTAINER_CONFIG -> {
            onConfirmClick = {
                // Save the container config permanently
                pendingSaveAppId?.let { appId ->
                    IntentLaunchManager.getEffectiveContainerConfig(context, appId)?.let { config ->
                        ContainerUtils.applyToContainer(context, appId, config)
                        Timber.i("[PluviaMain]: Saved container configuration for app $appId")
                    }
                    // Clear the temporary override after saving
                    IntentLaunchManager.clearTemporaryOverride(appId)
                }
                pendingSaveAppId = null
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissClick = {
                // Discard the temporary config and restore original
                pendingSaveAppId?.let { appId ->
                    IntentLaunchManager.restoreOriginalConfiguration(context, appId)
                    IntentLaunchManager.clearTemporaryOverride(appId)
                    Timber.i("[PluviaMain]: Discarded temporary config and restored original for app $appId")
                }
                pendingSaveAppId = null
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                // Treat closing dialog as discard
                pendingSaveAppId?.let { appId ->
                    IntentLaunchManager.restoreOriginalConfiguration(context, appId)
                    IntentLaunchManager.clearTemporaryOverride(appId)
                }
                pendingSaveAppId = null
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.APP_UPDATE -> {
            onConfirmClick = {
                setMessageDialogState(MessageDialogState(false))
                val updateInfo = viewModel.updateInfo.value
                if (updateInfo != null) {
                    scope.launch {
                        viewModel.setLoadingDialogVisible(true)
                        viewModel.setLoadingDialogMessage("Downloading update...")
                        viewModel.setLoadingDialogProgress(0f)

                        val success = UpdateInstaller.downloadAndInstall(
                            context = context,
                            downloadUrl = updateInfo.downloadUrl,
                            versionName = updateInfo.versionName,
                            onProgress = { progress ->
                                viewModel.setLoadingDialogProgress(progress)
                            },
                        )

                        viewModel.setLoadingDialogVisible(false)
                        if (!success) {
                            msgDialogState = MessageDialogState(
                                visible = true,
                                type = DialogType.SYNC_FAIL,
                                title = context.getString(R.string.main_update_failed_title),
                                message = context.getString(R.string.main_update_failed_message),
                                dismissBtnText = context.getString(R.string.ok),
                            )
                        }
                    }
                }
            }
            onDismissClick = {
                setMessageDialogState(MessageDialogState(false))
            }
            onDismissRequest = {
                setMessageDialogState(MessageDialogState(false))
            }
        }

        DialogType.WORKSHOP_UPDATE_PROMPT -> {
            onConfirmClick = {
                workshopUpdateDeferred?.complete(true)
            }
            onDismissClick = {
                workshopUpdateDeferred?.complete(false)
            }
            onDismissRequest = {
                workshopUpdateDeferred?.complete(false)
            }
        }

        else -> {
            onDismissRequest = null
            onDismissClick = null
            onConfirmClick = null
        }
    }

    val snackbarController = LocalSnackbarHostController.current
    var exitSnackbarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarController) {
        SnackbarManager.messages.collect { message ->
            if (
                withTimeoutOrNull(SNACKBAR_SHOW_TIMEOUT_MS) {
                    snackbarController.hostState.showSnackbar(message)
                } == null
            ) {
                Timber.w("[Snackbar]: Display timed out before dismissal")
            }
            // snackbar dismissed (timeout or new message) — reset exit flag
            exitSnackbarVisible = false
        }
    }

    BackHandler(
        enabled = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL &&
            state.currentScreen == PluviaScreen.Home &&
            (state.loadingDialogVisible || state.showBootingSplash) &&
            PluviaApp.xEnvironment == null,
    ) {
        cancelPendingLocalLaunch()
    }

    PluviaTheme(
        isDark = when (state.appTheme) {
            AppTheme.AUTO -> isSystemInDarkTheme()
            AppTheme.DAY -> false
            AppTheme.NIGHT -> true
            AppTheme.AMOLED -> true
        },
        isAmoled = (state.appTheme == AppTheme.AMOLED),
        style = state.paletteStyle,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LoadingDialog(
                visible = state.loadingDialogVisible && !BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                progress = state.loadingDialogProgress,
                message = state.loadingDialogMessage,
            )

            MessageDialog(
                visible = msgDialogState.visible,
                onDismissRequest = onDismissRequest,
                onConfirmClick = onConfirmClick,
                confirmBtnText = msgDialogState.confirmBtnText,
                onDismissClick = onDismissClick,
                dismissBtnText = msgDialogState.dismissBtnText,
                onActionClick = onActionClick,
                actionBtnText = msgDialogState.actionBtnText,
                icon = msgDialogState.type.icon,
                title = msgDialogState.title,
                message = msgDialogState.message,
            )

            val scope = rememberCoroutineScope()
            var containerConfigForDialog by remember(openContainerConfigForAppId) { mutableStateOf<ContainerData?>(null) }
            LaunchedEffect(openContainerConfigForAppId) {
                val appId = openContainerConfigForAppId
                if (appId == null) {
                    containerConfigForDialog = null
                    return@LaunchedEffect
                }
                containerConfigForDialog = withContext(Dispatchers.IO) {
                    val container = ContainerUtils.getOrCreateContainer(context, appId)
                    ContainerUtils.toContainerData(container)
                }
            }
            openContainerConfigForAppId?.let { appId ->
                containerConfigForDialog?.let { config ->
                    ContainerConfigDialog(
                        visible = true,
                        title = context.getString(R.string.container_config_title),
                        initialConfig = config,
                        onDismissRequest = { openContainerConfigForAppId = null },
                        onSave = { newConfig ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    ContainerUtils.applyToContainer(context, appId, newConfig)
                                }
                                openContainerConfigForAppId = null
                            }
                        },
                    )
                }
            }

            GameFeedbackDialog(
                state = gameFeedbackState,
                onStateChange = { gameFeedbackState = it },
                onSubmit = { feedbackState ->
                    Timber.d(
                        "GameFeedback: onSubmit called with rating=${feedbackState.rating}, tags=${feedbackState.selectedTags}, text=${
                            feedbackState.feedbackText.take(
                                20,
                            )
                        }",
                    )
                    try {
                        // Get the container for the app
                        val appId = feedbackState.appId
                        Timber.d("GameFeedback: Got appId=$appId")

                        // Submit feedback via worker API
                        Timber.d("GameFeedback: Starting coroutine for submission")
                        viewModel.viewModelScope.launch {
                            Timber.d("GameFeedback: Inside coroutine scope")
                            try {
                                Timber.d("GameFeedback: Calling submitGameFeedback with rating=${feedbackState.rating}")
                                val result = GameFeedbackUtils.submitGameFeedback(
                                    context = context,
                                    appId = appId,
                                    rating = feedbackState.rating,
                                    tags = feedbackState.selectedTags.toList(),
                                    notes = feedbackState.feedbackText.takeIf { it.isNotBlank() },
                                )

                                Timber.d("GameFeedback: Submission returned $result")
                                if (result) {
                                    Timber.d("GameFeedback: Showing success snackbar")
                                    SnackbarManager.show("Thank you for your feedback!")
                                } else {
                                    Timber.d("GameFeedback: Showing failure snackbar")
                                    SnackbarManager.show("Failed to submit feedback")
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "GameFeedback: Error submitting game feedback")
                                SnackbarManager.show("Error submitting feedback")
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "GameFeedback: Error preparing game feedback")
                        SnackbarManager.show("Failed to submit feedback")
                    } finally {
                        // Close the dialog regardless of success
                        Timber.d("GameFeedback: Closing dialog")
                        gameFeedbackState = GameFeedbackDialogState(visible = false)
                    }
                },
                onDismiss = {
                    gameFeedbackState = GameFeedbackDialogState(visible = false)
                },
                onDiscordSupport = {
                    uriHandler.openUri("https://discord.gg/2hKv4VfZfE")
                },
            )

            Box(modifier = Modifier.zIndex(10f)) {
                BootingSplash(
                    visible = state.showBootingSplash && !BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                    text = state.bootingSplashText,
                    heroImageUrl = state.bootingSplashHeroImageUrl,
                )
            }

            val startDestination = rememberSaveable { PluviaScreen.Home.route + "?offline=true" }

            NavHost(
                navController = navController,
                startDestination = startDestination,
            ) {
                /** Library, Downloads **/
                composable(
                    route = PluviaScreen.Home.route + "?offline={offline}",
                    deepLinks = listOf(navDeepLink { uriPattern = "pluvia://home" }),
                    arguments = listOf(
                        navArgument("offline") {
                            type = NavType.BoolType
                            defaultValue = false // default when the query param isn’t present
                        },
                    ),
                ) { backStackEntry ->
                    val isOffline = backStackEntry.arguments?.getBoolean("offline") ?: false

                    // GamePad PC is a self-contained offline launcher; do
                    // not show update/crash/support dialogs on first entry.
                    LaunchedEffect(Unit) {
                        val shouldShowDialogs = false

                        if (shouldShowDialogs && !state.annoyingDialogShown && PluviaApp.xEnvironment == null && !MainActivity.wasLaunchedViaExternalIntent) {
                            val currentUpdateInfo = updateInfo
                            if (currentUpdateInfo != null) {
                                viewModel.setAnnoyingDialogShown(true)
                                msgDialogState = MessageDialogState(
                                    visible = true,
                                    type = DialogType.APP_UPDATE,
                                    title = context.getString(R.string.main_update_available_title),
                                    message = context.getString(
                                        R.string.main_update_available_message,
                                        currentUpdateInfo.versionName,
                                        currentUpdateInfo.releaseNotes?.let { "\n\n$it" } ?: "",
                                    ),
                                    confirmBtnText = context.getString(R.string.main_update_button),
                                    dismissBtnText = context.getString(R.string.main_later_button),
                                )
                            } else if (state.hasCrashedLastStart) {
                                viewModel.setAnnoyingDialogShown(true)
                                msgDialogState = MessageDialogState(
                                    visible = true,
                                    type = DialogType.CRASH,
                                    title = context.getString(R.string.main_recent_crash_title),
                                    message = context.getString(R.string.main_recent_crash_message),
                                    confirmBtnText = context.getString(R.string.ok),
                                )
                            } else if (!(PrefManager.tipped || BuildConfig.GOLD)) {
                                viewModel.setAnnoyingDialogShown(true)
                                msgDialogState = MessageDialogState(
                                    visible = true,
                                    type = DialogType.SUPPORT,
                                    title = context.getString(R.string.main_thank_you_title),
                                    message = context.getString(R.string.main_thank_you_message),
                                    confirmBtnText = context.getString(R.string.main_join_kofi),
                                    dismissBtnText = context.getString(R.string.close),
                                    actionBtnText = context.getString(R.string.main_share),
                                )
                            }
                        }
                    }

                    HomeScreen(
                        onClickPlay = { appId, asContainer ->
                            when (GameFoldSessionPolicy.launchAction(
                                localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                                environmentPresent = PluviaApp.hasResumableGameEnvironment(),
                                activeAppId = PluviaApp.activeGameAppId,
                                selectedAppId = appId,
                                pausedAtHome = PluviaApp.gamePausedAtHome,
                            )) {
                                GameFoldSessionLaunchAction.RESUME_PRESERVED -> {
                                // Reattach the existing XServer/environment. No install, prefix
                                // setup, container recreation, or second game process is allowed.
                                Timber.i("[GameFoldSession] Resuming preserved appId=%s", appId)
                                // A preserved renderer is already running. Clear any stale root
                                // launch state before changing routes; the XServer host owns no
                                // preparation work on this path.
                                viewModel.setLoadingDialogVisible(false)
                                viewModel.setShowBootingSplash(false)
                                viewModel.setLaunchedAppId(appId)
                                viewModel.setBootToContainer(asContainer)
                                viewModel.setTestGraphics(false)
                                viewModel.setDiagnostics(false)
                                viewModel.setOffline(isOffline)
                                navController.navigate(PluviaScreen.XServer.route)
                                }
                                GameFoldSessionLaunchAction.REPLACE_PRESERVED -> {
                                // A local PC shell owns one runtime session. Replace a paused
                                // game before launching a different title.
                                Timber.i("[GameFoldSession] Replacing preserved appId=%s with appId=%s", PluviaApp.activeGameAppId, appId)
                                PluviaApp.shutdownEnvironment()
                                viewModel.prepareBootingSplash(context, appId)
                                viewModel.setLaunchedAppId(appId)
                                viewModel.setBootToContainer(asContainer)
                                viewModel.setTestGraphics(false)
                                viewModel.setDiagnostics(false)
                                viewModel.setOffline(isOffline)
                                preLaunchApp(
                                    context = context,
                                    appId = appId,
                                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                                    setMessageDialogState = { msgDialogState = it },
                                    onSuccess = viewModel::launchApp,
                                    isOffline = isOffline,
                                    bootToContainer = asContainer,
                                )
                                }
                                GameFoldSessionLaunchAction.COLD_START -> {
                                viewModel.prepareBootingSplash(context, appId)
                                viewModel.setLaunchedAppId(appId)
                                viewModel.setBootToContainer(asContainer)
                                viewModel.setTestGraphics(false)
                                viewModel.setDiagnostics(false)
                                viewModel.setOffline(isOffline)
                                preLaunchApp(
                                    context = context,
                                    appId = appId,
                                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                                    setMessageDialogState = { msgDialogState = it },
                                    onSuccess = viewModel::launchApp,
                                    isOffline = isOffline,
                                    bootToContainer = asContainer,
                                )
                                }
                            }
                        },
                        onTestGraphics = { appId ->
                            viewModel.prepareBootingSplash(context, appId)
                            viewModel.setLaunchedAppId(appId)
                            viewModel.setBootToContainer(true)
                            viewModel.setTestGraphics(true)
                            viewModel.setDiagnostics(false)
                            viewModel.setOffline(isOffline)
                            preLaunchApp(
                                context = context,
                                appId = appId,
                                setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                                setLoadingProgress = viewModel::setLoadingDialogProgress,
                                setLoadingMessage = viewModel::setLoadingDialogMessage,
                                setMessageDialogState = { msgDialogState = it },
                                onSuccess = viewModel::launchApp,
                                isOffline = isOffline,
                                bootToContainer = true,
                            )
                        },
                        onPlayWithDiagnostics = { appId ->
                            viewModel.prepareBootingSplash(context, appId)
                            viewModel.setLaunchedAppId(appId)
                            viewModel.setBootToContainer(false)
                            viewModel.setTestGraphics(false)
                            viewModel.setDiagnostics(true)
                            viewModel.setOffline(isOffline)
                            preLaunchApp(
                                context = context,
                                appId = appId,
                                setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                                setLoadingProgress = viewModel::setLoadingDialogProgress,
                                setLoadingMessage = viewModel::setLoadingDialogMessage,
                                setMessageDialogState = { msgDialogState = it },
                                onSuccess = viewModel::launchApp,
                                isOffline = isOffline,
                                bootToContainer = false,
                            )
                        },
                        onClickExit = {
                            when (
                                ExitConfirmationPolicy.action(
                                    warnBeforeExit = PrefManager.warnBeforeExit,
                                    warningVisible = exitSnackbarVisible,
                                )
                            ) {
                                ExitConfirmationPolicy.Action.EXIT -> {
                                    PluviaApp.events.emit(AndroidEvent.EndProcess)
                                }
                                ExitConfirmationPolicy.Action.WARN -> {
                                    exitSnackbarVisible = true
                                    SnackbarManager.show(context.getString(R.string.back_press_exit_warning))
                                }
                            }
                        },
                        onChat = {
                            navController.navigate(PluviaScreen.Chat.route(it))
                        },
                        onNavigateRoute = {
                            navController.navigate(it)
                        },
                        onLogout = {
                            Timber.i("Ignoring logout action in offline build")
                        },
                        onGoOnline = {
                            Timber.i("Ignoring online action in offline build")
                        },
                        onCancelLaunch = cancelPendingLocalLaunch,
                        isOffline = isOffline,
                        isSteamConnected = state.isSteamConnected,
                        loadingVisible = state.loadingDialogVisible,
                        loadingProgress = state.loadingDialogProgress,
                        loadingMessage = state.loadingDialogMessage,
                        bootingVisible = state.showBootingSplash,
                        bootingProgress = state.bootingSplashProgress,
                        bootingMessage = state.bootingSplashText,
                        bootingArtworkUrl = state.bootingSplashHeroImageUrl,
                        bootingTitle = state.bootingSplashTitle,
                    )
                }

                /** Full Screen Chat **/
                // Chat feature temporarily disabled - screen component removed
                /* composable(
                    route = "chat/{id}",
                    arguments = listOf(
                        navArgument(PluviaScreen.Chat.ARG_ID) {
                            type = NavType.LongType
                        },
                    ),
                ) {
                    val id = it.arguments?.getLong(PluviaScreen.Chat.ARG_ID) ?: throw RuntimeException("Unable to get ID to chat")
                    ChatScreen(
                        friendId = id,
                        onBack = {
                            CoroutineScope(Dispatchers.Main).launch {
                                navController.popBackStack()
                            }
                        },
                    )
                } */

                /** Game Screen **/
                composable(route = PluviaScreen.XServer.route) {
                    val xServerIsOffline by viewModel.isOffline.collectAsStateWithLifecycle()
                    XServerScreen(
                        appId = state.launchedAppId,
                        bootToContainer = state.bootToContainer,
                        testGraphics = state.testGraphics,
                        diagnostics = state.diagnostics,
                        isOffline = xServerIsOffline,
                        registerBackAction = { cb ->
                            Timber.d("registerBackAction called: $cb")
                            gameBackAction = cb
                        },
                        onBackExitRequest = { exitGame ->
                            when (
                                ExitConfirmationPolicy.action(
                                    warnBeforeExit = PrefManager.warnBeforeExit,
                                    warningVisible = exitSnackbarVisible,
                                )
                            ) {
                                ExitConfirmationPolicy.Action.EXIT -> exitGame()
                                ExitConfirmationPolicy.Action.WARN -> {
                                    exitSnackbarVisible = true
                                    SnackbarManager.show(context.getString(R.string.back_press_exit_warning))
                                }
                            }
                        },
                        navigateBack = {
                            CoroutineScope(Dispatchers.Main).launch {
                                val currentRoute = navController.currentBackStackEntry
                                    ?.destination
                                    ?.route

                                if (currentRoute == PluviaScreen.XServer.route) {
                                    if (MainActivity.wasLaunchedViaExternalIntent) {
                                        Timber.d("[IntentLaunch]: Finishing activity to return to external launcher")
                                        MainActivity.wasLaunchedViaExternalIntent = false
                                        (context as? android.app.Activity)?.finish()
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        },
                        navigateHome = {
                            // Home preserves the current runtime session and only changes the
                            // foreground route. Never finish an external-intent Activity here.
                            if (navController.currentDestination?.route == PluviaScreen.XServer.route) {
                                navController.popBackStack()
                            }
                        },
                        onBeforeNavigateHome = cancelPendingLocalLaunch,
                        onWindowMapped = { context, window ->
                            viewModel.onWindowMapped(context, window, state.launchedAppId)
                        },
                        onExit = { onComplete ->
                            viewModel.exitSteamApp(context, state.launchedAppId, onComplete)
                        },
                        onRestartAfterProvisioning = { restartAppId ->
                            if (navController.currentDestination?.route == PluviaScreen.XServer.route) {
                                navController.popBackStack()
                            }
                            viewModel.setLaunchedAppId(restartAppId)
                            viewModel.setBootToContainer(false)
                            scope.launch(Dispatchers.IO) {
                                preLaunchApp(
                                    context = context,
                                    appId = restartAppId,
                                    setLoadingDialogVisible = viewModel::setLoadingDialogVisible,
                                    setLoadingProgress = viewModel::setLoadingDialogProgress,
                                    setLoadingMessage = viewModel::setLoadingDialogMessage,
                                    setMessageDialogState = setMessageDialogState,
                                    onSuccess = viewModel::launchApp,
                                    isOffline = true,
                                )
                            }
                        },
                        onGameLaunchError = { error ->
                            viewModel.onGameLaunchError(error)
                        },
                    )
                }

                /** Settings **/
                composable(route = PluviaScreen.Settings.route) {
                    var settingsControllerNavigationRequests by remember { mutableIntStateOf(0) }
                    var settingsControllerNavigationButton by remember { mutableStateOf<GameFoldButton?>(null) }
                    var settingsControllerBackRequests by remember { mutableIntStateOf(0) }
                    val settingsControllerListener = remember {
                        object : GameFoldControllerListener {
                            // A/X must reach the focused Compose control on detail pages. The
                            // settings home also mirrors selection explicitly, so its selected
                            // card is given focus below before this synthesized confirm arrives.
                            override fun dispatchConfirmToCompose(): Boolean = true

                            // Settings detail rows use the semantic focus manager below. Avoid a
                            // second platform DPAD dispatch, which would advance two rows for a
                            // single physical press.
                            override fun dispatchNavigationToCompose(): Boolean = false

                            override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
                                if (pressed) {
                                    if (button == GameFoldButton.B || button == GameFoldButton.Y || button == GameFoldButton.HOME) {
                                        settingsControllerBackRequests++
                                    } else if (button == GameFoldButton.DPAD_UP ||
                                        button == GameFoldButton.DPAD_RIGHT ||
                                        button == GameFoldButton.DPAD_DOWN ||
                                        button == GameFoldButton.DPAD_LEFT ||
                                        button == GameFoldButton.A ||
                                        button == GameFoldButton.X
                                    ) {
                                        settingsControllerNavigationButton = button
                                        settingsControllerNavigationRequests++
                                    }
                                }
                            }

                            override fun onStickChanged(stick: GameFoldStick, x: Float, y: Float) = Unit
                        }
                    }
                    GameFoldPcShellHost(controllerListener = settingsControllerListener) {
                        SettingsScreen(
                            appTheme = state.appTheme,
                            paletteStyle = state.paletteStyle,
                            onAppTheme = viewModel::setTheme,
                            onPaletteStyle = viewModel::setPalette,
                            onBack = { navController.navigateUp() },
                            controllerBackRequests = settingsControllerBackRequests,
                            controllerNavigationRequests = settingsControllerNavigationRequests,
                            controllerNavigationButton = settingsControllerNavigationButton,
                        )
                    }
                }
            }

            if (snackbarController.rootOwnsHost) {
                SnackbarHost(
                    hostState = snackbarController.hostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility)
                        .padding(bottom = 16.dp),
                ) { data ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 4.dp,
                        ) {
                            Text(
                                text = data.visuals.message,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            AchievementOverlay()
        }
    }
}

private fun preLaunchLocalShell(
    context: Context,
    appId: String,
    useTemporaryOverride: Boolean,
    bootToContainer: Boolean,
    setLoadingDialogVisible: (Boolean) -> Unit,
    setLoadingProgress: (Float) -> Unit,
    setLoadingMessage: (String) -> Unit,
    setMessageDialogState: (MessageDialogState) -> Unit,
    onSuccess: KFunction3<Context, String, GameFoldPcPreLaunchTicket?, Unit>,
) {
    val singleFlight = GameFoldPcPreLaunchCoordinator.singleFlight
    val preLaunchTicket = singleFlight.tryStart(appId)
    if (preLaunchTicket == null) {
        Timber.tag("preLaunchApp").i("Ignoring duplicate local-shell pre-launch for appId=$appId")
        return
    }
    fun isActive(): Boolean = singleFlight.isActive(preLaunchTicket)
    fun setVisibleIfActive(value: Boolean) {
        if (isActive()) setLoadingDialogVisible(value)
    }
    fun setProgressIfActive(value: Float) {
        if (isActive()) setLoadingProgress(value)
    }
    fun setMessageIfActive(value: String) {
        if (isActive()) setLoadingMessage(value)
    }
    fun setDialogIfActive(value: MessageDialogState) {
        if (isActive()) setMessageDialogState(value)
    }
    setLoadingDialogVisible(true)
    CoroutineScope(Dispatchers.IO).launch {
      var handedOffToViewModel = false
      try {
        if (!isActive()) return@launch
        if (LaunchReadiness.pending) {
            setVisibleIfActive(false)
            (context as? Activity)?.let { LaunchReadiness.resolve(it) }
            return@launch
        }

        val gameSource = ContainerUtils.extractGameSourceFromContainerId(appId)
        if (gameSource != GameSource.CUSTOM_GAME) {
            Timber.tag("preLaunchApp").e("Local shell rejected non-local appId=$appId")
            setVisibleIfActive(false)
            setDialogIfActive(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.game_not_installed_title),
                    message = context.getString(R.string.game_not_installed_message, appId),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        setMessageIfActive("正在校验 GamePad PC 运行文件")
        val provisionResult = app.gamenative.gamefold.startup.GameFoldPcRuntimeProvisionCoordinator.ensureReady(
            context = context,
            onProgress = { update ->
                setMessageIfActive(
                    when (update.phase) {
                        app.gamenative.gamefold.startup.RuntimeProvisionPhase.VALIDATING_PUBLIC_PAYLOAD ->
                            "正在校验 GamePad PC 运行文件"
                        app.gamenative.gamefold.startup.RuntimeProvisionPhase.INSTALLING_SHARED_RUNTIME ->
                            "正在安装 Bionic 与 Proton 运行环境"
                        app.gamenative.gamefold.startup.RuntimeProvisionPhase.VERIFYING_PRIVATE_RUNTIME ->
                            "正在验证运行环境"
                    },
                )
                setProgressIfActive(update.percent?.div(100f) ?: -1f)
            },
        )
        if (!isActive()) return@launch
        if (provisionResult !is app.gamenative.gamefold.startup.RuntimeProvisionResult.Ready) {
            val (title, detail) = when (provisionResult) {
                app.gamenative.gamefold.startup.RuntimeProvisionResult.PermissionRequired ->
                    "缺少文件访问权限" to "请允许访问 /storage/emulated/0/GamePadPC 后重试。"
                is app.gamenative.gamefold.startup.RuntimeProvisionResult.PayloadInvalid ->
                    "GamePadPC 资源不完整" to
                        (provisionResult.missing + provisionResult.invalid).joinToString("\n")
                            .ifBlank { "请检查 manifest.json 与运行文件。" }
                is app.gamenative.gamefold.startup.RuntimeProvisionResult.Failed ->
                    "运行环境准备失败" to
                        "阶段：${provisionResult.phase.name}\n${provisionResult.message}"
                is app.gamenative.gamefold.startup.RuntimeProvisionResult.Ready -> error("unreachable")
            }
            Timber.tag("preLaunchApp").e("$title: $detail")
            setVisibleIfActive(false)
            setDialogIfActive(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = title,
                    message = detail,
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        val containerManager = ContainerManager(context)
        val container = if (useTemporaryOverride) {
            ContainerUtils.getOrCreateContainerWithOverride(context, appId)
        } else {
            ContainerUtils.getOrCreateContainer(context, appId)
        }
        container.clearSessionMetadata()

        val legacyImageFsRoot = File(context.filesDir, "imagefs")
        if (!ImageFSLegacyMigrator.migrateLegacyDirsIfNeeded(
                context,
                legacyImageFsRoot,
                container.wineVersion,
            )
        ) {
            Timber.tag("preLaunchApp").e("Legacy ImageFS migration failed: ${legacyImageFsRoot.absolutePath}")
            setVisibleIfActive(false)
            setDialogIfActive(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.install_failed_title),
                    message = context.getString(R.string.install_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        if (!bootToContainer && CustomGameScanner.getLaunchExecutable(container).isBlank()) {
            setVisibleIfActive(false)
            setDialogIfActive(
                MessageDialogState(
                    visible = true,
                    type = DialogType.EXECUTABLE_NOT_FOUND,
                    title = context.getString(R.string.game_executable_not_found_title),
                    message = context.getString(R.string.game_executable_not_found),
                    dismissBtnText = context.getString(R.string.ok),
                    actionBtnText = context.getString(AppOptionMenuType.EditContainer.title),
                ),
            )
            return@launch
        }

        SplitCompat.install(context)
        setMessageIfActive(
            if (container.containerVariant.equals(Container.GLIBC)) {
                "正在安装 glibc 运行环境"
            } else {
                "正在安装 Bionic 运行环境"
            },
        )
        val installed = ImageFsInstaller.installIfNeededFuture(
            context,
            context.assets,
            container,
        ) { progress -> setProgressIfActive(progress / 100f) }.get()
        if (!isActive()) return@launch
        if (!installed) {
            Timber.tag("preLaunchApp").e("ImageFS installation failed")
            setVisibleIfActive(false)
            setDialogIfActive(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.install_failed_title),
                    message = context.getString(R.string.install_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        // The startup gate provisions only the shared ImageFS + Proton core.
        // Deploy redirect/extras now that a concrete game container exists;
        // this keeps first launch predictable without changing the runtime
        // layout or downloading anything implicitly.
        val deferredDependenciesReady = ImageFsInstaller.ensureLaunchRuntimeDependencies(
            context,
            context.assets,
        ).get()
        if (!isActive()) return@launch
        if (!deferredDependenciesReady) {
            Timber.tag("preLaunchApp").e("Deferred GamePadPC launch dependencies failed")
            setVisibleIfActive(false)
            setDialogIfActive(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.install_failed_title),
                    message = context.getString(R.string.install_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        if (!isActive()) return@launch
        val gameDirectory = CustomGameScanner.getFolderPathFromAppId(appId)?.let(::File)
        val saveImportResult = if (gameDirectory == null) {
            Gta5SaveAutoImporter.Result.NotApplicable
        } else {
            Gta5SaveAutoImporter().importIfNeeded(
                Gta5SaveAutoImporter.Request(
                    appId = appId,
                    executablePath = CustomGameScanner.getLaunchExecutable(container),
                    gameDirectory = gameDirectory,
                    containerRoot = container.rootDir,
                    localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                    customGame = gameSource == GameSource.CUSTOM_GAME,
                    bootToContainer = bootToContainer,
                ),
            )
        }
        if (!isActive()) return@launch
        Timber.tag("Gta5SaveImport").i("result=$saveImportResult appId=$appId")
        val saveImportNotification = Gta5SaveImportLaunchPolicy.notificationFor(saveImportResult)
        val saveImportMessage = when (saveImportNotification) {
            Gta5SaveImportLaunchPolicy.Notification.IMPORTED -> context.getString(
                R.string.gta5_save_imported,
                (saveImportResult as Gta5SaveAutoImporter.Result.Imported).fileCount,
            )
            Gta5SaveImportLaunchPolicy.Notification.PRESERVED_EXISTING -> context.getString(
                R.string.gta5_save_preserved_existing,
            )
            Gta5SaveImportLaunchPolicy.Notification.FAILED -> context.getString(
                R.string.gta5_save_import_failed_message,
            )
            null -> null
        }
        if (saveImportMessage != null) {
            setMessageIfActive(saveImportMessage)
            setProgressIfActive(-1f)
            delay(Gta5SaveImportLaunchPolicy.NOTIFICATION_DURATION_MS)
            if (!isActive()) return@launch
        }

        val cyberpunkSaveImportResult = if (gameDirectory == null) {
            Cyberpunk2077SaveAutoImporter.Result.NotApplicable
        } else {
            Cyberpunk2077SaveAutoImporter().importIfNeeded(
                Cyberpunk2077SaveAutoImporter.Request(
                    appId = appId,
                    executablePath = CustomGameScanner.getLaunchExecutable(container),
                    gameDirectory = gameDirectory,
                    containerRoot = container.rootDir,
                    localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                    customGame = gameSource == GameSource.CUSTOM_GAME,
                    bootToContainer = bootToContainer,
                ),
            )
        }
        if (!isActive()) return@launch
        Timber.tag("CyberpunkSaveImport").i("result=$cyberpunkSaveImportResult appId=$appId")
        val cyberpunkSaveImportNotification =
            Cyberpunk2077SaveImportLaunchPolicy.notificationFor(cyberpunkSaveImportResult)
        val cyberpunkSaveImportMessage = when (cyberpunkSaveImportNotification) {
            Cyberpunk2077SaveImportLaunchPolicy.Notification.IMPORTED -> context.getString(
                R.string.cyberpunk2077_save_imported,
                (cyberpunkSaveImportResult as Cyberpunk2077SaveAutoImporter.Result.Imported)
                    .saveCount,
            )
            Cyberpunk2077SaveImportLaunchPolicy.Notification.PRESERVED_EXISTING -> context.getString(
                R.string.cyberpunk2077_save_preserved_existing,
            )
            Cyberpunk2077SaveImportLaunchPolicy.Notification.FAILED -> context.getString(
                R.string.cyberpunk2077_save_import_failed_message,
            )
            null -> null
        }
        if (cyberpunkSaveImportMessage != null) {
            setMessageIfActive(cyberpunkSaveImportMessage)
            setProgressIfActive(-1f)
            delay(Cyberpunk2077SaveImportLaunchPolicy.NOTIFICATION_DURATION_MS)
            if (!isActive()) return@launch
        }

        if (saveImportMessage == null && cyberpunkSaveImportMessage == null) {
            setMessageIfActive("正在启动 ${ContainerUtils.resolveGameName(appId)}")
        }
        setProgressIfActive(-1f)
        containerManager.activateContainer(container)
        if (!isActive()) return@launch
        setVisibleIfActive(false)
        onSuccess(context, appId, preLaunchTicket)
        handedOffToViewModel = true
      } catch (error: Throwable) {
        if (isActive()) {
            Timber.tag("preLaunchApp").e(error, "Local-shell pre-launch transaction failed for $appId")
            setVisibleIfActive(false)
            setDialogIfActive(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = "运行环境准备失败",
                    message = error.message ?: context.getString(R.string.install_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
        } else {
            Timber.tag("preLaunchApp").i("Discarding cancelled local-shell pre-launch for appId=$appId")
        }
      } finally {
        if (!handedOffToViewModel) singleFlight.finish(preLaunchTicket)
      }
    }
}

fun preLaunchApp(
    context: Context,
    appId: String,
    ignorePendingOperations: Boolean = false,
    preferredSave: SaveLocation = SaveLocation.None,
    useTemporaryOverride: Boolean = false,
    skipCloudSync: Boolean = false,
    setLoadingDialogVisible: (Boolean) -> Unit,
    setLoadingProgress: (Float) -> Unit,
    setLoadingMessage: (String) -> Unit,
    setMessageDialogState: (MessageDialogState) -> Unit,
    onSuccess: KFunction3<Context, String, GameFoldPcPreLaunchTicket?, Unit>,
    retryCount: Int = 0,
    isOffline: Boolean = false,
    bootToContainer: Boolean = false,
) {
    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        preLaunchLocalShell(
            context = context,
            appId = appId,
            useTemporaryOverride = useTemporaryOverride,
            bootToContainer = bootToContainer,
            setLoadingDialogVisible = setLoadingDialogVisible,
            setLoadingProgress = setLoadingProgress,
            setLoadingMessage = setLoadingMessage,
            setMessageDialogState = setMessageDialogState,
            onSuccess = onSuccess,
        )
        return
    }

    setLoadingDialogVisible(true)
    // TODO: add a way to cancel
    // TODO: add fail conditions

    val gameId = ContainerUtils.extractGameIdFromContainerId(appId)

    CoroutineScope(Dispatchers.IO).launch {
        if (LaunchReadiness.pending) {
            setLoadingDialogVisible(false)
            (context as? Activity)?.let { LaunchReadiness.resolve(it) }
            return@launch
        }

        // A separately named local-shell package starts with an empty private
        // data directory. Finish the public Bionic/ImageFS bootstrap before
        // creating its first container so container creation never observes a
        // partially installed runtime. Existing installs keep the fast path.
        if (
            LocalRuntimeBootstrapPolicy.shouldBootstrapImageFs(
                localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                imageFsValid = ImageFs.find(context).isValid(),
            )
        ) {
            setLoadingMessage("正在安装 Bionic 运行环境")
            val bootstrap = Container("GAMEFOLD_PC_BOOTSTRAP")
            bootstrap.containerVariant = requireNotNull(
                LocalRuntimeBootstrapPolicy.bootstrapContainerVariant(
                    localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
                ),
            )
            val imageFsReady = ImageFsInstaller.installIfNeededFuture(
                context,
                context.assets,
                bootstrap,
            ) { progress -> setLoadingProgress(progress / 100f) }.get()
            if (!imageFsReady) {
                Timber.tag("preLaunchApp").e("Local shell ImageFS bootstrap failed")
                setLoadingDialogVisible(false)
                setMessageDialogState(
                    MessageDialogState(
                        visible = true,
                        type = DialogType.SYNC_FAIL,
                        title = context.getString(R.string.install_failed_title),
                        message = context.getString(R.string.install_failed_message),
                        dismissBtnText = context.getString(R.string.ok),
                    ),
                )
                return@launch
            }
        }

        // create container if it does not already exist
        // TODO: combine somehow with container creation in HomeLibraryAppScreen
        val containerManager = ContainerManager(context)
        val container = if (useTemporaryOverride) {
            ContainerUtils.getOrCreateContainerWithOverride(context, appId)
        } else {
            ContainerUtils.getOrCreateContainer(context, appId)
        }

        // Clear session metadata on every launch to ensure fresh values
        container.clearSessionMetadata()

        val gameSource = ContainerUtils.extractGameSourceFromContainerId(appId)
        val isLocalSavesOnly = ContainerUtils.isLocalSavesOnly(context, appId)

        // GamePad PC is intentionally self-contained. Validate the public
        // payload before any launch dependency or manifest code can decide to
        // download something after the app's private data was cleared.
        val externalResourcesReady = if (BuildConfig.GAMEFOLD_PC_LAZY) {
            val validation = GameFoldPcResourceRoot.validateManifest(verifyHashes = true)
            if (!validation.valid) {
                Timber.tag("preLaunchApp").e(
                    "GamePadPC external payload is incomplete: ${validation.describe()}",
                )
                setLoadingDialogVisible(false)
                setMessageDialogState(
                    MessageDialogState(
                        visible = true,
                        type = DialogType.SYNC_FAIL,
                        title = "GamePadPC 资源不完整",
                        message = validation.describe().ifBlank {
                            "请补齐 /storage/emulated/0/GamePadPC/manifest.json 和对应资源。"
                        },
                        dismissBtnText = context.getString(R.string.ok),
                    ),
                )
                return@launch
            }
            Timber.tag("preLaunchApp").i("GamePadPC external payload validated; network installs disabled")
            true
        } else {
            false
        }

        // Migrate legacy on-disk imagefs layout (e.g. legacy Proton → shared paths) before manifest
        // installs or launch deps — resolveMissingManifestInstallRequests can install Proton too.
        val legacyImageFsRoot = File(context.filesDir, "imagefs")
        val migrationOk = ImageFSLegacyMigrator.migrateLegacyDirsIfNeeded(
            context,
            legacyImageFsRoot,
            container.wineVersion,
        )
        if (!migrationOk) {
            Timber.tag("preLaunchApp").e(
                "Legacy ImageFS migration failed: ${legacyImageFsRoot.absolutePath}",
            )
            setLoadingDialogVisible(false)
            setMessageDialogState(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.install_failed_title),
                    message = context.getString(R.string.install_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        // When "Open container" is used we boot to desktop/file manager only — skip executable check
        if (!bootToContainer) {
            // Verify we have a launch executable for all platforms before proceeding (fail fast, avoid black screen)
            val effectiveExe = when (gameSource) {
                GameSource.STEAM -> SteamService.getLaunchExecutable(appId, container)
                GameSource.GOG -> GOGService.getLaunchExecutable(appId, container)
                GameSource.EPIC -> EpicService.getLaunchExecutable(appId)
                GameSource.CUSTOM_GAME -> CustomGameScanner.getLaunchExecutable(container)
                GameSource.AMAZON -> AmazonService.getLaunchExecutable(appId)
            }
            if (effectiveExe.isBlank()) {
                Timber.tag("preLaunchApp").w("Cannot launch $appId: no executable found (game source: $gameSource)")
                setLoadingDialogVisible(false)
                setMessageDialogState(
                    MessageDialogState(
                        visible = true,
                        type = DialogType.EXECUTABLE_NOT_FOUND,
                        title = context.getString(R.string.game_executable_not_found_title),
                        message = context.getString(R.string.game_executable_not_found),
                        dismissBtnText = context.getString(R.string.ok),
                        actionBtnText = context.getString(AppOptionMenuType.EditContainer.title),
                    ),
                )
                return@launch
            }
        }

        // The external GamePadPC manifest is authoritative in the lazy build.
        // Do not ask the private-content resolver for install requests: after a
        // data clear it cannot see public archives and would show a misleading
        // download dialog or enter the server path.
        if (!externalResourcesReady) {
            try {
                val configJson = Json.parseToJsonElement(container.containerJson).jsonObject
                val missingRequests = BestConfigService.resolveMissingManifestInstallRequests(
                    context, configJson, "exact_gpu_match",
                )
                for (request in missingRequests) {
                    setLoadingMessage(context.getString(R.string.main_downloading_entry, request.entry.name))
                    try {
                        ManifestInstaller.installManifestEntry(
                            context, request.entry, request.isDriver, request.contentType,
                        ) { progress -> setLoadingProgress(progress.coerceIn(0f, 1f)) }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to install ${request.entry.name}, continuing")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to install manifest components")
                setLoadingDialogVisible(false)
                return@launch
            }
        }

        // Check if this is a Custom Game and validate executable selection before installing components
        // Skip the check if booting to container (Open Container menu option)
        val isCustomGame = gameSource == GameSource.CUSTOM_GAME

        // set up Ubuntu file system — download required files and install
        SplitCompat.install(context)

        // Dependency implementations still contain a few legacy English status strings. In the
        // PC shell, normalize those values at the presentation boundary so the loading card never
        // regresses to an opaque "Loading..." label while retaining the concrete resource name.
        val pcShellLoadingMessage: (String) -> Unit = { rawMessage ->
            if (!BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
                setLoadingMessage(rawMessage)
            } else {
                setLoadingMessage(GameFoldPcFrontendPolicy.localizeLoadingText(rawMessage))
            }
        }

        if (ProductModePolicy.capabilities(BuildConfig.GAMEFOLD_PC_LOCAL_SHELL).onlineRuntimeDownloads) {
        try {
            LaunchDependencies().ensureLaunchDependencies(
                context = context,
                container = container,
                gameSource = gameSource,
                gameId = gameId,
                setLoadingMessage = pcShellLoadingMessage,
                setLoadingProgress = setLoadingProgress,
            )
        } catch (e: Exception) {
            Timber.tag("preLaunchApp").e(e, "ensureLaunchDependencies failed")
            setLoadingDialogVisible(false)
            setMessageDialogState(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.launch_dependency_failed_title),
                    message = e.message ?: context.getString(R.string.launch_dependency_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        try {
            if (!SteamService.isImageFsInstallable(context, container.containerVariant)) {
                setLoadingMessage("正在下载首次运行文件")
                SteamService.downloadImageFs(
                    onDownloadProgress = { setLoadingProgress(it / 1.0f) },
                    this,
                    variant = container.containerVariant,
                    context = context,
                ).await()
            }
            if (container.containerVariant.equals(Container.GLIBC) &&
                !SteamService.isFileInstallable(context, "imagefs_patches_gamenative.tzst")
            ) {
                setLoadingMessage("正在下载 Wine")
                SteamService.downloadImageFsPatches(
                    onDownloadProgress = { setLoadingProgress(it / 1.0f) },
                    this,
                    context = context,
                ).await()
            }

            val experimentalDrm = GameFoldPcResourceRoot.resolve("experimental-drm-20260116.tzst")
            if (!container.isUseLegacyDRM && !container.isLaunchRealSteam &&
                !SteamService.isFileInstallable(context, "experimental-drm-20260116.tzst") &&
                experimentalDrm == null
            ) {
                setLoadingMessage("正在下载附加组件")
                SteamService.downloadFile(
                    onDownloadProgress = { setLoadingProgress(it / 1.0f) },
                    this,
                    context = context,
                    "experimental-drm-20260116.tzst",
                ).await()
            } else if (!container.isUseLegacyDRM && !container.isLaunchRealSteam && experimentalDrm != null &&
                !SteamService.isFileInstallable(context, "experimental-drm-20260116.tzst")
            ) {
                // Prime the private cache silently from the already-validated
                // public payload. This is local preparation, not a download.
                Timber.tag("preLaunchApp").i("Copying external experimental DRM archive into private cache")
                SteamService.downloadFile(
                    onDownloadProgress = { setLoadingProgress(it / 1.0f) },
                    this,
                    context,
                    "experimental-drm-20260116.tzst",
                ).await()
            }
            if ((container.isLaunchRealSteam || container.isLaunchBionicSteam) && !SteamService.isFileInstallable(context, "steam.tzst")) {
                setLoadingMessage(context.getString(R.string.main_downloading_steam))
                SteamService.downloadSteam(
                    onDownloadProgress = { setLoadingProgress(it / 1.0f) },
                    this,
                    context = context,
                ).await()
            }
            if ((container.isLaunchRealSteam || container.isLaunchBionicSteam) && !SteamService.isFileInstallable(context, "steam-token.tzst")) {
                setLoadingMessage("正在下载 Steam 登录令牌")
                SteamService.downloadFile(
                    onDownloadProgress = { setLoadingProgress(it / 1.0f) },
                    this,
                    context = context,
                    "steam-token.tzst",
                ).await()
            }
        } catch (e: Exception) {
            Timber.tag("preLaunchApp").e(e, "File download failed")
            setLoadingDialogVisible(false)
            setMessageDialogState(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.download_failed_title),
                    message = e.message ?: context.getString(R.string.download_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }
        } else {
            Timber.tag("preLaunchApp").i(
                "Local shell: public runtime validated; skipping online dependency and download stage",
            )
        }

        val loadingMessage = if (container.containerVariant.equals(Container.GLIBC)) {
            "正在安装 glibc 运行环境"
        } else {
            "正在安装 Bionic 运行环境"
        }
        setLoadingMessage(loadingMessage)
        val imageFsInstallSuccess =
            ImageFsInstaller.installIfNeededFuture(context, context.assets, container) { progress ->
                setLoadingProgress(progress / 100f)
            }.get()

        if (!imageFsInstallSuccess) {
            Timber.tag("preLaunchApp").e("ImageFS installation failed")
            setLoadingDialogVisible(false)
            setMessageDialogState(
                MessageDialogState(
                    visible = true,
                    type = DialogType.SYNC_FAIL,
                    title = context.getString(R.string.install_failed_title),
                    message = context.getString(R.string.install_failed_message),
                    dismissBtnText = context.getString(R.string.ok),
                ),
            )
            return@launch
        }

        // Do not collapse the PC shell into a generic "Loading..." state here. The
        // NS loading card is intentionally a progress/status surface and must keep
        // telling the player what stage is currently running.
        setLoadingMessage(
            if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
                "正在准备启动 ${ContainerUtils.resolveGameName(appId)}"
            } else {
                context.getString(R.string.main_loading)
            },
        )
        setLoadingProgress(-1f)

        // must activate container before downloading save files
        containerManager.activateContainer(container)

        // If another Steam game is running on this account elsewhere, prompt the user.
        // Skip the prompt for apps we don't recognise — non-Steam shortcuts on the remote
        // device report synthetic IDs that aren't kickable.
        val isSteamGame = gameSource == GameSource.STEAM
        if (isSteamGame) {
            try {
                val currentPlaying = SteamService.getSelfCurrentlyPlayingAppId()
                if (!isOffline && currentPlaying != null && currentPlaying != gameId) {
                    val otherApp = SteamService.getAppInfoOf(currentPlaying)
                    if (otherApp != null) {
                        setLoadingDialogVisible(false)
                        setMessageDialogState(
                            MessageDialogState(
                                visible = true,
                                type = DialogType.ACCOUNT_SESSION_ACTIVE,
                                title = context.getString(R.string.main_app_running_title),
                                message = context.getString(R.string.main_app_running_message, otherApp.name),
                                confirmBtnText = context.getString(R.string.main_play_anyway),
                                dismissBtnText = context.getString(R.string.cancel),
                            ),
                        )
                        return@launch
                    }
                }
            } catch (_: Exception) { /* ignore persona read errors */ }
        }

        // For Custom Games, bypass Steam Cloud operations entirely and proceed to launch
        if (isCustomGame) {
            Timber.tag("preLaunchApp").i("Custom Game detected for $appId — skipping Steam Cloud sync and launching container")
            setLoadingDialogVisible(false)
            onSuccess(context, appId, null)
            return@launch
        }

        // For GOG Games, sync cloud saves before launch (executable already verified above via GOGService.getLaunchExecutable)
        val isGOGGame = gameSource == GameSource.GOG
        if (isGOGGame) {
            if (isLocalSavesOnly) {
                Timber.tag("GOG").i("[Cloud Saves] Local saves only enabled for $appId — skipping pre-game cloud sync")
            } else {
                Timber.tag("GOG").i("[Cloud Saves] GOG Game detected for $appId — syncing cloud saves before launch")

                // Map an explicit user choice (from the conflict dialog) to a forced sync direction.
                val gogPreferredAction = when (preferredSave) {
                    SaveLocation.Local -> "forceupload" // "Keep local" -> force local up, past the conflict guard
                    SaveLocation.Remote -> "download" // "Keep remote" -> pull cloud down
                    SaveLocation.None -> null
                }

                // Initial launch (no choice yet): detect a conflict and prompt before syncing.
                if (gogPreferredAction == null) {
                    val conflict = GOGService.detectCloudSaveConflict(context, appId)
                    if (conflict != null) {
                        Timber.tag("GOG").i("[Cloud Saves] Conflict detected for $appId — prompting user")
                        val localDate = Date(conflict.localTimestamp).toString()
                        val remoteDate = Date(conflict.remoteTimestamp).toString()
                        setLoadingDialogVisible(false)
                        setMessageDialogState(
                            MessageDialogState(
                                visible = true,
                                type = DialogType.SYNC_CONFLICT,
                                title = context.getString(R.string.main_save_conflict_title),
                                message = context.getString(R.string.main_save_conflict_message, localDate, remoteDate),
                                dismissBtnText = context.getString(R.string.main_keep_local),
                                confirmBtnText = context.getString(R.string.main_keep_remote),
                            ),
                        )
                        // Wait for the user; the dialog handlers re-enter preLaunchApp with a SaveLocation.
                        return@launch
                    }
                }

                // No conflict, or the user already chose a side: perform the sync.
                val action = gogPreferredAction ?: "none"
                Timber.tag("GOG").d("[Cloud Saves] Starting pre-game sync for $appId (action: $action)")
                val syncSuccess = GOGService.syncCloudSaves(
                    context = context,
                    appId = appId,
                    preferredAction = action,
                )

                if (!syncSuccess) {
                    Timber.tag("GOG").w("[Cloud Saves] Pre-game sync failed for $appId, proceeding with launch anyway")
                    // Don't block launch on sync failure - log warning and continue
                } else {
                    Timber.tag("GOG").i("[Cloud Saves] Pre-game sync completed successfully for $appId")
                }
            }

            setLoadingDialogVisible(false)
            onSuccess(context, appId, null)
            return@launch
        }

        // For Amazon Games, skip cloud sync entirely (Amazon doesn't support cloud saves)
        val isAmazonGame = gameSource == GameSource.AMAZON
        if (isAmazonGame) {
            Timber.tag("preLaunchApp").i("Amazon Game detected for $appId — skipping cloud sync and launching container")
            setLoadingDialogVisible(false)
            onSuccess(context, appId, null)
            return@launch
        }

        // For Epic Games, sync cloud saves before launch (executable already verified above via EpicService.getLaunchExecutable)
        val isEpicGame = gameSource == GameSource.EPIC
        if (isEpicGame) {
            if (isLocalSavesOnly) {
                Timber.tag("Epic").i("[Cloud Saves] Local saves only enabled for $appId — skipping pre-game cloud sync")
            } else {
                // Handle Cloud Saves
                Timber.tag("Epic").i("[Cloud Saves] Epic Game detected for $appId — syncing cloud saves before launch")
                // Sync cloud saves (download latest saves before playing)
                Timber.tag("Epic").d("[Cloud Saves] Starting pre-game download sync for $appId")
                val syncSuccess = app.gamenative.service.epic.EpicCloudSavesManager.syncCloudSaves(
                    context = context,
                    appId = gameId,
                )

                if (!syncSuccess) {
                    Timber.tag("Epic").w("[Cloud Saves] Download sync failed for $appId, proceeding with launch anyway")
                    // Don't block launch on sync failure - log warning and continue
                } else {
                    Timber.tag("Epic").i("[Cloud Saves] Download sync completed successfully for $appId")
                }
            }

            // Delete Ownership Token if exists
            Timber.tag("Epic").i("[Ownership Tokens] Cleaning up launch tokens for Epic games...")
            EpicService.cleanupLaunchTokens(context, container)

            setLoadingDialogVisible(false)
            onSuccess(context, appId, null)
            return@launch
        }

        if (skipCloudSync || isLocalSavesOnly) {
            if (isLocalSavesOnly) {
                Timber.tag("preLaunchApp").i("Local saves only enabled for $appId — skipping Steam Cloud sync")
            } else {
                Timber.tag("preLaunchApp").w("Skipping Steam Cloud sync for $appId by user request")
            }
            setLoadingDialogVisible(false)
            onSuccess(context, appId, null)
            return@launch
        }

        // For Steam games, sync save files and check no pending remote operations are running
        val prefixToPath: (String) -> String = { prefix ->
            PathType.from(prefix).toAbsPath(container, gameId, SteamService.userSteamId!!.accountID)
        }

        // Workshop mod sync: check for updates and download if needed
        val appDao = SteamService.instance?.appDao
        val workshopMods = appDao?.getWorkshopMods(gameId) ?: false
        val enabledWorkshopIds = WorkshopManager.parseEnabledIds(
            appDao?.getEnabledWorkshopItemIds(gameId),
        )
        Timber.tag("Workshop").i(
            "Workshop launch check: workshopMods=$workshopMods, enabledIds=${enabledWorkshopIds.size}"
        )
        if (workshopMods && enabledWorkshopIds.isNotEmpty()) {
            try {
                // Skip workshop sync if Steam isn't fully connected yet
                // (can happen if the user taps Play immediately after opening the app).
                if (isOffline || !SteamService.isConnected || !SteamService.isLoggedIn) {
                    Timber.tag("Workshop").w(
                        "Steam not connected/logged in or offline, skipping workshop sync for appId=$gameId"
                    )
                    if (gameId == SlayTheSpireModTheSpireCompatibility.APP_ID) {
                        setLoadingMessage(context.getString(R.string.workshop_processing))
                        setLoadingProgress(-1f)
                        WorkshopManager.configureLocalWorkshopContentForEnabledIds(
                            context = context,
                            appId = gameId,
                            enabledIds = enabledWorkshopIds,
                        )
                    }
                } else {
                // If a background download is still running from the save
                // handler, wait for it to finish so its markers are on disk
                // before we check for updates (avoids false re-download prompt).
                SteamService.getAppDownloadInfo(gameId)?.let { activeDownload ->
                    Timber.tag("Workshop").i(
                        "Active workshop download in progress for appId=$gameId, waiting…"
                    )
                    setLoadingMessage(context.getString(R.string.workshop_checking_mods))
                    setLoadingProgress(-1f)
                    activeDownload.awaitCompletion(timeoutMs = 120_000)
                    Timber.tag("Workshop").i("Active download finished, proceeding with update check")
                }

                setLoadingMessage(context.getString(R.string.workshop_checking_mods))
                setLoadingProgress(-1f)

                val updateCheck = WorkshopManager.checkForWorkshopUpdates(
                    appId = gameId,
                    enabledIds = enabledWorkshopIds,
                    context = context,
                )

                if (updateCheck != null) {
                    val totalBytes = updateCheck.totalUpdateBytes
                    val thresholdBytes = WorkshopManager.getUpdateThresholdBytes()
                    val sizeMB = String.format(Locale.US, "%.0f", totalBytes / 1_048_576.0)
                    Timber.tag("Workshop").i(
                        "${updateCheck.itemsToSync.size} mod update(s), ${sizeMB}MB total"
                    )

                    // Decide whether to download: auto if under threshold, prompt if over
                    val shouldDownload = if (totalBytes > thresholdBytes) {
                        val userChoice = CompletableDeferred<Boolean>()
                        setLoadingDialogVisible(false)
                        setMessageDialogState(
                            MessageDialogState(
                                visible = true,
                                type = DialogType.WORKSHOP_UPDATE_PROMPT,
                                title = context.getString(R.string.workshop_update_title),
                                message = context.getString(R.string.workshop_update_prompt, sizeMB),
                                confirmBtnText = context.getString(R.string.workshop_download_btn),
                                dismissBtnText = context.getString(R.string.workshop_skip_btn),
                            )
                        )
                        workshopUpdateDeferred = userChoice
                        val result = userChoice.await()
                        workshopUpdateDeferred = null
                        setMessageDialogState(MessageDialogState(false))
                        setLoadingDialogVisible(true)
                        result
                    } else {
                        true // Under threshold, download automatically
                    }

                    var downloadedItems = false

                    if (shouldDownload) {
                        val spaceError = WorkshopManager.checkDiskSpace(
                            updateCheck.workshopContentDir, totalBytes * 2,
                        )
                        if (spaceError != null) {
                            Timber.tag("Workshop").e(spaceError)
                            SnackbarManager.show(spaceError)
                        } else {
                            val steamClient = SteamService.instance?.steamClient
                            if (steamClient != null) {
                                setLoadingMessage("正在下载 Workshop Mod（0/${updateCheck.itemsToSync.size}）")
                                val licenses = SteamService.getLicensesFromDb()
                                var currentCompleted = 0
                                var currentTitle = updateCheck.itemsToSync.firstOrNull()?.title ?: ""

                                val successCount = WorkshopManager.downloadItems(
                                    items = updateCheck.itemsToSync,
                                    steamClient = steamClient,
                                    licenses = licenses,
                                    workshopContentDir = updateCheck.workshopContentDir,
                                    onItemProgress = { completed, total, title ->
                                        currentCompleted = completed
                                        currentTitle = title
                                        setLoadingMessage(
                                            context.getString(R.string.workshop_downloading, completed, total) +
                                                "\n$title"
                                        )
                                    },
                                    onBytesProgress = { downloaded, total ->
                                        if (total > 0) {
                                            setLoadingProgress(downloaded.toFloat() / total.toFloat())
                                            setLoadingMessage(
                                                context.getString(R.string.workshop_downloading, currentCompleted, updateCheck.itemsToSync.size) +
                                                    "\n$currentTitle\n" +
                                                    "${StringUtils.formatBytes(downloaded)} / ${StringUtils.formatBytes(total)}"
                                            )
                                        }
                                    },
                                )

                                val failedCount = updateCheck.itemsToSync.size - successCount
                                if (failedCount > 0) {
                                    Timber.tag("Workshop").w("$failedCount mod update(s) failed")
                                    SnackbarManager.show(context.getString(R.string.workshop_failed_count, failedCount))
                                }
                                downloadedItems = true
                            }
                        }
                    } else {
                        Timber.tag("Workshop").i("User skipped workshop mod updates")
                    }

                    // Post-processing and symlinks (runs for both download and skip paths)
                    setLoadingMessage(context.getString(R.string.workshop_processing))
                    setLoadingProgress(-1f)
                    WorkshopManager.runPostProcessing(
                        if (downloadedItems) updateCheck.itemsToSync else null,
                        updateCheck.allItems, updateCheck.workshopContentDir,
                    ) { status ->
                        setLoadingMessage(status)
                    }
                    WorkshopManager.configureSymlinksForApp(
                        context, gameId, updateCheck.allItems,
                        updateCheck.winePrefix, updateCheck.workshopContentDir,
                    )
                }
                // If updateCheck is null, checkForWorkshopUpdates already handled everything
                Timber.tag("Workshop").i("Workshop mod sync complete for appId=$gameId")
                } // else (isLoggedIn)
            } catch (e: Exception) {
                Timber.tag("Workshop").e(e, "Workshop mod sync failed, continuing without mods")
            }
        } else if (
            gameId == SlayTheSpireModTheSpireCompatibility.APP_ID &&
            File(SteamService.getAppDirPath(gameId)).isDirectory
        ) {
            withContext(Dispatchers.IO) {
                WorkshopManager.cleanupDisabledWorkshopArtifactsForApp(context, gameId)
            }
        }

        setLoadingMessage("正在同步云存档")
        setLoadingProgress(-1f)
        val postSyncInfo = SteamService.beginLaunchApp(
            appId = gameId,
            prefixToPath = prefixToPath,
            ignorePendingOperations = ignorePendingOperations,
            preferredSave = preferredSave,
            parentScope = this,
            isOffline = isOffline,
            onProgress = { message, progress ->
                setLoadingMessage(message)
                setLoadingProgress(if (progress < 0) -1f else progress)
            },
        ).await()

        setLoadingDialogVisible(false)

        when (postSyncInfo.syncResult) {
            SyncResult.Conflict -> {
                val localDate = Date(postSyncInfo.localTimestamp).toString()
                val remoteDate = Date(postSyncInfo.remoteTimestamp).toString()
                val (conflictTitle, conflictMessage) = postSyncInfo.conflictUfsVersion
                    ?.let { v ->
                        val titleId = context.resources.getIdentifier(
                            "main_save_conflict_upgrade_v${v}_title", "string", context.packageName,
                        )
                        val msgId = context.resources.getIdentifier(
                            "main_save_conflict_upgrade_v${v}_message", "string", context.packageName,
                        )
                        if (titleId != 0 && msgId != 0) {
                            context.getString(titleId) to context.getString(msgId, localDate, remoteDate)
                        } else {
                            null
                        }
                    }
                    ?: run {
                        context.getString(R.string.main_save_conflict_title) to
                            context.getString(R.string.main_save_conflict_message, localDate, remoteDate)
                    }
                setMessageDialogState(
                    MessageDialogState(
                        visible = true,
                        type = DialogType.SYNC_CONFLICT,
                        title = conflictTitle,
                        message = conflictMessage,
                        dismissBtnText = context.getString(R.string.main_keep_local),
                        confirmBtnText = context.getString(R.string.main_keep_remote),
                    ),
                )
            }

            SyncResult.InProgress -> {
                if (useTemporaryOverride && retryCount < 5) {
                    // For intent launches, retry after a short delay (max 5 retries = ~10 seconds)
                    Timber.i("Sync in progress for intent launch, retrying in 2 seconds... (attempt ${retryCount + 1}/5)")
                    delay(2000)
                    preLaunchApp(
                        context = context,
                        appId = appId,
                        ignorePendingOperations = ignorePendingOperations,
                        preferredSave = preferredSave,
                        useTemporaryOverride = useTemporaryOverride,
                        setLoadingDialogVisible = setLoadingDialogVisible,
                        setLoadingProgress = setLoadingProgress,
                        setLoadingMessage = setLoadingMessage,
                        setMessageDialogState = setMessageDialogState,
                        onSuccess = onSuccess,
                        retryCount = retryCount + 1,
                        bootToContainer = bootToContainer,
                    )
                } else {
                    setMessageDialogState(
                        MessageDialogState(
                            visible = true,
                            type = DialogType.SYNC_IN_PROGRESS,
                            title = context.getString(R.string.sync_error_title),
                            message = context.getString(R.string.main_sync_in_progress_launch_anyway_message),
                            confirmBtnText = context.getString(R.string.main_launch_anyway),
                            dismissBtnText = context.getString(R.string.main_wait),
                        ),
                    )
                }
            }

            SyncResult.UnknownFail,
            SyncResult.DownloadFail,
            SyncResult.UpdateFail,
            -> {
                setMessageDialogState(
                    MessageDialogState(
                        visible = true,
                        type = DialogType.SYNC_FAIL,
                        title = context.getString(R.string.sync_error_title),
                        message = context.getString(R.string.main_sync_failed, postSyncInfo.syncResult.toString()),
                        dismissBtnText = context.getString(R.string.ok),
                    ),
                )
            }

            SyncResult.PendingOperations -> {
                Timber.i(
                    "Pending remote operations:${
                        postSyncInfo.pendingRemoteOperations.joinToString("\n") { pro ->
                            "\n\tmachineName: ${pro.machineName}" +
                                "\n\ttimestamp: ${Date(pro.timeLastUpdated * 1000L)}" +
                                "\n\toperation: ${pro.operation}"
                        }
                    }",
                )
                if (postSyncInfo.pendingRemoteOperations.size == 1) {
                    val pro = postSyncInfo.pendingRemoteOperations.first()
                    val gameName = SteamService.getAppInfoOf(ContainerUtils.extractGameIdFromContainerId(appId))?.name ?: ""
                    val dateStr = Date(pro.timeLastUpdated * 1000L).toString()
                    when (pro.operation) {
                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationUploadInProgress -> {
                            // maybe this should instead wait for the upload to finish and then
                            // launch the app
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    type = DialogType.PENDING_UPLOAD_IN_PROGRESS,
                                    title = context.getString(R.string.main_upload_in_progress_title),
                                    message = context.getString(
                                        R.string.main_upload_in_progress_message,
                                        gameName,
                                        pro.machineName,
                                        dateStr,
                                    ),
                                    dismissBtnText = context.getString(R.string.ok),
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationUploadPending -> {
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    type = DialogType.PENDING_UPLOAD,
                                    title = context.getString(R.string.main_pending_upload_title),
                                    message = context.getString(
                                        R.string.main_pending_upload_message,
                                        gameName,
                                        pro.machineName,
                                        dateStr,
                                    ),
                                    confirmBtnText = context.getString(R.string.main_play_anyway),
                                    dismissBtnText = context.getString(R.string.cancel),
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationAppSessionActive -> {
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    type = DialogType.APP_SESSION_ACTIVE,
                                    title = context.getString(R.string.main_app_running_title),
                                    message = context.getString(
                                        R.string.main_app_running_other_device,
                                        pro.machineName,
                                        gameName,
                                        dateStr,
                                    ),
                                    confirmBtnText = context.getString(R.string.main_play_anyway),
                                    dismissBtnText = context.getString(R.string.cancel),
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationAppSessionSuspended -> {
                            // I don't know what this means, yet
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    type = DialogType.APP_SESSION_SUSPENDED,
                                    title = context.getString(R.string.sync_error_title),
                                    message = context.getString(R.string.main_app_session_suspended),
                                    dismissBtnText = context.getString(R.string.ok),
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationNone -> {
                            // why are we here
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    type = DialogType.PENDING_OPERATION_NONE,
                                    title = context.getString(R.string.sync_error_title),
                                    message = context.getString(R.string.main_pending_operation_none),
                                    dismissBtnText = context.getString(R.string.ok),
                                ),
                            )
                        }
                    }
                } else {
                    val uploadInProgress = postSyncInfo.pendingRemoteOperations.firstOrNull {
                        it.operation == ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationUploadInProgress
                    }
                    if (uploadInProgress != null) {
                        val gameName = SteamService.getAppInfoOf(ContainerUtils.extractGameIdFromContainerId(appId))?.name ?: ""
                        setMessageDialogState(
                            MessageDialogState(
                                visible = true,
                                type = DialogType.PENDING_UPLOAD_IN_PROGRESS,
                                title = context.getString(R.string.main_upload_in_progress_title),
                                message = context.getString(
                                    R.string.main_upload_in_progress_message,
                                    gameName,
                                    uploadInProgress.machineName,
                                    Date(uploadInProgress.timeLastUpdated * 1000L).toString(),
                                ),
                                dismissBtnText = context.getString(R.string.ok),
                            ),
                        )
                    } else {
                        setMessageDialogState(
                            MessageDialogState(
                                visible = true,
                                type = DialogType.MULTIPLE_PENDING_OPERATIONS,
                                title = context.getString(R.string.sync_error_title),
                                message = context.getString(R.string.main_multiple_pending_operations_message),
                                confirmBtnText = context.getString(R.string.main_play_anyway),
                                dismissBtnText = context.getString(R.string.cancel),
                            ),
                        )
                    }
                }
            }

            SyncResult.UpToDate,
            SyncResult.Success,
            -> onSuccess(context, appId, null)
        }
    }
}

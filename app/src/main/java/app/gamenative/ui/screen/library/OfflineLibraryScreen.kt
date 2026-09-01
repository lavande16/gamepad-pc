package app.gamenative.ui.screen.library

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gamenative.ui.theme.PluviaTheme
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldPcFrontendPolicy
import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldPcPixelDialog
import app.gamenative.gamefold.ui.GameFoldPcPixelDialogAction
import app.gamenative.gamefold.ui.GameFoldPcPixelButton
import app.gamenative.gamefold.ui.GameFoldPixelWordmark
import app.gamenative.gamefold.ui.GameFoldNsTokens
import app.gamenative.gamefold.ui.gameFoldPixelMinimumWidthDp
import app.gamenative.gamefold.ui.gameFoldNsFocusFrame
import app.gamenative.gamefold.ui.gameFoldNsPanelSurface
import app.gamenative.gamefold.rear.GameFoldOrientation
import app.gamenative.gamefold.rear.VivoRearDisplayController
import app.gamenative.displayresize.DisplaySizeController
import app.gamenative.displayresize.WindowManagerCommandExecutor
import app.gamenative.gamepad.GamePadAddGamePolicy
import app.gamenative.gamepad.GamePadDisplayProfile
import app.gamenative.gamepad.GamePadHomeHeaderAction
import app.gamenative.gamepad.GamePadHomeHeaderPolicy
import app.gamenative.gamepad.GamePadProductContract
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.gamenative.data.LibraryItem
import app.gamenative.R
import app.gamenative.offline.OfflineGameEntryAction
import app.gamenative.offline.OfflineStorageAccess
import app.gamenative.offline.offlineGameEntryAction
import app.gamenative.ui.component.dialog.ContainerConfigDialog
import app.gamenative.ui.screen.PluviaScreen
import app.gamenative.utils.ContainerUtils
import app.gamenative.utils.CustomGameScanner
import app.gamenative.utils.GameFoldPcResourceRoot
import app.gamenative.utils.BrightnessManager
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import kotlin.math.roundToInt

internal enum class OfflineLibraryContent {
    STORAGE_PERMISSION,
    LOADING,
    CARTRIDGE_GRID,
}

internal fun offlineLibraryContent(
    hasStoragePermission: Boolean,
    isRefreshing: Boolean,
    gameCount: Int,
): OfflineLibraryContent = when {
    !hasStoragePermission -> OfflineLibraryContent.STORAGE_PERMISSION
    gameCount == 0 && isRefreshing -> OfflineLibraryContent.LOADING
    else -> OfflineLibraryContent.CARTRIDGE_GRID
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OfflineLibraryScreen(
    onClickPlay: (String, Boolean) -> Unit,
    onTestGraphics: (String) -> Unit,
    onPlayWithDiagnostics: (String) -> Unit,
    onClickExit: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    onControllerModalVisibilityChanged: (Boolean) -> Unit = {},
    onGameDetailsVisibilityChanged: (Boolean) -> Unit = {},
    controllerBackRequests: Int = 0,
    controllerNavigationRequests: Int = 0,
    controllerNavigationButton: GameFoldButton? = null,
    controllerAnyButtonRequests: Int = 0,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val uriHandler = LocalUriHandler.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var games by remember { mutableStateOf<List<LibraryItem>>(emptyList()) }
    var artworkByAppId by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }
    var selectedGame by remember { mutableStateOf<LibraryItem?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var hasStoragePermission by remember { mutableStateOf(OfflineStorageAccess.hasRequiredAccess(context)) }
    var showAddGameDialog by remember { mutableStateOf(false) }
    var addGameOpenedAtRequest by remember { mutableIntStateOf(0) }
    val dualScreenEnabled = false
    val dualScreenAvailable = GamePadProductContract.REAR_DISPLAY_ENABLED
    var selectedGameIndex by remember { mutableStateOf(0) }
    var controllerGameDetailsBackRequests by remember { mutableIntStateOf(0) }
    var lastConsumedControllerNavigationRequest by remember { mutableIntStateOf(0) }
    // Header actions are part of the same controller focus graph as the cartridge grid.
    // A null value means the grid/add-game rail owns focus.
    var selectedHeaderAction by remember { mutableStateOf<GamePadHomeHeaderAction?>(null) }
    val initialFocusRequester = remember { FocusRequester() }
    val headerDualFocusRequester = remember { FocusRequester() }
    val headerResizeFocusRequester = remember { FocusRequester() }
    val headerSettingsFocusRequester = remember { FocusRequester() }
    val displaySizeController = remember {
        DisplaySizeController(WindowManagerCommandExecutor())
    }
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(showAddGameDialog) {
        onControllerModalVisibilityChanged(showAddGameDialog)
    }

    LaunchedEffect(dualScreenAvailable) {
        if (!dualScreenAvailable) {
            if (dualScreenEnabled) {
                BrightnessManager.findActivity(context)?.let {
                    VivoRearDisplayController.setSessionEnabled(it, false)
                }
            }
            if (selectedHeaderAction == GamePadHomeHeaderAction.DUAL_SCREEN) {
                selectedHeaderAction = GamePadHomeHeaderAction.RESIZE
            }
        }
    }

    LaunchedEffect(controllerBackRequests) {
        if (controllerBackRequests == 0) return@LaunchedEffect
        when {
            selectedGame != null -> {
                controllerGameDetailsBackRequests++
            }
        }
    }

    LaunchedEffect(controllerAnyButtonRequests) {
        when {
            GameFoldPcFrontendPolicy.shouldDismissControllerModal(
                showAddGameDialog,
                controllerAnyButtonRequests,
                addGameOpenedAtRequest,
            ) -> showAddGameDialog = false
        }
    }

    LaunchedEffect(selectedHeaderAction) {
        if (selectedHeaderAction == null) return@LaunchedEffect
        delay(30)
        runCatching {
            when (selectedHeaderAction) {
                GamePadHomeHeaderAction.DUAL_SCREEN -> headerDualFocusRequester.requestFocus()
                GamePadHomeHeaderAction.RESIZE -> headerResizeFocusRequester.requestFocus()
                GamePadHomeHeaderAction.SETTINGS -> headerSettingsFocusRequester.requestFocus()
                null -> Unit
            }
        }
    }

    LaunchedEffect(controllerNavigationRequests, games.size) {
        val decision = GameFoldPcFrontendPolicy.controllerNavigationDecision(
            request = controllerNavigationRequests,
            lastConsumedRequest = lastConsumedControllerNavigationRequest,
            modalVisible = showAddGameDialog || selectedGame != null,
        )
        if (!decision.consumeRequest) return@LaunchedEffect
        lastConsumedControllerNavigationRequest = controllerNavigationRequests
        if (!decision.dispatchToHome) return@LaunchedEffect
        val itemCount = games.size + 1 // The final item is the Add Game cartridge.
        val columns = 4
        val headerActions = GamePadHomeHeaderPolicy.visibleActions(dualScreenAvailable)
        when (controllerNavigationButton) {
            GameFoldButton.DPAD_LEFT -> if (selectedHeaderAction != null) {
                val current = headerActions.indexOf(selectedHeaderAction).coerceAtLeast(0)
                selectedHeaderAction = headerActions[(current - 1 + headerActions.size) % headerActions.size]
            } else {
                selectedGameIndex = (selectedGameIndex - 1).coerceAtLeast(0)
            }
            GameFoldButton.DPAD_RIGHT -> if (selectedHeaderAction != null) {
                val current = headerActions.indexOf(selectedHeaderAction).coerceAtLeast(0)
                selectedHeaderAction = headerActions[(current + 1) % headerActions.size]
            } else {
                selectedGameIndex = (selectedGameIndex + 1).coerceAtMost(itemCount - 1)
            }
            GameFoldButton.DPAD_UP -> if (selectedHeaderAction != null) {
                // Keep the header rail stable at the top edge.
                selectedHeaderAction = selectedHeaderAction
            } else if (selectedGameIndex / columns == 0) {
                selectedHeaderAction = when (selectedGameIndex % columns) {
                    0 -> if (dualScreenAvailable) {
                        GamePadHomeHeaderAction.DUAL_SCREEN
                    } else {
                        GamePadHomeHeaderAction.SETTINGS
                    }
                    else -> GamePadHomeHeaderAction.SETTINGS
                }
            } else {
                selectedGameIndex = (selectedGameIndex - columns).coerceAtLeast(0)
            }
            GameFoldButton.DPAD_DOWN -> if (selectedHeaderAction != null) {
                selectedGameIndex = when (selectedHeaderAction) {
                    GamePadHomeHeaderAction.DUAL_SCREEN -> 0
                    else -> 3.coerceAtMost(itemCount - 1)
                }
                selectedHeaderAction = null
            } else {
                selectedGameIndex = (selectedGameIndex + columns).coerceAtMost(itemCount - 1)
            }
            GameFoldButton.A,
            GameFoldButton.X,
            -> when (selectedHeaderAction) {
                GamePadHomeHeaderAction.DUAL_SCREEN -> if (dualScreenAvailable) {
                    BrightnessManager.findActivity(context)?.let {
                        VivoRearDisplayController.setSessionEnabled(it, !dualScreenEnabled)
                    }
                }
                GamePadHomeHeaderAction.RESIZE -> {
                    scope.launch(Dispatchers.IO) {
                        toggleDisplaySizeSilently(context, displaySizeController)
                    }
                }
                GamePadHomeHeaderAction.SETTINGS -> onNavigateRoute(PluviaScreen.Settings.route)
                null -> if (selectedGameIndex < games.size) {
                    onClickPlay(games[selectedGameIndex].appId, false)
                } else {
                    addGameOpenedAtRequest = controllerAnyButtonRequests
                    showAddGameDialog = true
                }
            }
            else -> Unit
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        hasStoragePermission = OfflineStorageAccess.hasRequiredAccess(context)
    }

    fun refresh() {
        if (isRefreshing || !hasStoragePermission) return
        scope.launch {
            isRefreshing = true
            val scannedGames = withContext(Dispatchers.IO) {
                CustomGameScanner.scanAsLibraryItems(query = "")
            }
            games = scannedGames
            // Publish the library immediately. Artwork resolution can extract icons and walk a
            // bounded executable tree, so each game updates independently instead of one slow
            // title holding every cartridge blank until the full associate() completes.
            artworkByAppId = emptyMap()
            isRefreshing = false
            scannedGames.forEach { game ->
                scope.launch {
                    val artwork = withContext(Dispatchers.IO) {
                        CustomGameScanner.findLocalArtworkForCustomGame(context, game.appId)
                    }
                    artworkByAppId = artworkByAppId + (game.appId to artwork)
                }
            }
        }
    }

    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) refresh() else {
            games = emptyList()
            artworkByAppId = emptyMap()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = OfflineStorageAccess.hasRequiredAccess(context)
                hasStoragePermission = granted
                if (granted) refresh() else {
                    games = emptyList()
                    artworkByAppId = emptyMap()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler {
        when {
            selectedGame != null -> {
                selectedGame = null
                onGameDetailsVisibilityChanged(false)
            }
            else -> onClickExit()
        }
    }

    selectedGame?.let { game ->
        OfflineGameDetailsScreen(
            libraryItem = game,
            artworkUri = artworkByAppId[game.appId],
            onLaunch = { onClickPlay(game.appId, false) },
            controllerBackRequests = controllerGameDetailsBackRequests,
            onBack = {
                selectedGame = null
                onGameDetailsVisibilityChanged(false)
            },
        )
        return
    }

    val localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL
    val presentation = GameFoldPcFrontendPolicy.forProduct(localShell)
    val pixelBackground = if (presentation.contentConstrainedToDisplay) {
        GameFoldNsTokens.Background
    } else {
        colorResource(R.color.gamefold_splash_background)
    }
    val pixelSurface = if (presentation.contentConstrainedToDisplay) {
        GameFoldNsTokens.Surface
    } else {
        colorResource(R.color.gamefold_icon_background)
    }
    val pixelText = if (presentation.contentConstrainedToDisplay) {
        GameFoldNsTokens.Ink
    } else {
        colorResource(R.color.pixel_text_light)
    }
    Scaffold(
        containerColor = pixelBackground,
        topBar = {
            if (presentation.contentConstrainedToDisplay) {
                GameFoldPcHeader(
                    textColor = pixelText,
                    onDualScreen = {
                        selectedHeaderAction = GamePadHomeHeaderAction.DUAL_SCREEN
                        BrightnessManager.findActivity(context)?.let {
                            VivoRearDisplayController.setSessionEnabled(it, !dualScreenEnabled)
                        }
                    },
                    onResize = {
                        selectedHeaderAction = GamePadHomeHeaderAction.RESIZE
                        scope.launch(Dispatchers.IO) {
                            toggleDisplaySizeSilently(context, displaySizeController)
                        }
                    },
                    dualScreenEnabled = dualScreenEnabled,
                    dualScreenAvailable = dualScreenAvailable,
                    onSettings = {
                        selectedHeaderAction = GamePadHomeHeaderAction.SETTINGS
                        onNavigateRoute(PluviaScreen.Settings.route)
                    },
                    dualScreenFocused = selectedHeaderAction == GamePadHomeHeaderAction.DUAL_SCREEN,
                    resizeFocused = selectedHeaderAction == GamePadHomeHeaderAction.RESIZE,
                    settingsFocused = selectedHeaderAction == GamePadHomeHeaderAction.SETTINGS,
                    dualScreenFocusRequester = headerDualFocusRequester,
                    resizeFocusRequester = headerResizeFocusRequester,
                    settingsFocusRequester = headerSettingsFocusRequester,
                )
            } else {
                TopAppBar(
                    title = { Text("GamePad PC", color = pixelText) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = pixelBackground,
                        titleContentColor = pixelText,
                        actionIconContentColor = pixelText,
                    ),
                    actions = {
                        TextButton(onClick = { onNavigateRoute(PluviaScreen.Settings.route) }) {
                            Text("高级设置", color = pixelText)
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = ::refresh,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            when (
                offlineLibraryContent(
                    hasStoragePermission = hasStoragePermission,
                    isRefreshing = isRefreshing,
                    gameCount = games.size,
                )
            ) {
                OfflineLibraryContent.STORAGE_PERMISSION -> StoragePermissionPrompt(
                    onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            CustomGameScanner.requestManageExternalStoragePermission(context)
                        } else {
                            storagePermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                ),
                            )
                        }
                    },
                )
                OfflineLibraryContent.LOADING -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                OfflineLibraryContent.CARTRIDGE_GRID -> GameList(
                    games = games,
                    artworkByAppId = artworkByAppId,
                    textColor = pixelText,
                    nsDesign = presentation.contentConstrainedToDisplay,
                    initialFocusRequester = initialFocusRequester,
                    onLaunch = { onClickPlay(it.appId, false) },
                    onDetails = {
                        selectedGame = it
                        onGameDetailsVisibilityChanged(true)
                    },
                    onAddGame = {
                        addGameOpenedAtRequest = controllerAnyButtonRequests
                        showAddGameDialog = true
                    },
                    selectedIndex = selectedGameIndex.takeIf { selectedHeaderAction == null } ?: -1,
                    onSelectionChange = {
                        selectedHeaderAction = null
                        selectedGameIndex = it
                    },
                )
            }
        }
    }

    LaunchedEffect(games.size, hasStoragePermission) {
        if (hasStoragePermission) {
            // The shell's ComposeView receives focus one frame after the library grid is
            // attached. Retry after that frame so a cold home entry has the same green NS
            // selection frame as a controller-driven entry.
            runCatching { initialFocusRequester.requestFocus() }
            delay(120)
            runCatching { initialFocusRequester.requestFocus() }
        }
    }

    if (showAddGameDialog) {
        val addGameMessage = "将游戏文件夹放入 GamePad PC 游戏目录后，点击刷新即可。\n\n路径：${gamePadAddGameDialogPathLabel()}"
        val openDownloadPage = {
            showAddGameDialog = false
            runCatching { GamePadAddGamePolicy.openDownloadPage(uriHandler::openUri) }
            Unit
        }
        if (localShell) {
            GameFoldPcPixelDialog(
                visible = true,
                title = "添加 PC 游戏",
                message = addGameMessage,
                actions = listOf(
                    GameFoldPcPixelDialogAction("关闭", { showAddGameDialog = false }),
                    GameFoldPcPixelDialogAction("下载游戏", openDownloadPage, primary = true),
                ),
                cardWidthFraction = gamePadAddGameDialogWidthFraction(configuration.screenWidthDp),
                onDismissRequest = { showAddGameDialog = false },
            )
        } else {
            AlertDialog(
                onDismissRequest = { showAddGameDialog = false },
                containerColor = pixelSurface,
                titleContentColor = pixelText,
                textContentColor = pixelText,
                title = { Text("添加 PC 游戏") },
                text = {
                    Text(
                        addGameMessage,
                    )
                },
                confirmButton = {
                    TextButton(onClick = openDownloadPage) { Text("下载游戏", color = pixelText) }
                },
                dismissButton = {
                    TextButton(onClick = { showAddGameDialog = false }) { Text("关闭", color = pixelText) }
                },
            )
        }
    }

}

private fun toggleDisplaySizeSilently(
    context: Context,
    controller: DisplaySizeController,
) {
    try {
        val current = controller.read()
        val hasPermission = context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED
        val result = controller.toggle(current, hasPermission)
        if (result.isSuccess) {
            result.snapshot()?.let { GamePadDisplayProfile.apply(context, it) }
        } else {
            Log.w("GamePadDisplayResize", result.message())
        }
    } catch (error: Throwable) {
        Log.w("GamePadDisplayResize", "Display-size toggle failed", error)
    }
}

@Composable
private fun GameFoldPcHeader(
    textColor: Color,
    dualScreenEnabled: Boolean,
    dualScreenAvailable: Boolean,
    onDualScreen: () -> Unit,
    onResize: () -> Unit,
    onSettings: () -> Unit,
    dualScreenFocusRequester: FocusRequester? = null,
    resizeFocusRequester: FocusRequester? = null,
    settingsFocusRequester: FocusRequester? = null,
    dualScreenFocused: Boolean = false,
    resizeFocused: Boolean = false,
    settingsFocused: Boolean = false,
) {
    val horizontalInsetDp = gamePadHeaderHorizontalInsetDp(
        screenWidthDp = LocalConfiguration.current.screenWidthDp,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // NS places a 48dp header inside the stage's 8dp top/bottom gutter.  Keeping the
            // complete 64dp contract here also moves the content grid to the same first row.
            .height(64.dp)
            .padding(
                start = horizontalInsetDp.dp,
                end = horizontalInsetDp.dp,
                top = 8.dp,
                bottom = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.gamefold_pc_app_icon),
            contentDescription = "GamePad PC",
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        GameFoldPixelWordmark(
            text = "GAMEPAD PC",
            color = textColor,
            modifier = Modifier
                .padding(start = 8.dp)
                .width(gameFoldPixelMinimumWidthDp("GAMEPAD PC", heightDp = 18).dp)
                .height(18.dp),
        )
        Spacer(Modifier.weight(1f))
        if (dualScreenAvailable) {
            HeaderPixelAction(
                if (dualScreenEnabled) R.drawable.gamefold_dual_screen_on_ns else R.drawable.gamefold_dual_screen_off_ns,
                "双屏显示",
                false,
                onDualScreen,
                tint = null,
                ownsFrame = true,
                focusRequester = dualScreenFocusRequester,
                semanticFocused = dualScreenFocused,
            )
            Spacer(Modifier.width(8.dp))
        }
        HeaderPixelAction(
            R.drawable.pixel_icon_resize,
            "切换显示分辨率",
            false,
            onResize,
            tint = Color.White,
            backgroundRes = R.drawable.pixel_resize_button_normal,
            centerOnUnshadowedFace = true,
            focusRequester = resizeFocusRequester,
            semanticFocused = resizeFocused,
        )
        Spacer(Modifier.width(8.dp))
        HeaderPixelAction(
            R.drawable.pixel_icon_settings,
            "高级设置",
            true,
            onSettings,
            centerOnUnshadowedFace = true,
            focusRequester = settingsFocusRequester,
            semanticFocused = settingsFocused,
        )
    }
}

@Composable
private fun HeaderPixelAction(
    iconRes: Int,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    tint: Color? = null,
    ownsFrame: Boolean = false,
    backgroundRes: Int? = null,
    centerOnUnshadowedFace: Boolean = false,
    focusRequester: FocusRequester? = null,
    semanticFocused: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val visual = gameFoldHeaderActionVisual(
        selected = selected,
        focused = focused,
        semanticFocused = semanticFocused,
    )
    val focusLayer = GameFoldPcFrontendPolicy.headerFocusLayer(
        ownsFrame = ownsFrame,
        focused = visual.showForegroundFocus,
    )
    val iconOffsetDp = GameFoldPcFrontendPolicy.headerIconOffsetDp(
        centerOnUnshadowedFace = centerOnUnshadowedFace,
        focusLayer = focusLayer,
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (focusLayer == GameFoldPcFrontendPolicy.HeaderFocusLayer.BACKING) {
            // The dual-screen drawable owns its complete 48dp face. Keep that face untouched and
            // reveal the centered focus frame from behind its four edges.
            Box(
                modifier = Modifier
                    .requiredSize(56.dp)
                    .gameFoldNsFocusFrame(true),
            )
        }
        if (!ownsFrame) {
            // These are Android layer-list drawables, not vector/raster resources. Compose's
            // painterResource intentionally rejects layer-lists, so let ImageView render the
            // exact NS source asset instead of approximating its pixel shadow in Compose.
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.FIT_XY
                        setImageResource(
                            backgroundRes ?: if (selected) {
                                R.drawable.pixel_action_button_normal
                            } else {
                                R.drawable.pixel_button_normal
                            },
                        )
                    }
                },
                update = { imageView ->
                    imageView.setImageResource(
                        backgroundRes ?: if (selected) {
                            R.drawable.pixel_action_button_normal
                        } else {
                            R.drawable.pixel_button_normal
                        },
                    )
                },
            )
        }
        Image(
            painter = painterResource(iconRes),
            contentDescription = description,
            // The dual-screen source already contains its 48dp button face.  Shrinking that
            // complete asset to 30dp made it visibly smaller than the two composed buttons.
            modifier = if (ownsFrame) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .size(30.dp)
                    .offset(x = iconOffsetDp.dp, y = iconOffsetDp.dp)
            },
            colorFilter = (if (ownsFrame) tint else tint ?: visual.iconTint)?.let { ColorFilter.tint(it) },
        )
        if (focusLayer == GameFoldPcFrontendPolicy.HeaderFocusLayer.FOREGROUND) {
            // The composed Settings face retains its existing foreground ring.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .gameFoldNsFocusFrame(true),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OfflineGameDetailsScreen(
    libraryItem: LibraryItem,
    artworkUri: String?,
    onLaunch: () -> Unit,
    controllerBackRequests: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var containerConfig by remember(libraryItem.appId) {
        mutableStateOf<com.winlator.container.ContainerData?>(null)
    }
    var showConfigEditor by remember(libraryItem.appId) { mutableStateOf(false) }

    BackHandler {
        if (showConfigEditor) showConfigEditor = false else onBack()
    }

    LaunchedEffect(controllerBackRequests) {
        if (controllerBackRequests == 0) return@LaunchedEffect
        if (showConfigEditor) showConfigEditor = false else onBack()
    }

    LaunchedEffect(libraryItem.appId) {
        containerConfig = withContext(Dispatchers.IO) {
            ContainerUtils.toContainerData(
                ContainerUtils.getOrCreateContainer(context, libraryItem.appId),
            )
        }
    }

    val config = containerConfig
    if (showConfigEditor && config != null) {
        ContainerConfigDialog(
            title = "${libraryItem.name} 设置",
            initialConfig = config,
            onDismissRequest = { showConfigEditor = false },
            onSave = { newConfig ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        ContainerUtils.applyToContainer(context, libraryItem.appId, newConfig)
                    }
                    containerConfig = newConfig
                    showConfigEditor = false
                }
            },
        )
        return
    }

    val backFocus = remember { FocusRequester() }
    val settingsFocus = remember { FocusRequester() }
    val startFocus = remember { FocusRequester() }
    val detailsHorizontalInsetDp = gamePadOwnedPageHorizontalInsetDp(
        screenWidthDp = LocalConfiguration.current.screenWidthDp,
        localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
    )
    LaunchedEffect(config) {
        delay(80)
        runCatching { settingsFocus.requestFocus() }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(GameFoldNsTokens.Background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = detailsHorizontalInsetDp.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp).padding(bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp)
                        .gameFoldNsPanelSurface()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeaderPixelAction(
                        iconRes = R.drawable.pixel_icon_back,
                        description = "返回",
                        selected = false,
                        onClick = onBack,
                        focusRequester = backFocus,
                    )
                    LocalGameArtwork(
                        artworkUri = artworkUri,
                        contentDescription = libraryItem.name,
                        modifier = Modifier.padding(start = 16.dp).size(112.dp)
                            .border(2.dp, GameFoldNsTokens.Border, RectangleShape),
                    )
                    Column(Modifier.padding(horizontal = 16.dp).weight(1f)) {
                        Text(
                            text = libraryItem.name,
                            color = GameFoldNsTokens.Ink,
                            fontFamily = GameFoldNsTokens.LabelFont,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "PC GAME",
                            color = GameFoldNsTokens.Surface,
                            fontFamily = GameFoldNsTokens.NumericFont,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(GameFoldNsTokens.Shadow, RectangleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
                app.gamenative.gamefold.ui.GameFoldNsSettingsRow(
                    title = { Text("游戏设置") },
                    subtitle = {
                        Text(if (config == null) "正在加载…" else "图形、模拟、控制器与运行参数")
                    },
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.pixel_icon_settings),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                        )
                    },
                    onClick = { if (config != null) showConfigEditor = true },
                    modifier = Modifier.focusRequester(settingsFocus),
                )
            }
            app.gamenative.gamefold.ui.GameFoldNsButton(
                label = "启动游戏",
                onClick = onLaunch,
                primary = true,
                focusRequester = startFocus,
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.pixel_icon_play),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).width(144.dp),
            )
        }
    }
}

@Composable
private fun StoragePermissionPrompt(onGrant: () -> Unit) {
    val context = LocalContext.current
    if (!BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("需要文件访问权限", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "GamePad PC 需要访问 ${GameFoldPcResourceRoot.root().path}，才能扫描并启动本地游戏。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onGrant) { Text("去授权") }
            }
        }
        return
    }

    val ink = GameFoldNsTokens.Ink
    val muted = GameFoldNsTokens.Muted
    Box(
        modifier = Modifier.fillMaxSize().background(GameFoldNsTokens.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.86f),
        ) {
            Text("首次启动 · 素材访问", color = muted, fontFamily = GameFoldNsTokens.BodyFont, fontSize = 14.sp)
            GameFoldPixelWordmark(
                text = "GAMEPAD PC",
                color = ink,
                bold = true,
                modifier = Modifier.padding(top = 8.dp).width(190.dp).height(26.dp),
            )
            Text(
                "请允许访问本地素材",
                color = ink,
                fontFamily = GameFoldNsTokens.LabelFont,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                "GamePad PC 只扫描 ${GameFoldPcResourceRoot.root().path}，用于识别并启动本地游戏。",
                color = muted,
                fontFamily = GameFoldNsTokens.BodyFont,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text("素材源 · ${GameFoldPcResourceRoot.root().path}", color = muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                GameFoldPcPixelButton(
                    label = "暂不授权",
                    onClick = { (context as? Activity)?.finish() },
                )
                GameFoldPcPixelButton(
                    label = "确认授权",
                    onClick = onGrant,
                    primary = true,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameList(
    games: List<LibraryItem>,
    artworkByAppId: Map<String, String?>,
    textColor: Color,
    nsDesign: Boolean,
    initialFocusRequester: FocusRequester,
    onLaunch: (LibraryItem) -> Unit,
    onDetails: (LibraryItem) -> Unit,
    onAddGame: () -> Unit,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
) {
    val portraitCompact = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val gridWidthFraction = if (nsDesign && !portraitCompact) {
        gameFoldHomeGridWidthFraction(screenWidthDp)
    } else {
        1f
    }
    val cardScaleWithinGrid = if (nsDesign && !portraitCompact) {
        gameFoldHomeCardScaleWithinTightenedGrid()
    } else {
        gameFoldHomeCardScale()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      LazyVerticalGrid(
        columns = if (nsDesign) GridCells.Fixed(4) else GridCells.Adaptive(minSize = 168.dp),
        // NS uses the full usable display width: four cartridges sit in one row with compact,
        // even gutters. Keep the PC cover square inside the same cartridge silhouette.
        modifier = if (nsDesign) {
            Modifier.fillMaxHeight().fillMaxWidth(gridWidthFraction)
        } else {
            Modifier.fillMaxSize()
        },
        // NS has 8dp root padding + 8dp content margin + 8dp RecyclerView padding before each
        // item's own 4dp padding.  This 24dp grid inset reproduces the same measured columns.
        contentPadding = if (nsDesign) PaddingValues(
            start = gameFoldHomeGridHorizontalPaddingDp(portraitCompact).dp,
            end = gameFoldHomeGridHorizontalPaddingDp(portraitCompact).dp,
            top = 12.dp,
            bottom = 8.dp,
        )
            else PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(if (nsDesign) 0.dp else 20.dp),
        horizontalArrangement = Arrangement.spacedBy(if (nsDesign) 0.dp else 14.dp),
      ) {
        itemsIndexed(games, key = { _, game -> game.appId }) { index, game ->
            var focused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
              Column(
                modifier = Modifier.fillMaxWidth(if (nsDesign) cardScaleWithinGrid else 1f)
                    .then(
                        if (nsDesign) Modifier.padding(
                            if (portraitCompact) 8.dp else gameFoldHomeCardOuterPaddingDp().dp,
                        )
                        else Modifier,
                    )
                    .combinedClickable(
                    onClick = {
                        when (offlineGameEntryAction(longPressed = false)) {
                            OfflineGameEntryAction.Launch -> onLaunch(game)
                            OfflineGameEntryAction.Details -> onDetails(game)
                        }
                    },
                    onLongClick = {
                        when (offlineGameEntryAction(longPressed = true)) {
                            OfflineGameEntryAction.Launch -> onLaunch(game)
                            OfflineGameEntryAction.Details -> onDetails(game)
                        }
                    },
                ).onFocusChanged {
                    focused = it.isFocused
                    if (it.isFocused) onSelectionChange(index)
                }
                    .focusable()
                    .then(if (game == games.firstOrNull()) Modifier.focusRequester(initialFocusRequester) else Modifier),
            ) {
                // GameFold NS cartridge silhouette: the PC cover remains
                // square, while the surrounding shell and title follow the
                // official handheld library treatment.
                PixelCartridge(
                    artworkUri = artworkByAppId[game.appId],
                    contentDescription = game.name,
                    focused = focused || selectedIndex == index,
                )
                if (nsDesign) {
                    GameFoldCartridgeTitle(game.name, textColor, maxLines = 2)
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        game.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    )
                }
              }
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
                AddGameCartridge(
                    onClick = onAddGame,
                    textColor = textColor,
                    focused = selectedIndex == games.size,
                    portraitCompact = portraitCompact,
                    modifier = Modifier
                        .fillMaxWidth(if (nsDesign) cardScaleWithinGrid else 1f)
                        .then(
                            if (games.isEmpty()) Modifier.focusRequester(initialFocusRequester)
                            else Modifier,
                        ),
                )
            }
        }
      }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun AddGameCartridge(
    onClick: () -> Unit,
    textColor: Color,
    focused: Boolean = false,
    portraitCompact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(if (portraitCompact) 8.dp else gameFoldHomeCardOuterPaddingDp().dp)
            .combinedClickable(onClick = onClick, onLongClick = onClick)
            .focusable(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(343f / 392f),
        ) {
            Image(
                painter = painterResource(if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) R.drawable.gamefold_add_game_cartridge_ns else R.drawable.gamefold_add_game_cartridge),
                contentDescription = "添加游戏",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(gamefoldAddGameContentInsetDp().dp),
                contentScale = ContentScale.FillBounds,
            )
            if (focused && BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
                Image(
                    painter = painterResource(R.drawable.gamefold_cartridge_focus_frame_ns),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
        GameFoldCartridgeTitle("添加游戏", textColor, maxLines = 1)
    }
}

@Composable
private fun PixelCartridge(
    artworkUri: String?,
    contentDescription: String,
    focused: Boolean = false,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(343f / 392f),
    ) {
        val artworkWidth = maxWidth * gamefoldCartridgeArtworkWidthRatio()
        val artworkTop = maxWidth * gamefoldCartridgeArtworkTopRatio()
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(artworkWidth)
                    .aspectRatio(1f)
                    .align(Alignment.TopCenter)
                    .offset(y = artworkTop)
                    .clip(RectangleShape),
            ) {
                LocalGameArtwork(
                    artworkUri = artworkUri,
                    contentDescription = contentDescription,
                    // A tiny overscan removes source-image edge mattes while the parent keeps
                    // the exact NS square window. It never changes the cartridge geometry.
                    modifier = Modifier.fillMaxSize().scale(1.035f),
                )
            }
            Image(
                painter = painterResource(R.drawable.gamefold_cartridge_frame),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(gamefoldCartridgeContentInsetDp().dp),
                contentScale = ContentScale.FillBounds,
            )
            if (focused) {
                Image(
                    painter = painterResource(R.drawable.gamefold_cartridge_focus_frame_ns),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}

@Composable
private fun GameFoldCartridgeTitle(text: String, textColor: Color, maxLines: Int) {
    Spacer(Modifier.height(gamefoldCartridgeTitleGapDp().dp))
    Text(
        text = text,
        color = textColor,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        fontFamily = GameFoldNsTokens.NumericFont,
        fontSize = gameFoldCartridgeTitleFontSizeSp().sp,
        lineHeight = 16.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 4.dp),
    )
}

/** Stable geometry contract for the square PC cover inside the NS cartridge frame. */
internal fun gamefoldCartridgeArtworkTopRatio(): Float = 8f / 100f

/** card_game_grid.xml uses 14dp start/end margins in a 100dp cartridge reference. */
internal fun gamefoldCartridgeArtworkWidthRatio(): Float = 80f / 100f

/** NS card_game_grid.xml keeps normal cover+frame 3dp inside the full focus layer. */
internal fun gamefoldCartridgeContentInsetDp(): Int = 3

/** Add Game must share the normal cartridge's visible frame bounds. */
internal fun gamefoldAddGameContentInsetDp(): Int = gamefoldCartridgeContentInsetDp()

/** Both regular and Add Game titles use the same NS top margin. */
internal fun gamefoldCartridgeTitleGapDp(): Int = 4

internal fun gameFoldHomeCardScale(): Float = 0.7f

internal fun gameFoldHomeCardOuterPaddingDp(): Int = 4

internal fun gameFoldCartridgeTitleFontSizeSp(): Int = 12

private const val GAMEPAD_HOME_GRID_TIGHTENING = 0.8f

internal fun gameFoldHomeGridWidthFraction(screenWidthDp: Int): Float {
    val horizontalPadding = gameFoldHomeGridHorizontalPaddingDp(portrait = false) * 2f
    val tightenedContentWidth = (screenWidthDp - horizontalPadding) * GAMEPAD_HOME_GRID_TIGHTENING
    return ((tightenedContentWidth + horizontalPadding) / screenWidthDp).coerceIn(0f, 1f)
}

internal fun gameFoldHomeCardScaleWithinTightenedGrid(): Float =
    gameFoldHomeCardScale() / GAMEPAD_HOME_GRID_TIGHTENING

internal fun gamePadHeaderHorizontalInsetDp(screenWidthDp: Int): Int {
    val gridInset = gameFoldHomeGridHorizontalPaddingDp(portrait = false)
    val gridWidth = screenWidthDp * gameFoldHomeGridWidthFraction(screenWidthDp)
    val gridStart = (screenWidthDp - gridWidth) / 2f
    val cellWidth = (gridWidth - gridInset * 2) / 4f
    val scaledCardWidth = cellWidth * gameFoldHomeCardScaleWithinTightenedGrid()
    return (gridStart + gridInset + (cellWidth - scaledCardWidth) / 2f + gameFoldHomeCardOuterPaddingDp())
        .roundToInt()
}

internal fun gamePadAddGameDialogHorizontalInsetDp(screenWidthDp: Int): Int =
    gamePadHeaderHorizontalInsetDp(screenWidthDp)

internal fun gamePadAddGameDialogWidthFraction(screenWidthDp: Int): Float {
    if (screenWidthDp <= 0) return 0.84f
    val inset = gamePadAddGameDialogHorizontalInsetDp(screenWidthDp)
    return ((screenWidthDp - inset * 2f) / screenWidthDp).coerceIn(0.48f, 0.92f)
}

internal fun gamePadAddGameDialogPathLabel(): String = "根目录/GamePadPC/Game"

internal fun gamePadOwnedPageHorizontalInsetDp(
    screenWidthDp: Int,
    localShell: Boolean,
): Int = if (localShell) gamePadHeaderHorizontalInsetDp(screenWidthDp) else 0

internal fun gamePadHeaderStartInsetDp(screenWidthDp: Int): Int =
    gamePadHeaderHorizontalInsetDp(screenWidthDp)

internal fun gamePadHeaderEndInsetDp(screenWidthDp: Int): Int =
    gamePadHeaderHorizontalInsetDp(screenWidthDp)

internal data class GameFoldHeaderActionVisual(
    val iconTint: Color,
    val showForegroundFocus: Boolean,
)

internal fun gameFoldHeaderActionVisual(
    selected: Boolean,
    focused: Boolean,
    semanticFocused: Boolean,
): GameFoldHeaderActionVisual = GameFoldHeaderActionVisual(
    iconTint = if (selected) Color.White else GameFoldNsTokens.Ink,
    showForegroundFocus = focused || semanticFocused,
)

internal fun gameFoldHomeGridHorizontalPaddingDp(portrait: Boolean): Int =
    if (portrait) 64 else 48

@Composable
private fun LocalGameArtwork(
    artworkUri: String?,
    contentDescription: String,
    modifier: Modifier = Modifier.size(180.dp),
) {
    val placeholder = painterResource(R.drawable.ic_logo_color)
    CoilImage(
        modifier = modifier
            .clip(RectangleShape),
        imageModel = { artworkUri ?: R.drawable.ic_logo_color },
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            contentDescription = contentDescription,
        ),
        loading = {
            Image(
                painter = placeholder,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        },
        failure = {
            Image(
                painter = placeholder,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        },
        previewPlaceholder = placeholder,
    )
}

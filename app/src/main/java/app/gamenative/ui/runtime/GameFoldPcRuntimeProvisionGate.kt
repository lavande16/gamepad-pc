package app.gamenative.ui.runtime

import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldPcShellHost
import app.gamenative.gamefold.ui.GameFoldControllerListener
import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldStick
import app.gamenative.gamefold.ui.GameFoldPcPixelButton
import app.gamenative.gamefold.ui.GameFoldPixelWordmark
import app.gamenative.gamefold.ui.GameFoldNsTokens
import app.gamenative.gamefold.ui.GameFoldPcPixelDialog
import app.gamenative.gamefold.ui.GameFoldPcPixelDialogAction
import app.gamenative.gamefold.ui.gameFoldNsPanelSurface
import app.gamenative.ui.components.GameFoldRetroLoadingCard
import app.gamenative.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.RectangleShape
import app.gamenative.offline.OfflineStorageAccess
import app.gamenative.product.GameFoldPcProcessorPolicy
import app.gamenative.product.GameFoldPcStartupRoute
import app.gamenative.product.GameFoldPcStartupRoutePolicy
import app.gamenative.gamefold.startup.AndroidGameFoldPcRuntimeGateway
import app.gamenative.gamefold.startup.GameFoldPcRuntimeProvisionStore
import app.gamenative.gamefold.startup.GameFoldPcRuntimeProvisioner
import app.gamenative.gamefold.startup.GameFoldPcRuntimeProvisionCoordinator
import app.gamenative.gamefold.startup.RuntimeProvisionProgress
import app.gamenative.gamefold.startup.RuntimeProvisionResult
import app.gamenative.utils.CustomGameScanner
import app.gamenative.utils.GameFoldPcResourceRoot
import kotlinx.coroutines.launch
import java.io.File
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun GameFoldPcRuntimeProvisionGate(
    enabled: Boolean = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
    // The released GamePad PC flow always enters the homepage after the processor and
    // permission checks. Runtime archives remain on the mature click-to-launch path.
    preloadRuntime: Boolean = false,
    onStartupRouteReady: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        SideEffect { onStartupRouteReady() }
        content()
        return
    }

    // Mature GamePad PC startup contract: permission + processor check only.
    // Runtime archives are installed by preLaunchLocalShell when a concrete
    // game is opened. Keeping this switch explicit lets the long-running
    // provisioning implementation remain covered by tests without blocking the
    // user on first home-screen entry.
    if (!preloadRuntime) {
        GameFoldPcPermissionProcessorGate(
            onStartupRouteReady = onStartupRouteReady,
            content = content,
        )
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<RuntimeProvisionResult?>(null) }
    var progress by remember { mutableStateOf<RuntimeProvisionProgress?>(null) }
    var refreshInFlight by remember { mutableStateOf(false) }
    fun refresh() {
        if (refreshInFlight) return
        refreshInFlight = true
        scope.launch {
            try {
                result = null
                result = if (!GameFoldPcProcessorPolicy.isSupported(Build.SUPPORTED_ABIS)) {
                    RuntimeProvisionResult.Failed(
                        app.gamenative.gamefold.startup.RuntimeProvisionPhase.VALIDATING_PUBLIC_PAYLOAD,
                        "此设备不支持 ARM64 GamePad PC 运行时",
                    )
                } else {
                    // Runtime installation ultimately waits on a Java Future and
                    // performs filesystem work. Keep that entire operation off
                    // the Compose/UI dispatcher; otherwise the first-run preload
                    // can trigger a focus/input ANR while ImageFS is unpacked.
                    withContext(Dispatchers.IO) {
                        GameFoldPcRuntimeProvisionCoordinator.ensureReady(context, onProgress = { update ->
                            scope.launch { progress = update }
                        })
                    }
                }
            } finally {
                refreshInFlight = false
            }
        }
    }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { refresh() }

    LaunchedEffect(Unit) { refresh() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SideEffect { onStartupRouteReady() }
    when (val state = result) {
        is RuntimeProvisionResult.Ready -> content()
        is RuntimeProvisionResult.PermissionRequired -> ProvisionMessage(
            title = "需要文件访问权限",
            message = "首次运行 GamePad PC 需要访问 /storage/emulated/0/GamePadPC 中的运行文件。",
            action = "打开权限设置" to {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    CustomGameScanner.requestManageExternalStoragePermission(context)
                } else {
                    legacyPermissionLauncher.launch(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    )
                }
            },
        )
        is RuntimeProvisionResult.PayloadInvalid -> ProvisionMessage(
            title = "请先下载 GamePad PC 运行文件",
            message = (state.missing + state.invalid).joinToString("\n").ifBlank {
                "请检查 /storage/emulated/0/GamePadPC/manifest.json。"
            } + "\n\n下载页面：gamenative.app",
            action = "重新检查" to ::refresh,
        )
        is RuntimeProvisionResult.Failed -> ProvisionMessage(
            title = "正在准备 GamePad PC",
            message = state.message,
            action = "重试" to ::refresh,
        )
        null -> ProvisionProgress(progress)
    }
}

@Composable
private fun GameFoldPcPermissionProcessorGate(
    onStartupRouteReady: () -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var route by remember(context) { mutableStateOf(resolveStartupRoute(context)) }
    fun refresh() {
        route = resolveStartupRoute(context)
    }
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { refresh() }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    SideEffect { onStartupRouteReady() }
    when (route) {
        GameFoldPcStartupRoute.READY -> content()
        GameFoldPcStartupRoute.UNSUPPORTED_MTK -> GameFoldPcMtkUnsupportedScreen()
        GameFoldPcStartupRoute.UNSUPPORTED_ABI -> {
            GameFoldPcStartupPermissionDialog(
                unsupported = true,
                onGrant = ::refresh,
                onDecline = { (context as? android.app.Activity)?.finish() },
            )
        }

        GameFoldPcStartupRoute.PERMISSION_REQUIRED -> {
            GameFoldPcStartupPermissionDialog(
                unsupported = false,
                onGrant = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        CustomGameScanner.requestManageExternalStoragePermission(context)
                    } else {
                        legacyPermissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        )
                    }
                },
                onDecline = { (context as? android.app.Activity)?.finish() },
            )
        }
    }
}

private fun resolveStartupRoute(context: android.content.Context): GameFoldPcStartupRoute =
    GameFoldPcStartupRoutePolicy.resolve(
        supportedAbis = Build.SUPPORTED_ABIS,
        socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else "",
        hardware = Build.HARDWARE,
        hasStorageAccess = OfflineStorageAccess.hasRequiredAccess(context),
    )

@Composable
private fun GameFoldPcMtkUnsupportedScreen() {
    val uriHandler = LocalUriHandler.current
    val openFc: () -> Unit = remember(uriHandler) {
        {
            runCatching { uriHandler.openUri(FC_WEBSITE_URL) }
            Unit
        }
    }
    val controllerListener = remember(openFc) {
        object : GameFoldControllerListener {
            override fun dispatchNavigationToCompose(): Boolean = false

            override fun dispatchConfirmToCompose(): Boolean = false

            override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
                if (pressed) openFc()
            }

            override fun onStickChanged(stick: GameFoldStick, x: Float, y: Float) = Unit
        }
    }
    GameFoldPcShellHost(controllerListener = controllerListener) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GameFoldNsTokens.Background)
                .padding(start = 48.dp, top = 20.dp, end = 48.dp, bottom = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.gamefold_mtk_eyebrow),
                color = GameFoldNsTokens.Muted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.9.sp,
            )
            GameFoldPixelWordmark(
                text = "GAMEPAD PC",
                color = GameFoldNsTokens.Ink,
                bold = true,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(240.dp)
                    .height(34.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(2.dp)
                    .background(GameFoldNsTokens.Border),
            )
            Text(
                text = stringResource(R.string.gamefold_mtk_title),
                color = GameFoldNsTokens.Ink,
                fontFamily = GameFoldNsTokens.LabelFont,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.gamefold_mtk_detail),
                color = GameFoldNsTokens.Muted,
                fontFamily = GameFoldNsTokens.BodyFont,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(12.dp)
                    .background(GameFoldNsTokens.SurfaceDark, RectangleShape),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("MTK / MediaTek", color = GameFoldNsTokens.Muted, fontSize = 14.sp)
                Text("—", color = GameFoldNsTokens.Muted, fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.gamefold_mtk_fc_link),
                    color = GameFoldNsTokens.Action,
                    fontFamily = GameFoldNsTokens.BodyFont,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = openFc),
                )
                GameFoldPcPixelButton(
                    label = stringResource(R.string.download),
                    onClick = openFc,
                    primary = true,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }
}

/**
 * NS-style permission gate: the explanatory dialog lives inside the upper display aperture while
 * the official controller shell remains visible from the first frame. It deliberately does not use
 * a platform Dialog, which would cover the persistent controller on PC.
 */
@Composable
private fun GameFoldPcStartupPermissionDialog(
    unsupported: Boolean,
    onGrant: () -> Unit,
    onDecline: () -> Unit,
) {
    val ink = GameFoldNsTokens.Ink
    val muted = GameFoldNsTokens.Muted
    val controllerListener = remember(onDecline) {
        object : GameFoldControllerListener {
            // The permission surface is a real NS-style focusable page. Let physical D-pad and
            // left-stick input reach the focused square actions instead of making A/X the only
            // usable controller path.
            override fun dispatchNavigationToCompose(): Boolean = true

            override fun onButtonChanged(button: GameFoldButton, pressed: Boolean) {
                if (pressed && (button == GameFoldButton.B || button == GameFoldButton.Y || button == GameFoldButton.HOME)) {
                    onDecline()
                }
            }

            override fun onStickChanged(stick: GameFoldStick, x: Float, y: Float) = Unit
        }
    }
    GameFoldPcShellHost(controllerListener = controllerListener) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GameFoldNsTokens.Background),
        ) {
            // This is the actual NS assembly surface behind the permission prompt.  Keeping the
            // eyebrow, wordmark, divider, source line and progress track visible is important:
            // the permission card is a modal over a functional first-run page, not a black void.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, top = 20.dp, end = 48.dp, bottom = 16.dp),
            ) {
                Text(
                    text = "首次启动 · 素材访问",
                    color = muted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.9.sp,
                )
                GameFoldPixelWordmark(
                    text = "GAMEPAD PC",
                    color = ink,
                    bold = true,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(240.dp)
                        .height(34.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(2.dp)
                        .background(GameFoldNsTokens.Border),
                )
                Text(
                    text = if (unsupported) "当前设备暂不支持" else "允许读取 GamePad PC 素材包",
                    color = ink,
                    fontFamily = GameFoldNsTokens.LabelFont,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = if (unsupported) {
                        "由于驱动限制，此设备暂不支持 ARM64 GamePad PC 运行时。"
                    } else {
                        "GamePad PC 只扫描 ${GameFoldPcResourceRoot.root().path}；模拟器数据保存在独立私有目录。"
                    },
                    color = muted,
                    fontFamily = GameFoldNsTokens.BodyFont,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(12.dp)
                        .background(GameFoldNsTokens.SurfaceDark, RectangleShape),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("素材源 · ${GameFoldPcResourceRoot.root().path}", color = muted, fontSize = 14.sp)
                    Text("0%", color = muted, fontSize = 14.sp)
                }
                Spacer(Modifier.weight(1f))
                if (!unsupported) {
                    GameFoldPcPixelButton(
                        label = "允许文件访问",
                        onClick = onGrant,
                        primary = true,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.84f)
                    .gameFoldNsPanelSurface(dialog = true),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                        Text(
                            text = if (unsupported) "设备不支持 GamePad PC" else "授权访问 GamePad PC 素材",
                            color = ink,
                            fontFamily = GameFoldNsTokens.LabelFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (unsupported) {
                                "此设备不支持 ARM64 GamePad PC 运行时。"
                            } else {
                                "为了检查并装配游戏文件，需要开启“文件访问权限”，GamePad PC 仅使用该权限读取你准备的素材。"
                            },
                            color = muted,
                            fontFamily = GameFoldNsTokens.BodyFont,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GameFoldPcPixelButton(
                                label = "暂不授权",
                                onClick = onDecline,
                            )
                            GameFoldPcPixelButton(
                                label = if (unsupported) "重新检查" else "确认授权",
                                onClick = onGrant,
                                primary = true,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun ProvisionProgress(progress: RuntimeProvisionProgress?) {
    GameFoldPcShellHost {
        GameFoldRetroLoadingCard(
            text = progress?.phase?.name ?: "正在检查 GamePad PC 运行环境",
            progress = progress?.percent?.div(100f) ?: -1f,
            heroImageUrl = "",
            title = "GamePad PC",
        )
    }
}

@Composable
private fun ProvisionMessage(
    title: String,
    message: String,
    action: Pair<String, () -> Unit>,
) {
    val uriHandler = LocalUriHandler.current
    GameFoldPcShellHost {
        Box(Modifier.fillMaxSize().background(GameFoldNsTokens.Background)) {
            GameFoldPcPixelDialog(
                visible = true,
                title = title,
                message = message,
                actions = buildList {
                    if (message.contains("gamenative.app")) {
                        add(GameFoldPcPixelDialogAction("打开下载页面", {
                            uriHandler.openUri("https://gamenative.app")
                        }))
                    }
                    add(GameFoldPcPixelDialogAction(action.first, action.second, primary = true))
                },
                cardWidthFraction = 0.84f,
            )
        }
    }
}

private const val FC_WEBSITE_URL = "https://game-fold.pages.dev"

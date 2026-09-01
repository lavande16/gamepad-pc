package app.gamenative.ui.screen.settings

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gamenative.PrefManager
import app.gamenative.R
import app.gamenative.enums.AppTheme
import app.gamenative.BuildConfig
import app.gamenative.product.ProductModePolicy
import app.gamenative.ui.theme.PluviaTheme
import app.gamenative.gamefold.ui.GameFoldPcPixelButton
import app.gamenative.gamefold.ui.GameFoldButton
import app.gamenative.gamefold.ui.GameFoldNsListItem
import app.gamenative.gamefold.ui.GameFoldNsTokens
import app.gamenative.ui.screen.library.gamePadOwnedPageHorizontalInsetDp
import com.materialkolor.PaletteStyle

@Composable
fun SettingsScreen(
    appTheme: AppTheme,
    paletteStyle: PaletteStyle,
    onAppTheme: (AppTheme) -> Unit,
    onPaletteStyle: (PaletteStyle) -> Unit,
    onBack: () -> Unit,
    controllerBackRequests: Int = 0,
    controllerNavigationRequests: Int = 0,
    controllerNavigationButton: GameFoldButton? = null,
) {
    SettingsScreenContent(
        appTheme = appTheme,
        paletteStyle = paletteStyle,
        onAppTheme = onAppTheme,
        onPaletteStyle = onPaletteStyle,
        onBack = onBack,
        controllerBackRequests = controllerBackRequests,
        controllerNavigationRequests = controllerNavigationRequests,
        controllerNavigationButton = controllerNavigationButton,
    )
}

private enum class LocalSettingsPage {
    HOME,
    EMULATION,
    INTERFACE,
    DEBUG,
    ABOUT,
}

internal enum class SettingsBackAction { SHOW_SETTINGS_HOME, EXIT_SETTINGS }

internal fun settingsBackAction(onSettingsHome: Boolean): SettingsBackAction =
    if (onSettingsHome) SettingsBackAction.EXIT_SETTINGS else SettingsBackAction.SHOW_SETTINGS_HOME

@Composable
private fun SettingsScreenContent(
    appTheme: AppTheme,
    paletteStyle: PaletteStyle,
    onAppTheme: (AppTheme) -> Unit,
    onPaletteStyle: (PaletteStyle) -> Unit,
    onBack: () -> Unit,
    controllerBackRequests: Int,
    controllerNavigationRequests: Int,
    controllerNavigationButton: GameFoldButton?,
) {
    val scrollState = rememberScrollState()
    val localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL
    val pixelBackground = if (localShell) GameFoldNsTokens.Background else Color(0xFF252429)
    val pixelSurface = if (localShell) GameFoldNsTokens.Surface else Color(0xFF202125)
    val pixelText = if (localShell) GameFoldNsTokens.Ink else Color(0xFFFBFAF7)
    var localPage by remember { mutableStateOf(LocalSettingsPage.HOME) }
    var selectedHomeIndex by remember { mutableStateOf(0) }
    val settingsHomeFocusRequester = remember { FocusRequester() }
    val settingsCardFocusRequesters = remember { List(4) { FocusRequester() } }
    val settingsDetailBackFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val settingsHomeColumns = gameFoldSettingsColumns(screenWidthDp)
    val settingsContentHorizontalInsetDp = gamePadOwnedPageHorizontalInsetDp(
        screenWidthDp = screenWidthDp,
        localShell = localShell,
    )

    BackHandler {
        if (localPage == LocalSettingsPage.HOME) onBack() else localPage = LocalSettingsPage.HOME
    }

    // One monotonically increasing request represents exactly one physical button press. Do not
    // key this effect with localPage: changing DETAIL -> HOME would restart the same request and
    // immediately execute HOME -> desktop as a second back action.
    LaunchedEffect(controllerBackRequests) {
        if (controllerBackRequests == 0) return@LaunchedEffect
        when (settingsBackAction(localPage == LocalSettingsPage.HOME)) {
            SettingsBackAction.EXIT_SETTINGS -> onBack()
            SettingsBackAction.SHOW_SETTINGS_HOME -> localPage = LocalSettingsPage.HOME
        }
    }

    LaunchedEffect(localPage, localShell) {
        if (localShell && localPage == LocalSettingsPage.HOME) {
            kotlinx.coroutines.delay(80)
            runCatching { settingsCardFocusRequesters[selectedHomeIndex].requestFocus() }
        } else if (localShell) {
            kotlinx.coroutines.delay(80)
            runCatching { settingsDetailBackFocusRequester.requestFocus() }
        }
    }

    LaunchedEffect(selectedHomeIndex, localPage, localShell) {
        if (localShell && localPage == LocalSettingsPage.HOME) {
            kotlinx.coroutines.delay(30)
            runCatching { settingsCardFocusRequesters[selectedHomeIndex].requestFocus() }
        }
    }

    LaunchedEffect(controllerNavigationRequests, localPage) {
        if (controllerNavigationRequests == 0) return@LaunchedEffect
        if (localPage != LocalSettingsPage.HOME) {
            when (controllerNavigationButton) {
                GameFoldButton.DPAD_LEFT -> focusManager.moveFocus(FocusDirection.Left)
                GameFoldButton.DPAD_RIGHT -> focusManager.moveFocus(FocusDirection.Right)
                GameFoldButton.DPAD_UP -> focusManager.moveFocus(FocusDirection.Up)
                GameFoldButton.DPAD_DOWN -> focusManager.moveFocus(FocusDirection.Down)
                else -> Unit
            }
            return@LaunchedEffect
        }
        when (controllerNavigationButton) {
            GameFoldButton.DPAD_LEFT -> if (selectedHomeIndex % settingsHomeColumns > 0) {
                selectedHomeIndex--
            }
            GameFoldButton.DPAD_RIGHT -> if (
                selectedHomeIndex % settingsHomeColumns < settingsHomeColumns - 1 &&
                selectedHomeIndex < 3
            ) {
                selectedHomeIndex++
            }
            GameFoldButton.DPAD_UP -> selectedHomeIndex =
                (selectedHomeIndex - settingsHomeColumns).coerceAtLeast(0)
            GameFoldButton.DPAD_DOWN -> selectedHomeIndex =
                (selectedHomeIndex + settingsHomeColumns).coerceAtMost(3)
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (localShell) {
                    Modifier.background(pixelBackground)
                } else {
                    Modifier.background(Brush.verticalGradient(
                    colors = listOf(
                        PluviaTheme.colors.surfacePanel,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                    ),
                    ))
                },
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (localShell) Modifier
                    else Modifier.statusBarsPadding().displayCutoutPadding()
                ),
        ) {
            SettingsHeader(
                    title = when (localPage) {
                        LocalSettingsPage.HOME -> stringResource(R.string.settings_title)
                        LocalSettingsPage.EMULATION -> stringResource(R.string.settings_emulation_title)
                        LocalSettingsPage.INTERFACE -> stringResource(R.string.settings_interface_title)
                        LocalSettingsPage.DEBUG -> stringResource(R.string.settings_debug_title)
                        LocalSettingsPage.ABOUT -> "关于"
                    },
                    onBack = if (localShell && localPage != LocalSettingsPage.HOME) {
                        { localPage = LocalSettingsPage.HOME }
                    } else onBack,
                    focusRequester = if (localShell && localPage != LocalSettingsPage.HOME) {
                        settingsDetailBackFocusRequester
                    } else null,
                    modifier = if (localShell) Modifier.fillMaxWidth() else Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (localShell) {
                            Modifier.padding(horizontal = settingsContentHorizontalInsetDp.dp)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                if (localShell && localPage == LocalSettingsPage.HOME) {
                    LocalSettingsHome(
                        selectedIndex = selectedHomeIndex,
                        onSelectionChange = { selectedHomeIndex = it },
                        onOpenPage = { localPage = it },
                        initialFocusRequester = settingsHomeFocusRequester,
                        cardFocusRequesters = settingsCardFocusRequesters,
                    )
                } else if (localShell && localPage == LocalSettingsPage.ABOUT) {
                    AboutPage()
                } else {
                    // Scrollable detail content. Each NS-style entry owns one settings group;
                    // entering 模拟/界面/调试 no longer lands on the same combined directory.
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = if (localShell) 0.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                    when (localPage) {
                        LocalSettingsPage.EMULATION -> SettingsSection(
                            title = stringResource(R.string.settings_emulation_title),
                            icon = Icons.Default.Gamepad,
                            iconTint = PluviaTheme.colors.accentCyan,
                        ) { SettingsGroupEmulation() }
                        LocalSettingsPage.INTERFACE -> SettingsSection(
                            title = stringResource(R.string.settings_interface_title),
                            icon = Icons.Default.Palette,
                            iconTint = PluviaTheme.colors.accentPurple,
                        ) {
                            SettingsGroupInterface(
                                appTheme = appTheme,
                                paletteStyle = paletteStyle,
                                onAppTheme = onAppTheme,
                                onPaletteStyle = onPaletteStyle,
                            )
                        }
                        LocalSettingsPage.DEBUG -> SettingsSection(
                            title = stringResource(R.string.settings_debug_title),
                            icon = Icons.Default.BugReport,
                            iconTint = PluviaTheme.colors.accentWarning,
                        ) { SettingsGroupDebug() }
                        LocalSettingsPage.ABOUT -> AboutPage()
                        LocalSettingsPage.HOME -> Unit
                    }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

private data class LocalSettingsOption(
    val title: String,
    val description: String,
    val icon: Int,
    val page: LocalSettingsPage,
)

@Composable
private fun LocalSettingsHome(
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    onOpenPage: (LocalSettingsPage) -> Unit,
    initialFocusRequester: FocusRequester,
    cardFocusRequesters: List<FocusRequester>,
) {
    val options = remember {
        listOf(
            LocalSettingsOption("模拟设置", "模拟器与启动配置", R.drawable.pixel_icon_controller, LocalSettingsPage.EMULATION),
            LocalSettingsOption("界面设置", "显示与交互", R.drawable.pixel_icon_display, LocalSettingsPage.INTERFACE),
            LocalSettingsOption("调试设置", "日志与诊断", R.drawable.pixel_icon_debug, LocalSettingsPage.DEBUG),
            LocalSettingsOption("关于", "版本与信息", R.drawable.pixel_icon_info, LocalSettingsPage.ABOUT),
        )
    }
    val columns = gameFoldSettingsColumns(LocalConfiguration.current.screenWidthDp)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(options) { index, option ->
            GameFoldNsListItem(
                title = option.title,
                description = option.description,
                icon = {
                    androidx.compose.foundation.Image(
                        painter = painterResource(option.icon),
                        contentDescription = option.title,
                        modifier = Modifier.size(36.dp),
                        colorFilter = ColorFilter.tint(GameFoldNsTokens.Ink),
                    )
                },
                selected = selectedIndex == index,
                onFocused = { onSelectionChange(index) },
                onClick = { onOpenPage(option.page) },
                focusRequester = cardFocusRequesters.getOrNull(index)
                    ?: if (index == 0) initialFocusRequester else null,
            )
        }
    }
}

/** NS switches its settings directory to two columns at the w600dp resource boundary. */
internal fun gameFoldSettingsColumns(screenWidthDp: Int): Int = if (screenWidthDp >= 600) 2 else 1

@Composable
private fun SettingsHeader(
    title: String,
    onBack: () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
                    Modifier
                        .height(44.dp)
                        .background(GameFoldNsTokens.SurfaceDark, RectangleShape)
                        .padding(horizontal = 16.dp)
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BackButton(onClick = onBack, focusRequester = focusRequester)

        // NS uses one compact pixel title; the modern subtitle/decorative icon is retained only
        // for the non-local product build.
        Column {
            Text(
                text = title,
                style = if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = GameFoldNsTokens.LabelFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                } else MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
                color = if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) GameFoldNsTokens.Ink else MaterialTheme.colorScheme.onSurface,
            )
            if (!BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = PluviaTheme.colors.textMuted,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PluviaTheme.colors.accentCyan.copy(alpha = 0.2f),
                            Color.Transparent,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = PluviaTheme.colors.accentCyan.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        // NS uses a fixed pixel button in the local shell; the old spring zoom was a Material
        // affordance and made the back target drift away from the pixel-aligned header rail.
        targetValue = if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) 1f else if (isFocused) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "backButtonScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(44.dp)
            .clip(if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) RectangleShape else CircleShape)
            .background(
                if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL && !isFocused) {
                    // NS leaves the header back affordance on the header surface when it is not
                    // selected; a persistent filled square reads as an extra button card.
                    Color.Transparent
                } else if (isFocused) {
                    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) GameFoldNsTokens.FocusGlow else PluviaTheme.colors.accentCyan.copy(alpha = 0.2f)
                } else {
                    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) GameFoldNsTokens.SurfaceDark else PluviaTheme.colors.surfaceElevated
                },
            )
            .then(
                if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL && !isFocused) {
                    Modifier
                } else if (isFocused) {
                    Modifier.border(
                        2.dp,
                        if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) GameFoldNsTokens.Focus else PluviaTheme.colors.accentCyan.copy(alpha = 0.6f),
                        if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) RectangleShape else CircleShape,
                    )
                } else {
                    Modifier.border(
                        1.dp,
                        if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) GameFoldNsTokens.Border else PluviaTheme.colors.borderDefault.copy(alpha = 0.3f),
                        if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) RectangleShape else CircleShape,
                    )
                },
            )
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .selectable(
                selected = isFocused,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .focusable(interactionSource = interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.pixel_icon_back),
                contentDescription = stringResource(R.string.back),
                modifier = Modifier.size(26.dp),
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.back),
                tint = if (isFocused) PluviaTheme.colors.accentCyan else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        Column(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = GameFoldNsTokens.SpaceXs)
                    .height(GameFoldNsTokens.TouchTarget)
                    .background(GameFoldNsTokens.Shadow, RectangleShape)
                    .border(GameFoldNsTokens.BorderWidth, GameFoldNsTokens.Border, RectangleShape),
            ) {
                Box(
                    Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .background(GameFoldNsTokens.Moss, RectangleShape),
                )
                Text(
                    text = title,
                    color = GameFoldNsTokens.Surface,
                    fontFamily = GameFoldNsTokens.LabelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp),
                )
            }
            content()
        }
        return
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = PluviaTheme.colors.surfaceElevated,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        ) {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp,
                    ),
                    color = Color.White,
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = PluviaTheme.colors.borderDefault.copy(alpha = 0.2f),
            )

            // Section content
            content()
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1920px,height=1080px,dpi=440,orientation=landscape",
)
@Composable
private fun Preview_SettingsScreen() {
    val isPreview = LocalInspectionMode.current
    if (!isPreview) {
        val context = LocalContext.current
        PrefManager.init(context)
    }
    PluviaTheme {
        SettingsScreenContent(
            appTheme = AppTheme.DAY,
            paletteStyle = PaletteStyle.TonalSpot,
            onAppTheme = { },
            onPaletteStyle = { },
            onBack = { },
            controllerNavigationRequests = 0,
            controllerBackRequests = 0,
            controllerNavigationButton = null,
        )
    }
}

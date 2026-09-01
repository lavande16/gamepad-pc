package app.gamenative.ui.screen.settings

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.gamenative.PrefManager
import app.gamenative.BuildConfig
import app.gamenative.R
import app.gamenative.gamefold.rear.GameFoldOrientation
import app.gamenative.gamefold.rear.GameFoldRearDisplayPolicy
import app.gamenative.gamefold.rear.VivoRearDisplayController
import app.gamenative.ui.component.dialog.Box64PresetsDialog
import app.gamenative.ui.component.dialog.ContainerConfigDialog
import app.gamenative.ui.component.dialog.FEXCorePresetsDialog
import app.gamenative.ui.component.dialog.OrientationDialog
import app.gamenative.ui.theme.PluviaTheme
import app.gamenative.ui.theme.settingsTileColors
import app.gamenative.ui.theme.settingsTileColorsAlt
import app.gamenative.utils.ContainerUtils
import app.gamenative.utils.BrightnessManager
import com.alorma.compose.settings.ui.SettingsGroup

@Composable
fun SettingsGroupEmulation() {
    SettingsGroup(
    ) {
        var showConfigDialog by rememberSaveable { mutableStateOf(false) }
        var showOrientationDialog by rememberSaveable { mutableStateOf(false) }
        var showBox64PresetsDialog by rememberSaveable { mutableStateOf(false) }

        OrientationDialog(
            openDialog = showOrientationDialog,
            onDismiss = { showOrientationDialog = false },
        )

        ContainerConfigDialog(
            visible = showConfigDialog,
            title = stringResource(R.string.settings_emulation_default_config_dialog_title),
            default = true,
            initialConfig = ContainerUtils.getDefaultContainerData(),
            onDismissRequest = { showConfigDialog = false },
            onSave = {
                showConfigDialog = false
                ContainerUtils.setDefaultContainerData(it)
            },
        )

        Box64PresetsDialog(
            visible = showBox64PresetsDialog,
            onDismissRequest = { showBox64PresetsDialog = false },
        )
        var showFexcorePresetsDialog by rememberSaveable { mutableStateOf(false) }
        if (showFexcorePresetsDialog) {
            FEXCorePresetsDialog(
                visible = showFexcorePresetsDialog,
                onDismissRequest = { showFexcorePresetsDialog = false },
            )
        }

        var showDriverManager by rememberSaveable { mutableStateOf(false) }
        if (showDriverManager) {
            // Lazy-load dialog composable to avoid cyclic imports
            DriverManagerDialog(open = showDriverManager, onDismiss = { showDriverManager = false })
        }

        var showContentsManager by rememberSaveable { mutableStateOf(false) }
        if (showContentsManager) {
            ContentsManagerDialog(open = showContentsManager, onDismiss = { showContentsManager = false })
        }

        var showWineProtonManager by rememberSaveable { mutableStateOf(false) }
        if (showWineProtonManager) {
            WineProtonManagerDialog(open = showWineProtonManager, onDismiss = { showWineProtonManager = false })
        }

        var showInGameQuickMenu by rememberSaveable {
            mutableStateOf(PrefManager.showInGameQuickMenu)
        }
        SettingsSwitch(
            colors = settingsTileColorsAlt(),
            state = showInGameQuickMenu,
            title = { Text(text = stringResource(R.string.settings_emulation_show_quick_menu_title)) },
            subtitle = { Text(text = stringResource(R.string.settings_emulation_show_quick_menu_subtitle)) },
            onCheckedChange = {
                showInGameQuickMenu = it
                PrefManager.showInGameQuickMenu = it
            },
        )

        if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
            val context = LocalContext.current
            val rearSupported = VivoRearDisplayController.isSupportedDevice()
            val portrait = GameFoldOrientation.isPortrait(context) ||
                context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            if (GameFoldRearDisplayPolicy.shouldShowToggle(rearSupported, portrait)) {
                var dualScreenEnabled by remember {
                    mutableStateOf(VivoRearDisplayController.isSessionEnabled())
                }
                DisposableEffect(Unit) {
                    val listener = VivoRearDisplayController.SessionStateListener { state ->
                        dualScreenEnabled = state == VivoRearDisplayController.SessionState.STARTING ||
                            state == VivoRearDisplayController.SessionState.ACTIVE
                    }
                    VivoRearDisplayController.addSessionStateListener(listener)
                    onDispose { VivoRearDisplayController.removeSessionStateListener(listener) }
                }
                SettingsSwitch(
                    colors = settingsTileColorsAlt(),
                    state = dualScreenEnabled,
                    title = { Text(text = stringResource(R.string.settings_emulation_dual_screen_title)) },
                    subtitle = { Text(text = stringResource(R.string.settings_emulation_dual_screen_subtitle)) },
                    onCheckedChange = { enabled ->
                        val activity = BrightnessManager.findActivity(context)
                        if (activity == null) dualScreenEnabled = false
                        else VivoRearDisplayController.setSessionEnabled(activity, enabled)
                    },
                )
            }
        }

        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = stringResource(R.string.settings_emulation_orientations_title)) },
            subtitle = { Text(text = stringResource(R.string.settings_emulation_orientations_subtitle)) },
            onClick = { showOrientationDialog = true },
        )

        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = stringResource(R.string.settings_emulation_default_config_title)) },
            subtitle = { Text(text = stringResource(R.string.settings_emulation_default_config_subtitle)) },
            onClick = { showConfigDialog = true },
        )
        var autoApplyKnownConfig by rememberSaveable { mutableStateOf(PrefManager.autoApplyKnownConfig) }
        SettingsSwitch(
            colors = settingsTileColorsAlt(),
            state = autoApplyKnownConfig,
            title = { Text(text = stringResource(R.string.settings_emulation_auto_apply_known_config_title)) },
            subtitle = { Text(text = stringResource(R.string.settings_emulation_auto_apply_known_config_subtitle)) },
            onCheckedChange = {
                autoApplyKnownConfig = it
                PrefManager.autoApplyKnownConfig = it
            },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = stringResource(R.string.settings_emulation_box64_presets_title)) },
            subtitle = { Text(stringResource(R.string.settings_emulation_box64_presets_subtitle)) },
            onClick = { showBox64PresetsDialog = true },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = stringResource(R.string.fexcore_presets)) },
            subtitle = { Text(text = stringResource(R.string.fexcore_presets_description)) },
            onClick = { showFexcorePresetsDialog = true },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = stringResource(R.string.settings_emulation_driver_manager_title)) },
            subtitle = { Text(text = stringResource(R.string.settings_emulation_driver_manager_subtitle)) },
            onClick = { showDriverManager = true },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = stringResource(R.string.settings_emulation_contents_manager_title)) },
            subtitle = { Text(text = stringResource(R.string.settings_emulation_contents_manager_subtitle)) },
            onClick = { showContentsManager = true },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = stringResource(R.string.settings_emulation_wine_proton_manager_title)) },
            subtitle = { Text(text = stringResource(R.string.settings_emulation_wine_proton_manager_subtitle)) },
            onClick = { showWineProtonManager = true },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_SettingsGroupEmulation() {
    val isPreview = LocalInspectionMode.current
    if (!isPreview) {
        val context = LocalContext.current
        PrefManager.init(context)
    }
    PluviaTheme {
        SettingsGroupEmulation()
    }
}

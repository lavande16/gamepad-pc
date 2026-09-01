package app.gamenative.ui.component.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldPcPixelSwitch
import app.gamenative.gamefold.ui.GameFoldNsSettingsRow
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.base.internal.LocalSettingsGroupEnabled
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults

@Composable
fun SettingsSwitchWithAction(
    state: Boolean,
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = LocalSettingsGroupEnabled.current,
    icon: @Composable (() -> Unit)? = null,
    subtitle: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    colors: SettingsTileColors = SettingsTileDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    onCheckedChange: (Boolean) -> Unit,
) {
    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        GameFoldNsSettingsRow(
            title = title,
            modifier = modifier,
            enabled = enabled,
            icon = icon,
            subtitle = subtitle,
            action = {
                Row {
                    GameFoldPcPixelSwitch(
                        enabled = enabled,
                        checked = state,
                        onCheckedChange = onCheckedChange,
                    )
                    action?.invoke()
                }
            },
            onClick = { onCheckedChange(!state) },
        )
        return
    }
    SettingsMenuLink(
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        subtitle = subtitle,
        action = {
            Row {
                androidx.compose.material3.Switch(
                    enabled = enabled,
                    checked = state,
                    onCheckedChange = onCheckedChange,
                )
                action?.invoke()
            }
        },
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        onClick = { onCheckedChange(!state) },
    )
}

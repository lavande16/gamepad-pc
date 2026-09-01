package app.gamenative.ui.component.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldPcPixelSwitch
import app.gamenative.gamefold.ui.GameFoldNsSettingsSwitchRow
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.base.internal.LocalSettingsGroupEnabled
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults

/**
 * Shared switch used by the per-game container tabs.  The upstream settings library keeps a
 * rounded Material switch; GamePad PC's local shell must use the same rectangular NS switch as
 * the global settings page while non-PC product flavors retain the upstream control.
 */
@Composable
fun SettingsSwitch(
    state: Boolean,
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    subtitle: (@Composable (() -> Unit))? = null,
    enabled: Boolean = LocalSettingsGroupEnabled.current,
    colors: SettingsTileColors = SettingsTileDefaults.colors(),
    onCheckedChange: (Boolean) -> Unit,
) {
    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        GameFoldNsSettingsSwitchRow(
            checked = state,
            onCheckedChange = onCheckedChange,
            title = title,
            subtitle = subtitle,
            modifier = modifier,
            enabled = enabled,
        )
        return
    }
    SettingsMenuLink(
        title = title,
        subtitle = subtitle,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = colors,
        action = {
            Row(modifier = Modifier.padding(end = 12.dp)) {
                androidx.compose.material3.Switch(
                    checked = state,
                    enabled = enabled,
                    onCheckedChange = onCheckedChange,
                )
            }
        },
        onClick = { onCheckedChange(!state) },
    )
}

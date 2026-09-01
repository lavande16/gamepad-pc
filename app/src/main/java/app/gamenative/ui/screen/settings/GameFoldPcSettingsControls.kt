package app.gamenative.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldNsTokens
import app.gamenative.gamefold.ui.gameFoldNsSettingsItemSurface
import app.gamenative.gamefold.ui.GameFoldNsSettingsSwitchRow
import com.alorma.compose.settings.ui.base.internal.LocalSettingsGroupEnabled
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults

/**
 * Local-shell replacement for the library's rounded Material switch.  NS uses a rectangular
 * pixel switch, so keep the upstream settings row semantics and swap only the trailing control.
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
    com.alorma.compose.settings.ui.SettingsMenuLink(
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

/**
 * NS list_item_setting.xml projection used by every public PC settings page. Keeping the
 * upstream signature lets the non-local product retain the Compose Settings implementation,
 * while the local GameFold shell gets one rectangular 64dp list contract everywhere.
 */
@Composable
fun SettingsMenuLink(
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    subtitle: (@Composable (() -> Unit))? = null,
    enabled: Boolean = LocalSettingsGroupEnabled.current,
    colors: SettingsTileColors = SettingsTileDefaults.colors(),
    icon: (@Composable (() -> Unit))? = null,
    action: (@Composable (() -> Unit))? = null,
    onClick: () -> Unit,
) {
    if (!BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        com.alorma.compose.settings.ui.SettingsMenuLink(
            title = title,
            modifier = modifier,
            subtitle = subtitle,
            enabled = enabled,
            colors = colors,
            icon = icon,
            action = action,
            onClick = onClick,
        )
        return
    }

    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = GameFoldNsTokens.ListItemHeight)
            .gameFoldNsSettingsItemSurface(focused)
            .clickable(
                enabled = enabled,
                interactionSource = source,
                indication = null,
                onClick = onClick,
            )
            .focusable(enabled = enabled, interactionSource = source)
            .padding(horizontal = GameFoldNsTokens.SpaceMd, vertical = GameFoldNsTokens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Row(
                modifier = Modifier.size(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) { it() }
            androidx.compose.foundation.layout.Spacer(Modifier.width(GameFoldNsTokens.SpaceMd))
        }
        Column(Modifier.weight(1f)) {
            ProvideTextStyle(
                TextStyle(
                    color = if (enabled) GameFoldNsTokens.Ink else GameFoldNsTokens.DisabledText,
                    fontFamily = GameFoldNsTokens.LabelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
            ) { title() }
            subtitle?.let {
                ProvideTextStyle(
                    TextStyle(
                        color = if (enabled) GameFoldNsTokens.Muted else GameFoldNsTokens.DisabledText,
                        fontFamily = GameFoldNsTokens.BodyFont,
                        fontSize = 14.sp,
                    ),
                ) { it() }
            }
        }
        action?.let {
            androidx.compose.foundation.layout.Spacer(Modifier.width(GameFoldNsTokens.SpaceSm))
            it()
        }
    }
}

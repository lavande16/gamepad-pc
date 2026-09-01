package app.gamenative.ui.component.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.gamenative.ui.component.NoExtractOutlinedTextField
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldNsSettingsRow
import app.gamenative.gamefold.ui.GameFoldNsTokens
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.base.internal.LocalSettingsGroupEnabled
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults

@Composable
fun SettingsTextField(
    value: String,
    title: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    enabled: Boolean = LocalSettingsGroupEnabled.current,
    icon: @Composable (() -> Unit)? = null,
    subtitle: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
    colors: SettingsTileColors = SettingsTileDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    onValueChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        GameFoldNsSettingsRow(
            title = title,
            modifier = modifier,
            enabled = enabled,
            icon = icon,
            subtitle = subtitle,
            action = {
                Row {
                    NoExtractOutlinedTextField(
                        modifier = Modifier.focusRequester(focusRequester).width(92.dp),
                        enabled = enabled,
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        shape = RectangleShape,
                        textStyle = TextStyle(
                            color = GameFoldNsTokens.Ink,
                            fontFamily = GameFoldNsTokens.NumericFont,
                            fontSize = 14.sp,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GameFoldNsTokens.Surface,
                            unfocusedContainerColor = GameFoldNsTokens.Surface,
                            focusedBorderColor = GameFoldNsTokens.Focus,
                            unfocusedBorderColor = GameFoldNsTokens.Border,
                            focusedTextColor = GameFoldNsTokens.Ink,
                            unfocusedTextColor = GameFoldNsTokens.Ink,
                        ),
                    )
                    action?.invoke()
                }
            },
            onClick = { focusRequester.requestFocus() },
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
                NoExtractOutlinedTextField(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .width(76.dp),
                    enabled = enabled,
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                )
                action?.invoke()
            }
        },
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        onClick = { focusRequester.requestFocus() },
    )
}

package app.gamenative.ui.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gamenative.BuildConfig
import app.gamenative.gamefold.ui.GameFoldNsTokens
import app.gamenative.gamefold.ui.gameFoldNsSettingsItemSurface
import com.alorma.compose.settings.ui.base.internal.LocalSettingsGroupEnabled
import com.alorma.compose.settings.ui.base.internal.SettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
import com.alorma.compose.settings.ui.base.internal.SettingsTileScaffold

@Composable
fun SettingsListDropdown(
    modifier: Modifier = Modifier,
    enabled: Boolean = LocalSettingsGroupEnabled.current,
    value: Int,
    items: List<String>,
    itemMuted: List<Boolean>? = null,
    fallbackDisplay: String = "",
    onItemSelected: (Int) -> Unit,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    colors: SettingsTileColors = SettingsTileDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    action: @Composable (() -> Unit)? = null,
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedText = if (value >= 0 && value < items.size) items[value] else fallbackDisplay

    // Keep a handle on the anchor tile so we can return focus to it when the menu
    // closes. Without this, dismissing the popup leaves focus cleared and D-pad /
    // controller navigation has nowhere to go.
    val focusRequester = remember { FocusRequester() }

    if (BuildConfig.GAMEFOLD_PC_LOCAL_SHELL) {
        val source = remember { MutableInteractionSource() }
        val focused by source.collectIsFocusedAsState()
        Row(
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = GameFoldNsTokens.ListItemHeight)
                .gameFoldNsSettingsItemSurface(focused)
                .focusRequester(focusRequester)
                .clickable(
                    enabled = enabled,
                    interactionSource = source,
                    indication = null,
                    onClick = { isDropdownExpanded = true },
                )
                .focusable(enabled = enabled, interactionSource = source)
                .padding(horizontal = GameFoldNsTokens.SpaceMd, vertical = GameFoldNsTokens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.let {
                it()
                Spacer(Modifier.width(GameFoldNsTokens.SpaceMd))
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
                Text(
                    text = selectedText,
                    color = if (enabled) GameFoldNsTokens.Muted else GameFoldNsTokens.DisabledText,
                    fontFamily = GameFoldNsTokens.NumericFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = if (isDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = "Dropdown arrow",
                tint = GameFoldNsTokens.Ink,
            )
            action?.let {
                Spacer(Modifier.width(GameFoldNsTokens.SpaceMd))
                it()
            }
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = {
                    isDropdownExpanded = false
                    focusRequester.requestFocus()
                },
                modifier = Modifier
                    .background(GameFoldNsTokens.Surface, RectangleShape)
                    .border(GameFoldNsTokens.BorderWidth, GameFoldNsTokens.Border, RectangleShape),
            ) {
                items.forEachIndexed { index, text ->
                    val isMuted = itemMuted?.getOrNull(index) == true
                    DropdownMenuItem(
                        enabled = enabled,
                        modifier = Modifier.defaultMinSize(minHeight = GameFoldNsTokens.TouchTarget),
                        text = {
                            Text(
                                text = text,
                                color = if (isMuted || !enabled) GameFoldNsTokens.DisabledText else GameFoldNsTokens.Ink,
                                fontFamily = GameFoldNsTokens.BodyFont,
                                fontSize = 16.sp,
                            )
                        },
                        onClick = {
                            onItemSelected(index)
                            isDropdownExpanded = false
                            focusRequester.requestFocus()
                        },
                    )
                }
            }
        }
        return
    }

    SettingsTileScaffold(
        modifier = Modifier
            .focusRequester(focusRequester)
            .clickable(
                enabled = enabled,
                onClick = { isDropdownExpanded = true },
            ).then(modifier),
        enabled = enabled,
        title = title,
        subtitle = {
            if (subtitle != null) {
                Column {
                    ProvideTextStyle(value = LocalTextStyle.current.merge(TextStyle(fontStyle = FontStyle.Italic))) {
                        subtitle()
                    }
                    Text(
                        text = selectedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(fontWeight = FontWeight.Bold),
                    )
                }
            } else {
                Text(
                    text = selectedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                )
            }
        },
        icon = icon,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = {
                isDropdownExpanded = false
                focusRequester.requestFocus()
            },
        ) {
            items.forEachIndexed { index, text ->
                val isMuted = itemMuted?.getOrNull(index) == true
                val textColor = if (isMuted) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                DropdownMenuItem(
                    enabled = enabled,
                    text = { Text(text = text, color = textColor) },
                    onClick = {
                        onItemSelected(index)
                        isDropdownExpanded = false
                        focusRequester.requestFocus()
                    },
                )
            }
        }
        Row {
            Icon(
                modifier = Modifier.align(Alignment.CenterVertically),
                imageVector = if (isDropdownExpanded) {
                    Icons.Filled.ArrowDropUp
                } else {
                    Icons.Filled.ArrowDropDown
                },
                contentDescription = "Dropdown arrow",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (action != null) {
                Spacer(modifier.width(16.dp))
                action()
            }
        }
    }
}

package app.gamenative.ui.component.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.gamenative.gamefold.ui.GameFoldNsTokens

private val GamePadPresetEditorLightColorScheme = lightColorScheme(
    primary = GameFoldNsTokens.Moss,
    onPrimary = GameFoldNsTokens.Ink,
    primaryContainer = GameFoldNsTokens.FocusGlow,
    onPrimaryContainer = GameFoldNsTokens.Ink,
    secondary = GameFoldNsTokens.Focus,
    onSecondary = GameFoldNsTokens.Surface,
    secondaryContainer = GameFoldNsTokens.SurfaceDark,
    onSecondaryContainer = GameFoldNsTokens.Ink,
    tertiary = GameFoldNsTokens.Action,
    onTertiary = GameFoldNsTokens.Surface,
    background = GameFoldNsTokens.Background,
    onBackground = GameFoldNsTokens.Ink,
    surface = GameFoldNsTokens.Surface,
    onSurface = GameFoldNsTokens.Ink,
    surfaceVariant = GameFoldNsTokens.SurfaceDark,
    onSurfaceVariant = GameFoldNsTokens.Muted,
    error = GameFoldNsTokens.Action,
    onError = GameFoldNsTokens.Surface,
    outline = GameFoldNsTokens.Border,
    outlineVariant = GameFoldNsTokens.DisabledText,
    scrim = GameFoldNsTokens.Scrim,
    surfaceTint = Color.Transparent,
    surfaceContainer = GameFoldNsTokens.SurfaceDark,
)

internal fun gamePadPresetEditorUsesLightPalette(localShell: Boolean): Boolean = localShell

internal fun gamePadPresetEditorColorScheme(
    localShell: Boolean,
    inherited: ColorScheme,
): ColorScheme = if (gamePadPresetEditorUsesLightPalette(localShell)) {
    GamePadPresetEditorLightColorScheme
} else {
    inherited
}

@Composable
internal fun GamePadPresetEditorTheme(
    localShell: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = gamePadPresetEditorColorScheme(localShell, MaterialTheme.colorScheme),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
        content()
    }
}

@Composable
internal fun gamePadPresetEditorBackground(localShell: Boolean): Color =
    if (gamePadPresetEditorUsesLightPalette(localShell)) {
        GameFoldNsTokens.Background
    } else {
        MaterialTheme.colorScheme.background
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GamePadPresetEditorScaffold(
    localShell: Boolean,
    title: String,
    closeContentDescription: String,
    onDismissRequest: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    GamePadPresetEditorTheme(localShell = localShell) {
        Scaffold(
            containerColor = gamePadPresetEditorBackground(localShell),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = title) },
                    actions = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Done, closeContentDescription)
                        }
                    },
                )
            },
            content = content,
        )
    }
}

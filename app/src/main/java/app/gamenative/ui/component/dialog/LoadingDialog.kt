package app.gamenative.ui.component.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.gamenative.ui.theme.PluviaTheme
import app.gamenative.gamefold.ui.LocalGameFoldPcShellOverlay
import app.gamenative.gamefold.ui.LocalGameFoldPcShellOverlayRegistry
import app.gamenative.gamepad.GamePadProductContract
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @param progress A value between 0 and 1 (inclusive), if the value is below 0 then the bar is
 * displayed as indeterminate
 */
@Composable
fun LoadingDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit = {},
    progress: Float,
    message: String = "正在准备运行环境",
) {
    LocalGameFoldPcShellOverlay.current?.let { overlayController ->
        val owner = remember { Any() }
        DisposableEffect(overlayController, owner) {
            onDispose { overlayController.clear(owner) }
        }
        LaunchedEffect(visible, progress, message) {
            if (visible) {
                overlayController.showLoading(owner, message.ifBlank { "正在准备运行环境" }, progress)
            } else {
                overlayController.clear(owner)
            }
        }
        return
    }

    if (app.gamenative.BuildConfig.GAMEFOLD_PC_LOCAL_SHELL &&
        GamePadProductContract.NON_GAME_CONTROLLER_OVERLAY_ENABLED
    ) {
        val overlayRegistry = LocalGameFoldPcShellOverlayRegistry.current
        val owner = remember { Any() }
        DisposableEffect(overlayRegistry, owner) {
            onDispose { overlayRegistry.clear(owner) }
        }
        LaunchedEffect(overlayRegistry.activeController, visible, progress, message) {
            if (visible) {
                overlayRegistry.showLoading(owner, message.ifBlank { "正在准备运行环境" }, progress)
            } else {
                overlayRegistry.clear(owner)
            }
        }
        if (overlayRegistry.activeController != null || visible) return
    }

    // A local-shell loading state must never fall back to a platform Dialog: that would cover
    // the persistent controller. Root launch state is rendered by the display aperture host;
    // callers outside a host are intentionally suppressed until they are routed into one.
    if (app.gamenative.BuildConfig.GAMEFOLD_PC_LOCAL_SHELL &&
        GamePadProductContract.NON_GAME_CONTROLLER_OVERLAY_ENABLED
    ) return

    when {
        visible -> {
            Dialog(
                onDismissRequest = onDismissRequest,
            ) {
                Card {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(message)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (progress >= 0) {
                            Text(min(100, (progress * 100.0).roundToInt()).toString() + "%")
                            LinearProgressIndicator(
                                progress = { progress },
                            )
                        } else {
                            LinearProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_LoadingDialog() {
    PluviaTheme {
        LoadingDialog(
            visible = true,
            progress = .75f,
        )
    }
}

package app.gamenative.gamefold.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class GameFoldPcPixelDialogAction(
    val label: String,
    val onClick: () -> Unit,
    val primary: Boolean = false,
    val focused: Boolean = false,
)

/** A modal that follows the NS pixel card palette without creating a platform dialog window. */
@Composable
fun GameFoldPcPixelDialog(
    visible: Boolean,
    title: String,
    message: String,
    actions: List<GameFoldPcPixelDialogAction>,
    onDismissRequest: (() -> Unit)? = null,
    centerTitleAndMessage: Boolean = false,
    equalActionWidths: Boolean = false,
    cardWidthFraction: Float = 0.84f,
    neutralFocusFrame: Boolean = false,
) {
    if (!visible) return
    val firstActionFocusRequester = remember { FocusRequester() }
    LaunchedEffect(visible, actions.size) {
        runCatching { firstActionFocusRequester.requestFocus() }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameFoldNsTokens.Scrim)
            .pointerInput(onDismissRequest) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                        if (event.changes.all { !it.pressed }) break
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
            Column(
                modifier = Modifier
                .fillMaxWidth(cardWidthFraction.coerceIn(0.48f, 0.92f))
                .gameFoldNsPanelSurface(dialog = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GameFoldNsTokens.SpaceLg),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    title,
                    color = GameFoldNsTokens.Ink,
                    fontFamily = GameFoldNsTokens.LabelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (centerTitleAndMessage) TextAlign.Center else TextAlign.Start,
                )
                Text(
                    message,
                    color = GameFoldNsTokens.Ink,
                    fontFamily = GameFoldNsTokens.BodyFont,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (centerTitleAndMessage) TextAlign.Center else TextAlign.Start,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    actions.forEachIndexed { index, action ->
                        val interactionSource = remember(action.label) { MutableInteractionSource() }
                        val interactionFocused by interactionSource.collectIsFocusedAsState()
                        val isFocused = action.focused || interactionFocused
                        if (index > 0) Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .then(if (equalActionWidths) Modifier.weight(1f) else Modifier)
                                .background(if (action.primary) GameFoldNsTokens.Action else Color.Transparent, RectangleShape)
                                .border(
                                    if (neutralFocusFrame && isFocused) 4.dp else 2.dp,
                                    when {
                                        action.primary -> GameFoldNsTokens.Action
                                        else -> GameFoldNsTokens.Border
                                    },
                                    RectangleShape,
                                )
                                .then(
                                    if (!neutralFocusFrame) Modifier.gameFoldNsFocusFrame(isFocused)
                                    else Modifier
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = action.onClick,
                                )
                                .focusable(interactionSource = interactionSource)
                                .then(if (index == 0) Modifier.focusRequester(firstActionFocusRequester) else Modifier)
                                .height(GameFoldNsTokens.TouchTarget)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                action.label,
                                color = if (action.primary) GameFoldNsTokens.Surface else GameFoldNsTokens.Ink,
                                fontFamily = GameFoldNsTokens.LabelFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

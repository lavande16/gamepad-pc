package app.gamenative.gamefold.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.ui.text.TextStyle

/**
 * Compose projection of GameFold NS 2.9 R2's pixel_colors.xml, pixel_dimens.xml and
 * pixel_styles.xml. The values here are intentionally literal mirrors of the NS resources;
 * display-area pages must consume this contract instead of declaring local palettes.
 */
object GameFoldNsTokens {
    val Background = Color(0xFFF2F1ED)
    val Deep = Color(0xFF252429)
    val Surface = Color(0xFFFBFAF7)
    val SurfaceDark = Color(0xFFE2DED8)
    val SurfacePressed = Color(0xFFD8D2CA)
    val Border = Color(0xFF787370)
    val Ink = Color(0xFF1B1C1D)
    val Muted = Color(0xFF68635F)
    val Focus = Color(0xFF6D8F9C)
    val FocusGlow = Color(0xFFD7E7EB)
    val Action = Color(0xFFB65454)
    val ActionPressed = Color(0xFF8F4040)
    val ActionShadow = Color(0xFF6F3636)
    val Shadow = Color(0xFF5E5A57)
    val DisabledSurface = Color(0xFFDDD8D1)
    val DisabledText = Color(0xFF9A948E)
    val DecorativeBlue = Color(0xFFA9C2CF)
    val CartridgeInner = Color(0xFFB9B5B0)
    val Moss = Color(0xFFC4EA72)
    val Scrim = Color(0x991B1C1D)

    val Pixel = 2.dp
    val BorderWidth = 2.dp
    val FocusWidth = 4.dp
    val FocusGlowWidth = 5.dp
    val ShadowOffset = 4.dp
    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 16.dp
    val SpaceLg = 24.dp
    val SpaceXl = 32.dp
    val TouchTarget = 48.dp
    val ToolbarHeight = 44.dp
    val ButtonHeight = 56.dp
    val ListItemHeight = 64.dp
    val SwitchWidth = 56.dp
    val SwitchHeight = 32.dp

    val BodyFont = FontFamily.SansSerif
    val LabelFont = FontFamily.SansSerif
    val NumericFont = FontFamily.Monospace
}

/** Shared geometry for fragment_emulation.xml's single wrap-content loading card. */
object GameFoldNsLoadingContract {
    const val CardWidthDp = 380
    const val CardHeightDp = 144
    const val CoverSizeDp = 112
    const val TextColumnWidthDp = 220
    const val ProgressWidthDp = 192
    const val ProgressHeightDp = 16
    const val SegmentCount = 6
    const val SegmentWidthDp = 24
    const val SegmentGapDp = 6
    const val SegmentStartDp = 6
    const val SegmentTopDp = 4
    const val SegmentHeightDp = 8
}

/** pixel_progress_track.xml + pixel_loading_progress_segments.xml projection. */
@Composable
fun GameFoldNsLoadingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.size(
            GameFoldNsLoadingContract.ProgressWidthDp.dp,
            GameFoldNsLoadingContract.ProgressHeightDp.dp,
        ),
    ) {
        // NS's loading state uses pixel_loading_progress_segments directly. There is no
        // enclosing track, dark fill, or outline around the six free-standing segments.
        val visibleWidth = if (progress < 0f) size.width else size.width * progress.coerceIn(0f, 1f)
        clipRect(right = visibleWidth) {
            repeat(GameFoldNsLoadingContract.SegmentCount) { index ->
                drawRect(
                    color = if (index % 2 == 0) GameFoldNsTokens.Focus else GameFoldNsTokens.Action,
                    topLeft = Offset(
                        (GameFoldNsLoadingContract.SegmentStartDp +
                            index * (GameFoldNsLoadingContract.SegmentWidthDp + GameFoldNsLoadingContract.SegmentGapDp)).dp.toPx(),
                        GameFoldNsLoadingContract.SegmentTopDp.dp.toPx(),
                    ),
                    size = Size(
                        GameFoldNsLoadingContract.SegmentWidthDp.dp.toPx(),
                        GameFoldNsLoadingContract.SegmentHeightDp.dp.toPx(),
                    ),
                )
            }
        }
    }
}

/** Exact Compose equivalent of pixel_focus_ring_overlay.xml. */
fun Modifier.gameFoldNsFocusFrame(visible: Boolean): Modifier {
    if (!visible) return this
    return drawBehind {
        val glow = GameFoldNsTokens.FocusGlowWidth.toPx()
        val ring = GameFoldNsTokens.BorderWidth.toPx()
        drawRect(
            color = GameFoldNsTokens.FocusGlow,
            topLeft = Offset(glow / 2f, glow / 2f),
            size = Size((size.width - glow).coerceAtLeast(0f), (size.height - glow).coerceAtLeast(0f)),
            style = Stroke(width = glow),
        )
        val inset = glow + ring / 2f
        drawRect(
            color = GameFoldNsTokens.Focus,
            topLeft = Offset(inset, inset),
            size = Size(
                (size.width - inset * 2f).coerceAtLeast(0f),
                (size.height - inset * 2f).coerceAtLeast(0f),
            ),
            style = Stroke(width = ring),
        )
    }
}

/** Drawable-equivalent renderer for pixel_button_normal/focused/pressed/disabled. */
private fun Modifier.gameFoldNsButtonSurface(
    focused: Boolean,
    pressed: Boolean,
    enabled: Boolean,
    face: Color,
    shadow: Color,
): Modifier = drawBehind {
    val border = GameFoldNsTokens.BorderWidth.toPx()
    fun borderedFace(left: Float, top: Float, right: Float, bottom: Float, color: Color) {
        val width = (size.width - left - right).coerceAtLeast(0f)
        val height = (size.height - top - bottom).coerceAtLeast(0f)
        drawRect(color, Offset(left, top), Size(width, height))
        drawRect(
            GameFoldNsTokens.Border,
            Offset(left + border / 2f, top + border / 2f),
            Size((width - border).coerceAtLeast(0f), (height - border).coerceAtLeast(0f)),
            style = Stroke(border),
        )
    }
    when {
        !enabled -> {
            drawRect(GameFoldNsTokens.DisabledSurface)
            drawRect(
                GameFoldNsTokens.DisabledText,
                Offset(border / 2f, border / 2f),
                Size((size.width - border).coerceAtLeast(0f), (size.height - border).coerceAtLeast(0f)),
                style = Stroke(border),
            )
        }
        pressed -> borderedFace(2.dp.toPx(), 2.dp.toPx(), 2.dp.toPx(), 2.dp.toPx(), GameFoldNsTokens.SurfacePressed)
        focused -> {
            drawRect(GameFoldNsTokens.FocusGlow)
            val focusInset = 2.dp.toPx()
            drawRect(
                GameFoldNsTokens.Focus,
                Offset(focusInset, focusInset),
                Size(
                    (size.width - focusInset * 2f).coerceAtLeast(0f),
                    (size.height - focusInset * 2f).coerceAtLeast(0f),
                ),
            )
            val faceInset = 6.dp.toPx()
            borderedFace(faceInset, faceInset, faceInset, faceInset, GameFoldNsTokens.Surface)
        }
        else -> {
            val offset = GameFoldNsTokens.ShadowOffset.toPx()
            drawRect(
                shadow,
                Offset(offset, offset),
                Size((size.width - offset).coerceAtLeast(0f), (size.height - offset).coerceAtLeast(0f)),
            )
            borderedFace(0f, 0f, offset, offset, face)
        }
    }
}

/** Drawable-equivalent renderer for pixel_list_item_normal/focused. */
fun Modifier.gameFoldNsSettingsItemSurface(focused: Boolean): Modifier = if (focused) {
    gameFoldNsButtonSurface(
        focused = true,
        pressed = false,
        enabled = true,
        face = GameFoldNsTokens.Surface,
        shadow = GameFoldNsTokens.Shadow,
    )
} else {
    drawBehind {
        val border = GameFoldNsTokens.BorderWidth.toPx()
        drawRect(GameFoldNsTokens.Surface)
        drawRect(
            GameFoldNsTokens.Border,
            Offset(border / 2f, border / 2f),
            Size((size.width - border).coerceAtLeast(0f), (size.height - border).coerceAtLeast(0f)),
            style = Stroke(border),
        )
        drawRect(GameFoldNsTokens.Focus, size = Size(4.dp.toPx(), size.height))
        val bottom = 2.dp.toPx()
        drawRect(
            GameFoldNsTokens.SurfaceDark,
            Offset(0f, (size.height - bottom).coerceAtLeast(0f)),
            Size(size.width, bottom),
        )
    }
}

/** pixel_panel_background.xml / pixel_dialog_background.xml without layout-shifting padding. */
fun Modifier.gameFoldNsPanelSurface(dialog: Boolean = false): Modifier = drawBehind {
    val shadowOffset = 4.dp.toPx()
    val faceTrim = (if (dialog) 3.dp else 4.dp).toPx()
    val highlightRight = (if (dialog) 5.dp else 6.dp).toPx()
    val border = GameFoldNsTokens.BorderWidth.toPx()
    drawRect(
        GameFoldNsTokens.Shadow,
        Offset(shadowOffset, shadowOffset),
        Size(
            (size.width - shadowOffset).coerceAtLeast(0f),
            (size.height - shadowOffset).coerceAtLeast(0f),
        ),
    )
    val faceWidth = (size.width - faceTrim).coerceAtLeast(0f)
    val faceHeight = (size.height - faceTrim).coerceAtLeast(0f)
    drawRect(GameFoldNsTokens.Surface, size = Size(faceWidth, faceHeight))
    drawRect(
        GameFoldNsTokens.Border,
        Offset(border / 2f, border / 2f),
        Size((faceWidth - border).coerceAtLeast(0f), (faceHeight - border).coerceAtLeast(0f)),
        style = Stroke(border),
    )
    val highlightLeft = 2.dp.toPx()
    val highlightHeight = 2.dp.toPx()
    drawRect(
        GameFoldNsTokens.Surface,
        Offset(highlightLeft, highlightLeft),
        Size(
            (size.width - highlightLeft - highlightRight).coerceAtLeast(0f),
            highlightHeight,
        ),
    )
}

/** GameFoldPixel.Button / GameFoldPixel.Button.Action projection for the PC Compose host. */
@Composable
fun GameFoldNsButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val pressed by source.collectIsPressedAsState()
    val face = when {
        !enabled -> GameFoldNsTokens.DisabledSurface
        primary -> GameFoldNsTokens.Action
        else -> GameFoldNsTokens.Surface
    }
    val shadow = if (primary) GameFoldNsTokens.ActionShadow else GameFoldNsTokens.Shadow
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = GameFoldNsTokens.TouchTarget)
            .height(GameFoldNsTokens.ButtonHeight)
            .gameFoldNsButtonSurface(
                focused = focused,
                pressed = pressed,
                enabled = enabled,
                face = face,
                shadow = shadow,
            )
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .clickable(
                enabled = enabled,
                interactionSource = source,
                indication = null,
                onClick = onClick,
            )
            .focusable(enabled = enabled, interactionSource = source)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon?.let {
                it()
                Spacer(Modifier.width(GameFoldNsTokens.SpaceSm))
            }
            Text(
                text = label,
                color = when {
                    !enabled -> GameFoldNsTokens.DisabledText
                    primary -> GameFoldNsTokens.Surface
                    else -> GameFoldNsTokens.Ink
                },
                fontFamily = GameFoldNsTokens.LabelFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
            )
        }
    }
}

/** card_home_option.xml projection: 68dp row, 36dp icon slot and semantic text hierarchy. */
@Composable
fun GameFoldNsListItem(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    selected: Boolean = false,
    focusRequester: FocusRequester? = null,
    onFocused: () -> Unit = {},
) {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val pressed by source.collectIsPressedAsState()
    val highlighted = focused || selected
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .defaultMinSize(minHeight = 68.dp)
            .gameFoldNsButtonSurface(
                focused = highlighted,
                pressed = pressed,
                enabled = true,
                face = GameFoldNsTokens.Surface,
                shadow = GameFoldNsTokens.Shadow,
            )
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .onFocusChanged { if (it.isFocused) onFocused() }
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .focusable(interactionSource = source)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.padding(start = 8.dp).size(36.dp), contentAlignment = Alignment.Center) {
            icon()
        }
        Column(Modifier.padding(start = 16.dp, end = 8.dp).weight(1f)) {
            Text(
                text = title,
                color = GameFoldNsTokens.Ink,
                fontFamily = GameFoldNsTokens.LabelFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = description,
                color = GameFoldNsTokens.Muted,
                fontFamily = GameFoldNsTokens.BodyFont,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            detail?.let {
                Text(
                    text = it,
                    color = GameFoldNsTokens.Muted,
                    fontFamily = GameFoldNsTokens.NumericFont,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun GameFoldNsPixelSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    Box(
        modifier = modifier
            .width(GameFoldNsTokens.SwitchWidth)
            .height(GameFoldNsTokens.TouchTarget)
            .then(if (focused) Modifier.gameFoldNsFocusFrame(true) else Modifier)
            .clickable(
                enabled = enabled,
                interactionSource = source,
                indication = null,
                onClick = { onCheckedChange(!checked) },
            )
            .focusable(enabled = enabled, interactionSource = source),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = GameFoldNsTokens.SwitchWidth, height = GameFoldNsTokens.SwitchHeight)
                .background(
                    when {
                        !enabled -> GameFoldNsTokens.DisabledSurface
                        checked -> GameFoldNsTokens.Moss
                        else -> GameFoldNsTokens.SurfaceDark
                    },
                    RectangleShape,
                )
                .border(2.dp, if (enabled) GameFoldNsTokens.Border else GameFoldNsTokens.DisabledText),
        ) {
            Box(
                modifier = Modifier
                    .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 4.dp)
                    .size(24.dp)
                    .background(
                        when {
                            !enabled -> GameFoldNsTokens.DisabledText
                            checked -> GameFoldNsTokens.Focus
                            else -> GameFoldNsTokens.Surface
                        },
                        RectangleShape,
                    )
                    .border(2.dp, if (enabled) GameFoldNsTokens.Border else GameFoldNsTokens.DisabledText),
            )
        }
    }
}

@Composable
fun GameFoldNsSettingsSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    GameFoldNsSettingsRow(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        action = {
            GameFoldNsPixelSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        },
    )
}

/** list_item_setting.xml projection shared by links, fields, dropdowns and switch rows. */
@Composable
fun GameFoldNsSettingsRow(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { it() }
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

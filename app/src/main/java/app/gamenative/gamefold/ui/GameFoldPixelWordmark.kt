package app.gamenative.gamefold.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.math.ceil
import kotlin.math.min

/** The official NS 5x7 bitmap wordmark renderer, shared by PC's local shell surfaces. */
@Composable
fun GameFoldPixelWordmark(
    text: String = "GAMEFOLD",
    modifier: Modifier = Modifier,
    color: Color = GameFoldNsTokens.Ink,
    // The NS PixelWordmarkView defaults to regular 5x7 glyphs; opt into weight only when a
    // caller explicitly needs a heavier control label.
    bold: Boolean = false,
    shadow: Boolean = false,
) {
    Canvas(modifier) {
        val glyphs = text.uppercase().map(::gameFoldPixelGlyphRows)
        val horizontalUnits = gameFoldPixelWordmarkUnits(text).toFloat()
        // PixelWordmarkView measures its own intrinsic glyph width. Compose callers often provide
        // a bounded slot, so fit the same 5x7 grid by both width and height and center it instead
        // of clipping the trailing glyphs.
        val pixel = min(size.height / 7f, size.width / horizontalUnits).coerceAtLeast(1f)
        val contentWidth = horizontalUnits * pixel
        val originOffsetX = ((size.width - contentWidth) / 2f).coerceAtLeast(0f)
        val gap = pixel
        val pixelWeight = if (bold) (pixel * 0.24f).coerceAtLeast(1f) else 0f
        val shadowOffset = if (shadow) pixel else 0f
        glyphs.forEachIndexed { index, glyph ->
            val originX = originOffsetX + index * (5f * pixel + gap)
            glyph.forEachIndexed { row, bits ->
                repeat(5) { column ->
                    if (bits and (1 shl (4 - column)) != 0) {
                        if (shadow) {
                            drawRect(
                                color = GameFoldNsTokens.Border,
                                topLeft = androidx.compose.ui.geometry.Offset(
                                    originX + column * pixel + shadowOffset,
                                    row * pixel + shadowOffset,
                                ),
                                size = androidx.compose.ui.geometry.Size(pixel, pixel),
                            )
                        }
                        drawRect(
                            color = color,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                originX + column * pixel,
                                row * pixel,
                            ),
                            size = androidx.compose.ui.geometry.Size(pixel + pixelWeight, pixel),
                        )
                    }
                }
            }
        }
    }
}

private val PIXEL_GLYPHS = mapOf(
    'G' to intArrayOf(0b01110, 0b10001, 0b10000, 0b10111, 0b10001, 0b10001, 0b01110),
    'A' to intArrayOf(0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001),
    'M' to intArrayOf(0b10001, 0b11011, 0b10101, 0b10101, 0b10001, 0b10001, 0b10001),
    'E' to intArrayOf(0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111),
    'F' to intArrayOf(0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000),
    'O' to intArrayOf(0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110),
    'L' to intArrayOf(0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111),
    'D' to intArrayOf(0b11110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b11110),
    'C' to intArrayOf(0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110),
    'H' to intArrayOf(0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001),
    'V' to intArrayOf(0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100),
    'U' to intArrayOf(0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110),
    'N' to intArrayOf(0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001, 0b10001),
    'P' to intArrayOf(0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000),
    'R' to intArrayOf(0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001),
    '!' to intArrayOf(0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0, 0b00100),
    ' ' to intArrayOf(0, 0, 0, 0, 0, 0, 0),
)

internal fun gameFoldPixelGlyphRows(character: Char): IntArray =
    PIXEL_GLYPHS[character.uppercaseChar()] ?: PIXEL_GLYPHS.getValue(' ')

internal fun gameFoldPixelWordmarkUnits(text: String): Int =
    text.length * 5 + (text.length - 1).coerceAtLeast(0)

internal fun gameFoldPixelMinimumWidthDp(text: String, heightDp: Int): Int =
    ceil(gameFoldPixelWordmarkUnits(text) * heightDp / 7f).toInt()

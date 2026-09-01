package app.gamenative.ui.component.dialog

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import app.gamenative.gamefold.ui.GameFoldNsTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetEditorThemeTest {
    @Test
    fun localShell_replacesInheritedDarkColorsWithTheGamePadLightPalette() {
        val paletteFactory = paletteFactory()
        val inherited = darkColorScheme(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainer = Color.Black,
            onSurface = Color.White,
        )

        val palette = paletteFactory.invoke(null, true, inherited) as ColorScheme

        assertEquals(GameFoldNsTokens.Background, palette.background)
        assertEquals(GameFoldNsTokens.Surface, palette.surface)
        assertEquals(GameFoldNsTokens.SurfaceDark, palette.surfaceContainer)
        assertEquals(GameFoldNsTokens.Ink, palette.onSurface)
    }

    @Test
    fun nonLocalShell_preservesTheInheritedPalette() {
        val paletteFactory = paletteFactory()
        val inherited = darkColorScheme(
            background = Color(0xFF010203),
            surface = Color(0xFF040506),
        )

        val palette = paletteFactory.invoke(null, false, inherited) as ColorScheme

        assertSame(inherited, palette)
    }

    private fun paletteFactory(): java.lang.reflect.Method {
        val method = Class.forName("app.gamenative.ui.component.dialog.PresetEditorThemeKt")
            .declaredMethods
            .singleOrNull { it.name == "gamePadPresetEditorColorScheme" }
        assertTrue("Preset editor color scheme factory must exist", method != null)
        method ?: error("Missing gamePadPresetEditorColorScheme")
        method.isAccessible = true
        return method
    }
}

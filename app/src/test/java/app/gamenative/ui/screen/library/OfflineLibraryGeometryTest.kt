package app.gamenative.ui.screen.library

import app.gamenative.gamefold.ui.gameFoldPixelGlyphRows
import app.gamenative.gamefold.ui.gameFoldPixelMinimumWidthDp
import app.gamenative.gamefold.ui.gameFoldPixelWordmarkUnits
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import androidx.compose.ui.graphics.Color
import org.junit.Test

class OfflineLibraryGeometryTest {

    @Test
    fun emptyLibraryAfterScan_usesCartridgeGridInsteadOfEmptyPanel() {
        assertEquals(
            OfflineLibraryContent.CARTRIDGE_GRID,
            offlineLibraryContent(
                hasStoragePermission = true,
                isRefreshing = false,
                gameCount = 0,
            ),
        )
    }

    @Test
    fun gameFoldPcWordmark_hasRenderableCGlyph() {
        assertArrayEquals(
            intArrayOf(0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110),
            gameFoldPixelGlyphRows('C'),
        )
    }

    @Test
    fun gamePadPcWordmark_usesExpectedHeaderWidth() {
        assertEquals(59, gameFoldPixelWordmarkUnits("GAMEPAD PC"))
        assertEquals(152, gameFoldPixelMinimumWidthDp("GAMEPAD PC", heightDp = 18))
    }

    @Test
    fun headerEdges_alignWithTheLandscapeCartridgeOuterEdges() {
        assertEquals(145, gamePadHeaderHorizontalInsetDp(screenWidthDp = 922))
        assertEquals(145, gamePadHeaderStartInsetDp(screenWidthDp = 922))
        assertEquals(145, gamePadHeaderEndInsetDp(screenWidthDp = 922))
    }

    @Test
    fun landscapeGameCards_scaleAsOneUnitWithoutChangingTitleTypography() {
        assertEquals(0.7f, gameFoldHomeCardScale(), 0.0001f)
        assertEquals(4, gameFoldHomeCardOuterPaddingDp())
        assertEquals(12, gameFoldCartridgeTitleFontSizeSp())
    }
    @Test
    fun nsCartridgeArtwork_matchesTheSquareImageSlot() {
        val ratio = gamefoldCartridgeArtworkTopRatio()
        assertEquals(8f / 100f, ratio, 0.0001f)
        assertEquals(80f / 100f, gamefoldCartridgeArtworkWidthRatio(), 0.0001f)
    }

    @Test
    fun portraitLibraryUsesMoreInsetToKeepCartridgesCompact() {
        assertEquals(48, gameFoldHomeGridHorizontalPaddingDp(portrait = false))
        assertTrue(gameFoldHomeGridHorizontalPaddingDp(portrait = true) > gameFoldHomeGridHorizontalPaddingDp(portrait = false))
    }

    @Test
    fun nsFocusLayerIsLargerThanNormalCartridge_andTitlesShareOneGap() {
        assertEquals(3, gamefoldCartridgeContentInsetDp())
        assertEquals(4, gamefoldCartridgeTitleGapDp())
    }

    @Test
    fun addGameCartridge_usesTheSameVisibleInsetAsNormalCartridges() {
        assertEquals(gamefoldCartridgeContentInsetDp(), gamefoldAddGameContentInsetDp())
    }

    @Test
    fun addGameDialogAlignsToTheHomeContentInsets() {
        assertEquals(145, gamePadAddGameDialogHorizontalInsetDp(screenWidthDp = 922))
        assertEquals(632f / 922f, gamePadAddGameDialogWidthFraction(screenWidthDp = 922), 0.0001f)
    }

    @Test
    fun addGameDialogUsesTheApprovedFriendlyPathLabel() {
        assertEquals("根目录/GamePadPC/Game", gamePadAddGameDialogPathLabel())
    }

    @Test
    fun selectedHeaderAction_usesWhiteIconTint() {
        val visual = gameFoldHeaderActionVisual(
            selected = true,
            focused = false,
            semanticFocused = false,
        )

        assertEquals(Color.White, visual.iconTint)
        assertFalse(visual.showForegroundFocus)
    }

    @Test
    fun headerFocus_isRenderedAsForegroundForPhysicalOrSemanticFocus() {
        assertTrue(
            gameFoldHeaderActionVisual(
                selected = false,
                focused = true,
                semanticFocused = false,
            ).showForegroundFocus,
        )
        assertTrue(
            gameFoldHeaderActionVisual(
                selected = false,
                focused = false,
                semanticFocused = true,
            ).showForegroundFocus,
        )
        assertFalse(
            gameFoldHeaderActionVisual(
                selected = false,
                focused = false,
                semanticFocused = false,
            ).showForegroundFocus,
        )
    }
}

package app.gamenative.gamepad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GamePadViewportPolicyTest {
    @Test
    fun sixteenByTen_fillsTheWholeTargetWindow() {
        assertEquals(
            GamePadViewport(left = 0, top = 0, width = 2304, height = 1440),
            GamePadViewportPolicy.fit(2304, 1440, 1280, 800),
        )
    }

    @Test
    fun sixteenByNine_isCenteredWithExact72PixelLetterboxes() {
        assertEquals(
            GamePadViewport(left = 0, top = 72, width = 2304, height = 1296),
            GamePadViewportPolicy.fit(2304, 1440, 1920, 1080),
        )
    }

    @Test
    fun invalidSourceSize_isRejectedInsteadOfGuessed() {
        assertThrows(IllegalArgumentException::class.java) {
            GamePadViewportPolicy.fit(2304, 1440, 0, 1080)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GamePadViewportPolicy.fit(2304, 1440, 1920, -1)
        }
    }

    @Test
    fun containerScreenSize_parserRejectsMalformedOrNonPositiveValues() {
        assertEquals(GamePadSourceSize(1920, 1080), GamePadViewportPolicy.parseSourceSize("1920x1080"))
        assertEquals(GamePadSourceSize(1280, 800), GamePadViewportPolicy.parseSourceSize(" 1280X800 "))
        assertNull(GamePadViewportPolicy.parseSourceSize("1920"))
        assertNull(GamePadViewportPolicy.parseSourceSize("0x1080"))
        assertNull(GamePadViewportPolicy.parseSourceSize("wide x tall"))
    }

    @Test
    fun runtimeSourceValidation_returnsExplicitErrorInsteadOfThrowing() {
        assertEquals(
            GamePadRuntimeSourceValidation.Valid(GamePadSourceSize(1920, 1080)),
            GamePadViewportPolicy.validateRuntimeSource("1920x1080"),
        )
        assertEquals(
            GamePadRuntimeSourceValidation.Invalid(
                "GamePad PC 无法读取游戏分辨率：bad-size",
            ),
            GamePadViewportPolicy.validateRuntimeSource("bad-size"),
        )
    }
}

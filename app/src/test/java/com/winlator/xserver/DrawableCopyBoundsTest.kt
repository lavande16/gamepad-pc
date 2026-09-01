package com.winlator.xserver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class DrawableCopyBoundsTest {
    @Test
    fun clip_movesNegativeSourceOffsetIntoDestinationAndShrinksTheCopy() {
        val region = DrawableCopyBounds.clip(
            -2, -1, 4, 3, 10, 8, 20, 10, 20, 10,
        )

        assertNotNull(region)
        region ?: return
        assertEquals(0, region.srcX)
        assertEquals(0, region.srcY)
        assertEquals(6, region.dstX)
        assertEquals(4, region.dstY)
        assertEquals(8, region.width)
        assertEquals(6, region.height)
    }

    @Test
    fun clip_limitsTheCopyToBothDirectBufferBounds() {
        val region = DrawableCopyBounds.clip(
            6, 4, 3, 2, 10, 10, 8, 6, 12, 7,
        )

        assertNotNull(region)
        region ?: return
        assertEquals(2, region.width)
        assertEquals(2, region.height)
    }

    @Test
    fun clip_rejectsMalformedOrCompletelyOutOfBoundsCopies() {
        assertNull(DrawableCopyBounds.clip(0, 0, 0, 0, 4, 4, 0, 4, 4, 4))
        assertNull(DrawableCopyBounds.clip(20, 0, 0, 0, 4, 4, 8, 4, 8, 4))
        assertNull(DrawableCopyBounds.clip(0, 0, 0, 0, -1, 4, 8, 4, 8, 4))
    }
}

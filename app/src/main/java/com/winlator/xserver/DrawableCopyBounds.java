package com.winlator.xserver;

/** Clips a pixel copy against both direct-buffer rectangles before JNI receives it. */
final class DrawableCopyBounds {
    static final class Region {
        final int srcX;
        final int srcY;
        final int dstX;
        final int dstY;
        final int width;
        final int height;

        Region(int srcX, int srcY, int dstX, int dstY, int width, int height) {
            this.srcX = srcX;
            this.srcY = srcY;
            this.dstX = dstX;
            this.dstY = dstY;
            this.width = width;
            this.height = height;
        }
    }

    private DrawableCopyBounds() {}

    static Region clip(
            int srcX, int srcY, int dstX, int dstY, int width, int height,
            int srcStride, int srcRows, int dstStride, int dstRows
    ) {
        if (width <= 0 || height <= 0 || srcStride <= 0 || srcRows <= 0 ||
                dstStride <= 0 || dstRows <= 0 ||
                srcStride > Short.MAX_VALUE || dstStride > Short.MAX_VALUE) {
            return null;
        }

        if (srcX < 0) {
            int delta = -srcX;
            srcX = 0;
            dstX += delta;
            width -= delta;
        }
        if (srcY < 0) {
            int delta = -srcY;
            srcY = 0;
            dstY += delta;
            height -= delta;
        }
        if (dstX < 0) {
            int delta = -dstX;
            dstX = 0;
            srcX += delta;
            width -= delta;
        }
        if (dstY < 0) {
            int delta = -dstY;
            dstY = 0;
            srcY += delta;
            height -= delta;
        }

        if (width <= 0 || height <= 0 || srcX >= srcStride || srcY >= srcRows ||
                dstX >= dstStride || dstY >= dstRows) {
            return null;
        }

        width = Math.min(width, srcStride - srcX);
        width = Math.min(width, dstStride - dstX);
        height = Math.min(height, srcRows - srcY);
        height = Math.min(height, dstRows - dstY);
        width = Math.min(width, Short.MAX_VALUE);
        height = Math.min(height, Short.MAX_VALUE);
        if (width <= 0 || height <= 0) return null;

        return new Region(srcX, srcY, dstX, dstY, width, height);
    }
}

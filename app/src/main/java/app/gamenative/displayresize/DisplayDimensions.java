package app.gamenative.displayresize;

import java.util.Objects;

public final class DisplayDimensions {
    private final int width;
    private final int height;

    public DisplayDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("分辨率必须大于 0");
        }
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DisplayDimensions)) return false;
        DisplayDimensions that = (DisplayDimensions) other;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}

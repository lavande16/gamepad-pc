package app.gamenative.displayresize;

public final class DisplaySizeSnapshot {
    public static final DisplayDimensions TARGET = new DisplayDimensions(1296, 2304);

    private final DisplayDimensions physical;
    private final DisplayDimensions override;

    public DisplaySizeSnapshot(DisplayDimensions physical, DisplayDimensions override) {
        this.physical = physical;
        this.override = override;
    }

    public DisplayDimensions physical() {
        return physical;
    }

    public DisplayDimensions override() {
        return override;
    }

    public boolean hasOverride() {
        return override != null;
    }

    public boolean isTargetOverride() {
        return TARGET.equals(override);
    }

    public DisplayDimensions current() {
        return override != null ? override : physical;
    }
}

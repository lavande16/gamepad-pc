package app.gamenative.displayresize;

public final class ToggleResult {
    private final boolean success;
    private final String message;
    private final DisplaySizeSnapshot snapshot;

    private ToggleResult(boolean success, String message, DisplaySizeSnapshot snapshot) {
        this.success = success;
        this.message = message;
        this.snapshot = snapshot;
    }

    public static ToggleResult success(DisplaySizeSnapshot snapshot) {
        return new ToggleResult(true, "", snapshot);
    }

    public static ToggleResult failure(String message) {
        return new ToggleResult(false, message, null);
    }

    public static ToggleResult failure(String message, DisplaySizeSnapshot snapshot) {
        return new ToggleResult(false, message, snapshot);
    }

    public boolean isSuccess() {
        return success;
    }

    public String message() {
        return message;
    }

    public DisplaySizeSnapshot snapshot() {
        return snapshot;
    }
}

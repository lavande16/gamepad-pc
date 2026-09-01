package app.gamenative.displayresize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class WindowManagerCommandExecutorTest {
    @Test
    public void readReportsPhysicalAndForcedSizes() {
        FakeBridge bridge = new FakeBridge(
                new DisplayDimensions(1440, 2304),
                new DisplayDimensions(1296, 2304));
        WindowManagerCommandExecutor executor = new WindowManagerCommandExecutor(bridge);

        CommandResult result = executor.execute("/system/bin/wm", "size");

        assertEquals(0, result.exitCode());
        assertEquals(
                "Physical size: 1440x2304\nOverride size: 1296x2304\n",
                result.stdout());
    }

    @Test
    public void targetCommandCallsForcedDisplaySizeWithExactTarget() {
        FakeBridge bridge = new FakeBridge(
                new DisplayDimensions(1440, 2304),
                new DisplayDimensions(1440, 2304));
        WindowManagerCommandExecutor executor = new WindowManagerCommandExecutor(bridge);

        CommandResult result = executor.execute("/system/bin/wm", "size", "1296x2304");

        assertEquals(0, result.exitCode());
        assertEquals(new DisplayDimensions(1296, 2304), bridge.forcedSize);
    }

    @Test
    public void resetCommandCallsClearForcedDisplaySize() {
        FakeBridge bridge = new FakeBridge(
                new DisplayDimensions(1440, 2304),
                new DisplayDimensions(1296, 2304));
        WindowManagerCommandExecutor executor = new WindowManagerCommandExecutor(bridge);

        CommandResult result = executor.execute("/system/bin/wm", "size", "reset");

        assertEquals(0, result.exitCode());
        assertTrue(bridge.cleared);
    }

    private static final class FakeBridge implements WindowManagerCommandExecutor.Bridge {
        private final DisplayDimensions initial;
        private DisplayDimensions base;
        private DisplayDimensions forcedSize;
        private boolean cleared;

        private FakeBridge(DisplayDimensions initial, DisplayDimensions base) {
            this.initial = initial;
            this.base = base;
        }

        @Override
        public DisplayDimensions initialSize() {
            return initial;
        }

        @Override
        public DisplayDimensions baseSize() {
            return base;
        }

        @Override
        public void setForcedSize(int width, int height) {
            forcedSize = new DisplayDimensions(width, height);
            base = forcedSize;
        }

        @Override
        public void clearForcedSize() {
            cleared = true;
            base = initial;
        }
    }
}

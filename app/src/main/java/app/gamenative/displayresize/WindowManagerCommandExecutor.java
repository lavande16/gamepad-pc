package app.gamenative.displayresize;

public final class WindowManagerCommandExecutor implements CommandExecutor {
    interface Bridge {
        DisplayDimensions initialSize() throws Exception;
        DisplayDimensions baseSize() throws Exception;
        void setForcedSize(int width, int height) throws Exception;
        void clearForcedSize() throws Exception;
    }

    private final Bridge bridge;

    public WindowManagerCommandExecutor() {
        this(new ReflectiveWindowManagerBridge());
    }

    WindowManagerCommandExecutor(Bridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public CommandResult execute(String... command) {
        try {
            if (isSizeRead(command)) {
                DisplayDimensions initial = bridge.initialSize();
                DisplayDimensions base = bridge.baseSize();
                StringBuilder output = new StringBuilder()
                        .append("Physical size: ")
                        .append(initial)
                        .append('\n');
                if (!base.equals(initial)) {
                    output.append("Override size: ").append(base).append('\n');
                }
                return new CommandResult(0, output.toString(), "");
            }
            if (isSizeReset(command)) {
                bridge.clearForcedSize();
                return new CommandResult(0, "", "");
            }
            if (isTargetSize(command)) {
                bridge.setForcedSize(1296, 2304);
                return new CommandResult(0, "", "");
            }
            return new CommandResult(2, "", "不支持的 WindowManager 命令");
        } catch (Throwable error) {
            String detail = error.getMessage();
            if (detail == null || detail.trim().isEmpty()) {
                detail = error.getClass().getSimpleName();
            }
            return new CommandResult(1, "", detail);
        }
    }

    private static boolean isSizeRead(String[] command) {
        return command != null
                && command.length == 2
                && "/system/bin/wm".equals(command[0])
                && "size".equals(command[1]);
    }

    private static boolean isSizeReset(String[] command) {
        return command != null
                && command.length == 3
                && "/system/bin/wm".equals(command[0])
                && "size".equals(command[1])
                && "reset".equals(command[2]);
    }

    private static boolean isTargetSize(String[] command) {
        return command != null
                && command.length == 3
                && "/system/bin/wm".equals(command[0])
                && "size".equals(command[1])
                && "1296x2304".equals(command[2]);
    }
}

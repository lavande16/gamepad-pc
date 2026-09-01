package app.gamenative.displayresize;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DisplaySizeController {
    private static final String WM = "/system/bin/wm";

    private final CommandExecutor executor;
    private final AtomicBoolean busy = new AtomicBoolean(false);

    public DisplaySizeController(CommandExecutor executor) {
        this.executor = executor;
    }

    public DisplaySizeSnapshot read() throws Exception {
        CommandResult result = executor.execute(WM, "size");
        if (result.exitCode() != 0) {
            throw new IllegalStateException(commandError("读取失败", result));
        }
        return DisplaySizeParser.parse(result.stdout());
    }

    public ToggleResult toggle(DisplaySizeSnapshot current, boolean hasWriteSecureSettings) {
        if (!hasWriteSecureSettings) {
            return ToggleResult.failure("需要一次 ADB 授权");
        }
        if (!busy.compareAndSet(false, true)) {
            return ToggleResult.failure("操作正在进行中");
        }

        try {
            boolean reset = current.hasOverride();
            CommandResult write = reset
                    ? executor.execute(WM, "size", "reset")
                    : executor.execute(WM, "size", "1296x2304");
            if (write.exitCode() != 0) {
                return ToggleResult.failure(commandError("切换失败", write));
            }

            DisplaySizeSnapshot verified = read();
            boolean matches = reset ? !verified.hasOverride() : verified.isTargetOverride();
            if (!matches) {
                return ToggleResult.failure("切换后分辨率校验不一致", verified);
            }
            return ToggleResult.success(verified);
        } catch (Exception error) {
            String detail = error.getMessage();
            return ToggleResult.failure(
                    detail == null || detail.trim().isEmpty()
                            ? "切换失败"
                            : "切换失败：" + detail.trim());
        } finally {
            busy.set(false);
        }
    }

    private static String commandError(String prefix, CommandResult result) {
        String detail = result.stderr().trim();
        if (detail.isEmpty()) {
            detail = result.stdout().trim();
        }
        if (detail.isEmpty()) {
            detail = "退出码 " + result.exitCode();
        }
        return prefix + "：" + detail;
    }
}

package app.gamenative.displayresize;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public final class DisplaySizeControllerTest {
    private static final DisplaySizeSnapshot DEFAULT = new DisplaySizeSnapshot(
            new DisplayDimensions(1440, 2304), null);
    private static final DisplaySizeSnapshot TARGET = new DisplaySizeSnapshot(
            new DisplayDimensions(1440, 2304), new DisplayDimensions(1296, 2304));

    @Test
    public void defaultStateSetsTargetAndRequiresMatchingReadBack() {
        QueueExecutor executor = new QueueExecutor(
                ok(""),
                ok("Physical size: 1440x2304\nOverride size: 1296x2304\n"));
        DisplaySizeController controller = new DisplaySizeController(executor);

        ToggleResult result = controller.toggle(DEFAULT, true);

        assertTrue(result.isSuccess());
        assertTrue(result.snapshot().isTargetOverride());
        assertArrayEquals(
                new String[]{"/system/bin/wm", "size", "1296x2304"},
                executor.commands.get(0));
        assertArrayEquals(
                new String[]{"/system/bin/wm", "size"},
                executor.commands.get(1));
    }

    @Test
    public void targetStateClearsOverrideInsteadOfForcingPhysicalSize() {
        QueueExecutor executor = new QueueExecutor(
                ok(""),
                ok("Physical size: 1440x2304\n"));
        DisplaySizeController controller = new DisplaySizeController(executor);

        ToggleResult result = controller.toggle(TARGET, true);

        assertTrue(result.isSuccess());
        assertFalse(result.snapshot().hasOverride());
        assertArrayEquals(
                new String[]{"/system/bin/wm", "size", "reset"},
                executor.commands.get(0));
    }

    @Test
    public void unfamiliarOverrideAlsoUsesExplicitReset() {
        QueueExecutor executor = new QueueExecutor(
                ok(""),
                ok("Physical size: 1440x2304\n"));
        DisplaySizeController controller = new DisplaySizeController(executor);
        DisplaySizeSnapshot unfamiliar = new DisplaySizeSnapshot(
                new DisplayDimensions(1440, 2304),
                new DisplayDimensions(1080, 1920));

        ToggleResult result = controller.toggle(unfamiliar, true);

        assertTrue(result.isSuccess());
        assertArrayEquals(
                new String[]{"/system/bin/wm", "size", "reset"},
                executor.commands.get(0));
    }

    @Test
    public void missingPermissionExecutesNoCommand() {
        QueueExecutor executor = new QueueExecutor();
        DisplaySizeController controller = new DisplaySizeController(executor);

        ToggleResult result = controller.toggle(DEFAULT, false);

        assertFalse(result.isSuccess());
        assertTrue(executor.commands.isEmpty());
    }

    @Test
    public void mismatchedReadBackCannotReportSuccess() {
        QueueExecutor executor = new QueueExecutor(
                ok(""),
                ok("Physical size: 1440x2304\nOverride size: 1080x1920\n"));
        DisplaySizeController controller = new DisplaySizeController(executor);

        ToggleResult result = controller.toggle(DEFAULT, true);

        assertFalse(result.isSuccess());
        assertEquals("切换后分辨率校验不一致", result.message());
        assertNotNull(result.snapshot());
    }

    @Test
    public void overlappingActionIsRejectedWhileFirstCommandRuns() throws Exception {
        BlockingExecutor executor = new BlockingExecutor();
        DisplaySizeController controller = new DisplaySizeController(executor);
        AtomicReference<ToggleResult> firstResult = new AtomicReference<>();
        Thread first = new Thread(() -> firstResult.set(controller.toggle(DEFAULT, true)));

        first.start();
        assertTrue(executor.started.await(2, TimeUnit.SECONDS));
        ToggleResult secondResult = controller.toggle(DEFAULT, true);
        executor.release.countDown();
        first.join(2000);

        assertFalse(secondResult.isSuccess());
        assertTrue(firstResult.get().isSuccess());
    }

    private static CommandResult ok(String stdout) {
        return new CommandResult(0, stdout, "");
    }

    private static final class QueueExecutor implements CommandExecutor {
        private final Queue<CommandResult> results = new ArrayDeque<>();
        private final List<String[]> commands = new ArrayList<>();

        QueueExecutor(CommandResult... results) {
            this.results.addAll(Arrays.asList(results));
        }

        @Override
        public CommandResult execute(String... command) {
            commands.add(command);
            if (results.isEmpty()) {
                throw new AssertionError("没有为命令准备返回值: " + Arrays.toString(command));
            }
            return results.remove();
        }
    }

    private static final class BlockingExecutor implements CommandExecutor {
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private int calls;

        @Override
        public CommandResult execute(String... command) throws Exception {
            calls++;
            if (calls == 1) {
                started.countDown();
                release.await(2, TimeUnit.SECONDS);
                return ok("");
            }
            return ok("Physical size: 1440x2304\nOverride size: 1296x2304\n");
        }
    }
}

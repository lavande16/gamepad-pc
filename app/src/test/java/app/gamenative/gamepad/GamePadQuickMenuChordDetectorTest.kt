package app.gamenative.gamepad

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePadQuickMenuChordDetectorTest {
    @Test
    fun secondButtonWithinFiveHundredMillisecondsTriggersImmediatelyInEitherOrder() {
        val l3ThenR3 = detector()
        assertFalse(l3ThenR3.change("L3", true, 1_000L))
        assertTrue(l3ThenR3.change("R3", true, 1_500L))

        val r3ThenL3 = detector()
        assertFalse(r3ThenL3.change("R3", true, 2_000L))
        assertTrue(r3ThenL3.change("L3", true, 2_499L))
    }

    @Test
    fun pressesOutsideTheWindowOrSinglePressesDoNotTrigger() {
        val detector = detector()
        assertFalse(detector.change("L3", true, 1_000L))
        assertFalse(detector.change("R3", true, 1_501L))
    }

    @Test
    fun heldButtonsTriggerOnlyOnceUntilBothAreReleased() {
        val detector = detector()
        assertFalse(detector.change("L3", true, 1_000L))
        assertTrue(detector.change("R3", true, 1_200L))
        assertFalse(detector.change("R3", true, 1_250L))
        assertFalse(detector.change("L3", false, 1_300L))
        assertFalse(detector.change("L3", true, 1_350L))
        assertFalse(detector.change("R3", false, 1_400L))
        assertFalse(detector.change("L3", false, 1_450L))

        assertFalse(detector.change("R3", true, 2_000L))
        assertTrue(detector.change("L3", true, 2_100L))
    }

    @Test
    fun gameScreenObservesBothPhysicalAndOnScreenStickButtonsWithoutConsumingThem() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt",
        ).readText()

        assertTrue(source.contains("KeyEvent.KEYCODE_BUTTON_THUMBL -> GamePadQuickMenuChordButton.L3"))
        assertTrue(source.contains("KeyEvent.KEYCODE_BUTTON_THUMBR -> GamePadQuickMenuChordButton.R3"))
        assertTrue(source.contains("GameFoldButton.L3 -> GamePadQuickMenuChordButton.L3"))
        assertTrue(source.contains("GameFoldButton.R3 -> GamePadQuickMenuChordButton.R3"))
        assertTrue(source.contains("view.post { openQuickMenu(\"controller L3+R3\") }"))
    }

    private fun detector(): ReflectedDetector {
        val detectorClass = runCatching {
            Class.forName("app.gamenative.gamepad.GamePadQuickMenuChordDetector")
        }.getOrNull()
        assertNotNull("GamePadQuickMenuChordDetector must exist", detectorClass)
        return ReflectedDetector(detectorClass!!)
    }

    private class ReflectedDetector(detectorClass: Class<*>) {
        private val buttonClass = Class.forName("app.gamenative.gamepad.GamePadQuickMenuChordButton")
        private val instance = detectorClass.getDeclaredConstructor(Long::class.javaPrimitiveType)
            .apply { isAccessible = true }
            .newInstance(500L)
        private val change = detectorClass.getDeclaredMethod(
            "onButtonChanged",
            buttonClass,
            Boolean::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
        ).apply { isAccessible = true }

        fun change(button: String, pressed: Boolean, nowMs: Long): Boolean {
            @Suppress("UNCHECKED_CAST")
            val enumValue = java.lang.Enum.valueOf(buttonClass as Class<out Enum<*>>, button)
            return change.invoke(instance, enumValue, pressed, nowMs) as Boolean
        }
    }

    private fun sourceFile(path: String): File {
        val workingDirectory = requireNotNull(System.getProperty("user.dir"))
        var current = File(workingDirectory).canonicalFile
        repeat(8) {
            val candidate = File(current, path)
            if (candidate.exists()) return candidate
            current = current.parentFile ?: return candidate
        }
        return File(workingDirectory, path)
    }
}

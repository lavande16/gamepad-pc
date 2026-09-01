package app.gamenative.gamepad

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdbQuickMenuContractTest {
    @Test
    fun manifestExposesOnlyAnExplicitDumpProtectedReceiver() {
        val manifest = sourceFile("app/src/main/AndroidManifest.xml").readText()
        val declaration = Regex(
            """<receiver\s+android:name="\.diagnostics\.AdbQuickMenuReceiver"\s+android:exported="true"\s+android:permission="android\.permission\.DUMP"\s*/>""",
        )

        assertTrue("ADB quick-menu receiver must be exported behind DUMP", declaration.containsMatchIn(manifest))

        val receiverStart = manifest.indexOf("android:name=\".diagnostics.AdbQuickMenuReceiver\"")
        if (receiverStart >= 0) {
            val nearbyManifest = manifest.substring(receiverStart, minOf(manifest.length, receiverStart + 300))
            assertFalse("Receiver must not expose an implicit intent filter", nearbyManifest.contains("<intent-filter>"))
        }
    }

    @Test
    fun receiverAcceptsOnlyTheExactGamePadActionAndRoutesAnInternalEvent() {
        val receiver = sourceFile("app/src/main/java/app/gamenative/diagnostics/AdbQuickMenuReceiver.kt")
        assertTrue("ADB quick-menu receiver source must exist", receiver.isFile)
        if (!receiver.isFile) return

        val source = receiver.readText()
        assertTrue(source.contains("com.gamepad.pc.action.OPEN_QUICK_MENU"))
        assertTrue(source.contains("BuildConfig.GAMEFOLD_PC_LOCAL_SHELL"))
        assertTrue(source.contains("intent.action != ACTION_OPEN_QUICK_MENU"))
        assertTrue(source.contains("AndroidEvent.OpenAdbQuickMenu"))
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

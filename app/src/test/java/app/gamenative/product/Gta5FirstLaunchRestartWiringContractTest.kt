package app.gamenative.product

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Gta5FirstLaunchRestartWiringContractTest {
    @Test
    fun completedGta5PrerequisitesExitCleanlyBeforeAutomaticRelaunch() {
        val xServerSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt",
        ).readText()
        val eventsSource = sourceFile(
            "app/src/main/java/app/gamenative/events/AndroidEvent.kt",
        ).readText()
        val mainSource = sourceFile(
            "app/src/main/java/app/gamenative/ui/PluviaMain.kt",
        ).readText()

        assertTrue(eventsSource.contains("data class RestartGameAfterProvisioning(val appId: String)"))
        assertTrue(xServerSource.contains("Gta5FirstLaunchRestartPolicy.shouldRestartAfterPreInstall("))
        assertTrue(xServerSource.contains("AndroidEvent.RestartGameAfterProvisioning(appId)"))
        assertTrue(xServerSource.contains("onRestartAfterProvisioning(event.appId)"))
        assertTrue(xServerSource.contains("afterExit ="))
        assertTrue(mainSource.contains("onRestartAfterProvisioning = { restartAppId ->"))
        assertTrue(mainSource.contains("preLaunchApp("))
    }

    private fun sourceFile(path: String): File = listOf(
        File(path),
        File("../$path"),
    ).firstOrNull { it.isFile }
        ?: error("Source file not found from test working directory: $path")
}

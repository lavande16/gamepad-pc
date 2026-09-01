package app.gamenative.product

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Cyberpunk2077SaveImportWiringContractTest {
    @Test
    fun localShellLaunchImportsAfterRuntimeReadinessAndNeverBlocksActivation() {
        val source = sourceFile(
            "app/src/main/java/app/gamenative/ui/PluviaMain.kt",
        ).readText()
        val functionStart = source.indexOf("private fun preLaunchLocalShell(")
        val functionEnd = source.indexOf("\nfun preLaunchApp(", functionStart)
        assertTrue(functionStart >= 0 && functionEnd > functionStart)
        val launch = source.substring(functionStart, functionEnd)

        val runtimeReady = launch.indexOf("if (!deferredDependenciesReady)")
        val runtimeFailureReturn = launch.indexOf("return@launch", runtimeReady)
        val importer = launch.indexOf("Cyberpunk2077SaveAutoImporter().importIfNeeded(")
        val executable = launch.indexOf("CustomGameScanner.getLaunchExecutable(container)", importer)
        val containerRoot = launch.indexOf("containerRoot = container.rootDir", importer)
        val policy = launch.indexOf(
            "Cyberpunk2077SaveImportLaunchPolicy.notificationFor(cyberpunkSaveImportResult)",
        )
        val loadingResult = launch.indexOf("setMessageIfActive(cyberpunkSaveImportMessage)")
        val temporaryDelay = launch.indexOf(
            "delay(Cyberpunk2077SaveImportLaunchPolicy.NOTIFICATION_DURATION_MS)",
        )
        val activate = launch.indexOf("containerManager.activateContainer(container)")
        val success = launch.indexOf("onSuccess(context, appId, preLaunchTicket)")

        assertTrue(runtimeFailureReturn in runtimeReady until importer)
        assertTrue(executable in importer until policy)
        assertTrue(containerRoot in importer until policy)
        assertTrue(policy in importer until loadingResult)
        assertTrue(loadingResult in policy until temporaryDelay)
        assertTrue(temporaryDelay in loadingResult until activate)
        assertTrue(!launch.contains("cyberpunk2077_save_import_failed_title"))
        assertTrue(activate < success)
    }

    private fun sourceFile(path: String): File = listOf(
        File(path),
        File("../$path"),
    ).firstOrNull { it.isFile }
        ?: error("Source file not found from test working directory: $path")
}

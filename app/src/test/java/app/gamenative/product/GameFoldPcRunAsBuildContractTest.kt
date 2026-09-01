package app.gamenative.product

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFoldPcRunAsBuildContractTest {
    @Test
    fun personalBaselineEnablesRunAsByDefaultAndStillAllowsAnExplicitOverride() {
        val appBuildScript = sourceFile("app/build.gradle.kts").readText()
        val manifest = sourceFile("app/src/main/AndroidManifest.xml").readText()

        assertTrue(
            appBuildScript.contains(
                "providers.gradleProperty(\"GAMEFOLD_PC_RUN_AS_ENABLED\")" +
                    ".orNull?.toBooleanStrictOrNull() ?: true",
            ),
        )
        assertTrue(manifest.contains("android:debuggable=\"\${gameFoldPcRunAsEnabled}\""))
        assertTrue(manifest.contains("tools:ignore=\"HardcodedDebugMode\""))
        assertTrue(
            appBuildScript.contains(
                "release {\n            // Internal diagnostics may explicitly enable run-as without disabling Release optimizations.\n" +
                    "            manifestPlaceholders[\"gameFoldPcRunAsEnabled\"] = gameFoldPcRunAsEnabled",
            ),
        )
        assertTrue(
            Regex(
                "create\\(\"release-signed\"\\) \\{\\s+" +
                    "manifestPlaceholders\\[\"gameFoldPcRunAsEnabled\"\\] = false",
            ).containsMatchIn(appBuildScript),
        )
        assertTrue(
            Regex(
                "create\\(\"release-gold\"\\) \\{\\s+" +
                    "manifestPlaceholders\\[\"gameFoldPcRunAsEnabled\"\\] = false",
            ).containsMatchIn(appBuildScript),
        )
        assertTrue(!appBuildScript.contains("isDebuggable = gameFoldPcRunAsEnabled"))
    }

    private fun sourceFile(relativePath: String): File {
        val userDirectory = System.getProperty("user.dir") ?: error("user.dir is unavailable")
        var cursor = File(userDirectory).absoluteFile
        repeat(8) {
            val candidate = File(cursor, relativePath)
            if (candidate.exists()) return candidate
            val parent = cursor.parentFile ?: error("Reached filesystem root while locating $relativePath")
            cursor = parent
        }
        error("Unable to locate $relativePath from $userDirectory")
    }
}

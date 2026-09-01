package app.gamenative.utils

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LsfgDllLocatorTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun resolve_usesNonEmptyLocalOverrideBeforeSteamCandidate() {
        val local = fileWithContents("local.dll", "official-dll")
        val steam = fileWithContents("steam.dll", "legacy-dll")

        assertEquals(local, LsfgDllLocator.resolve(local.absolutePath, steam))
    }

    @Test
    fun resolve_fallsBackToSteamCandidateWhenLocalOverrideIsMissing() {
        val steam = fileWithContents("steam.dll", "legacy-dll")
        val missingLocal = File(temporaryFolder.root, "missing.dll")

        assertEquals(steam, LsfgDllLocator.resolve(missingLocal.absolutePath, steam))
    }

    @Test
    fun resolve_ignoresZeroByteLocalOverrideAndUsesSteamCandidate() {
        val emptyLocal = File(temporaryFolder.root, "empty.dll").apply { createNewFile() }
        val steam = fileWithContents("steam.dll", "legacy-dll")

        assertEquals(steam, LsfgDllLocator.resolve(emptyLocal.absolutePath, steam))
    }

    private fun fileWithContents(name: String, contents: String): File =
        File(temporaryFolder.root, name).apply { writeText(contents) }
}

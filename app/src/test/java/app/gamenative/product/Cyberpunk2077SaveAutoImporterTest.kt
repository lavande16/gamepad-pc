package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

class Cyberpunk2077SaveAutoImporterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun supportedGameWithoutSavesReturnsNoSource() {
        val gameDirectory = cyberpunkGame()

        val result = Cyberpunk2077SaveAutoImporter().importIfNeeded(request(gameDirectory))

        assertEquals(Cyberpunk2077SaveAutoImporter.Result.NoSource, result)
    }

    @Test
    fun unsupportedLaunchContextsAndExecutablesAreNotApplicable() {
        val gameDirectory = cyberpunkGame()
        val supported = request(gameDirectory)
        val requests = listOf(
            supported.copy(localShell = false),
            supported.copy(customGame = false),
            supported.copy(bootToContainer = true),
            supported.copy(executablePath = File(gameDirectory, "REDlauncher.exe").absolutePath),
        )

        requests.forEach { candidate ->
            assertEquals(
                candidate.toString(),
                Cyberpunk2077SaveAutoImporter.Result.NotApplicable,
                Cyberpunk2077SaveAutoImporter().importIfNeeded(candidate),
            )
        }
    }

    @Test
    fun sourceDiscoveryAcceptsOnlyCompleteSafeCyberpunkSaveDirectories() {
        val gameDirectory = cyberpunkGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        val manual = saveSlot(saves, "ManualSave-0", "manual")
        File(manual, "notes.txt").writeText("ignored")
        saveSlot(saves, "AutoSave-12", "automatic", metadataVersion = 12)
        File(saves, "ManualSave-bad").mkdirs()
        File(saves, "QuickSave-1").apply {
            mkdirs()
            File(this, "screenshot.png").writeText("missing primary")
        }
        Files.createSymbolicLink(
            File(saves, "EndGameSave-3").toPath(),
            manual.toPath(),
        )

        val discovered = Cyberpunk2077SaveAutoImporter().discoverSourceSaves(gameDirectory)

        assertEquals(listOf("AutoSave-12", "ManualSave-0"), discovered.map { it.directory.name })
        assertEquals(
            listOf("metadata.9.json", "sav.dat", "screenshot.png"),
            discovered.single { it.directory.name == "ManualSave-0" }.files.map(File::getName),
        )
    }

    @Test
    fun freshTargetCopiesFourSavesVerifiesFilesAndWritesOneTimeDecision() {
        val gameDirectory = cyberpunkGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        listOf("ManualSave-0", "ManualSave-1", "ManualSave-2", "ManualSave-10")
            .forEachIndexed { index, name -> saveSlot(saves, name, "save-$index") }
        val request = request(gameDirectory)

        val result = Cyberpunk2077SaveAutoImporter().importIfNeeded(request)

        assertEquals(Cyberpunk2077SaveAutoImporter.Result.Imported(4), result)
        listOf("ManualSave-0", "ManualSave-1", "ManualSave-2", "ManualSave-10")
            .forEachIndexed { index, name ->
                assertEquals("save-$index", targetFile(request, "$name/sav.dat").readText())
                assertEquals("screen-save-$index", targetFile(request, "$name/screenshot.png").readText())
                assertEquals("metadata-save-$index", targetFile(request, "$name/metadata.9.json").readText())
            }
        val decision = decisionFile(request).readText()
        assertTrue(decision.contains("\"status\":\"imported\""))
        assertTrue(decision.contains("\"name\":\"ManualSave-10\""))
        assertTrue(decision.contains("\"sha256\":"))
    }

    @Test
    fun completedDecisionPreventsEveryLaterImport() {
        val gameDirectory = cyberpunkGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        val source = saveSlot(saves, "ManualSave-0", "first").resolve("sav.dat")
        val request = request(gameDirectory)
        assertEquals(
            Cyberpunk2077SaveAutoImporter.Result.Imported(1),
            Cyberpunk2077SaveAutoImporter().importIfNeeded(request),
        )
        source.writeText("changed")

        val secondResult = Cyberpunk2077SaveAutoImporter().importIfNeeded(request)

        assertEquals(Cyberpunk2077SaveAutoImporter.Result.AlreadyHandled, secondResult)
        assertEquals("first", targetFile(request, "ManualSave-0/sav.dat").readText())
    }

    @Test
    fun existingTargetIsPreservedAndHandledWithoutOverwrite() {
        val gameDirectory = cyberpunkGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        saveSlot(saves, "ManualSave-0", "packaged")
        val request = request(gameDirectory)
        targetFile(request, "ManualSave-99/sav.dat").apply {
            parentFile!!.mkdirs()
            writeText("user-progress")
        }

        val firstResult = Cyberpunk2077SaveAutoImporter().importIfNeeded(request)

        assertEquals(Cyberpunk2077SaveAutoImporter.Result.PreservedExisting(1), firstResult)
        assertEquals("user-progress", targetFile(request, "ManualSave-99/sav.dat").readText())
        assertFalse(targetFile(request, "ManualSave-0").exists())
        assertEquals(
            Cyberpunk2077SaveAutoImporter.Result.AlreadyHandled,
            Cyberpunk2077SaveAutoImporter().importIfNeeded(request),
        )
    }

    @Test
    fun copyFailureCleansEveryDirectoryCreatedByTheImportAndLeavesNoDecision() {
        val gameDirectory = cyberpunkGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        saveSlot(saves, "ManualSave-0", "first")
        saveSlot(saves, "ManualSave-1", "second")
        val request = request(gameDirectory)
        val failingFileOps = object : Cyberpunk2077SaveAutoImporter.FileOps by
            Cyberpunk2077SaveAutoImporter.RealFileOps {
            override fun copyNoReplaceAndSync(source: File, destination: File): Boolean {
                if (source.parentFile?.name == "ManualSave-1") {
                    throw IllegalStateException("simulated copy failure")
                }
                return Cyberpunk2077SaveAutoImporter.RealFileOps.copyNoReplaceAndSync(
                    source,
                    destination,
                )
            }
        }

        val result = Cyberpunk2077SaveAutoImporter(failingFileOps).importIfNeeded(request)

        assertEquals(
            Cyberpunk2077SaveAutoImporter.Result.Failed("simulated copy failure"),
            result,
        )
        assertFalse(decisionFile(request).exists())
        assertFalse(targetFile(request, "ManualSave-0").exists())
        assertFalse(targetFile(request, "ManualSave-1").exists())
    }

    @Test
    fun unreadableSourceReturnsFailureWithoutCreatingTargetOrDecision() {
        val gameDirectory = cyberpunkGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        val request = request(gameDirectory)
        val unreadableFileOps = object : Cyberpunk2077SaveAutoImporter.FileOps by
            Cyberpunk2077SaveAutoImporter.RealFileOps {
            override fun listFiles(directory: File): Array<File>? =
                if (directory == saves) {
                    null
                } else {
                    Cyberpunk2077SaveAutoImporter.RealFileOps.listFiles(directory)
                }
        }

        val result = Cyberpunk2077SaveAutoImporter(unreadableFileOps).importIfNeeded(request)

        assertTrue(result is Cyberpunk2077SaveAutoImporter.Result.Failed)
        assertFalse(decisionFile(request).exists())
        assertFalse(targetFile(request, "ManualSave-0").exists())
    }

    @Test
    fun malformedOrIncompleteDecisionBlocksImport() {
        listOf(
            "not-json",
            """{"schemaVersion":1,"status":"imported"}""",
        ).forEachIndexed { index, decisionText ->
            val gameDirectory = cyberpunkGame("decision-$index")
            val saves = File(gameDirectory, "saves").apply { mkdirs() }
            saveSlot(saves, "ManualSave-0", "packaged")
            val request = request(gameDirectory)
            decisionFile(request).apply {
                parentFile!!.mkdirs()
                writeText(decisionText)
            }

            val result = Cyberpunk2077SaveAutoImporter().importIfNeeded(request)

            assertEquals(
                Cyberpunk2077SaveAutoImporter.Result.Failed(
                    "Cyberpunk 2077 save import state is invalid",
                ),
                result,
            )
            assertFalse(targetFile(request, "ManualSave-0").exists())
        }
    }

    private fun cyberpunkGame(name: String = "Cyberpunk 2077"): File =
        temporaryFolder.newFolder(name).apply {
            File(this, "bin/x64").mkdirs()
            File(this, "bin/x64/Cyberpunk2077.exe").writeText("game")
        }

    private fun saveSlot(
        saves: File,
        name: String,
        content: String,
        metadataVersion: Int = 9,
    ): File = File(saves, name).apply {
        mkdirs()
        File(this, "sav.dat").writeText(content)
        File(this, "screenshot.png").writeText("screen-$content")
        File(this, "metadata.$metadataVersion.json").writeText("metadata-$content")
    }

    private fun request(gameDirectory: File) = Cyberpunk2077SaveAutoImporter.Request(
        appId = "CUSTOM_GAME_2077",
        executablePath = File(gameDirectory, "bin/x64/Cyberpunk2077.exe").absolutePath,
        gameDirectory = gameDirectory,
        containerRoot = temporaryFolder.newFolder("container-${System.nanoTime()}"),
        localShell = true,
        customGame = true,
        bootToContainer = false,
    )

    private fun targetFile(request: Cyberpunk2077SaveAutoImporter.Request, path: String) = File(
        request.containerRoot,
        ".wine/drive_c/users/xuser/Saved Games/CD Projekt Red/Cyberpunk 2077/$path",
    )

    private fun decisionFile(request: Cyberpunk2077SaveAutoImporter.Request) = File(
        request.containerRoot,
        ".gamefold/cyberpunk2077-save-import-v1/decision.json",
    )
}

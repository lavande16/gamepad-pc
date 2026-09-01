package app.gamenative.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

class Gta5SaveAutoImporterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun supportedRldBuildWithoutSavesReturnsNoSource() {
        val gameDirectory = gtaGame()

        val result = Gta5SaveAutoImporter().importIfNeeded(request(gameDirectory))

        assertEquals(Gta5SaveAutoImporter.Result.NoSource, result)
    }

    @Test
    fun unsupportedLaunchContextsAreNotApplicable() {
        val gameDirectory = gtaGame()
        val supported = request(gameDirectory)
        val wrongIniGame = temporaryFolder.newFolder("wrong-ini").apply {
            File(this, "GTAVLauncher.exe").writeText("launcher")
            File(this, "socialclub.ini").writeText("[Socialclub]\nAppId=999\n")
        }
        val requests = listOf(
            supported.copy(localShell = false),
            supported.copy(customGame = false),
            supported.copy(bootToContainer = true),
            supported.copy(executablePath = File(gameDirectory, "PlayGTAV.exe").absolutePath),
            supported.copy(gameDirectory = wrongIniGame),
        )

        requests.forEach { candidate ->
            assertEquals(
                candidate.toString(),
                Gta5SaveAutoImporter.Result.NotApplicable,
                Gta5SaveAutoImporter().importIfNeeded(candidate),
            )
        }
    }

    @Test
    fun sourceDiscoveryAcceptsOnlyDirectRegularGtaSaveFiles() {
        val gameDirectory = gtaGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        File(saves, "SGTA50015").writeText("save")
        File(saves, "sgta50015.BAK").writeText("backup")
        File(saves, "cfg.dat").writeText("settings")
        File(saves, "SGTA5bad").writeText("invalid")
        File(saves, "nested").mkdirs()
        File(saves, "nested/SGTA50016").writeText("nested")
        Files.createSymbolicLink(
            File(saves, "SGTA50017").toPath(),
            File(saves, "SGTA50015").toPath(),
        )

        val discovered = Gta5SaveAutoImporter().discoverSourceFiles(gameDirectory)

        assertEquals(listOf("SGTA50015", "sgta50015.BAK"), discovered.map(File::getName))
    }

    @Test
    fun freshTargetCopiesAndVerifiesSavesThenWritesOneTimeDecision() {
        val gameDirectory = gtaGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        File(saves, "SGTA50015").writeText("primary-save")
        File(saves, "SGTA50015.bak").writeText("backup-save")
        val request = request(gameDirectory)

        val result = Gta5SaveAutoImporter().importIfNeeded(request)

        assertEquals(Gta5SaveAutoImporter.Result.Imported(2), result)
        assertEquals("primary-save", targetFile(request, "SGTA50015").readText())
        assertEquals("backup-save", targetFile(request, "SGTA50015.bak").readText())
        assertEquals("primary-save", File(saves, "SGTA50015").readText())
        val decision = decisionFile(request).readText()
        assertTrue(decision.contains("\"status\":\"imported\""))
        assertTrue(decision.contains("28cbf8464e714d006d5edce8ee3c998e583a9669ad5c82909305c10bf77ddca3"))
        assertTrue(decision.contains("662ea78411ca4dcd4f2c99eeba08c69fea42297048670fd5bd81cf78048a0e60"))
    }

    @Test
    fun completedDecisionPreventsEveryLaterImport() {
        val gameDirectory = gtaGame()
        File(gameDirectory, "saves").mkdirs()
        val source = File(gameDirectory, "saves/SGTA50015").apply { writeText("first-source") }
        val request = request(gameDirectory)
        assertEquals(
            Gta5SaveAutoImporter.Result.Imported(1),
            Gta5SaveAutoImporter().importIfNeeded(request),
        )
        source.writeText("changed-source")

        val secondResult = Gta5SaveAutoImporter().importIfNeeded(request)

        assertEquals(Gta5SaveAutoImporter.Result.AlreadyHandled, secondResult)
        assertEquals("first-source", targetFile(request, "SGTA50015").readText())
    }

    @Test
    fun existingTargetIsPreservedAndHandledWithoutOverwrite() {
        val gameDirectory = gtaGame()
        File(gameDirectory, "saves").mkdirs()
        File(gameDirectory, "saves/SGTA50015").writeText("packaged-save")
        val request = request(gameDirectory)
        targetFile(request, "SGTA50015").apply {
            parentFile!!.mkdirs()
            writeText("user-progress")
        }

        val firstResult = Gta5SaveAutoImporter().importIfNeeded(request)

        assertEquals(Gta5SaveAutoImporter.Result.PreservedExisting(1), firstResult)
        assertEquals("user-progress", targetFile(request, "SGTA50015").readText())
        assertEquals(
            Gta5SaveAutoImporter.Result.AlreadyHandled,
            Gta5SaveAutoImporter().importIfNeeded(request),
        )
    }

    @Test
    fun targetCreatedDuringCopyIsPreservedWithoutOverwrite() {
        val gameDirectory = gtaGame()
        File(gameDirectory, "saves").mkdirs()
        File(gameDirectory, "saves/SGTA50015").writeText("packaged-save")
        val request = request(gameDirectory)
        val racingFileOps = object : Gta5SaveAutoImporter.FileOps by Gta5SaveAutoImporter.RealFileOps {
            override fun copyNoReplaceAndSync(source: File, destination: File): Boolean {
                destination.writeText("new-user-progress")
                return Gta5SaveAutoImporter.RealFileOps.copyNoReplaceAndSync(source, destination)
            }
        }

        val result = Gta5SaveAutoImporter(racingFileOps).importIfNeeded(request)

        assertEquals(Gta5SaveAutoImporter.Result.PreservedExisting(1), result)
        assertEquals("new-user-progress", targetFile(request, "SGTA50015").readText())
    }

    @Test
    fun copyFailureLeavesNoDecisionOrPartialTarget() {
        val gameDirectory = gtaGame()
        File(gameDirectory, "saves").mkdirs()
        File(gameDirectory, "saves/SGTA50015").writeText("packaged-save")
        val request = request(gameDirectory)
        val failingFileOps = object : Gta5SaveAutoImporter.FileOps by Gta5SaveAutoImporter.RealFileOps {
            override fun copyNoReplaceAndSync(source: File, destination: File): Boolean {
                throw IllegalStateException("simulated copy failure")
            }
        }

        val result = Gta5SaveAutoImporter(failingFileOps).importIfNeeded(request)

        assertEquals(Gta5SaveAutoImporter.Result.Failed("simulated copy failure"), result)
        assertFalse(decisionFile(request).exists())
        assertFalse(targetFile(request, "SGTA50015").exists())
    }

    @Test
    fun obsoleteTransactionFilesAreClearedWithoutTouchingTargetSaves() {
        val gameDirectory = gtaGame()
        File(gameDirectory, "saves").mkdirs()
        File(gameDirectory, "saves/SGTA50015").writeText("packaged-save")
        val request = request(gameDirectory)
        val oldStateStaging = File(
            request.containerRoot,
            ".gamefold/gta5-save-import-v1/staging/SGTA50015",
        ).apply { parentFile!!.mkdirs(); writeText("old-staging") }
        val oldTargetStaging = File(
            request.containerRoot,
            ".wine/drive_c/ProgramData/Socialclub/RLD!/271590/.gamefold-import-v1-staging/SGTA50015",
        ).apply { parentFile!!.mkdirs(); writeText("old-staging") }
        val oldJournal = File(
            request.containerRoot,
            ".gamefold/gta5-save-import-v1/transaction.json",
        ).apply { parentFile!!.mkdirs(); writeText("old-journal") }

        val result = Gta5SaveAutoImporter().importIfNeeded(request)

        assertEquals(Gta5SaveAutoImporter.Result.Imported(1), result)
        assertFalse(oldStateStaging.parentFile!!.exists())
        assertFalse(oldTargetStaging.parentFile!!.exists())
        assertFalse(oldJournal.exists())
        assertEquals("packaged-save", targetFile(request, "SGTA50015").readText())
    }

    @Test
    fun existingButUnreadableSourceDirectoryBlocksLaunch() {
        val gameDirectory = gtaGame()
        File(gameDirectory, "saves").mkdirs()
        val request = request(gameDirectory)
        val unreadableFileOps = object : Gta5SaveAutoImporter.FileOps by Gta5SaveAutoImporter.RealFileOps {
            override fun listFiles(directory: File): Array<File>? {
                return if (directory == File(gameDirectory, "saves")) {
                    null
                } else {
                    Gta5SaveAutoImporter.RealFileOps.listFiles(directory)
                }
            }
        }

        val result = Gta5SaveAutoImporter(unreadableFileOps).importIfNeeded(request)

        assertTrue(result is Gta5SaveAutoImporter.Result.Failed)
        assertFalse(decisionFile(request).exists())
    }

    @Test
    fun caseCollidingSourceNamesFailBeforeCopy() {
        val gameDirectory = gtaGame()
        val saves = File(gameDirectory, "saves").apply { mkdirs() }
        File(saves, "SGTA50015").writeText("primary")
        File(saves, "sgta50015").writeText("case-collision")
        val request = request(gameDirectory)
        val caseCollidingFileOps = object : Gta5SaveAutoImporter.FileOps by Gta5SaveAutoImporter.RealFileOps {
            override fun listFiles(directory: File): Array<File>? {
                return if (directory == saves) {
                    arrayOf(File(saves, "SGTA50015"), File(saves, "sgta50015"))
                } else {
                    Gta5SaveAutoImporter.RealFileOps.listFiles(directory)
                }
            }
        }

        val result = Gta5SaveAutoImporter(caseCollidingFileOps).importIfNeeded(request)

        assertTrue(result is Gta5SaveAutoImporter.Result.Failed)
        assertFalse(decisionFile(request).exists())
        assertFalse(targetFile(request, "SGTA50015").exists())
    }

    @Test
    fun malformedOrIncompleteDecisionBlocksImport() {
        listOf(
            "not-json",
            """{"schemaVersion":1,"status":"imported"}""",
        ).forEachIndexed { index, decisionText ->
            val gameDirectory = gtaGame("decision-$index")
            File(gameDirectory, "saves").mkdirs()
            File(gameDirectory, "saves/SGTA50015").writeText("packaged-save")
            val request = request(gameDirectory)
            decisionFile(request).apply {
                parentFile!!.mkdirs()
                writeText(decisionText)
            }

            val result = Gta5SaveAutoImporter().importIfNeeded(request)

            assertEquals(
                Gta5SaveAutoImporter.Result.Failed("GTA5 save import state is invalid"),
                result,
            )
            assertFalse(targetFile(request, "SGTA50015").exists())
        }
    }

    private fun gtaGame(name: String = "Grand Theft Auto V"): File = temporaryFolder.newFolder(name).apply {
        File(this, "GTAVLauncher.exe").writeText("launcher")
        File(this, "socialclub.ini").writeText(
            """
            [Settings]
            UserName=RLD!
            [Socialclub]
            AppId=271590
            """.trimIndent(),
        )
    }

    private fun request(gameDirectory: File) = Gta5SaveAutoImporter.Request(
        appId = "CUSTOM_GAME_1960474552",
        executablePath = File(gameDirectory, "GTAVLauncher.exe").absolutePath,
        gameDirectory = gameDirectory,
        containerRoot = temporaryFolder.newFolder("container-${System.nanoTime()}"),
        localShell = true,
        customGame = true,
        bootToContainer = false,
    )

    private fun targetFile(request: Gta5SaveAutoImporter.Request, name: String) = File(
        request.containerRoot,
        ".wine/drive_c/ProgramData/Socialclub/RLD!/271590/$name",
    )

    private fun decisionFile(request: Gta5SaveAutoImporter.Request) = File(
        request.containerRoot,
        ".gamefold/gta5-save-import-v1/decision.json",
    )
}

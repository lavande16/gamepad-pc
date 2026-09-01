# GameFold PC GTA5 Save Auto-Import Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a GTA5-only, first-launch save importer that copies validated `SGTA5*` files from the game's public `saves` folder into its private Wine Social Club directory exactly once without overwriting existing progress.

**Architecture:** A pure Kotlin `Gta5SaveAutoImporter` owns eligibility, source filtering, transactional file copy, container-scoped decisions, and crash recovery using only `java.io.File`. `preLaunchLocalShell` resolves the existing game and container paths, calls the importer after runtime setup and before container activation, then maps the sealed result to continue, notify, or block launch.

**Tech Stack:** Kotlin, Java file I/O, `org.json`, JUnit 4, Robolectric where Android types are required, Android Gradle Plugin legacy release tasks, ADB.

**Spec:** `docs/superpowers/specs/2026-08-19-gamefold-pc-gta5-save-auto-import-design.md`

## Global Constraints

- Start from the immutable `gamefold-pc-v1.0.3-baseline` tag on branch `codex/gamefold-pc-v1.0.4-gta5-save-import`.
- Keep package `com.gamefold.pc`; use candidate identity `1.0.4 / 10168`.
- Only GameFold PC local-shell custom-game GTA5 normal launches are eligible.
- Accept direct regular non-symbolic-link files matching `(?i)^SGTA5[0-9]+(?:\.bak)?$`.
- Use the fixed destination `.wine/drive_c/ProgramData/Socialclub/RLD!/271590`; never interpolate INI values into a path.
- Never modify source files and never overwrite a pre-existing target save.
- Never modify the GTA5 executable path or use Wine, `start.exe`, BAT files, or the container desktop for copying.
- Preserve V1.0.3 baselines, artifacts, tags, current app data, permissions, and the user's live container.

---

### Task 1: GTA5 eligibility and source discovery

**Files:**
- Create: `app/src/main/java/app/gamenative/product/Gta5SaveAutoImporter.kt`
- Create: `app/src/test/java/app/gamenative/product/Gta5SaveAutoImporterTest.kt`
- Modify: `app/src/main/java/app/gamenative/product/GameFoldPcGameIdentity.kt`
- Modify: `app/src/test/java/app/gamenative/product/GameFoldPcGameIdentityTest.kt`

**Interfaces:**
- Consumes: `GameFoldPcGameIdentity.isGtaVExecutable(path: String): Boolean`.
- Produces: `Gta5SaveAutoImporter.importIfNeeded(request: Request): Result`, `Request`, and the sealed `Result` variants `NotApplicable`, `NoSource`, `AlreadyHandled`, `Imported`, `PreservedExisting`, and `Failed`.

- [ ] **Step 1: Write failing identity and discovery tests**

```kotlin
@Test fun supportedRldBuildWithoutSavesReturnsNoSource() {
    val game = gtaGame(socialClub = "[Settings]\nUserName=RLD!\n[Socialclub]\nAppId=271590\n")
    val result = Gta5SaveAutoImporter().importIfNeeded(request(game))
    assertEquals(Gta5SaveAutoImporter.Result.NoSource, result)
}

@Test fun otherExecutableIniAndOpenContainerAreNotApplicable() {
    val importer = Gta5SaveAutoImporter()
    assertEquals(Result.NotApplicable, importer.importIfNeeded(request(gtaGame(), executable = "PlayGTAV.exe")))
    assertEquals(Result.NotApplicable, importer.importIfNeeded(request(gtaGame(appId = "999"))))
    assertEquals(Result.NotApplicable, importer.importIfNeeded(request(gtaGame(), bootToContainer = true)))
}

@Test fun discoversOnlyDirectRegularGtaSaveFiles() {
    val game = gtaGameWithSaves("SGTA50015", "SGTA50015.bak", "cfg.dat", "../ignored")
    File(game, "saves/nested/SGTA50016").apply { parentFile.mkdirs(); writeText("nested") }
    Files.createSymbolicLink(File(game, "saves/SGTA50017").toPath(), File(game, "saves/SGTA50015").toPath())
    assertEquals(listOf("SGTA50015", "SGTA50015.bak"), Gta5SaveAutoImporter().discoverSourceFiles(game).map(File::getName))
}
```

- [ ] **Step 2: Run the focused tests and verify RED**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.product.Gta5SaveAutoImporterTest --offline --console=plain
```

Expected: compilation fails because `Gta5SaveAutoImporter` does not exist.

- [ ] **Step 3: Implement the minimal pure API and eligibility parser**

```kotlin
class Gta5SaveAutoImporter internal constructor(
    private val fileOps: FileOps = RealFileOps,
) {
    data class Request(
        val appId: String,
        val executablePath: String,
        val gameDirectory: File,
        val containerRoot: File,
        val localShell: Boolean,
        val customGame: Boolean,
        val bootToContainer: Boolean,
    )

    sealed interface Result {
        data object NotApplicable : Result
        data object NoSource : Result
        data object AlreadyHandled : Result
        data class Imported(val fileCount: Int) : Result
        data class PreservedExisting(val fileCount: Int) : Result
        data class Failed(val message: String) : Result
    }

    fun importIfNeeded(request: Request): Result
    internal fun discoverSourceFiles(gameDirectory: File): List<File>
}
```

Parse INI section/key pairs case-insensitively, compare exact trimmed values `RLD!` and `271590`, and return `NoSource` only after the GTA5 build is applicable.

- [ ] **Step 4: Run identity and discovery tests and verify GREEN**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests 'app.gamenative.product.GameFoldPcGameIdentityTest' --tests 'app.gamenative.product.Gta5SaveAutoImporterTest' --offline --console=plain
```

Expected: all focused tests pass.

- [ ] **Step 5: Commit the eligibility slice**

```bash
git add app/src/main/java/app/gamenative/product/GameFoldPcGameIdentity.kt app/src/main/java/app/gamenative/product/Gta5SaveAutoImporter.kt app/src/test/java/app/gamenative/product/GameFoldPcGameIdentityTest.kt app/src/test/java/app/gamenative/product/Gta5SaveAutoImporterTest.kt
git commit -m "feat: identify GTA5 save import sources"
```

### Task 2: One-time verified import and upgrade protection

**Files:**
- Modify: `app/src/main/java/app/gamenative/product/Gta5SaveAutoImporter.kt`
- Modify: `app/src/test/java/app/gamenative/product/Gta5SaveAutoImporterTest.kt`

**Interfaces:**
- Consumes: `Request`, discovered source files, fixed target relative path.
- Produces: atomic `decision.json` with `imported` or `skipped_existing_target`, file sizes, and SHA-256 hashes.

- [ ] **Step 1: Write failing import, idempotence, and preservation tests**

```kotlin
@Test fun freshTargetImportsAndSecondRunDoesNotTouchFiles() {
    val request = request(gtaGameWithSaves("SGTA50015", "SGTA50015.bak"))
    val importer = Gta5SaveAutoImporter()
    assertEquals(Result.Imported(2), importer.importIfNeeded(request))
    val target = targetFile(request, "SGTA50015")
    val firstModified = target.lastModified()
    assertEquals(Result.AlreadyHandled, importer.importIfNeeded(request))
    assertEquals(firstModified, target.lastModified())
}

@Test fun existingTargetIsNeverOverwritten() {
    val request = request(gtaGameWithSaves("SGTA50015" to "packaged"))
    targetFile(request, "SGTA50015").apply { parentFile.mkdirs(); writeText("progress") }
    val importer = Gta5SaveAutoImporter()
    assertEquals(Result.PreservedExisting(1), importer.importIfNeeded(request))
    assertEquals("progress", targetFile(request, "SGTA50015").readText())
    assertEquals(Result.AlreadyHandled, importer.importIfNeeded(request))
}

@Test fun deletingContainerStateAllowsAReplacementContainerToImport() {
    val first = request(gtaGameWithSaves("SGTA50015"))
    assertEquals(Result.Imported(1), Gta5SaveAutoImporter().importIfNeeded(first))
    first.containerRoot.deleteRecursively()
    first.containerRoot.mkdirs()
    assertEquals(Result.Imported(1), Gta5SaveAutoImporter().importIfNeeded(first))
}
```

- [ ] **Step 2: Run tests and verify RED**

Run the Task 1 focused command. Expected: `NoSource` or missing transaction behavior instead of `Imported` and `PreservedExisting`.

- [ ] **Step 3: Implement staging, SHA-256 verification, and atomic decision writing**

Use these fixed locations:

```kotlin
private const val TARGET_RELATIVE = ".wine/drive_c/ProgramData/Socialclub/RLD!/271590"
private const val STATE_RELATIVE = ".gamefold/gta5-save-import-v1"
private const val DECISION_FILE = "decision.json"
private const val JOURNAL_FILE = "transaction.json"
private const val STAGING_DIR = "staging"
```

Write JSON to `decision.json.tmp`, flush and sync the stream file descriptor, then rename to `decision.json`. Check for accepted target saves before staging and record `skipped_existing_target` without copying.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run the Task 1 focused command. Expected: all tests pass and source contents remain unchanged.

- [ ] **Step 5: Commit the one-time import slice**

```bash
git add app/src/main/java/app/gamenative/product/Gta5SaveAutoImporter.kt app/src/test/java/app/gamenative/product/Gta5SaveAutoImporterTest.kt
git commit -m "feat: import GTA5 saves once without overwrite"
```

### Task 3: Crash recovery and deterministic failure handling

**Files:**
- Modify: `app/src/main/java/app/gamenative/product/Gta5SaveAutoImporter.kt`
- Modify: `app/src/test/java/app/gamenative/product/Gta5SaveAutoImporterTest.kt`

**Interfaces:**
- Consumes: verified manifest stored in `transaction.json`.
- Produces: retry-safe recovery and a package-internal `FileOps` seam used to simulate copy/verification failures.

- [ ] **Step 1: Write failing recovery and failure tests**

```kotlin
@Test fun copyFailureLeavesNoDecisionAndRetriesSuccessfully() {
    val request = request(gtaGameWithSaves("SGTA50015"))
    val failed = Gta5SaveAutoImporter(FailingOnceFileOps()).importIfNeeded(request)
    assertTrue(failed is Result.Failed)
    assertFalse(decisionFile(request).exists())
    assertEquals(Result.Imported(1), Gta5SaveAutoImporter().importIfNeeded(request))
}

@Test fun preparedJournalRemovesOnlyMatchingPartialOutputThenRetries() {
    val request = preparedTransaction(partialTargetContent = "packaged", manifestContent = "packaged")
    assertEquals(Result.Imported(1), Gta5SaveAutoImporter().importIfNeeded(request))
}

@Test fun preparedJournalPreservesDifferingUserOutput() {
    val request = preparedTransaction(partialTargetContent = "new-progress", manifestContent = "packaged")
    assertEquals(Result.PreservedExisting(1), Gta5SaveAutoImporter().importIfNeeded(request))
    assertEquals("new-progress", targetFile(request, "SGTA50015").readText())
}

@Test fun malformedDecisionAndJournalNeverGrantOverwritePermission() {
    val decisionRequest = request(gtaGameWithSaves("SGTA50015"))
    decisionFile(decisionRequest).apply { parentFile.mkdirs(); writeText("not-json") }
    assertTrue(Gta5SaveAutoImporter().importIfNeeded(decisionRequest) is Result.Failed)
    val journalRequest = request(gtaGameWithSaves("SGTA50016"))
    journalFile(journalRequest).apply { parentFile.mkdirs(); writeText("not-json") }
    assertTrue(Gta5SaveAutoImporter().importIfNeeded(journalRequest) is Result.Failed)
}
```

- [ ] **Step 2: Run focused tests and verify RED**

Run the Task 1 focused command. Expected: missing recovery and injectable file-operation behavior.

- [ ] **Step 3: Implement prepared-journal recovery**

```kotlin
internal interface FileOps {
    fun copy(source: File, destination: File)
    fun move(source: File, destination: File): Boolean
    fun sha256(file: File): String
}
```

Before existing-target protection, load a valid prepared journal. Delete only destination files whose basename and SHA-256 exactly match the journal, clear staging, then retry. If a recorded destination differs, preserve it and write `skipped_existing_target`. Malformed decisions or journals return `Failed` and never overwrite.

- [ ] **Step 4: Run focused tests and verify GREEN**

Run the Task 1 focused command. Expected: all recovery and failure tests pass.

- [ ] **Step 5: Commit crash recovery**

```bash
git add app/src/main/java/app/gamenative/product/Gta5SaveAutoImporter.kt app/src/test/java/app/gamenative/product/Gta5SaveAutoImporterTest.kt
git commit -m "feat: recover interrupted GTA5 save imports"
```

### Task 4: Integrate the importer into the local-shell launch transaction

**Files:**
- Create: `app/src/main/java/app/gamenative/product/Gta5SaveImportLaunchPolicy.kt`
- Create: `app/src/test/java/app/gamenative/product/Gta5SaveImportLaunchPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/PluviaMain.kt:1632-1663`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh-rCN/strings.xml`

**Interfaces:**
- Consumes: `Gta5SaveAutoImporter.Result`.
- Produces: `Gta5SaveImportLaunchPolicy.Decision(continueLaunch, snackbarMessageKey, blockingError)` and actual `preLaunchLocalShell` wiring.

- [ ] **Step 1: Write failing launch-policy tests**

```kotlin
@Test fun failureBlocksLaunch() {
    val decision = Gta5SaveImportLaunchPolicy.decide(Result.Failed("disk full"))
    assertFalse(decision.continueLaunch)
    assertEquals("disk full", decision.blockingError)
}

@Test fun importAndPreservationContinueWithOneNotification() {
    assertEquals(Notification.IMPORTED, decide(Result.Imported(2)).notification)
    assertEquals(Notification.PRESERVED_EXISTING, decide(Result.PreservedExisting(1)).notification)
    assertTrue(decide(Result.AlreadyHandled).continueLaunch)
    assertNull(decide(Result.AlreadyHandled).notification)
}
```

- [ ] **Step 2: Run launch-policy tests and verify RED**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.product.Gta5SaveImportLaunchPolicyTest --offline --console=plain
```

Expected: compilation fails because the policy does not exist.

- [ ] **Step 3: Implement the pure policy and production wiring**

In `preLaunchLocalShell`, after deferred runtime dependencies succeed and before `containerManager.activateContainer(container)`:

```kotlin
val gameDirectory = CustomGameScanner.getFolderPathFromAppId(appId)?.let(::File)
val importResult = if (gameDirectory == null) {
    Gta5SaveAutoImporter.Result.NotApplicable
} else {
    Gta5SaveAutoImporter().importIfNeeded(
        Gta5SaveAutoImporter.Request(
            appId = appId,
            executablePath = CustomGameScanner.getLaunchExecutable(container),
            gameDirectory = gameDirectory,
            containerRoot = container.rootDir,
            localShell = BuildConfig.GAMEFOLD_PC_LOCAL_SHELL,
            customGame = true,
            bootToContainer = bootToContainer,
        ),
    )
}
```

Map `Imported` and `PreservedExisting` to Chinese resource-backed snackbar messages. Map `Failed` to a `SYNC_FAIL` dialog titled “GTA5 存档导入失败”, hide loading, and return before activation. All other results preserve the existing launch path.

- [ ] **Step 4: Run policy, importer, and protected-runtime tests and verify GREEN**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests 'app.gamenative.product.Gta5Save*' --tests app.gamenative.product.GameFoldPcProtectedRuntimeContractTest --offline --console=plain
```

Expected: all focused tests pass.

- [ ] **Step 5: Commit launch integration**

```bash
git add app/src/main/java/app/gamenative/product/Gta5SaveImportLaunchPolicy.kt app/src/main/java/app/gamenative/ui/PluviaMain.kt app/src/main/res/values/strings.xml app/src/main/res/values-zh-rCN/strings.xml app/src/test/java/app/gamenative/product/Gta5SaveImportLaunchPolicyTest.kt
git commit -m "feat: import GTA5 saves before first launch"
```

### Task 5: Candidate identity, regression, build, install, and device acceptance

**Files:**
- Modify: `app/build.gradle.kts:29-33`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt:98-102`
- Create: `releases/candidates/V1.0.4/GameFold-PC-V1.0.4-GTA5-SAVE-IMPORT-10168.apk`
- Create: `releases/candidates/V1.0.4/README.md`

**Interfaces:**
- Consumes: completed feature and frozen V1.0.3 signing/build contract.
- Produces: an installable, separately named `com.gamefold.pc` candidate with verified identity and hashes.

- [ ] **Step 1: Change the version contract test first**

```kotlin
@Test
fun gameFoldPcOnePointZeroFourCandidate_usesVersionCode10168() {
    assertEquals("1.0.4", BuildConfig.VERSION_NAME)
    assertEquals(10168, BuildConfig.VERSION_CODE)
}
```

- [ ] **Step 2: Run the version test and verify RED**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.ui.screen.settings.SettingsGeometryTest --offline --console=plain
```

Expected: expected `1.0.4 / 10168`, actual `1.0.3 / 10167`.

- [ ] **Step 3: Set default candidate identity and verify GREEN**

Set `gameFoldPcVersionName` default to `1.0.4` and `gameFoldPcVersionCode` default to `10168`, then rerun the Step 2 command.

- [ ] **Step 4: Run focused and full local-shell regressions**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --offline --console=plain
```

Expected: all filtered GameFold PC legacy tests pass with zero failures.

- [ ] **Step 5: Build the signed legacy release candidate**

Run:

```bash
./gradlew :app:assembleLegacyRelease --offline --console=plain
```

Expected: `app/build/outputs/apk/legacy/release/app-legacy-release.apk` exists and is signed with the established certificate.

- [ ] **Step 6: Archive and hash the candidate without changing any baseline**

Copy the APK to the exact candidate path, record size and SHA-256 in `releases/candidates/V1.0.4/README.md`, and verify package `com.gamefold.pc`, version `1.0.4 / 10168`, and certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`.

- [ ] **Step 7: Record device state and source hashes before installation**

Run read-only ADB checks for device model, installed version, first/install times, `MANAGE_EXTERNAL_STORAGE`, and SHA-256 of `/storage/emulated/0/GameFoldPC/game/Grand Theft Auto V/saves/SGTA50015*`.

- [ ] **Step 8: Install the candidate in place and verify installed identity**

Use the approved vivo installer helper with the candidate APK. Pull the installed `base.apk` and confirm its SHA-256 exactly matches the archived candidate; confirm package, version, install time, preserved first-install time, and storage permission.

- [ ] **Step 9: Verify live-container preservation and one-time behavior**

Launch GTA5 once through GameFold PC. Expect the preservation notification because the current target already has saves. Confirm GTA5 still displays the existing save, exit, launch again, and confirm no copy or notification repeats. Recheck the public source hashes and confirm they did not change.

- [ ] **Step 10: Commit candidate metadata**

```bash
git add app/build.gradle.kts app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt releases/candidates/V1.0.4
git commit -m "release: archive GameFold PC 1.0.4 GTA5 save import candidate"
```

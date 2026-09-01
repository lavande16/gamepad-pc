# GameFold PC 0.6.6 × NS 2.9 R2 Autonomous Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a separately named, in-place installable GameFold PC release candidate that preserves the frozen 0.6.6 PC runtime while adopting the complete GameFold NS 2.9 R2 official foreground UI, controller, startup assembly, settings presentation, orientation behavior, runtime loading/FPS presentation, unsupported-window protection, and vivo rear-touch experience.

**Architecture:** Keep the GameFold PC 0.6.6 repository as the only application/runtime base. Add a hybrid GameFold product shell: exact NS Android View drawing/asset behavior where fidelity matters, Compose hosts/adapters where PC already owns navigation and state, and narrow ports between the shell and scanner, Container/DataStore, XServer, WinHandler/evshim, metrics, and rear display. Common ImageFS/Proton/shared guest content is provisioned after processor validation and storage authorization but before home; concrete game containers remain lazy and isolated.

**Tech Stack:** Android/Kotlin 2.1.21, Java, Jetpack Compose BOM 2025.01.01, Android View interop, Hilt, StateFlow, Room/DataStore, Robolectric/JUnit4, XServer/Winlator, Bionic/Proton ARM64EC, WinHandler/evshim, Gradle 8.12.1, ADB, vivo installation helper.

## Global Constraints

- Work only in `/Users/11085429/Documents/Game Fold/gamefold-pc-ns-ui-integration` on `codex/gamefold-pc-ns-ui-integration`.
- The PC input is the frozen `gamefold-pc-v0.6.6-baseline` tag at source commit `c0320448b6256ada0d6e175385d4c8ca43df64d9`, frozen tag commit `139d0d460038c9175f1ef27c4ade5e2189c01729`, APK SHA-256 `3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad`, version `0.6.6` / `10057`; never import moving RC outputs or reset the 0.6.6 tag.
- The authoritative PC package remains `com.gamefold.pc.v04localshell`; Legacy minSdk/targetSdk remain `26/28`; the PC signer remains `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`.
- Never overwrite either frozen APK, reset a baseline pointer, clear app data, uninstall the package, reset file authorization, delete a game, delete `/storage/emulated/0/GameFoldPC`, or delete an existing container.
- Preserve the `CUSTOM_GAME_` plus numeric-ID identity format, folder-scoped executable resolution, canonical `A:` mapping, Container persistence, 1067x600/GTA synchronization, XServer/Surface/AHB, Bionic/Proton, public manifest aliases, WinHandler/evshim, LSFG and teardown behavior. Retain the 0.6.6 defaults: LSFG flow `0.50`, FSR Fit screen effects, Steam Input off, XInput+DInput on, Preferred API BOTH, Standard DInput mapper, quick menu off, exit warning on, GameFold launch branding, fixed launch tips hidden, hidden icon-style entry, one-time `1280x720`/empty global-size migration to `1067x600`, and global-template inheritance for new games.
- GameFold NS 2.9 R2 APK SHA-256 `156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292` is the UI truth. The current NS source is migration reference only.
- Startup order is processor gate → storage permission → public-payload preflight → conditional full SHA validation → transactional shared runtime provisioning → home. Full hashes run on first provision, manifest/receipt metadata change, incomplete private runtime, or explicit force; an exactly matching receipt uses only a cheap path/size/metadata check.
- Scheme A uses existing `/storage/emulated/0/GameFoldPC`; it does not add a seed-copy or network-download mechanism.
- Pre-home provisioning installs ImageFS, Proton, shared redirect/guest libraries and common `extras` through a full-runtime sibling staging directory, validation journal, backup and same-filesystem atomic switch; a failed/ENOSPC/interrupted attempt preserves the last valid runtime and can roll back on next launch. Per-game container patterns/FEXCore/PulseAudio/input/graphics/wincomponents stay available in the public payload and are extracted by the established container path when needed. Existing per-game launch validation remains defense in depth.
- Every task follows red → green → affected regression → commit. Do not commit a known-red checkpoint.
- The frozen 0.6.6 supported Legacy contract is 227 tests across 39 classes, 0 failures/errors/skips. Acceptance freezes a stable XML inventory and exact historical failure/error case set from the 0.6.6 tag; suite/test-case set changes and any new failure/error fail the gate. Counts alone never offset a new failure.
- A release candidate must use a versionCode greater than `10057`, every archived candidate, and the installed package. Different APK bytes must never reuse a versionCode.
- The autonomous terminal state is `RC_READY_FOR_MANUAL_ACCEPTANCE`, not baseline promotion.
- If a physical fold, rear-display observation, tactile judgment, or security confirmation cannot be automated safely, continue all other work and record that cell as `HOLD_FOR_DEVICE` rather than claiming it passed.

## Autonomous Execution Protocol

1. Before each task, confirm `git status --short` contains only the task's expected edits.
2. Run the task's focused red test and retain its failure text in the task notes; never manufacture a red result by breaking unrelated code.
3. Implement the minimum scoped change with `apply_patch`; use mechanical copy only for the enumerated binary NS assets.
4. Run focused tests, affected contract tests, `git diff --check`, and the task-specific artifact/resource guard.
5. Review the diff against the task scope. Revert only the current task if its acceptance gate fails; do not reset prior green commits.
6. Commit one behavior per task using the exact commit subject shown below.
7. At phase boundaries, run the cumulative local-shell suite and compare suite names/counts to the previous boundary.
8. Retry an ADB disconnect or Gradle daemon/environment failure once. Do not retry assertions, hashes, signatures, data loss, wrong executable, black Surface, stuck input, or native crashes as a matter of chance.
9. The 0.6.6 baseline is already terminal and accepted; Task 15 now records the frozen tag/artifact provenance and imports no 0.6.6 code wholesale. Any later 0.6.6 maintenance is a separate, explicitly verified source/artifact pair.
10. Stop immediately if a frozen artifact changes, package/signature/data migration is required, user data disappears, a runtime regression cannot be isolated to the current task, or a visual dispute cannot be resolved from the frozen NS APK/evidence.

## Planned File Structure

```text
app/src/main/java/app/gamenative/gamefold/
  contract/
    GameFoldGame.kt
    GameCatalogPort.kt
    RuntimeSessionPort.kt
    RuntimeInputPort.kt
    PerformanceStatsPort.kt
    GameFoldSettingsRepository.kt
    PackagePreflightPort.kt
    RearDisplayPort.kt
  adapter/
    PcGameCatalogAdapter.kt
    PcRuntimeSessionAdapter.kt
    PcRuntimeInputAdapter.kt
    PcPerformanceStatsAdapter.kt
    PcSettingsAdapter.kt
  startup/
    GameFoldPcRuntimeProvisioning.kt
    GameFoldPcRuntimeProvisionStore.kt
    AndroidGameFoldPcRuntimeGateway.kt
    GameFoldPcStartupViewModel.kt
  ui/
    GameFoldRoot.kt
    GameFoldDesignTokens.kt
    shell/GameFoldShellLayout.kt
    shell/GameFoldViewportLayout.kt
    shell/GameFoldShellHost.kt
    shell/GameFoldControllerInput.kt
    library/GameFoldLibraryScreen.kt
    library/GameFoldCartridgeCard.kt
    runtime/GameFoldLoadingOverlay.kt
    runtime/GameFoldPixelNumberView.kt
    settings/GameFoldAdvancedSettingsScreen.kt
    settings/GameFoldGameSettingsScreen.kt
  display/
    GameFoldOrientationController.kt
    GameFoldWindowSupportPolicy.kt
    VivoRearDisplayController.kt
    RearTouchActivity.kt
    RearTouchInputRouter.kt

app/src/test/java/app/gamenative/gamefold/...
app/src/androidTest/java/app/gamenative/gamefold/...
packaging/gamefold-ns-ui-assets.sha256
packaging/test_gamefold_integration_baselines.sh
packaging/test_gamefold_ns_ui_assets.sh
docs/gamefold/NS_2_9_R2_UI_EVIDENCE.md
docs/gamefold/PC_NS_INTEGRATION_ACCEPTANCE.md
docs/gamefold/V066_IMPORT_LEDGER.md
```

Names in this map are contracts. If existing source proves a class must live under an established package, record the single rename in the plan checklist before its first consumer is committed and update all later tasks in the same commit.

---

### Task 1: Freeze integration evidence and executable baseline guards

**Files:**

- Create: `docs/gamefold/NS_2_9_R2_UI_EVIDENCE.md`
- Create: `docs/gamefold/PC_NS_INTEGRATION_ACCEPTANCE.md`
- Create: `packaging/gamefold-ns-ui-assets.sha256`
- Create: `packaging/test_gamefold_integration_baselines.sh`
- Create: `app/src/test/java/app/gamenative/product/GameFoldPcProtectedRuntimeContractTest.kt`

**Interfaces:**

- Produces: an executable source/artifact guard and a human-readable acceptance ledger used by every later task.
- Consumes: frozen PC 0.6.6 tag/APK metadata and frozen NS 2.9 R2 APK metadata.

- [ ] **Step 1: Write the baseline guard before copying any UI assets.**

```bash
#!/usr/bin/env bash
set -euo pipefail

pc_tag_commit="139d0d460038c9175f1ef27c4ade5e2189c01729"
pc_apk="/Users/11085429/Documents/Game Fold/gamefold-pc-v065-fullscreen-release/releases/baselines/V0.6.6/GameFold-PC-V0.6.6-Baseline-10057.apk"
ns_apk="/Users/11085429/Documents/Game Fold/baselines/GameFold-2.9-Mainline-Baseline-R2/GameFold-2.9-Baseline-R2.apk"

test "$(git rev-parse gamefold-pc-v0.6.6-baseline^{commit})" = "$pc_tag_commit"
test "$(shasum -a 256 "$pc_apk" | awk '{print $1}')" = "3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad"
test "$(shasum -a 256 "$ns_apk" | awk '{print $1}')" = "156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292"
```

- [ ] **Step 2: Add a protected-runtime behavior test.** Construct real default/container inputs and assert the resulting `1067x600` setting, exact GTA-only classification, public-root resolution, and `CUSTOM_GAME_` plus numeric-ID parsing. Package injection belongs to the executable APK guard, not this JVM test. Do not assert private source text or bare constants.
- [ ] **Step 3: Run the guard and the existing 13 direct tests.**

```bash
bash packaging/test_gamefold_integration_baselines.sh
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:testLegacyReleaseUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests app.gamenative.product.GameFoldPcProtectedRuntimeContractTest \
  --tests app.gamenative.product.GameFoldPcDefaultsTest \
  --tests app.gamenative.product.GameFoldPcPerGameDefaultsTest \
  --tests app.gamenative.utils.GtaFullscreenConfigTest \
  --offline --no-daemon --console=plain
```

Expected: all new tests and the 13 pre-existing tests pass; the guard prints both approved hashes.

- [ ] **Step 4: Populate the UI evidence document.** Include exact APK identity, the source-traceability warning, screenshots/evidence paths, all required screens/states, the 2480×2200 shell reference, cartridge 343:392 ratio, 16:9 aperture, controller semantics, pixel palette, orientation, unsupported state and vivo rear-screen behavior.
- [ ] **Step 5: Initialize the acceptance ledger.** Use status values `NOT_RUN`, `PASS`, `FAIL`, `HOLD_FOR_DEVICE`, and `NOT_APPLICABLE`; prefill only the already-run 13-test baseline as `PASS` with command and exit code.
- [ ] **Step 6: Commit.**

```bash
git add docs/gamefold packaging app/src/test/java/app/gamenative/product/GameFoldPcProtectedRuntimeContractTest.kt
git commit -m "test: lock GameFold integration baselines"
```

---

### Task 2: Add deterministic public-payload validation and manifest fingerprints

**Files:**

- Modify: `app/src/main/java/app/gamenative/utils/GameFoldPcResourceRoot.kt`
- Create: `app/src/test/java/app/gamenative/utils/GameFoldPcResourceRootTest.kt`

**Interfaces:**

- Produces:

```kotlin
data class ManifestValidationResult(
    val manifestPresent: Boolean,
    val valid: Boolean,
    val missing: List<String>,
    val invalid: List<String>,
    val manifestSha256: String?,
)

internal fun validateManifestAt(root: File, verifyHashes: Boolean): ManifestValidationResult
```

- Preserves: `GameFoldPcResourceRoot.validateManifest(verifyHashes)` as the public production entry.

- [ ] **Step 1: Write temp-directory tests** for missing manifest, missing `required`, empty/traversal paths, missing files, wrong size, wrong SHA-256, `verifyHashes=false`, stable fingerprint, and changed-manifest fingerprint.

```kotlin
@Test fun manifestFingerprintChangesWhenManifestBytesChange() {
    val first = validateManifestAt(writePayload(version = 1), verifyHashes = false)
    val second = validateManifestAt(writePayload(version = 2), verifyHashes = false)
    assertNotEquals(first.manifestSha256, second.manifestSha256)
}
```

- [ ] **Step 2: Run the new test and verify it fails** because `validateManifestAt` and `manifestSha256` do not exist.
- [ ] **Step 3: Extract root injection without changing production resolution.** Reject traversal before joining paths, fingerprint the raw manifest bytes, sort diagnostics deterministically, and preserve current Chinese `describe()` output.
- [ ] **Step 4: Run focused and existing resource/downloader tests.**

```bash
./gradlew :app:testLegacyDebugUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests app.gamenative.utils.GameFoldPcResourceRootTest \
  --tests app.gamenative.utils.downloader.ContainerFilesDownloaderTest \
  --tests com.winlator.xenvironment.ImageFsInstallerTest \
  --offline --no-daemon --console=plain
```

- [ ] **Step 5: Commit.**

```bash
git add app/src/main/java/app/gamenative/utils/GameFoldPcResourceRoot.kt app/src/test/java/app/gamenative/utils/GameFoldPcResourceRootTest.kt
git commit -m "feat: fingerprint GameFold PC runtime payload"
```

---

### Task 3: Build the idempotent runtime-provisioning coordinator and atomic receipt

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/startup/GameFoldPcRuntimeProvisioning.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/startup/GameFoldPcRuntimeProvisionStore.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/startup/GameFoldPcRuntimeProvisionerTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/startup/GameFoldPcRuntimeProvisionStoreTest.kt`

**Interfaces:**

```kotlin
enum class RuntimeProvisionPhase {
    VALIDATING_PUBLIC_PAYLOAD,
    INSTALLING_SHARED_RUNTIME,
    VERIFYING_PRIVATE_RUNTIME,
}

data class RuntimeProvisionProgress(
    val phase: RuntimeProvisionPhase,
    val percent: Int? = null,
)

data class PrivateRuntimeStatus(
    val imageFsReady: Boolean,
    val protonReady: Boolean,
) { val ready: Boolean get() = imageFsReady && protonReady }

sealed interface RuntimeProvisionResult {
    data class Ready(val reused: Boolean) : RuntimeProvisionResult
    data object PermissionRequired : RuntimeProvisionResult
    data class PayloadInvalid(val missing: List<String>, val invalid: List<String>) : RuntimeProvisionResult
    data class Failed(val phase: RuntimeProvisionPhase, val message: String) : RuntimeProvisionResult
}

interface PrivateRuntimeGateway {
    fun inspect(): PrivateRuntimeStatus
    fun installSharedRuntime(onProgress: (Int) -> Unit): Boolean
    fun invalidateIncomplete()
}

interface RuntimeProvisioner {
    suspend fun ensureReady(
        forceFullValidation: Boolean = false,
        onProgress: (RuntimeProvisionProgress) -> Unit = {},
    ): RuntimeProvisionResult
}
```

- [ ] **Step 1: Write coordinator tests** for permission denied/no writes, any missing manifest entry blocking installation, first install, post-install Proton failure, invalidation on failure, retry success, matching-receipt reuse, changed-manifest validation without reinstall when private runtime remains exact, and two concurrent callers invoking installation once.
- [ ] **Step 2: Write receipt-store tests.** Receipt schema is exactly:

```kotlin
@Serializable
data class RuntimeProvisionReceipt(
    val schema: Int = 1,
    val manifestSha256: String,
    val imageFsVersion: Int = 28,
    val variant: String = "bionic",
    val protonEntry: String = "proton-10.0-arm64ec-2",
)
```

Store it through `AtomicFile` at `filesDir/gamefold_pc/runtime-provision-v1.json`. A truncated or mismatched receipt reads as absent.
- [ ] **Step 3: Run both tests and verify red.**
- [ ] **Step 4: Implement a `Mutex`-guarded coordinator.** Perform the cheap path/size/metadata preflight first. Perform full SHA validation before private writes on first provision, receipt/manifest metadata change, incomplete private status, or explicit force; a completely matching receipt may use the cheap path. Write the receipt only after post-install inspection is ready.
- [ ] **Step 5: Run focused tests.**

```bash
./gradlew :app:testLegacyDebugUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests app.gamenative.gamefold.startup.GameFoldPcRuntimeProvisionerTest \
  --tests app.gamenative.gamefold.startup.GameFoldPcRuntimeProvisionStoreTest \
  --offline --no-daemon --console=plain
```

- [ ] **Step 6: Commit.**

```bash
git add app/src/main/java/app/gamenative/gamefold/startup app/src/test/java/app/gamenative/gamefold/startup
git commit -m "feat: coordinate first-run PC runtime provisioning"
```

---

### Task 4: Make ImageFS, shared guest content, and Proton provisioning truthful

**Files:**

- Modify: `app/src/main/java/com/winlator/xenvironment/ImageFsInstaller.java`
- Create: `app/src/main/java/app/gamenative/gamefold/startup/AndroidGameFoldPcRuntimeGateway.kt`
- Modify: `app/src/test/java/com/winlator/xenvironment/ImageFsInstallerTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/startup/AndroidGameFoldPcRuntimeGatewayTest.kt`

**Interfaces:**

```java
public static Future<Boolean> installSharedRuntimeIfNeededFuture(
    Context context,
    AssetManager assets,
    Container bootstrap,
    Callback<Integer> progress
);

static boolean ensureBundledProtonInstalled(
    Context context,
    AssetManager assets,
    String expectedEntry
);
```

- [ ] **Step 1: Add red tests** proving that a Proton extraction/profile/bin/lib failure returns false, does not write the completion marker, and is not hidden by a successful ImageFS extraction.
- [ ] **Step 2: Add red tests** proving that valid ImageFS with missing Proton repairs Proton, valid complete runtime does not re-extract, shared redirect/extras are installed pre-home, and per-game container patterns/FEXCore/PulseAudio/input/graphics archives are not extracted by this entry.
- [ ] **Step 3: Add marker-order tests.** `.variant` must be written and read back before `.img_version`; `.img_version` is the final commit marker. `ImageFs.isValid()` alone is insufficient for the gateway's ready result.
- [ ] **Step 4: Implement the shared-runtime entry.** Refactor current private helpers to return real booleans. Stage the complete managed ImageFS, shared redirect/guest content, common extras and exact default Proton under sibling directories on the same private filesystem. Record a commit journal, validate every required marker/profile/link, atomically rename the new runtime into place with a backup, and only delete the backup after a second post-switch inspection. On exception, cancellation or ENOSPC, restore the backup or preserve the previous valid runtime; never clear `imagefs/home`, user-imported Wine/Proton, or per-game containers.
- [ ] **Step 5: Implement `AndroidGameFoldPcRuntimeGateway`.** Its inspector requires ImageFS version 28, variant `bionic`, exact Proton content profile, binary directory, library directory and prefix pack. Directory existence alone is not ready.
- [ ] **Step 6: Handle interrupted/ENOSPC installs.** A journal is recoverable on next launch; no receipt or final marker is committed until the switch and post-inspection succeed. ENOSPC must leave the old valid runtime usable, with no destructive cleanup of its home or containers. Do not hardcode a free-space threshold from the compression-ratio estimate; record actual private-storage delta during device validation before adding any future threshold.
- [ ] **Step 7: Run tests and the 13 baseline tests.**

```bash
./gradlew :app:testLegacyDebugUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests com.winlator.xenvironment.ImageFsInstallerTest \
  --tests app.gamenative.gamefold.startup.AndroidGameFoldPcRuntimeGatewayTest \
  --tests app.gamenative.product.GameFoldPcDefaultsTest \
  --tests app.gamenative.product.GameFoldPcPerGameDefaultsTest \
  --tests app.gamenative.utils.GtaFullscreenConfigTest \
  --offline --no-daemon --console=plain
```

- [ ] **Step 8: Commit.**

```bash
git add app/src/main/java/com/winlator/xenvironment/ImageFsInstaller.java app/src/main/java/app/gamenative/gamefold/startup app/src/test
git commit -m "feat: provision shared PC runtime before home"
```

---

### Task 5: Establish official NS 2.9 R2 design resources and pure shell geometry

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldDesignTokens.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/shell/GameFoldViewportLayout.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/shell/GameFoldViewportCalculator.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/shell/GameFoldShellLayout.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/shell/GameFoldShellHost.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/ui/shell/GameFoldViewportCalculatorTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/ui/shell/GameFoldWindowGeometryTest.kt`
- Add: exact prefixed NS pixel colors/styles/dimens/attrs and enumerated shell/controller assets under `app/src/main/res/`.
- Create: `packaging/test_gamefold_ns_ui_assets.sh`

**Interfaces:**

```kotlin
data class GameFoldViewportGeometry(
    val shellWidth: Int,
    val shellHeight: Int,
    val viewport: Rect,
    val controllerRegion: Rect,
    val supported: Boolean,
)

@Composable
fun GameFoldShellHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
```

- [ ] **Step 1: Create the asset manifest and failing guard.** Treat the frozen NS 2.9 R2 APK (`156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292`) as the asset truth. Extract/map resources from the APK: preserve direct ZIP-entry bytes and SHA where possible; for aapt-transcoded images compare dimensions plus decoded RGBA SHA; canonicalize XML/tokens from `aapt2` resource dumps. The dirty NS source is implementation reference only. Any asset without a unique APK mapping requires same-state frozen-APK/device screenshots and is not self-certified by hashing a copied working-tree file.
- [ ] **Step 2: Add geometry tests** for the 2480×2200 reference, a conventional landscape viewport, portrait 16:9 aperture, top dynamic corners, fixed hinge-adjacent bottom corners, 7 px reference border scaling, and unsupported minimum dimensions.

```kotlin
@Test fun portraitViewportRemainsSixteenByNine() {
    val g = calculator.calculate(width = 2200, height = 2480, portrait = true, insets = testInsets)
    assertEquals(16f / 9f, g.viewport.width().toFloat() / g.viewport.height(), 0.002f)
}
```

- [ ] **Step 3: Mechanically copy only the approved binary assets**, retaining exact bytes and recording hashes. Prefix colliding XML resource names; do not copy Switch icons, ROM assets or Yuzu settings resources. Geometry anchors must be within 2 px at reference size, stable palette samples exact, and masked SSIM against frozen-APK captures at least 0.98 after masking dynamic cover/title/FPS content.
- [ ] **Step 4: Port pure drawing/measurement code.** Remove Yuzu imports and leave controller touch handling disabled in this task; Task 10 adds the typed PC input listener. Host a `ComposeView` in the display aperture with `DisposeOnViewTreeLifecycleDestroyed`.
- [ ] **Step 5: Run geometry tests, resource linking, and asset guard.**

```bash
bash packaging/test_gamefold_ns_ui_assets.sh
./gradlew :app:testLegacyDebugUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests 'app.gamenative.gamefold.ui.shell.*' \
  :app:processLegacyDebugResources \
  --offline --no-daemon --console=plain
```

- [ ] **Step 6: Commit.**

```bash
git add app/src/main/java/app/gamenative/gamefold/ui app/src/main/res app/src/test packaging
git commit -m "feat: port the official GameFold product shell"
```

---

### Task 6: Add the official processor → permission → provisioning startup gate

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/contract/PackagePreflightPort.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/startup/GameFoldSocGate.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/startup/GameFoldPcStartupViewModel.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/startup/GameFoldPcStartupGate.kt`
- Modify: `app/src/main/java/app/gamenative/offline/OfflineStorageAccess.kt`
- Modify: `app/src/main/java/app/gamenative/MainActivity.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh-rCN/strings.xml`
- Create: `app/src/test/java/app/gamenative/gamefold/startup/GameFoldSocGateTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/startup/GameFoldPcStartupViewModelTest.kt`

**Interfaces:**

```kotlin
sealed interface StartupState {
    data object CheckingProcessor : StartupState
    data class UnsupportedProcessor(val reason: String) : StartupState
    data object PermissionRequired : StartupState
    data class Validating(val progress: Int? = null) : StartupState
    data class Installing(val progress: Int? = null) : StartupState
    data class Failed(val title: String, val details: String) : StartupState
    data object Ready : StartupState
}

interface PackagePreflightPort {
    val state: StateFlow<StartupState>
    suspend fun advance()
    suspend fun retry()
}
```

- [ ] **Step 1: Add state-machine tests.** Assert processor always runs before permission; unsupported MediaTek never opens permission; permission denial performs zero private writes; returning from system settings rechecks once; valid payload provisions before content; invalid payload shows exact diagnostics and retry; `Ready` renders content exactly once.
- [ ] **Step 2: Port the NS processor rule** from `GameFoldSocGate` without Switch/Yuzu imports. Add arm64 ABI reporting as diagnostics, not a new broad blacklist.
- [ ] **Step 3: Centralize the storage-settings Intent** in `OfflineStorageAccess`; do not let startup depend on the game scanner.
- [ ] **Step 4: Implement a shell-hosted pixel startup UI** matching the NS 2.9 R2 assembly page states. It has no seed-copy or downloader. The existing download URL is shown only where the approved NS flow already provides it.
- [ ] **Step 5: Wrap the PC root in `MainActivity`.** The startup gate is enabled only for `BuildConfig.GAMEFOLD_PC_LOCAL_SHELL`; its `Ready` content initially remains `PluviaMain` so this task cannot change home or runtime behavior.
- [ ] **Step 6: Remove only the duplicate first-game shared bootstrap** from `preLaunchLocalShell`; preserve full manifest validation and per-game full installation as defense in depth.
- [ ] **Step 7: Run startup/provisioning tests and compile.**

```bash
./gradlew :app:testLegacyDebugUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests 'app.gamenative.gamefold.startup.*' \
  --tests app.gamenative.offline.OfflineStorageAccessTest \
  :app:compileLegacyDebugKotlin \
  --offline --no-daemon --console=plain
```

- [ ] **Step 8: Commit.**

```bash
git add app/src/main app/src/test
git commit -m "feat: assemble PC runtime during first app launch"
```

---

### Task 7: Define UI ports and PC adapters without changing screens

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/contract/GameFoldGame.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/contract/GameCatalogPort.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/contract/RuntimeSessionPort.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/contract/RuntimeInputPort.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/contract/PerformanceStatsPort.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/contract/GameFoldSettingsRepository.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/contract/RearDisplayPort.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/adapter/PcGameCatalogAdapter.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/adapter/PcRuntimeSessionAdapter.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/adapter/PcGameCatalogAdapterTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/adapter/PcRuntimeSessionAdapterTest.kt`

**Interfaces:**

```kotlin
data class LocalGameSummary(
    val appId: String,
    val title: String,
    val artworkPath: String?,
    val folderAvailable: Boolean,
)

sealed interface RuntimeLaunchState {
    data object Idle : RuntimeLaunchState
    data class Checking(val message: String) : RuntimeLaunchState
    data class Installing(val progress: Float?, val message: String) : RuntimeLaunchState
    data class Booting(val game: LocalGameSummary, val message: String) : RuntimeLaunchState
    data class Running(val appId: String) : RuntimeLaunchState
    data class Failed(val appId: String?, val error: RuntimeLaunchError) : RuntimeLaunchState
}

data class RuntimeLaunchError(
    val kind: Kind,
    val message: String,
    val recoverable: Boolean,
) {
    enum class Kind { PERMISSION, PUBLIC_PAYLOAD, MISSING_EXECUTABLE, RUNTIME_SETUP, GUEST_EXIT, UNKNOWN }
}

enum class RuntimeLaunchMode { GAME, CONTAINER, GRAPHICS_TEST, DIAGNOSTIC }
@JvmInline value class GameFoldSessionId(val value: String)
sealed interface RuntimeSessionState {
    data object Idle : RuntimeSessionState
    data class Preparing(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Checking(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Installing(val id: GameFoldSessionId, val appId: String, val progress: Float?) : RuntimeSessionState
    data class Booting(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Running(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Paused(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Stopping(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Failed(val id: GameFoldSessionId?, val appId: String?, val error: RuntimeLaunchError) : RuntimeSessionState
}
sealed interface SessionCommandResult {
    data class Applied(val id: GameFoldSessionId) : SessionCommandResult
    data class AlreadyInState(val id: GameFoldSessionId) : SessionCommandResult
    data class Rejected(val reason: String) : SessionCommandResult
    data class Failed(val error: RuntimeLaunchError) : SessionCommandResult
}
enum class GameFoldButton { DPAD_UP, DPAD_RIGHT, DPAD_DOWN, DPAD_LEFT, X, A, B, Y, MINUS, PLUS, L, R, ZL, ZR, L3, R3, HOME }
enum class GameFoldStick { LEFT, RIGHT }
enum class InputSource { FRONT_TOUCH, REAR_TOUCH, PHYSICAL }
interface RuntimeInputLease : AutoCloseable {
    fun setButton(button: GameFoldButton, pressed: Boolean)
    fun setTrigger(button: GameFoldButton, value: Float)
    fun setStick(stick: GameFoldStick, x: Float, y: Float)
    fun releaseAll()
    override fun close()
}
enum class RearDisplayState { OFF, STARTING, ACTIVE, STOPPING, FAILED }

interface GameCatalogPort {
    suspend fun scanGames(): List<LocalGameSummary>
    suspend fun refresh(): List<LocalGameSummary>
}

interface RuntimeSessionPort {
    val state: StateFlow<RuntimeSessionState>
    suspend fun launch(appId: String, mode: RuntimeLaunchMode = RuntimeLaunchMode.GAME): SessionCommandResult
    suspend fun pause(id: GameFoldSessionId): SessionCommandResult
    suspend fun resume(id: GameFoldSessionId): SessionCommandResult
    suspend fun stop(id: GameFoldSessionId): SessionCommandResult
}

interface RuntimeInputPort {
    fun attach(source: InputSource, slot: Int = 0): RuntimeInputLease
}

interface PerformanceStatsPort { val framesPerSecond: StateFlow<Int> }
interface RearDisplayPort {
    val state: StateFlow<RearDisplayState>
    fun isSupported(): Boolean
    suspend fun enable()
    suspend fun disable()
}

sealed interface GameFoldSettingValue {
    data class Toggle(val enabled: Boolean) : GameFoldSettingValue
    data class Choice(val selectedId: String, val choices: List<String>) : GameFoldSettingValue
    data class Range(val value: Float, val minimum: Float, val maximum: Float, val step: Float) : GameFoldSettingValue
    data class Text(val value: String) : GameFoldSettingValue
    data object Action : GameFoldSettingValue
}

data class GameFoldSettingItem(
    val id: String,
    val title: String,
    val summary: String?,
    val value: GameFoldSettingValue,
    val enabled: Boolean = true,
)
data class GameFoldSettingsSection(val id: String, val title: String, val items: List<GameFoldSettingItem>)
data class GameFoldSettingsSnapshot(val sections: List<GameFoldSettingsSection>)
sealed interface GameFoldSettingsScope {
    data object Global : GameFoldSettingsScope
    data class Game(val appId: String) : GameFoldSettingsScope
}
interface GameFoldSettingsRepository {
    fun observe(scope: GameFoldSettingsScope): Flow<GameFoldSettingsSnapshot>
    suspend fun update(scope: GameFoldSettingsScope, itemId: String, value: GameFoldSettingValue)
}
```

`LocalGameSummary` exposes no online/store fields.

- [ ] **Step 1: Write catalog adapter tests** for empty/multiple games, stable ID/order, square artwork path, missing artwork, missing folder and refresh without duplicates.
- [ ] **Step 2: Write runtime adapter tests** mapping PC states into `Idle/Checking/Installing/Booting/Running/Failed`, forwarding four launch modes, and releasing stale loading state after failure.
- [ ] **Step 3: Implement adapters around existing scanner and prelaunch callbacks.** Do not move or rewrite `ContainerUtils`, `setupXEnvironment`, WinHandler, LSFG or XServer.
- [ ] **Step 4: Add architecture guard.** Fail if files under `app/gamenative/gamefold/ui` import `ContainerManager`, `ImageFs`, `NativeInput`, Yuzu packages, or `XServerScreen` directly.
- [ ] **Step 5: Run focused tests and scanner/launch tests.**

```bash
./gradlew :app:testLegacyDebugUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests 'app.gamenative.gamefold.adapter.*' \
  --tests app.gamenative.utils.CustomGameScannerFirstScanTest \
  --tests app.gamenative.utils.LocalGameExecutableResolverTest \
  --tests app.gamenative.offline.OfflineGameEntryActionTest \
  --offline --no-daemon --console=plain
```

- [ ] **Step 6: Commit.**

```bash
git add app/src/main/java/app/gamenative/gamefold app/src/test
git commit -m "refactor: isolate GameFold UI from the PC runtime"
```

---

### Task 8: Replace the home library with official cartridge UI

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldRoot.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/library/GameFoldLibraryScreen.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/library/GameFoldCartridgeCard.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/library/GameFoldAddGameDialog.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/ui/library/GameFoldLibraryPresenterTest.kt`
- Create: `app/src/androidTest/java/app/gamenative/gamefold/ui/library/GameFoldLibraryScreenTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/HomeScreen.kt`

**Interfaces:**

```kotlin
data class GameFoldLibraryUiState(
    val games: List<LocalGameSummary>,
    val loading: Boolean,
    val error: String? = null,
)

sealed interface GameFoldLibraryAction {
    data class Launch(val appId: String) : GameFoldLibraryAction
    data class OpenGameSettings(val appId: String) : GameFoldLibraryAction
    data object OpenAdvancedSettings : GameFoldLibraryAction
    data object ToggleOrientation : GameFoldLibraryAction
    data object ToggleRearDisplay : GameFoldLibraryAction
    data object ShowAddGame : GameFoldLibraryAction
}
```

- [ ] **Step 1: Write presenter tests** for empty/one/four/many games, long Chinese/English titles, missing artwork, add card position, stable focus order, click launch, long press settings and no long-press behavior on the add card.
- [ ] **Step 2: Write Compose UI tests** using tags `gamefold_library`, `gamefold_game_${appId}`, `gamefold_add_game`, `gamefold_advanced_settings`, `gamefold_orientation`, and `gamefold_rear_display`.
- [ ] **Step 3: Implement the exact cartridge composition.** Outer ratio is 343:392; artwork is square and `ContentScale.Crop`; title is two lines below. Use exact NS frame/focus/add assets.
- [ ] **Step 4: Implement the add dialog.** Text path is exactly `/storage/emulated/0/GameFoldPC/game/`; the approved existing download URL is unchanged.
- [ ] **Step 5: Route the settings icon directly to PC advanced settings.** Keep current PC settings as the destination until Task 12 reskins it.
- [ ] **Step 6: Run tests, resource guard and screenshot captures** at the reference landscape and portrait sizes. Compare perceptual/geometry diffs to documented NS evidence; any unexplained large diff blocks commit.
- [ ] **Step 7: Commit.**

```bash
git add app/src/main app/src/test app/src/androidTest
git commit -m "feat: adopt the official GameFold cartridge library"
```

---

### Task 9: Reuse official loading states and pixel FPS

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/ui/runtime/GameFoldLoadingOverlay.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/runtime/GameFoldPixelNumberView.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/adapter/PcPerformanceStatsAdapter.kt`
- Modify: `app/src/main/java/app/gamenative/ui/model/MainViewModel.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/ui/runtime/GameFoldLoadingStateTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/ui/runtime/GameFoldPixelNumberViewTest.kt`

**Interfaces:** `RuntimeLaunchState` and `PerformanceStatsPort.framesPerSecond` from Task 7.

- [ ] **Step 1: Add loading-state tests** for 0%, 50%, 100%, indeterminate, missing resource, missing executable, runtime setup failure, retry, game switch and guest exit.
- [ ] **Step 2: Port the 3×5 pixel-number renderer** and test 0, 1, 9, 10, 60, 120 and three digits. FPS has no added label if the NS reference has no label.
- [ ] **Step 3: Map all current prelaunch callbacks and unexpected exceptions** to typed UI states; add the missing outer exception boundary so a coroutine failure cannot leave loading stuck.
- [ ] **Step 4: Hide the previous runtime Surface while a different game is preparing.** Reveal only after the new XServer window-mapped callback; failure must leave the pixel error panel, not the old frame.
- [ ] **Step 5: Feed FPS from the existing renderer callback** and keep the current 800 ms presentation cadence. Stop collection when disabled or the session stops.
- [ ] **Step 6: Run focused tests plus LSFG/HUD tests and compile.**

```bash
./gradlew :app:testLegacyDebugUnitTest \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  --tests 'app.gamenative.gamefold.ui.runtime.*' \
  --tests app.gamenative.utils.LsfgDllLocatorTest \
  :app:compileLegacyDebugKotlin \
  --offline --no-daemon --console=plain
```

- [ ] **Step 7: Commit.**

```bash
git add app/src/main app/src/test
git commit -m "feat: use GameFold loading and pixel FPS presentation"
```

---

### Task 10: Host the unchanged PC Surface in the shell and connect the official controller

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/ui/shell/GameFoldControllerInput.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/adapter/PcRuntimeInputAdapter.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/runtime/GameFoldRuntimeHost.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/ui/shell/GameFoldControllerInputTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/adapter/PcRuntimeInputAdapterTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`

**Interfaces:** `GameFoldButton`, `GameFoldStick`, and `RuntimeInputPort` from Task 7.

- [ ] **Step 1: Port controller hit/gesture behavior** with red tests for every button down/up pair, multi-touch, cancel, focus loss, background, orientation, unsupported mode, stick center/boundary/release and L3/R3 at 599/600 ms.
- [ ] **Step 2: Add the exact PC mapping test.** Map GameFold A/B/X/Y to the existing Xbox indices, D-pad to hat/dpad state, L/R/ZL/ZR to shoulder/trigger state, L3/R3 to stick clicks, and sticks to normalized axes. HOME is a shell/session navigation action and never enters guest `GamepadState`. Attach FRONT_TOUCH, REAR_TOUCH and PHYSICAL leases to one reducer: buttons OR, triggers max, axes choose greatest absolute magnitude (latest sequence on ties), a lease release clears only that source, and a combined neutral state is emitted once when all sources are neutral. Assert the physical path also goes through the reducer and the final neutral state reaches `WinHandler.sendVirtualGamepadState`.
- [ ] **Step 3: Introduce `GameFoldRuntimeHost`.** It owns shell presentation and receives the existing PC XServer View as the display child. It does not create a second XServer, Surface or renderer.
- [ ] **Step 4: Disable the old generic touch-controls layer only when the GameFold shell is active.** Physical controllers attach a PHYSICAL lease to the same reducer; WinHandler/evshim layouts and environment variables remain untouched.
- [ ] **Step 5: Preserve upper-display PC pointer/touch behavior** within the exact aperture. Clamp and transform coordinates to the runtime Surface; touches in the controller region never leak to the Surface.
- [ ] **Step 6: Run controller, physical-input, XServer compile and protected-runtime tests.**
- [ ] **Step 7: Commit.**

```bash
git add app/src/main app/src/test
git commit -m "feat: connect the GameFold controller to PC input"
```

---

### Task 11: Complete HOME, pause/resume, Back and exit semantics

**Files:**

- Modify: `app/src/main/java/app/gamenative/gamefold/adapter/PcRuntimeSessionAdapter.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/runtime/GameFoldSessionNavigationPolicy.kt`
- Modify: `app/src/main/java/app/gamenative/ui/PluviaMain.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/ui/runtime/GameFoldSessionNavigationPolicyTest.kt`

**Lifecycle contract:** `PcRuntimeSessionController` owns one `GameFoldSessionId` and is bound to the actual XEnvironment/XServer/Surface create, mapped, pause, resume and teardown callbacks. It implements the Task 7 suspend commands with `Applied`, `AlreadyInState`, `Rejected` and `Failed` acknowledgements; no composable-local fake callback may claim lifecycle success. Returning HOME pauses/navigation-hides the same session, and re-entry resumes it without recreating or double-closing the environment.

**Interfaces:**

```kotlin
sealed interface SessionNavigationAction {
    data object ReturnHomeAndPause : SessionNavigationAction
    data object ResumeRunningGame : SessionNavigationAction
    data object RequestExit : SessionNavigationAction
    data object None : SessionNavigationAction
}
```

- [ ] **Step 1: Add tests** for HOME while running, A on the paused-home affordance, Back with quick menu enabled, Back with it disabled and exit warning on/off, double-Back timeout, switching games, and teardown once.
- [ ] **Step 2: Implement one policy path.** HOME returns to the GameFold library and pauses without destroying the Wine session; resuming reuses the active session. Back behavior follows the currently accepted PC setting and never becomes a swallowed no-op.
- [ ] **Step 3: Run navigation tests, pause/resume tests and a JVM lifecycle fake.** Assert no second environment and no double shutdown.
- [ ] **Step 4: Commit.**

```bash
git add app/src/main app/src/test
git commit -m "feat: align PC session navigation with GameFold"
```

---

### Task 12: Reskin global advanced settings and per-game settings

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/adapter/PcSettingsAdapter.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/settings/GameFoldAdvancedSettingsScreen.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/settings/GameFoldGameSettingsScreen.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/ui/settings/GameFoldGamePropertiesHeader.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/adapter/PcSettingsAdapterTest.kt`
- Create: `app/src/androidTest/java/app/gamenative/gamefold/ui/settings/GameFoldSettingsScreenTest.kt`
- Modify: PC navigation destinations currently opening `SettingsScreen` and the offline details screen.

**Interfaces:** `GameFoldSettingsRepository.observe(scope)` returns sectioned `GameFoldSettingsSnapshot`; `update(scope, itemId, value)` persists one typed toggle/choice/range/text/action value. The adapter retains existing Container/DataStore value types internally and exposes no Yuzu `NativeConfig`.

- [ ] **Step 1: Write round-trip tests** for global vs per-game resolution, executable, Wine/Proton, driver, language, HUD, LSFG, input and diagnostics. Repeating a migration is idempotent and custom values survive. Include the V0.6.6 one-time global empty/`1280x720` → `1067x600` migration and verify existing per-game resolutions are never overwritten.
- [ ] **Step 2: Write UI tests** for official section/header/control/focus states, direct home-to-advanced navigation, long-press game-to-properties/settings, saving, canceling and returning to the same card.
- [ ] **Step 3: Implement the official GameFold pixel presentation.** Keep PC-relevant advanced controls; remove Switch firmware/keys/applets/addons and hide Steam/GOG/Epic account, recommendation, update and import-as-Steam surfaces.
- [ ] **Step 4: Preserve diagnostic launch modes** behind the GameFold-styled per-game page. Do not remove container/open-container/graphics-test/diagnostic functionality.
- [ ] **Step 5: Run adapter/UI tests and existing container/default tests.**
- [ ] **Step 6: Commit.**

```bash
git add app/src/main app/src/test app/src/androidTest
git commit -m "feat: apply GameFold design to PC settings"
```

---

### Task 13: Add explicit orientation and unsupported-window protection

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/display/GameFoldOrientationController.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/display/GameFoldWindowSupportPolicy.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/display/GameFoldOrientationControllerTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/display/GameFoldWindowSupportPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/MainActivity.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/ui/shell/GameFoldShellLayout.kt`

**Interfaces:**

```kotlin
enum class GameFoldOrientation { LANDSCAPE, PORTRAIT }

data class WindowSupportState(
    val supported: Boolean,
    val reason: Reason? = null,
) {
    enum class Reason { MULTI_WINDOW, PICTURE_IN_PICTURE, TOO_SMALL }
}
```

- [ ] **Step 1: Add persistence tests.** Orientation is explicit, survives process recreation and never follows the sensor.
- [ ] **Step 2: Add support-policy tests** for full-screen, multi-window, PiP and undersized dimensions. Every transition into unsupported state closes/releaseAlls the FRONT_TOUCH and REAR_TOUCH leases once (the PHYSICAL lease remains owned by the physical handler), emits one reducer-neutral state, and hides child content.
- [ ] **Step 3: Implement NS behavior.** Show the official unsupported panel, swallow shell touches, preserve but do not relaunch the runtime session, and restore focus/content on return.
- [ ] **Step 4: Add a 20-cycle instrumentation test** for landscape → portrait → landscape with stable 16:9 geometry, focus and neutral input.
- [ ] **Step 5: Run tests and commit.**

```bash
git add app/src/main app/src/test app/src/androidTest
git commit -m "feat: preserve GameFold orientation and window rules"
```

---

### Task 14: Port vivo rear-touch as a capability-backed PC display feature

**Files:**

- Create: `app/src/main/java/app/gamenative/gamefold/display/VivoRearDisplayController.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/display/RearTouchActivity.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/display/RearTouchInputRouter.kt`
- Create: `app/src/main/java/app/gamenative/gamefold/display/FrontEdgeLightController.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/display/VivoRearDisplayControllerTest.kt`
- Create: `app/src/test/java/app/gamenative/gamefold/display/RearTouchInputRouterTest.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Add: exact NS rear-touch assets.

**Interfaces:** Consumes `RearDisplayPort`, `RearDisplayState`, `RuntimeInputPort.attach(InputSource.REAR_TOUCH)`, `GameFoldButton.ZL`, and `GameFoldButton.ZR` from Task 7. Produces the vivo implementation without changing those contracts.

- [ ] **Step 1: Write state-machine tests** for unsupported devices, portrait hiding, repeated enable/disable, app background, session stop, vendor command failure, display launch failure and release of ZL/ZR.
- [ ] **Step 2: Port the vivo implementation** with capability checks, the validated vendor/display sequence, independent rear Activity, reverse-landscape behavior, transparent-pixel hit testing and front edge feedback.
- [ ] **Step 3: Route rear ZL/ZR through a REAR_TOUCH lease on the same reducer.** No Yuzu `NativeInput`; front, rear and physical input must merge without one source clearing another.
- [ ] **Step 4: Preserve blank-frame shutdown.** Hide controls, commit a black frame using `registerFrameCommitCallback`, then restore single-screen state; keep the 150 ms fallback only as the existing safety net.
- [ ] **Step 5: Keep PC generic Presentation as a separate fallback.** Non-vivo devices hide the private rear-screen button.
- [ ] **Step 6: Run state/router tests, manifest guard and compile.** Commit only if automation is green; mark physical no-flash observation `HOLD_FOR_DEVICE` until Task 16.
- [ ] **Step 7: Commit.**

```bash
git add app/src/main app/src/test
git commit -m "feat: port GameFold rear-touch controls to PC"
```

---

### Task 15: Record and protect the frozen 0.6.6 semantic baseline

**Files:**

- Create: `docs/gamefold/V066_IMPORT_LEDGER.md`
- Create: `packaging/test_gamefold_pc_v066_baseline.sh`
- Add/modify tests adjacent to each retained semantic contract.

**Interfaces:** No new runtime ABI. Imported behavior must flow through Tasks 7, 11 and 12 ports/policies.

- [ ] **Step 1: Verify the frozen tag/artifact pair.** Assert tag commit `139d0d4`, source commit `c032044`, baseline APK SHA `3460a103...5268ad`, package/version/signer, and the archived 227-test/39-class XML inventory or baseline record. Do not inspect or import RC1/RC2 outputs.
- [ ] **Step 2: Write the ledger classification against `gamefold-pc-v0.6.6-baseline`.** Mark the baseline defaults/migrations, quick-menu + Back pair, and offline Proton 10 selector as `RETAIN`; mark splash/icon presentation as `SUPERSEDED_BY_NS_UI`; mark scanner, A:, ImageFS, Proton runtime, XServer/Surface/AHB, WinHandler/evshim, GTA and LSFG wiring as `PROTECTED`.
- [ ] **Step 3: Add semantic regression tests.** Cover LSFG `0.50`, FSR Fit, controller defaults, quick menu/exit warning, GameFold branding/tip suppression/icon-picker hiding, one-time global resolution migration, existing-game resolution preservation, and new-game global-template inheritance.
- [ ] **Step 4: Keep quick-menu and Back behavior atomic.** `c343ccf` gating and `6fe04e0` disabled-menu Back/exit handling remain one protected pair; route the result through Task 11 session policy.
- [ ] **Step 5: Run the frozen 0.6.6 source tests plus cumulative integration tests** with `-PGAMEFOLD_PC_LOCAL_SHELL=true`. Any conflict touching protected runtime surfaces stops the slice.
- [ ] **Step 6: Commit the provenance ledger and guard.** The current branch already contains the 0.6.6 behavior through the frozen tag; no wholesale cherry-pick is permitted.

---

### Task 16: Full regression, release candidate, installation, and autonomous device acceptance

**Files:**

- Create: `packaging/test_gamefold_pc_ns_ui_apk.sh`
- Update: `docs/gamefold/PC_NS_INTEGRATION_ACCEPTANCE.md`
- Produce immutable candidate APKs under: `releases/candidates/V0.7.0/`
- Create matching immutable metadata records under: `releases/candidates/V0.7.0/`

**Interfaces:** Consumes every previous task. Produces one immutable RC bound to one clean commit and one evidence record.

- [ ] **Step 1: Run cumulative focused suites and guards.** Include baseline, public resources, provisioning, adapters, startup, shell geometry, library, loading/FPS, input, settings, orientation, rear state and the final 0.6.6 tests. All must be green.
- [ ] **Step 2: Run the complete Legacy task and compare to the frozen 0.6.6 local-shell inventory.** Pass `-PGAMEFOLD_PC_LOCAL_SHELL=true`; require the exact 39-suite/227-test inventory unless a reviewed integration test intentionally adds a case, and compare exact historical failure/error case IDs rather than a count. Any new failure/error or silently removed test fails the gate.
- [ ] **Step 3: Compute a unique versionCode.** Read all candidate `output-metadata.json`/APK metadata and the device's installed code; use maximum + 1. Use versionName `0.7.0-ns-ui-rc1`, incrementing the RC suffix and versionCode for every changed APK byte.
- [ ] **Step 4: Build explicitly as the local PC product.**

```bash
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:assembleLegacyRelease \
  --offline --no-daemon --console=plain \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  -PGAMEFOLD_PC_NATIVE_PRUNE=true \
  -PGAMEFOLD_PC_APPLICATION_ID=com.gamefold.pc.v04localshell \
  -PGAMEFOLD_PC_VERSION_CODE="$gamefold_candidate_version_code" \
  -PGAMEFOLD_PC_VERSION_NAME="$gamefold_candidate_version_name"
```

- [ ] **Step 5: Verify and archive bytes.** Check package, versionName/code, min/target SDK, v2 signature, signer, arm64 ABI, permissions/activities whitelist, protected native `.so` hashes/diff, asset presence, APK SHA-256, and byte equality between build output and independent archive. Commit source before declaring the APK final; record commit and exact build command in metadata.
- [ ] **Step 6: Check the connected device and install in place.** Start with `/Users/11085429/Library/Android/sdk/platform-tools/adb devices -l`. Use `/Users/11085429/.codex/skills/install-apk-vivo/scripts/install_apk_vivo.py`; do not uninstall or clear data. Verify pulled-back APK SHA, package/version, signer, unchanged first-install time, new update time, `MANAGE_EXTERNAL_STORAGE`, game count and container count.
- [ ] **Step 7: Exercise first-run/provisioning safely.** On the existing user-data install, verify receipt reuse/no re-extraction. Test missing/corrupt payload and interrupted provisioning only with injected test roots/unit tests or a separate test package/profile; never damage the user's public payload or clear the accepted package.
- [ ] **Step 8: Automate UI acceptance.** Capture startup/home/empty/error/loading/settings/portrait/landscape/unsupported screenshots, assert UI tags/text and compare geometry/color/asset evidence to NS 2.9 R2. Run 20 orientation cycles; inspect logcat for crashes, ANRs and sticky input.
- [ ] **Step 9: Launch Black Myth Pixel and GTA V.** Verify correct EXE and folder/A: isolation; GTA command line; XServer, Surface and five AHB buffers at 1067x600; LSFG layer/swapchain; pixel FPS; controller state transitions; pause/home/resume; Back/exit; no fatal/ANR/native signal.
- [ ] **Step 10: Verify vivo rear display.** Automate state/display/activity/log/input checks and capture each display where supported. Observe committed-black-frame ordering. If a physical fold/no-flash/tactile fact cannot be established from device state or video, mark only that row `HOLD_FOR_DEVICE` and keep the overall artifact at `RC_READY_FOR_MANUAL_ACCEPTANCE`.
- [ ] **Step 11: Request independent reviews.** Dispatch one reviewer for spec/plan compliance, one for runtime-regression risk and one for test/artifact evidence. Resolve all P0/P1 issues with a new commit, versionCode and APK; document lower-priority findings.
- [ ] **Step 12: Finalize acceptance record.** Every row contains status, command/evidence, timestamp and candidate SHA. Confirm both frozen baselines are unchanged. Do not update `CURRENT_GAMEFOLD_PC_BASELINE.md`, create a baseline tag, or overwrite a baseline APK.

## Completion Definition

The autonomous run is complete when:

- all code and docs are committed on the isolated integration branch;
- all new and affected tests/guards pass;
- full Legacy results preserve the frozen 0.6.6 suite/test inventory and introduce no new failure/error beyond its exact recorded set;
- a unique, signed, archived RC is byte-verified and installed in place;
- automated device checks for startup, shared provisioning, library, settings, both games, Surface/AHB, LSFG, FPS, input, orientation and available dual-screen evidence pass;
- unresolved physical-only cells are explicitly `HOLD_FOR_DEVICE`;
- the final state is reported as `RC_READY_FOR_MANUAL_ACCEPTANCE` with no baseline promotion.

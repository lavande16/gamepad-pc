# GameFold PC Restore Unsaved MessageDialog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the original unsaved-changes `MessageDialog` for global and per-game container settings while preserving manual Save behavior and all accepted branding changes.

**Architecture:** `ContainerConfigDialog` remains the single editor shared by both entry points. A pure Back-decision function distinguishes unchanged configuration (`DISMISS`) from changed configuration (`SHOW_CONFIRMATION`); all Back entry points use that decision, while the existing Save action remains the only call to `onSave(config)`.

**Tech Stack:** Kotlin, Jetpack Compose, `MessageDialogState`, Robolectric/JUnit, Gradle Android `legacyRelease`, vivo ADB installation.

## Global Constraints

- Preserve the accepted launcher/Home/About icon, Home `GAMEFOLD PC` title, and About social icons.
- Restore the exact existing unsaved-title, unsaved-message, discard, and cancel resources from the 0.8.3 implementation.
- Toolbar Back, Android system Back, and platform dialog dismissal must share the same policy.
- Explicit Save is the only configuration persistence path.
- Keep `versionName=0.8.5`; increment `versionCode` from installed `10141` to `10142` because APK bytes change.
- Do not create `gamefold-pc-v0.8.5-baseline` until the user verifies the newly installed candidate.

---

### Task 1: Restore the Unsaved-Changes Decision and MessageDialog

**Files:**
- Modify: `app/src/test/java/app/gamenative/ui/component/dialog/ContainerConfigDialogContainerUpdateTest.kt`
- Modify: `app/src/main/java/app/gamenative/ui/component/dialog/ContainerConfigDialog.kt`

**Interfaces:**
- Produces: `ContainerConfigBackAction { DISMISS, SHOW_CONFIRMATION }`
- Produces: `containerConfigBackAction(initialConfig: ContainerData, currentConfig: ContainerData): ContainerConfigBackAction`
- Uses: existing `MessageDialogState`, `MessageDialog`, and `R.string.container_config_unsaved_changes_*` resources.

- [ ] **Step 1: Write failing policy tests**

Add two tests: changed `ContainerData` expects `SHOW_CONFIRMATION`; unchanged data expects `DISMISS`. Remove the temporary silent-dismiss callback test.

- [ ] **Step 2: Verify the tests fail**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests 'app.gamenative.ui.component.dialog.ContainerConfigDialogContainerUpdateTest.backAction_*' --no-daemon --offline --console=plain
```

Expected: compilation fails because `ContainerConfigBackAction` and `containerConfigBackAction` are absent.

- [ ] **Step 3: Restore the decision and original dialog**

Restore `MessageDialogState`, the original `dismissDialogState`, and the original `MessageDialog`. Implement:

```kotlin
internal enum class ContainerConfigBackAction {
    DISMISS,
    SHOW_CONFIRMATION,
}

internal fun containerConfigBackAction(
    initialConfig: ContainerData,
    currentConfig: ContainerData,
): ContainerConfigBackAction = if (initialConfig == currentConfig) {
    ContainerConfigBackAction.DISMISS
} else {
    ContainerConfigBackAction.SHOW_CONFIRMATION
}
```

The shared Back handler opens the original confirmation when dirty and invokes `onDismissRequest` when unchanged. Cancel closes only the confirmation. Discard invokes only `onDismissRequest`. Save remains `{ onSave(config) }`.

- [ ] **Step 4: Verify the focused tests pass**

Run the same focused test command and require zero failures.

- [ ] **Step 5: Commit the behavior restoration**

Commit the two modified files with `fix: restore unsaved container confirmation`.

### Task 2: Issue, Build, Install, and Archive the New Candidate

**Files:**
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`
- Modify: `app/build.gradle.kts`
- Create: `releases/candidates/V0.8.5-UNSAVED-DIALOG-RC2/GameFold-PC-V0.8.5-UNSAVED-DIALOG-RC2-10142.apk`
- Create: `releases/candidates/V0.8.5-UNSAVED-DIALOG-RC2/manifest.txt`

**Interfaces:**
- Produces Android identity: `com.gamefold.pc`, `versionName=0.8.5`, `versionCode=10142`.

- [ ] **Step 1: Update the identity test first**

Change the existing BuildConfig assertion to expect `0.8.5 / 10142`, then run only `SettingsGeometryTest`. Expected: failure showing actual `10141`.

- [ ] **Step 2: Update the production versionCode**

Change only the default `GAMEFOLD_PC_VERSION_CODE` fallback in `app/build.gradle.kts` from `10141` to `10142`; keep `versionName` at `0.8.5`. Rerun the identity test and require a pass.

- [ ] **Step 3: Commit the new candidate identity**

Commit both identity files with `release: issue GameFold PC 0.8.5 unsaved-dialog RC2`.

- [ ] **Step 4: Run full verification and build**

Run `:app:testLegacyReleaseUnitTest :app:assembleLegacyRelease --rerun-tasks` offline. Require zero test failures and a successful release build.

- [ ] **Step 5: Verify and install exact APK bytes**

Use `aapt` and `apksigner` to verify package, version, SDKs, v2 signer, and required branding resources. Compute size and SHA-256. Install with `install_apk_vivo.py` without uninstalling or clearing data, then pull the installed `base.apk` and require the same size and SHA-256.

- [ ] **Step 6: Archive candidate evidence**

Copy the exact built APK and record source commit, package/version, signer, test totals, device metadata, install method, installed hash match, interaction contract, and pending-baseline status in `manifest.txt`. Commit the archive. Do not create or move a baseline pointer or tag.

# Folder-Scoped Local Game Launch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Resolve and persist each offline game's launch executable strictly within that game's own folder, preserving GTA V's launcher while allowing other games to launch their own executables.

**Architecture:** Add a pure Kotlin `LocalGameExecutableResolver` that validates persisted relative paths, scans a bounded directory tree, filters non-game tools, and deterministically scores candidates. Route container creation/repair, pre-launch validation, and XServer command generation through the same folder-scoped result so a stale cross-game path can never survive to Wine.

**Tech Stack:** Kotlin, Android/Compose application, JUnit 4 JVM tests, Gradle Android plugin, ADB, vivo package installer helper.

## Global Constraints

- Never resolve an executable from the shared `/storage/emulated/0/GameFoldPC/game` root for an individual game.
- Preserve V0.6 Proton, LSFG-VK, HUD, language, controller, public-resource, and advanced-settings behavior.
- Preserve installed app data and storage authorization during installation.
- Build `com.gamefold.pc.v04localshell` as `versionName=0.6.4-v06-folder-scoped-launch`, `versionCode=10049`.
- Do not modify either game's files during verification.

---

### Task 1: Pure folder-scoped executable resolver

**Files:**
- Create: `app/src/main/java/app/gamenative/utils/LocalGameExecutableResolver.kt`
- Create: `app/src/test/java/app/gamenative/utils/LocalGameExecutableResolverTest.kt`

**Interfaces:**
- Consumes: a game root `File` and optional persisted relative path.
- Produces: `LocalGameExecutableResolver.resolve(gameFolder: File, persistedRelativePath: String? = null): String?`.

- [ ] **Step 1: Write failing behavior tests**

Create literal temporary-folder fixtures proving:

```kotlin
assertEquals("Grand Theft Auto V/GTAVLauncher.exe", resolve(gtaFolder))
assertEquals("黑神话像素版1.0 电脑版.exe", resolve(blackMythFolder))
assertEquals("黑神话像素版1.0 电脑版.exe", resolve(blackMythFolder, "../Grand Theft Auto V/GTAVLauncher.exe"))
assertEquals("Custom.exe", resolve(folder, "Custom.exe"))
```

Also assert that `unins000.exe`, `setup.exe`, `CrashReporter.exe`, and a path escaping the folder cannot be selected.

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```bash
./gradlew :app:testLegacyDebugUnitTest --tests app.gamenative.utils.LocalGameExecutableResolverTest --offline --no-daemon --console=plain
```

Expected: compilation failure because `LocalGameExecutableResolver` does not exist.

- [ ] **Step 3: Implement the minimal pure resolver**

Implement canonical containment validation, depth-four scanning, ignored-name filtering, deterministic scoring, forward-slash relative paths, and stable tie-breaking. Do not reference Android framework or `BuildConfig` from this class.

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the Step 2 command. Expected: all resolver tests pass with zero failures.

- [ ] **Step 5: Commit the resolver**

```bash
git add app/src/main/java/app/gamenative/utils/LocalGameExecutableResolver.kt app/src/test/java/app/gamenative/utils/LocalGameExecutableResolverTest.kt
git commit -m "fix: resolve local games inside their own folders"
```

### Task 2: Container repair and launch-path integration

**Files:**
- Modify: `app/src/main/java/app/gamenative/utils/CustomGameScanner.kt`
- Modify: `app/src/main/java/app/gamenative/utils/ContainerUtils.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Test: `app/src/test/java/app/gamenative/utils/LocalGameExecutableResolverTest.kt`

**Interfaces:**
- Consumes: `LocalGameExecutableResolver.resolve(...)` from Task 1.
- Produces: a validated executable saved in the current container and used directly by XServer command generation.

- [ ] **Step 1: Add a failing stale-path regression case**

Add a test where the library root contains sibling GTA and Black Myth folders, but resolving Black Myth with the persisted GTA relative path must return Black Myth's own executable. Verify the test fails if resolution is accidentally performed against the library root.

- [ ] **Step 2: Run the focused test and verify RED**

Temporarily exercise the existing shared-root path behavior through the integration seam; expected result is the GTA path instead of the Black Myth path.

- [ ] **Step 3: Replace GTA-root special cases with the resolver**

- Make `CustomGameScanner.findGameFoldPcDefaultExecutable(folderPath)` delegate to the folder-scoped resolver for compatibility with existing callers.
- Make `CustomGameScanner.getLaunchExecutable(container)` return the folder-scoped result.
- Remove the `GameFoldPcResourceRoot.gameRoot()` executable overwrite from `ContainerUtils.getOrCreateContainer()`.
- After mapping A: to the exact game folder, resolve the persisted value; save only when a valid replacement differs.
- In XServer custom-game command generation, resolve using the A-drive folder regardless of whether the stored path is empty; save the repair and launch the resolved value.

- [ ] **Step 4: Run focused and surrounding tests**

Run:

```bash
./gradlew :app:testLegacyDebugUnitTest --tests 'app.gamenative.utils.LocalGameExecutableResolverTest' --tests 'app.gamenative.utils.CustomGameScannerFirstScanTest' --offline --no-daemon --console=plain
```

Expected: zero failures.

- [ ] **Step 5: Run the full legacy unit-test task**

```bash
./gradlew :app:testLegacyDebugUnitTest --offline --no-daemon --console=plain
```

Expected: zero failures.

- [ ] **Step 6: Commit integration**

```bash
git add app/src/main/java/app/gamenative/utils/CustomGameScanner.kt app/src/main/java/app/gamenative/utils/ContainerUtils.kt app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt app/src/test/java/app/gamenative/utils/LocalGameExecutableResolverTest.kt
git commit -m "fix: isolate executable paths per local game"
```

### Task 3: Build, install, and device verification

**Files:**
- Build artifact: `app/build/outputs/apk/legacy/release/app-legacy-release.apk`

**Interfaces:**
- Consumes: Task 2's validated launch flow and the existing public GameFold PC resource payload.
- Produces: an installed V0.6.4 candidate plus logs proving per-game path isolation.

- [ ] **Step 1: Build the release candidate**

```bash
./gradlew :app:assembleLegacyRelease --offline --no-daemon --console=plain \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  -PGAMEFOLD_PC_APPLICATION_ID=com.gamefold.pc.v04localshell \
  -PGAMEFOLD_PC_VERSION_CODE=10049 \
  -PGAMEFOLD_PC_VERSION_NAME=0.6.4-v06-folder-scoped-launch
```

Expected: Gradle exit code 0 and a newly generated APK.

- [ ] **Step 2: Verify APK identity before installation**

Use `aapt dump badging`, `apksigner verify --verbose`, `stat`, and `shasum -a 256`. Confirm package, version, signature validity, size, build timestamp, and SHA-256.

- [ ] **Step 3: Preserve data and install with the vivo helper**

```bash
python3 /Users/11085429/.codex/skills/install-apk-vivo/scripts/install_apk_vivo.py \
  /absolute/path/to/app-legacy-release.apk
```

Do not uninstall and do not clear package data or permissions.

- [ ] **Step 4: Verify installed identity**

Use `adb shell dumpsys package com.gamefold.pc.v04localshell` and pull the installed base APK. Confirm installed `versionName`, `versionCode`, update time, signature compatibility, and SHA-256 match the built artifact.

- [ ] **Step 5: Verify Black Myth launch**

Clear logcat only, ask/allow the user to tap Black Myth, then verify:

- A: maps to `/GameFoldPC/game/黑神话像素版`;
- the command contains `A:\\黑神话像素版1.0 电脑版.exe`;
- the command does not contain `GTAVLauncher.exe`;
- Wine does not show `Path not found`.

- [ ] **Step 6: Verify GTA V regression**

Return to the library, launch GTA V, and verify its command still contains `A:\\Grand Theft Auto V\\GTAVLauncher.exe`, with its A: drive mapped only to the GTA V folder.

- [ ] **Step 7: Record candidate commit and working-tree state**

Run `git status --short`, `git log -4 --oneline`, and retain the artifact hash and installed identity in the handoff.

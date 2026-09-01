# GameFold PC Offline Hard Cut Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a smaller local-only GameFold PC candidate by removing online account, recommendation, update, analytics, and download surfaces without changing the verified local game launch path.

**Architecture:** Keep the existing local-shell runtime and make the app entry path structurally local-only. Remove online manifest components, source packages, dependencies, and catalog assets only after their call sites are removed; retain the public-resource validator and local runtime installer that reads existing files. Use a repository surface test plus existing local runtime tests as the deletion guard.

**Tech Stack:** Kotlin, Android Manifest, Gradle Kotlin DSL, R8/resource shrinking, JUnit/Robolectric, adb, vivo APK installer.

## Global Constraints

- Preserve the frozen 0.4.5 APK and all prior 0.4.x commits as rollback points.
- Candidate package stays `com.gamefold.pc.v04localshell`; build versionCode `10036` and versionName `0.5.0-v04-local-shell-offline-cut`.
- Only `/storage/emulated/0/GameFoldPC/game` is scanned for games and only public runtime resources are accepted.
- Do not remove advanced settings, local container editing, Bionic/Proton/DXVK/Turnip, controller input, LSFG-VK, local artwork, or the GTA5 launch path.
- Do not add a network fallback or replace removed downloads with bundled copies.

---

### Task 1: Add the failing offline-surface guard

**Files:**
- Create: `packaging/test_local_shell_surface.sh`
- Test: `app/src/test/java/app/gamenative/product/ProductModePolicyTest.kt` (extend existing test only if required)

**Interfaces:**
- The shell guard accepts the repository root as its first argument and exits non-zero while any forbidden manifest component, permission, dependency, or catalog asset remains.

- [ ] **Step 1: Write the failing shell guard**

```bash
#!/usr/bin/env bash
set -euo pipefail
ROOT="${1:?repository root required}"
MANIFEST="$ROOT/app/src/main/AndroidManifest.xml"
BUILD="$ROOT/app/build.gradle.kts"

! rg -n 'GOGOAuthActivity|EpicOAuthActivity|AmazonOAuthActivity|service\.gog\.GOGService|service\.epic\.EpicService|service\.amazon\.AmazonService' "$MANIFEST"
! rg -n 'android\.permission\.INTERNET|ACCESS_NETWORK_STATE|ACCESS_WIFI_STATE|REQUEST_INSTALL_PACKAGES' "$MANIFEST"
! rg -n 'posthog|auth0\.android|androidx\.browser:browser' "$BUILD"
for asset in graphics_driver_download.json core_drivers_download.json dxwrapper_download.json wincomponents_download.json container_files_download.json; do
  test ! -e "$ROOT/app/src/main/assets/$asset"
done
test -e "$ROOT/app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt"
test -e "$ROOT/app/src/main/java/app/gamenative/utils/LsfgDllLocator.kt"
```

- [ ] **Step 2: Run the guard and confirm it fails**

Run: `bash packaging/test_local_shell_surface.sh "$PWD"`

Expected: FAIL because the current manifest, Gradle dependencies, and catalog assets still contain online surfaces.

- [ ] **Step 3: Commit the failing guard**

```bash
git add packaging/test_local_shell_surface.sh
git commit -m "test: guard offline local shell surface"
```

### Task 2: Cut online startup and navigation references

**Files:**
- Modify: `app/src/main/java/app/gamenative/PluviaApp.kt`
- Modify: `app/src/main/java/app/gamenative/MainActivity.kt`
- Modify: `app/src/main/java/app/gamenative/ui/PluviaMain.kt`
- Modify: `app/src/main/java/app/gamenative/ui/HomeScreen.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Local shell startup initializes preferences, local resource paths, container migration, and launch readiness only.
- `PluviaMain` keeps `OfflineLibraryScreen`, local launch callbacks, advanced settings, and XServer navigation; it no longer starts/reconnects account services or displays connection/login/update surfaces.

- [ ] **Step 1: Remove online initialization and lifecycle callbacks**

Delete local-shell-unreachable calls/imports for `NetworkMonitor`, PostHog, Play Integrity warm-up, GOG/Amazon path migration, platform service restarts, and analytics foreground/background events. Keep `DownloadService.populateDownloadService` only if the retained local path code still reads its base directory.

- [ ] **Step 2: Remove online manifest declarations and permissions**

Delete OAuth activities, GOG/Epic/Amazon service declarations, Steam foreground-service declaration when no retained local code starts it, network/update/install permissions, and the `nxm://` intent filter. Keep the main activity, launcher alias, local file provider, and permissions required for external public storage and runtime input/audio.

- [ ] **Step 3: Run the surface guard and compile to expose remaining references**

Run: `bash packaging/test_local_shell_surface.sh "$PWD"`

Expected: the guard still reports source/dependency/catalog items; compile output identifies exact remaining online references to remove in Task 3.

### Task 3: Delete online source/dependencies/assets and retain local runtime code

**Files:**
- Delete only after call sites are removed: `app/src/main/java/app/gamenative/ui/screen/auth/**`, `ui/screen/login/**`, online downloads/recommendation screens and view models, platform service packages, update/analytics helpers, and online-only data/repository files.
- Modify: `app/build.gradle.kts`
- Delete: online catalog JSON assets listed in the guard and any online-only asset directories proven unused by the local runtime.
- Modify: `app/src/main/java/app/gamenative/data/GameSource.kt`, Room/Hilt wiring, and `app/src/main/java/app/gamenative/ui/model/MainViewModel.kt` only where compilation requires removing platform-only branches.

**Interfaces:**
- The local library continues to produce `CUSTOM_GAME_<id>` items and `preLaunchApp` continues through local manifest validation, ImageFS installation, container activation, and `onSuccess`.
- No retained code imports an online service, OAuth activity, HTTP downloader, or recommendation repository.

- [ ] **Step 1: Remove browser/auth/analytics dependencies and verify dependency graph**

Remove the `androidx.browser`, `com.auth0.android:jwtdecode`, PostHog, JavaSteam, and online-only library dependencies only when `rg` confirms no retained local source references. Keep libraries used by the container/runtime/input stack.

- [ ] **Step 2: Delete source trees and online data files in small groups**

After each group, run `./gradlew :app:compileLegacyReleaseKotlin --offline --no-daemon --console=plain -q` and fix only compile errors caused by that group. Do not delete `LsfgVkManager`, `CustomGameScanner`, `GameFoldPcResourceRoot`, `LocalGameArtworkResolver`, `BionicProgramLauncherComponent`, or local runtime asset directories.

- [ ] **Step 3: Make local pre-launch a hard local path**

Ensure local shell never enters `UpdateChecker`, `ManifestInstaller`, online `LaunchDependencies`, cloud-save, workshop, or platform-session branches. Missing public files must return a local error, not trigger a download.

- [ ] **Step 4: Run the guard and targeted tests**

Run: `bash packaging/test_local_shell_surface.sh "$PWD"`

Run: `ANDROID_HOME='/Users/11085429/Library/Android/sdk' JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' GRADLE_USER_HOME='/Users/11085429/.gradle' ./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.product.ProductModePolicyTest --tests app.gamenative.utils.LocalGameArtworkResolverTest --tests app.gamenative.utils.CustomGameScannerFirstScanTest --offline --no-daemon --console=plain -q`

Expected: guard passes and all selected tests pass.

### Task 4: Build and measure the hard-cut candidate

**Files:**
- Create: `releases/GameFold-PC-V0.5.0-OfflineCut-10036.apk`
- Modify only if needed: packaging metadata/version fields

- [ ] **Step 1: Build the legacy release**

```bash
ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
GRADLE_USER_HOME='/Users/11085429/.gradle' \
./gradlew :app:assembleLegacyRelease :app:testLegacyReleaseUnitTest \
  --offline --no-daemon --console=plain -q \
  -PGAMEFOLD_PC_APPLICATION_ID=com.gamefold.pc.v04localshell \
  -PGAMEFOLD_PC_VERSION_CODE=10036 \
  -PGAMEFOLD_PC_VERSION_NAME=0.5.0-v04-local-shell-offline-cut \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  -PGAMEFOLD_PC_LSFG_LOCAL_DLL_PATH=/storage/emulated/0/GameFoldPC/lsfg/inputtest/Lossless.dll
```

- [ ] **Step 2: Verify metadata, signature, and size**

Run `aapt dump badging`, `apksigner verify --verbose`, `shasum -a 256`, `stat -f%z`, and `unzip -l`. Compare bytes and top-level entries against `releases/GameFold-PC-V0.4.5-LocalShell-Artwork-SizeFix-10035.apk`; report both absolute and percentage reduction.

- [ ] **Step 3: Commit source and packaging changes**

```bash
git add app packaging
git commit -m "feat: remove online features from local shell"
```

### Task 5: Install and verify the device behavior

- [ ] **Step 1: Uninstall the prior candidate and install 10036** using `install_apk_vivo.py`.
- [ ] **Step 2: Verify installed package, versionCode, lastUpdateTime, and pulled APK hash.**
- [ ] **Step 3: Temporarily grant `MANAGE_EXTERNAL_STORAGE`, launch, and verify the library shows GTA5 artwork without pull-to-refresh.**
- [ ] **Step 4: Long-press the card, open advanced/container settings, launch GTA5, and collect logcat for fatal exceptions.**
- [ ] **Step 5: Reset the app-op to `default` and force-stop the app so the user can perform a clean first-run authorization test.**
- [ ] **Step 6: Report APK size delta, retained runtime surfaces, test results, and rollback APK path.**

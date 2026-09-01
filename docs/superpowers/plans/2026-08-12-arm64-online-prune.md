# GameFold PC ARM64 and Online Dependency Prune Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and install an arm64-only GameFold PC candidate that removes unused native libraries and packaged online/Steam client code without changing the verified local GTA5 runtime.

**Architecture:** Treat the signed APK as the product boundary. Add an executable artifact guard first, then narrow JNI packaging and local-shell dependency packaging behind the existing `GAMEFOLD_PC_LOCAL_SHELL` build switch. Preserve the Wine-side offline compatibility layer and validate the real device launch path after every artifact-level removal.

**Tech Stack:** Android Gradle Plugin, Kotlin, R8, JNI/ELF, JUnit, shell artifact tests, `dexdump`, `aapt2`, `apksigner`, adb, vivo installer helper.

## Global Constraints

- Package: `com.gamefold.pc.v04localshell`.
- Candidate: versionCode `10038`, versionName `0.5.1-v04-local-shell-arm64-pruned`.
- Install only as an in-place upgrade; never uninstall, clear data, or reset file authorization.
- Keep local scan, artwork, advanced settings, public-directory runtime resources, GTA5 launch, controller input, Bionic/Proton/DXVK/Turnip/FEX, and LSFG-VK.
- Keep `ACCESS_NETWORK_STATE`; remove `INTERNET` and all account/store network clients.
- Preserve `10037` as rollback.

---

### Task 1: Add the failing APK prune guard

**Files:**
- Create: `packaging/test_pruned_local_shell_apk.sh`

**Interfaces:**
- Consumes an APK path and Android SDK build-tools path.
- Returns non-zero when the APK contains a non-arm64 ABI, forbidden native library, JavaSteam/platform service class, online permission/component, or lacks LSFG/runtime entries.

- [ ] Write the artifact guard using `unzip`, `aapt2`, and `dexdump`.
- [ ] Run it against `GameFold-PC-V0.5.0-OfflineCut-R2-10037.apk` and verify it fails because 32-bit JNI, `libpatchelf.so`, `libopenxr_loader.so`, and JavaSteam are present.
- [ ] Keep the failing guard as the executable specification for the candidate.

### Task 2: Prune Android ABI and unused native libraries

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `packaging/test_pruned_local_shell_apk.sh` only if the real APK layout requires a parser correction.

**Interfaces:**
- Local-shell release packages only `lib/arm64-v8a/**`.
- Local-shell release excludes `**/libpatchelf.so` and `**/libopenxr_loader.so`.

- [ ] Apply arm64-only ABI filtering to the local-shell candidate.
- [ ] Add local-shell JNI excludes for the two unused libraries.
- [ ] Build the candidate and run the artifact guard; expect remaining failure only from packaged online classes/dependencies.

### Task 3: Remove packaged Steam and online dependency payloads

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/proguard-rules.pro`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/app/gamenative/PluviaApp.kt`
- Modify: `app/src/main/java/app/gamenative/ui/PluviaMain.kt`
- Modify only as required by compilation: local-shell startup/view-model/runtime call sites that retain online service roots.

**Interfaces:**
- No `in.dragonbra.javasteam`, `app.gamenative.service.SteamService`, `service.gog`, `service.epic`, or `service.amazon` class is packaged in local-shell DEX.
- No JavaSteam/depot downloader, SpongyCastle, DNS-over-HTTPS, Play Integrity, media playback, QR login, or online-only image decoder payload is packaged by the local-shell dependency configuration.
- Local custom-game launch reaches `preLaunchApp` without account, cloud, or download branches.

- [ ] Remove R8 keep rules that force JavaSteam and SpongyCastle into the artifact.
- [ ] Make online dependencies absent from the local-shell runtime configuration while retaining compile-time compatibility only where unavoidable during source separation.
- [ ] Remove manifest and startup roots that keep platform services reachable.
- [ ] Compile, resolve only local-shell compile failures, build, and run the artifact guard until it passes.
- [ ] Run targeted offline, artwork, scanner, and LSFG tests.

### Task 4: Build, measure, and verify candidate 10038

**Files:**
- Create: `releases/GameFold-PC-V0.5.1-Arm64-Pruned-10038.apk`

**Interfaces:**
- The release artifact has verified metadata, signature, SHA-256, and size comparison against `10037`.

- [ ] Assemble `legacyRelease` with the candidate properties.
- [ ] Run the surface guard and APK prune guard.
- [ ] Verify `aapt2` metadata/permissions, `apksigner`, SHA-256, ZIP inventory, DEX prefixes, and exact byte delta.

### Task 5: In-place device validation

**Files:** none.

**Interfaces:**
- The installed candidate retains package data and file authorization and reaches a visible GTA5 Wine window.

- [ ] Record pre-install package/app-op state.
- [ ] Install with the vivo helper using replace mode.
- [ ] Verify versionCode, versionName, install timestamp, app-op, and pulled APK hash.
- [ ] Launch the app, confirm GTA5 and artwork, open advanced/container settings, then launch GTA5.
- [ ] Capture process/log/screenshot evidence and report any boundary not verified automatically.

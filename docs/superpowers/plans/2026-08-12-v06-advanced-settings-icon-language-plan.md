# GameFold PC V0.6 Advanced Settings, NS Icon, and Per-Game Language Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the Advanced Settings crash, use the exact full-frame GameFold NS 2.9 launcher icon, and make every game container default to Simplified Chinese.

**Architecture:** Keep the V0.6 baseline frozen and make three isolated candidate changes: a preview-safe notification vector, a full-frame launcher vector, and a versioned per-game container-default applicator. `ContainerUtils` saves only when the applicator reports a migration.

**Tech Stack:** Android/Kotlin, Jetpack Compose, Winlator `Container`, Robolectric/JUnit4, Android resource XML, Gradle, ADB.

## Global Constraints

- Preserve package `com.gamefold.pc.v04localshell` and install in place.
- Do not clear app data or storage authorization.
- Do not change GTA5 launch, Proton, LSFG-VK runtime, input, or FPS-only HUD code beyond the defaults applicator.
- Default and alternate launcher styles must be identical to the installed GameFold NS 2.9 full-frame vector.

---

### Task 1: Per-game defaults schema v2

**Files:**
- Create: `app/src/main/java/app/gamenative/product/GameFoldPcPerGameDefaults.kt`
- Create: `app/src/test/java/app/gamenative/product/GameFoldPcPerGameDefaultsTest.kt`
- Modify: `app/src/main/java/app/gamenative/product/GameFoldPcDefaults.kt`
- Modify: `app/src/main/java/app/gamenative/utils/ContainerUtils.kt`

**Interfaces:**
- Produces: `GameFoldPcPerGameDefaults.applyTo(container: Container): Boolean`.
- Returns `true` only when the container was mutated and must be saved.

- [ ] Write a failing test that starts with schema `1`, English, HUD off, and LSFG off; assert schema `2`, `schinese`, `zh_CN.UTF-8`, HUD on, and LSFG on after applying.
- [ ] Assert a second call returns `false` and leaves the already migrated state unchanged.
- [ ] Run the focused test and confirm it fails because schema `1` is still current and no applicator exists.
- [ ] Add the minimal applicator, increment `DEFAULTS_VERSION` to `2`, and call it from `ContainerUtils`.
- [ ] Run the focused product/container tests and commit.

### Task 2: Advanced Settings preview and canonical NS icon

**Files:**
- Create: `app/src/main/res/drawable/gamefold_system_icon.xml`
- Modify: `app/src/main/java/app/gamenative/ui/screen/settings/SettingsGroupInterface.kt`
- Modify: `app/src/main/res/mipmap-anydpi/ic_launcher*.xml`
- Modify: `app/src/main/res/mipmap-anydpi-v26/ic_launcher*.xml`
- Modify: `app/src/main/res/mipmap-anydpi-v33/ic_launcher*.xml`
- Modify: `packaging/test_gamefold_pc_icon_defaults.sh`

**Interfaces:**
- Consumes: canonical GameFold NS 2.9 full-frame vector geometry.
- Produces: preview-safe VectorDrawable resources and identical default/alternate launcher icons.

- [ ] Add an artifact/resource guard that rejects the scaled `0.56` foreground and requires the canonical full-frame vector for all launcher variants.
- [ ] Run the guard against V0.6.2/current source and confirm the expected failure.
- [ ] Add `gamefold_system_icon`, make every launcher variant use the full-frame vector, and pass `gamefold_notification_mark` to both preview cards.
- [ ] Run the resource guard and targeted unit tests, then commit.

### Task 3: Build, install, and real-device acceptance

**Files:**
- Modify: `app/build.gradle.kts`
- Produce: `releases/GameFold-PC-V0.6.3-Advanced-Icon-Language-10048.apk`

**Interfaces:**
- Consumes: Tasks 1-2 implementation.
- Produces: an in-place installable arm64-only release candidate.

- [ ] Bump version name/code and assemble the release APK offline.
- [ ] Run targeted unit tests, icon/default guards, and the pruned-local-shell APK guard.
- [ ] Install with the vivo installer helper using `-r`; do not uninstall.
- [ ] Verify package, version, code, ABI, SHA-256, install time, and storage app-op.
- [ ] Reproduce Home > Advanced Settings and assert the app remains alive with no fatal log.
- [ ] Compare the launcher icon visually with installed GameFold NS 2.9.
- [ ] Open GTA5 settings and assert Simplified Chinese, LSFG-VK on, and HUD on; launch GTA5 and confirm LSFG plus FPS-only HUD.
- [ ] Stop the test process and report any unrelated historical-suite failures separately.

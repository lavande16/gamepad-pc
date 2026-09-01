# GameFold PC Icon, Social Links, and Back-Save Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce and install a verified `0.8.4-ICON-BACKSAVE-RC1` APK with the supplied PC icon, restored About social icons, `GAMEFOLD PC` home title, and automatic container saving on Back.

**Architecture:** Keep all behavior inside the existing shared `ContainerConfigDialog` and existing GameFold PC presentation components. Add one pure Back-decision helper for deterministic testing, use one canonical in-app icon bitmap, and update existing launcher/splash resources without introducing dependencies.

**Tech Stack:** Android, Kotlin, Jetpack Compose, Android resources, Robolectric, JUnit 4, Gradle 8.12.1.

## Global Constraints

- Start from `gamefold-pc-v0.8.3-baseline`; do not mutate the baseline tag or archived baseline.
- Candidate identity is `0.8.4-ICON-BACKSAVE-RC1` / `10140` / `com.gamefold.pc`.
- Preserve package signature, app data, permissions, games, containers, and external `GameFoldPC` payloads.
- Do not modify GameFold NS or FC/NES sources.

---

### Task 1: Regression tests and candidate identity

**Files:**
- Modify: `app/src/test/java/app/gamenative/ui/component/dialog/ContainerConfigDialogContainerUpdateTest.kt`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/GameFoldAboutPageInflationTest.kt`
- Modify: `app/src/test/java/app/gamenative/ui/screen/library/OfflineLibraryGeometryTest.kt`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`

**Interfaces:**
- Consumes: existing `ContainerData`, Android resources, and GameFold wordmark renderer.
- Produces: failing coverage for `containerConfigBackAction`, About social visibility, the `C` glyph/home wordmark width, and RC1 version identity.

- [ ] **Step 1: Add Back-decision tests**

Add tests asserting that identical `ContainerData` returns `DISMISS` and changed data returns `SAVE`.

- [ ] **Step 2: Add About social resource tests**

Inflate the real layout and assert both social buttons are visible and have non-null drawables.

- [ ] **Step 3: Add wordmark and candidate identity tests**

Assert the literal `C` 5x7 rows, the 65-unit width of `GAMEFOLD PC`, and BuildConfig identity `0.8.4-ICON-BACKSAVE-RC1` / `10140`.

- [ ] **Step 4: Verify RED**

Run the four targeted Legacy tests. Expected: failures for missing social assets/visibility, missing Back helper, missing `C` glyph API, and old version identity.

### Task 2: Restore About social icons

**Files:**
- Create: `app/src/main/res/drawable-nodpi/about_xiaohongshu.webp`
- Create: `app/src/main/res/drawable-nodpi/about_dribbble.webp`
- Modify: `app/src/main/res/layout/fragment_about.xml`

**Interfaces:**
- Consumes: existing `button_xiaohongshu` and `button_dribbble` click handlers.
- Produces: two visible, focusable About social buttons using the canonical NS images.

- [ ] **Step 1: Copy canonical NS WebP assets**

Copy the two byte-identical files from `../gamefold-auto-assembly-v1/src/android/app/src/main/res/drawable-nodpi/`.

- [ ] **Step 2: Restore the social row**

Remove `android:visibility="gone"` from the existing row without changing its layout or focus order.

- [ ] **Step 3: Run the About test**

Run `GameFoldAboutPageInflationTest`; expected PASS.

### Task 3: Automatic save on Back

**Files:**
- Modify: `app/src/main/java/app/gamenative/ui/component/dialog/ContainerConfigDialog.kt`
- Test: `app/src/test/java/app/gamenative/ui/component/dialog/ContainerConfigDialogContainerUpdateTest.kt`

**Interfaces:**
- Produces: `ContainerConfigBackAction { SAVE, DISMISS }` and `containerConfigBackAction(initialConfig, currentConfig)`.
- Consumes: existing `onSave(ContainerData)` and `onDismissRequest()` callbacks.

- [ ] **Step 1: Implement the pure decision**

Return `SAVE` only when `initialConfig != currentConfig`, otherwise `DISMISS`.

- [ ] **Step 2: Route every Back path through one callback**

Replace the discard-confirmation branch with an exit callback that invokes `onSave(config)` for `SAVE` and `onDismissRequest()` for `DISMISS`. Use it for the toolbar action and dialog `onDismissRequest`.

- [ ] **Step 3: Run the Back-decision test**

Run the targeted test; expected PASS.

### Task 4: PC icon and home title

**Files:**
- Create: `app/src/main/res/drawable-nodpi/gamefold_pc_app_icon.png`
- Modify: `app/src/main/res/values/gamefold_about_resources.xml`
- Modify: `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPixelWordmark.kt`
- Modify: `app/src/main/res/drawable/gamefold_splash_icon.xml`
- Modify: `app/src/main/res/mipmap-anydpi*/ic_launcher*.xml`
- Modify: `app/src/main/res/mipmap-*/ic_launcher*.webp`
- Test: `app/src/test/java/app/gamenative/ui/screen/library/OfflineLibraryGeometryTest.kt`

**Interfaces:**
- Produces: `gameFoldPixelGlyphRows('C')`, `gameFoldPixelWordmarkUnits("GAMEFOLD PC") == 65`, and consistent supplied PC icon resources.
- Consumes: supplied `/Users/11085429/Desktop/其他文件/vibe coding/GAMEFOLD非AI素材/UI素材/PC icon/icon-512.png`.

- [ ] **Step 1: Add testable wordmark geometry and C glyph**

Use the same glyph map and width formula as the Canvas renderer. Render `GAMEFOLD PC` explicitly in the shared home/quick-start header and increase its slot width from 124dp to 168dp so the 18dp-high glyphs keep their original pixel size.

- [ ] **Step 2: Install the canonical in-app bitmap**

Copy `icon-512.png` to `gamefold_pc_app_icon.png` and point `gamefold_brand_mark` and the home header image to it.

- [ ] **Step 3: Update launcher and splash resources**

Generate density-specific legacy launcher files from the 512 px master, keep default/round/alternate aliases visually consistent, and update the existing vector/splash colors to the supplied artwork.

- [ ] **Step 4: Run wordmark and About tests**

Run both targeted tests; expected PASS.

### Task 5: Build, archive, install, and verify

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `releases/candidates/V0.8.4-ICON-BACKSAVE-RC1/README.md`
- Create: `releases/candidates/V0.8.4-ICON-BACKSAVE-RC1/BASELINE.json`
- Create: `releases/candidates/V0.8.4-ICON-BACKSAVE-RC1/GameFold-PC-V0.8.4-ICON-BACKSAVE-RC1-10140.apk`

**Interfaces:**
- Produces: signed Legacy release APK and matching metadata/hash evidence.

- [ ] **Step 1: Update candidate defaults**

Set `GAMEFOLD_PC_VERSION_NAME` fallback to `0.8.4-ICON-BACKSAVE-RC1` and `GAMEFOLD_PC_VERSION_CODE` fallback to `10140`.

- [ ] **Step 2: Run targeted and full verification**

Run targeted tests, then fresh `:app:testLegacyReleaseUnitTest` and `:app:assembleLegacyRelease` with the Android Studio JBR and offline Gradle cache.

- [ ] **Step 3: Archive and inspect the APK**

Copy the built APK into the candidate directory; record byte size, SHA-256, `aapt dump badging`, and `apksigner verify --print-certs` evidence.

- [ ] **Step 4: Install in place**

Confirm one connected device with `adb devices -l`, then install using `install_apk_vivo.py` without clearing data.

- [ ] **Step 5: Verify installed identity**

Read `dumpsys package com.gamefold.pc`, pull the installed base APK, and require its SHA-256 to match the archived candidate exactly.

# GamePad PC Home Resize Button Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an immediate two-state display-size toggle immediately left of Settings on the GamePad PC home header.

**Architecture:** Port the proven Resize app's small Binder-backed display-size core into a focused `app.gamenative.displayresize` package, then call it from the existing Compose header. Keep header ordering in a pure policy type so controller order and UI order share one tested contract.

**Tech Stack:** Android/Kotlin/Java, Jetpack Compose, Android Binder, JUnit/Robolectric, Gradle legacy release variant.

**Spec:** `docs/superpowers/specs/2026-08-24-gamepad-pc-home-resize-button-design.md`

## Global Constraints

- Exact source baseline: `com.gamepad.pc`, `1.7 / 10015`, archived SHA-256 `7389c647c8dd95e20dbc57b4cf7ceda232513a316a43741a59c2234751a84a6a`.
- Toggle only `1440 x 2304` and `1080 x 1920`.
- Reuse the proven Resize Binder mechanism and its verification logic.
- No user-visible prompt or status message on click or failure.
- New candidate identity: `1.7 / 10016`.
- Preserve unrelated untracked release directories and all non-GamePad products.
- Default external-display swap on for GamePad PC global and new-game templates without changing generic products.
- A successful tablet-size toggle must persist the paired landscape game size to the global template and every existing game.

---

### Task 1: Display-size core

**Files:**
- Create: `app/src/main/java/app/gamenative/displayresize/DisplayDimensions.java`
- Create: `app/src/main/java/app/gamenative/displayresize/DisplaySizeSnapshot.java`
- Create: `app/src/main/java/app/gamenative/displayresize/CommandExecutor.java`
- Create: `app/src/main/java/app/gamenative/displayresize/CommandResult.java`
- Create: `app/src/main/java/app/gamenative/displayresize/ToggleResult.java`
- Create: `app/src/main/java/app/gamenative/displayresize/DisplaySizeParser.java`
- Create: `app/src/main/java/app/gamenative/displayresize/WindowManagerCommandExecutor.java`
- Create: `app/src/main/java/app/gamenative/displayresize/ReflectiveWindowManagerBridge.java`
- Create: `app/src/main/java/app/gamenative/displayresize/DisplaySizeController.java`
- Create: `app/src/test/java/app/gamenative/displayresize/DisplaySizeControllerTest.java`
- Create: `app/src/test/java/app/gamenative/displayresize/WindowManagerCommandExecutorTest.java`

**Interfaces:**
- Produces: `DisplaySizeController.read()` and `DisplaySizeController.toggle(DisplaySizeSnapshot, boolean)`.
- Produces: `WindowManagerCommandExecutor`, backed by the exact Resize `IWindowManager` reflection and Binder transactions.

- [ ] Write tests proving no override writes `1080x1920`, any override clears to physical size, permission denial performs no write, verification mismatch fails, and concurrent activation is rejected.
- [ ] Run the focused tests and observe compilation/test failure because the display-resize package is absent.
- [ ] Port the exact proven Resize production core, changing only its Java package.
- [ ] Run the focused tests and verify every branch passes.

### Task 2: Header action contract and visual resource

**Files:**
- Create: `app/src/main/java/app/gamenative/gamepad/GamePadHomeHeaderPolicy.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadHomeHeaderPolicyTest.kt`
- Create: `app/src/main/res/drawable/pixel_resize_button_normal.xml`
- Create: `app/src/main/res/drawable/pixel_icon_resize.xml`
- Modify: `app/src/main/res/values/pixel_colors.xml`
- Modify: `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt`

**Interfaces:**
- Produces: ordered `GamePadHomeHeaderAction` values `DUAL_SCREEN`, `RESIZE`, `SETTINGS`.
- Consumes: `DisplaySizeController(WindowManagerCommandExecutor())` and the manifest permission check.

- [ ] Write a failing policy test proving Resize is immediately before Settings with and without Dual Screen.
- [ ] Implement the minimal ordered-action policy and verify the focused test passes.
- [ ] Add the light-blue layer-list and temporary pixel corner glyph using existing Settings geometry.
- [ ] Add a background-resource parameter to `HeaderPixelAction`, render Resize immediately before Settings, and include it in mouse/touch and controller activation.
- [ ] Run focused UI policy and display-size tests.

### Task 3: Permission and candidate identity

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/product/GamePadProductContractTest.kt`

**Interfaces:**
- Produces: `com.gamepad.pc`, `versionName=1.7`, `versionCode=10016`, with `WRITE_SECURE_SETTINGS` declared.

- [ ] Update the product contract test to require versionCode 10016 and manifest permission.
- [ ] Run the product contract test and observe failure against 10015/missing permission.
- [ ] Add the permission and advance versionCode to 10016.
- [ ] Run the product contract test and verify it passes.

### Task 4: External-display default and paired game-size synchronization

**Files:**
- Modify: `app/src/main/java/app/gamenative/product/GameFoldPcDefaults.kt`
- Modify: `app/src/main/java/app/gamenative/PrefManager.kt`
- Modify: `app/src/main/java/com/winlator/container/Container.java`
- Modify: `app/src/main/java/com/winlator/container/ContainerData.kt`
- Modify: `app/src/main/java/com/winlator/container/ContainerManager.java`
- Create: `app/src/main/java/app/gamenative/gamepad/GamePadDisplayProfile.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadDisplayProfileTest.kt`
- Modify: `app/src/test/java/app/gamenative/product/GameFoldPcDefaultsTest.kt`

**Interfaces:**
- Produces: `GameFoldPcDefaults.EXTERNAL_DISPLAY_SWAP=true` for GamePad global/new-container fallbacks.
- Produces: `GamePadDisplayProfile.gameScreenSize(DisplaySizeSnapshot)` and `apply(Context, DisplaySizeSnapshot)`.

- [ ] Write failing tests for global/new-container external-display defaults, `1080x1920 -> 1920x1080`, `1440x2304 -> 2304x1440`, and persistence across every supplied container.
- [ ] Run focused tests and observe the missing/default-false failures.
- [ ] Implement GamePad-only external-display defaults in PrefManager, Container, ContainerData, and ContainerManager.
- [ ] Implement the profile synchronizer and call it only after successful Resize read-back.
- [ ] Run focused tests and verify they pass.

### Task 5: Full build, archive, and device verification

**Files:**
- Create: `releases/candidates/V1.7-HOME-RESIZE-RC1/README.md`
- Create: `releases/candidates/V1.7-HOME-RESIZE-RC1/CANDIDATE.json`
- Create: `releases/candidates/V1.7-HOME-RESIZE-RC1/GamePad-PC-V1.7-HOME-RESIZE-RC1-10016.apk`

**Interfaces:**
- Consumes: release APK output and connected vivo tablet.
- Produces: archived candidate identity, build/test evidence, installed-package evidence, and two-direction display-size evidence.

- [ ] Run the complete `:app:testLegacyReleaseUnitTest --rerun-tasks` suite.
- [ ] Run `:app:assembleLegacyRelease` and inspect badging, manifest permission, debuggable state, signature, and SHA-256.
- [ ] Archive the byte-identical APK and candidate metadata.
- [ ] Install with the vivo installer workflow, grant `WRITE_SECURE_SETTINGS`, and verify installed version/hash/install time.
- [ ] Activate the home Resize button twice on-device, verify `1080x1920` then physical `1440x2304`, and leave the device restored.
- [ ] After each activation, inspect global and every saved container screen size for the matching `1920x1080` then `2304x1440` value.

### Task 6: Header glyph alignment and 1080p game-window support

**Files:**
- Modify: `app/src/main/res/drawable/pixel_icon_resize.xml`
- Modify: `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt`
- Modify: `app/src/main/java/app/gamenative/gamepad/GamePadWindowPolicy.kt`
- Modify: `app/src/test/java/app/gamenative/gamepad/GamePadWindowPolicyTest.kt`
- Create: `app/src/test/java/app/gamenative/gamepad/GamePadResizeIconStyleTest.kt`

**Interfaces:**
- Produces: white Resize glyph and shared 2dp up/left face-centered alignment for Resize and Settings.
- Produces: exact full-screen `1920x1080` as a second supported GamePad game-window profile while retaining split-screen and picture-in-picture blocking.

- [x] Add failing tests for the white/face-centered glyph contract and the exact 1080p policy profile.
- [x] Verify the tests fail against the earlier black/whole-canvas-centered glyph and single-size policy.
- [x] Implement the shared icon offset and 1080p window profile.
- [x] Run focused tests proving both valid sizes pass and multi-window/PiP/off-by-one sizes remain blocked.
- [ ] Rebuild, archive as a superseding candidate, install, and verify the 1080p game surface is interactive without the unsupported-mode mask.

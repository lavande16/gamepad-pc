# GameFold PC V0.6.6 Global Screen Default Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the global “修改默认配置” screen size default to `1067x600` without changing existing game containers.

**Architecture:** Add a pure, versioned global-screen migration policy to `GameFoldPcDefaults`, then invoke it from the existing local-shell preference initialization transaction. Keep this migration separate from per-game defaults so existing container resolutions are untouched and later user choices remain stable.

**Tech Stack:** Kotlin, Android DataStore Preferences, JUnit, Gradle Legacy release flavor.

## Global Constraints

- Package remains `com.gamefold.pc.v04localshell`.
- Final candidate uses `versionName=0.6.6` and a new monotonically increasing `versionCode`.
- Do not modify existing `Container.screenSize` values.
- Do not uninstall the app or clear local data and storage authorization.
- Freeze the exact installed APK as the V0.6.6 baseline only after user acceptance.

---

### Task 1: Versioned global screen-size migration

**Files:**
- Modify: `app/src/main/java/app/gamenative/product/GameFoldPcDefaults.kt`
- Modify: `app/src/main/java/app/gamenative/PrefManager.kt`
- Test: `app/src/test/java/app/gamenative/product/GameFoldPcDefaultsTest.kt`

**Interfaces:**
- Produces: `GameFoldPcDefaults.migrateGlobalScreenSize(currentScreenSize, appliedVersion): String?`.
- A non-null result is persisted together with `GLOBAL_SCREEN_SIZE_DEFAULTS_VERSION`; `null` means migration already ran.

- [ ] **Step 1: Write failing policy tests**

Add literal assertions that missing and legacy `1280x720` values migrate to `1067x600`, custom values survive, and the current migration version returns `null`.

- [ ] **Step 2: Verify RED**

Run `:app:testLegacyReleaseUnitTest --tests app.gamenative.product.GameFoldPcDefaultsTest` and confirm compilation fails because the migration API is absent.

- [ ] **Step 3: Implement the minimal policy and DataStore integration**

Add an independent migration version and update only `SCREEN_SIZE` inside the local-shell initialization transaction. Do not call or modify per-game container migration.

- [ ] **Step 4: Verify GREEN and regression**

Run the focused defaults tests, then the complete supported Legacy unit-test suite. Confirm zero failures and `git diff --check` success.

- [ ] **Step 5: Commit**

Commit only the policy, integration, tests, and these two documents.

### Task 2: Build, install, and validate the baseline candidate

**Files:**
- Produce: `releases/candidates/V0.6.6/GameFold-PC-V0.6.6-Baseline-Candidate-10056.apk`

**Interfaces:**
- Consumes: the tested V0.6.6 source commit.
- Produces: a signed APK whose package, version, certificate, and SHA-256 are verified before and after installation.

- [ ] **Step 1: Build Legacy release**

Build offline with local-shell mode, package `com.gamefold.pc.v04localshell`, `versionCode=10056`, and `versionName=0.6.6`.

- [ ] **Step 2: Verify artifact metadata**

Check package/version with `aapt`, signature with `apksigner`, and compute SHA-256.

- [ ] **Step 3: Cover-install without clearing data**

Use the vivo installer helper with `adb install -r`; do not uninstall.

- [ ] **Step 4: Verify device state**

Confirm installed metadata, retained `MANAGE_EXTERNAL_STORAGE`, device-pulled APK hash equality, and the `1067x600` default-config UI.

- [ ] **Step 5: Await user acceptance**

Keep the APK as a candidate until the user approves it; then freeze the same bytes and baseline metadata without rebuilding.

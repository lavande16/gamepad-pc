# GameFold PC Cold Start and MTK Gate Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an NS-style cold-start splash that hides permission and orientation intermediates, and block MediaTek devices with the NS compatibility experience.

**Architecture:** A pure startup-route policy owns MTK, ABI, and permission priority. AndroidX SplashScreen covers window creation through the first stable Compose route, while the saved GameFold orientation is applied before `super.onCreate()`.

**Tech Stack:** Kotlin, AndroidX SplashScreen, Jetpack Compose, Robolectric/JUnit4, Gradle Android plugin.

## Global Constraints

- Start from frozen GameFold PC V1.0.1 / 10164 commit `d15eb3c01e29f2f33af9dda29f60b199d472e064`.
- Build a separate V1.0.2 / 10165 RC1 candidate; do not mutate the V1.0.1 baseline archive or tag.
- MTK route must take precedence over storage permission.
- An already-authorized device must never compose the permission route during cold start.
- No arbitrary splash timeout; release only after a stable route composes.
- Preserve existing Home, controller shell, game launch, Wine runtime, and rear-display behavior.

---

### Task 1: Pure startup compatibility and route policy

**Files:**
- Create: `app/src/main/java/app/gamenative/product/GameFoldPcStartupRoutePolicy.kt`
- Create: `app/src/test/java/app/gamenative/product/GameFoldPcStartupRoutePolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/product/GameFoldPcProcessorPolicy.kt`
- Modify: `app/src/test/java/app/gamenative/product/GameFoldPcProcessorPolicyTest.kt`

**Interfaces:**
- Consumes: `supportedAbis: Array<String>`, `socModel: String?`, `hardware: String?`, `hasStorageAccess: Boolean`.
- Produces: `GameFoldPcStartupRoutePolicy.resolve(...): GameFoldPcStartupRoute` and `GameFoldPcProcessorPolicy.isMediaTek(...)`.

- [ ] **Step 1: Write failing processor tests**

Add literal cases for `MediaTek Dimensity 9300`, `MT6989`, `mtk`, and Qualcomm `SM8650`, plus a test that ARM64 MediaTek is rejected.

- [ ] **Step 2: Verify processor tests fail for missing MTK behavior**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.product.GameFoldPcProcessorPolicyTest --offline --no-daemon --console=plain
```

Expected: failure because `isMediaTek` and SoC-aware support do not exist.

- [ ] **Step 3: Implement minimal MTK detection**

Normalize explicit model/hardware inputs with `trim().uppercase()` and match `MEDIATEK`, `DIMENSITY`, `MTK`, or `(^|[^A-Z0-9])MT\\d{3,}[A-Z0-9]*($|[^A-Z0-9])`.

- [ ] **Step 4: Verify processor tests pass**

Run the Task 1 processor test command and require zero failures.

- [ ] **Step 5: Write failing startup-route tests**

Cover these literal outputs: MTK plus missing permission → `UNSUPPORTED_MTK`; x86 plus permission → `UNSUPPORTED_ABI`; ARM64 plus missing permission → `PERMISSION_REQUIRED`; ARM64 Qualcomm plus permission → `READY`.

- [ ] **Step 6: Verify route tests fail because the policy is missing**

Run:

```bash
./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.product.GameFoldPcStartupRoutePolicyTest --offline --no-daemon --console=plain
```

- [ ] **Step 7: Implement the route enum and resolver**

Use processor order `MTK`, then ABI, then permission, then ready. Keep all inputs explicit and Android-free.

- [ ] **Step 8: Run both product tests green**

Run both Task 1 test classes and require zero failures.

### Task 2: Stable permission/processor Compose route

**Files:**
- Modify: `app/src/main/java/app/gamenative/ui/runtime/GameFoldPcRuntimeProvisionGate.kt`
- Modify: `app/src/test/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicyTest.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh-rCN/strings.xml`

**Interfaces:**
- Consumes: `GameFoldPcStartupRoutePolicy.resolve()` and `onStartupRouteReady: () -> Unit`.
- Produces: exactly one stable Home, permission, MTK, or ABI route per composition.

- [ ] **Step 1: Write a failing UI contract test**

Require the startup gate to accept `onStartupRouteReady`, resolve a `GameFoldPcStartupRoute`, and include the NS MTK copy plus FC action.

- [ ] **Step 2: Verify the UI contract test fails**

Run `GameFoldPcFrontendPolicyTest.localShellUsesStableStartupRouteAndMtkSurface` and confirm the new contract is absent.

- [ ] **Step 3: Replace the false-ready initial state**

Initialize the route synchronously from current Build and storage values, refresh on permission result and `ON_RESUME`, and call `onStartupRouteReady` from a Compose `SideEffect` after the selected route composes.

- [ ] **Step 4: Add the MTK blocked surface**

Reuse `GameFoldPcShellHost`, NS tokens, GameFold wordmark, progress row, controller input, and a browser intent for the FC website. Do not expose permission actions or Home underneath.

- [ ] **Step 5: Run targeted UI and product tests green**

Run the modified frontend test plus both Task 1 test classes.

### Task 3: Native splash and pre-create orientation

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/app/gamenative/MainActivity.kt`
- Reuse: `app/src/main/res/drawable/gamefold_splash_icon.xml`

**Interfaces:**
- Consumes: existing GameFold orientation preference and `onStartupRouteReady` callback.
- Produces: an AndroidX splash starting window held until stable Compose content.

- [ ] **Step 1: Add an AndroidX SplashScreen dependency alias**

Use `androidx.core:core-splashscreen:1.0.1`, matching the working GameFold NS reference.

- [ ] **Step 2: Add `Theme.GameFoldPc.Splash`**

Set `windowSplashScreenBackground` to `gamefold_splash_background`, `windowSplashScreenAnimatedIcon` to `gamefold_splash_icon`, and `postSplashScreenTheme` to `Theme.Pluvia`.

- [ ] **Step 3: Point `MainActivity` to the splash theme**

Keep launcher aliases and deep links unchanged.

- [ ] **Step 4: Install and hold the splash before `super.onCreate()`**

Apply `GameFoldOrientation` first for the local shell, call `installSplashScreen()`, use a boolean keep condition, then release it only through the stable-route callback passed to `GameFoldPcRuntimeProvisionGate`.

- [ ] **Step 5: Compile and run targeted tests**

Run the Task 1 and Task 2 tests plus `GameFoldOrientationTest`.

### Task 4: Candidate identity, full verification, and device installation

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `releases/candidates/V1.0.2-RC1/README.md`
- Create: `releases/candidates/V1.0.2-RC1/BASELINE.json`
- Create: `releases/candidates/V1.0.2-RC1/GameFold-PC-V1.0.2-COLD-START-MTK-RC1-10165.apk`

**Interfaces:**
- Consumes: verified release APK.
- Produces: separately named V1.0.2 / 10165 RC1 candidate and an in-place device installation.

- [ ] **Step 1: Set candidate Android identity**

Set default `versionName` to `1.0.2` and `versionCode` to `10165` without editing V1.0.1 baseline metadata.

- [ ] **Step 2: Run the full release gate**

```bash
./gradlew :app:testLegacyReleaseUnitTest :app:assembleLegacyRelease --offline --no-daemon --console=plain
```

Require zero test failures and successful APK assembly.

- [ ] **Step 3: Archive and hash the candidate**

Copy the built legacy release APK to the exact candidate filename, record SHA-256, package name, version name/code, source commit, baseline parent, and verification command in `BASELINE.json` and `README.md`.

- [ ] **Step 4: Install through the vivo workflow**

Use `/Users/11085429/.codex/skills/install-apk-vivo/scripts/install_apk_vivo.py` for an in-place update that preserves data and permissions.

- [ ] **Step 5: Verify installed identity**

Check package `com.gamefold.pc`, `1.0.2 / 10165`, install time, storage permission, pull the installed `base.apk`, compare SHA-256, and require byte equality with the candidate archive.

- [ ] **Step 6: Exercise cold-start scenarios**

Force-stop and launch with saved landscape and saved portrait modes on the connected non-MTK device. Confirm the first visible business frame is stable and no MTK false positive occurs. Record that an actual MTK phone remains required for final physical MTK acceptance if none is connected.

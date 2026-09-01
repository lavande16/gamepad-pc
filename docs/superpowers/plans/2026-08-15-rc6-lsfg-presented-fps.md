# GameFold PC V0.8.2 RC6 LSFG Presented FPS Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the GameFold PC HUD display the measured post-LSFG presentation rate without multiplying it a second time.

**Architecture:** Keep the existing guest/window FPS path for non-LSFG sessions. Add an independently readable native presentation rate to `FrameRateSampler`/`FrameRating`, then make a pure frontend policy select it only while LSFG is active.

**Tech Stack:** Kotlin, Java, Android Compose, Robolectric/JUnit, Gradle Android plugin.

## Global Constraints

- Preserve the formal V0.8.1 baseline and every prior candidate.
- Package remains `com.gamefold.pc`; RC6 is `0.8.2-rc6` / `10136`.
- Do not clamp a bad counter to the panel refresh rate or infer guest FPS by dividing callback counts.
- Install in place without clearing app data or permissions.

---

### Task 1: Separate the native presentation rate

**Files:**
- Modify: `app/src/main/java/com/winlator/widget/FrameRateSampler.java`
- Modify: `app/src/main/java/com/winlator/widget/FrameRating.java`
- Test: `app/src/test/java/com/winlator/widget/FrameRateSamplerTest.kt`
- Test: `app/src/test/java/com/winlator/widget/FrameRatingPresentedCounterTest.kt`

**Interfaces:**
- Produces: `FrameRateSampler.presentedFps(long nowMs): float`
- Produces: `FrameRating.getPresentedFPS(): float`

- [ ] **Step 1: Write failing tests** proving a fresh 60 FPS native counter remains readable even when a fresh 125 FPS legacy sample wins `currentFps`, and a stalled counter returns zero.
- [ ] **Step 2: Run the focused tests** and confirm failure because the presented-only API does not exist.
- [ ] **Step 3: Implement the minimal presented-only getters** using the existing native freshness/stall state.
- [ ] **Step 4: Run the focused tests** and confirm they pass.
- [ ] **Step 5: Commit** the independently readable presented-rate contract.

### Task 2: Select the correct HUD FPS source

**Files:**
- Modify: `app/src/main/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicy.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Test: `app/src/test/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicyTest.kt`

**Interfaces:**
- Produces: `GameFoldPcFrontendPolicy.displayedFps(lsfgActive: Boolean, guestFps: Float, presentedFps: Float): Float`

- [ ] **Step 1: Write failing policy tests** asserting LSFG active selects literal `60f` from `(guest=125f, presented=60f)` and inactive selects literal `30f` from the guest path.
- [ ] **Step 2: Run the focused tests** and confirm failure because the selection API is missing.
- [ ] **Step 3: Implement the pure selector and wire the HUD** to use it without multiplier arithmetic; expand telemetry with guest and presented rates.
- [ ] **Step 4: Run focused sampler, rating, and frontend policy tests** and confirm they pass.
- [ ] **Step 5: Commit** the HUD correction.

### Task 3: Produce and validate RC6

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC6/manifest.txt`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC6/GameFold-PC-V0.8.2-DUAL-SCREEN-RC6-10136.apk`

**Interfaces:**
- Produces: signed `legacyRelease` APK for package `com.gamefold.pc`, version `0.8.2-rc6` / `10136`.

- [ ] **Step 1: Change the version identity test** to RC6 and confirm it fails against RC5.
- [ ] **Step 2: Update the build defaults** to `10136` and `0.8.2-rc6`, then confirm the version test passes.
- [ ] **Step 3: Run** `:app:testLegacyReleaseUnitTest :app:assembleLegacyRelease` offline and require zero failures.
- [ ] **Step 4: Verify** package, version, signer, SHA-256, and test totals; archive the APK and manifest.
- [ ] **Step 5: Install in place on vivo**, verify installed metadata and pulled-APK SHA-256, then inspect `GameFoldFPS` logs for `selected=NATIVE_PRESENTED` and a shown value matching the measured presented rate.
- [ ] **Step 6: Commit** the RC6 candidate and acceptance evidence while leaving V0.8.1 unchanged.


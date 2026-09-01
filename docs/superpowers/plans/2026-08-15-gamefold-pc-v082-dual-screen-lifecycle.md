# GameFold PC V0.8.2 Dual-Screen Lifecycle Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make GameFold PC close its rear-display session whenever it leaves the foreground, prevent session resurrection on return, and remove the rear-view layout loop while preserving the V0.8.1 safe shutdown chain.

**Architecture:** Keep `VivoRearDisplayController` as the single owner of the dual-screen session. Replace the broad grace-period suppression with a bounded internal rear-launch transition that defers and later resolves leave signals. Add delayed started-count and focus-loss fallbacks, bind callbacks to the active generation, and make both ACTIVE reconciliation and rear-view layout updates idempotent.

**Tech Stack:** Kotlin, Android Activity lifecycle, JUnit 4, Gradle legacy release variant, vivo rear-display shell bridge.

---

### Task 1: Freeze the failure semantics in pure policy tests

**Files:**
- Modify: `app/src/test/java/app/gamenative/gamefold/rear/GameFoldRearDisplayPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/rear/GameFoldRearDisplayPolicy.kt`

**Step 1: Write failing tests**

Add tests proving that a leave signal is deferred only during an explicit internal rear transition, a deferred leave closes only when the front app is no longer foreground, and focus/stopped fallbacks close only after the internal transition has ended.

**Step 2: Run the focused test and verify RED**

Run `:app:testLegacyReleaseUnitTest --tests app.gamenative.gamefold.rear.GameFoldRearDisplayPolicyTest`; expect compilation/test failure because the new policy API does not exist.

**Step 3: Implement the minimum policy**

Introduce small pure functions/enums for defer/resolve/close decisions. Remove the elapsed-time grace-period rule from policy semantics.

**Step 4: Run the focused test and verify GREEN**

Run the same focused test and confirm all rear-display policy cases pass.

**Step 5: Commit**

Commit only the policy and its tests.

### Task 2: Integrate the bounded transition and background fallback

**Files:**
- Modify: `app/src/main/java/app/gamenative/gamefold/rear/VivoRearDisplayController.kt`
- Modify: `app/src/test/java/app/gamenative/gamefold/ui/GameFoldPcFrontendPolicyTest.kt`

**Step 1: Write failing controller contracts**

Add source/contract tests for the bounded transition, pending-leave resolution, started-count fallback, focus-loss close without the old `userLeaving` gate, and cancellation/generation checks.

**Step 2: Run the focused contracts and verify RED**

Run the frontend policy test class and record the missing lifecycle constructs.

**Step 3: Implement the controller state machine**

Track front started state; begin/end/timeout the internal rear transition; defer and resolve leave signals; add delayed stopped/focus fallbacks; cancel pending work on foreground recovery; no-op a healthy ACTIVE request; ensure close/reset invalidates all pending callbacks.

**Step 4: Run focused policy and contract tests and verify GREEN**

Run both rear policy and frontend policy test classes.

**Step 5: Commit**

Commit the lifecycle state-machine integration.

### Task 3: Make rear layout and immersive updates idempotent

**Files:**
- Modify: `app/src/test/java/app/gamenative/gamefold/rear/GameFoldRearDisplayPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/rear/GameFoldRearDisplayPolicy.kt`
- Modify: `app/src/main/java/app/gamenative/gamefold/rear/GameFoldRearTouchActivity.kt`

**Step 1: Write failing layout tests**

Add pure tests for landscape/portrait target geometry and whether an update is required when current geometry/rotation already equals the target.

**Step 2: Run the focused test and verify RED**

Expect failure because the target-layout policy is absent.

**Step 3: Implement minimal idempotence**

Calculate one target layout and only assign `layoutParams`, rotation, or system UI flags when the effective value changes.

**Step 4: Run focused tests and verify GREEN**

Run rear policy plus frontend contracts and inspect the source diff for unconditional layout assignment.

**Step 5: Commit**

Commit layout idempotence separately from lifecycle logic.

### Task 4: Create the independent V0.8.2 candidate

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `releases/candidates/V0.8.2-DUAL-SCREEN-RC1/manifest.txt`
- Create: `docs/gamefold/evidence/2026-08-15-v082-dual-screen-rc1/README.md`

**Step 1: Set candidate identity**

Set default versionName to `0.8.2` and increment versionCode from `10130` to `10131`. Do not move or modify the V0.8.1 tag/baseline.

**Step 2: Run full static/unit verification**

Run all legacy release unit tests, `git diff --check`, and confirm no FC/NS/runtime files changed.

**Step 3: Build release APK**

Build `:app:assembleLegacyRelease` offline. Verify package, version, signer/certificate, and SHA-256.

**Step 4: Archive candidate evidence**

Copy the APK into the candidate directory and record source commit, artifact hash, package metadata, signer digest, and test totals.

### Task 5: Install in place and execute the vivo lifecycle matrix

**Files:**
- Modify: `docs/gamefold/evidence/2026-08-15-v082-dual-screen-rc1/README.md`
- Create: `docs/gamefold/evidence/2026-08-15-v082-dual-screen-rc1/*.log`

**Step 1: Capture pre-install identity**

Record `adb devices -l`, installed package/version/install time, and current single-screen device state. Do not clear app data or permissions.

**Step 2: Install the independent candidate**

Use the vivo install workflow for an in-place update and verify the installed APK by pulling it back and hashing it.

**Step 3: Run the lifecycle matrix**

Exercise dual-screen enable, Home, Settings/app switch, return, recent-task switch, screen off/on, repeat enable, manual close, and app destruction. Capture filtered controller/window/activity/ANR logs for every path.

**Step 4: Evaluate objective gates**

Require: background reaches OFF/single; return does not issue an unsolicited dual request; display 3 is disabled; no ANR/FATAL/native crash; no sustained requestLayout burst; rear touch still works after a fresh manual enable.

**Step 5: Request user acceptance only for subjective experience**

If objective gates pass, ask the user only to verify perceived smoothness and real gameplay resume behavior. If active-game behavior fails, collect thread/ANR/native evidence before expanding scope.

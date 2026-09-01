# GameFold PC Splash Orientation Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep the startup splash visible until both startup routing and the saved display orientation are ready.

**Architecture:** Add a pure two-signal readiness gate and let `MainActivity` update it from the Compose startup-route callback and Android configuration callbacks. Keep the existing pre-`super.onCreate()` orientation request.

**Tech Stack:** Kotlin, Android Activity lifecycle, AndroidX SplashScreen, JUnit/Robolectric, Gradle.

## Global Constraints

- Preserve all RC1 startup, permission, MTK, shell, and icon behavior.
- Release identity is `1.0.2 / 10166 RC2`.
- Install the candidate but leave visual acceptance to the user.

---

### Task 1: Orientation-aware splash gate

**Files:**
- Create: `app/src/main/java/app/gamenative/product/GameFoldPcSplashReadiness.kt`
- Create: `app/src/test/java/app/gamenative/product/GameFoldPcSplashReadinessTest.kt`
- Modify: `app/src/main/java/app/gamenative/MainActivity.kt`

**Interfaces:**
- Produces: `GameFoldPcSplashReadiness.updateRouteReady()`, `updateOrientationReady(Boolean)`, and `shouldKeepOnScreen(): Boolean`.
- Consumes: `GameFoldOrientation.isPortrait(Context)` and Android `Configuration.orientation`.

- [ ] **Step 1: Write the failing readiness tests**

Cover route-first, orientation-first, and both-ready sequences with literal expected booleans.

- [ ] **Step 2: Run the focused tests and verify RED**

Run `./gradlew :app:testLegacyReleaseUnitTest --tests app.gamenative.product.GameFoldPcSplashReadinessTest --offline --no-daemon --console=plain` and expect compilation to fail because the gate is absent.

- [ ] **Step 3: Implement the minimal gate and activity wiring**

Use the gate in `setKeepOnScreenCondition`. Initialize target-orientation readiness before `super.onCreate()`, set route readiness from `onStartupRouteReady`, and refresh orientation readiness in `onConfigurationChanged`.

- [ ] **Step 4: Run focused startup/orientation tests and verify GREEN**

Run the new test plus `GameFoldOrientationTest` and `GameFoldPcFrontendPolicyTest`.

### Task 2: RC2 identity, build, and install

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/test/java/app/gamenative/ui/screen/settings/SettingsGeometryTest.kt`
- Create: `releases/candidates/V1.0.2-COLD-START-MTK-RC2/*`

**Interfaces:**
- Produces: `GameFold-PC-V1.0.2-COLD-START-MTK-RC2-10166.apk`.

- [ ] **Step 1: Write the failing version identity assertion**

Expect `versionName=1.0.2` and `versionCode=10166`.

- [ ] **Step 2: Verify RED, then increment versionCode to 10166**

Run the focused identity test before and after the production change.

- [ ] **Step 3: Run the release gate and build**

Run `:app:testLegacyReleaseUnitTest :app:assembleLegacyRelease` offline and require zero failures.

- [ ] **Step 4: Archive and verify the candidate**

Verify package identity, signature, SHA-256, and byte equality between build output and archive.

- [ ] **Step 5: Install and verify installed identity**

Install through the vivo installer workflow and verify package name, version name/code, update time, and pulled APK hash. Do not launch or visually evaluate the app.

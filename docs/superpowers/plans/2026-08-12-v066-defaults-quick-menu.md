# GameFold PC V0.6.6 Defaults and Quick Menu Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce and install an in-place V0.6.6 candidate that updates GameFold PC defaults, gates the in-game quick menu, and rebrands the launch splash without changing the V0.6.5 runtime contract.

**Architecture:** Centralize product-specific values in `GameFoldPcDefaults`, apply conservative per-game version-three migration in `GameFoldPcPerGameDefaults`, and express Back handling as a pure quick-menu policy consumed by `XServerScreen`. Keep UI removal and splash changes conditional on `GAMEFOLD_PC_LOCAL_SHELL`, preserving upstream behavior in other builds.

**Tech Stack:** Kotlin, Jetpack Compose, Android DataStore preferences, JUnit/Robolectric, Gradle Kotlin DSL, Bash packaging checks, Android APK tooling and ADB.

## Global Constraints

- Preserve the immutable V0.6.5 baseline artifact and tag.
- Keep package `com.gamefold.pc.v04localshell` and the current signer.
- Use versionCode `10054` and versionName `0.6.6-defaults-rc1`.
- Do not uninstall, clear data, reset file authorization, or alter local game/runtime paths.
- Do not change scanner, executable selection, GTA fullscreen logic, Bionic/Proton launch, LSFG DLL resolution, public resources, or the existing input bridge.

---

### Task 1: Product defaults and conservative per-game migration

**Files:**
- Modify: `app/src/test/java/app/gamenative/product/GameFoldPcDefaultsTest.kt`
- Modify: `app/src/test/java/app/gamenative/product/GameFoldPcPerGameDefaultsTest.kt`
- Modify: `app/src/main/java/app/gamenative/product/GameFoldPcDefaults.kt`
- Modify: `app/src/main/java/app/gamenative/product/GameFoldPcPerGameDefaults.kt`
- Modify: `app/src/main/java/app/gamenative/utils/LsfgVkManager.kt`
- Modify: `app/src/main/java/com/winlator/container/ContainerData.kt`

**Interfaces:**
- Produces: `GameFoldPcDefaults.DEFAULTS_VERSION == "3"`, `LSFG_FLOW_SCALE == 0.50f`, FSR Fit mode, and the offline BOTH controller defaults.
- Produces: `GameFoldPcPerGameDefaults.applyTo(Container): Boolean` that upgrades version-two old defaults without resetting unrelated settings.

- [ ] **Step 1: Add failing default and migration tests**

Extend `GameFoldPcDefaultsTest` to require the new global, per-game, splash, screen-effect, and controller constants. Extend `GameFoldPcPerGameDefaultsTest` with one version-two old-default migration and one custom-value preservation case. Assert LSFG armed state, language, screen size, HUD, and executable-related fields remain unchanged for version-two migration.

- [ ] **Step 2: Run the two suites and verify RED**

```bash
env JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
  ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
  ./gradlew :app:testLegacyReleaseUnitTest \
  --tests app.gamenative.product.GameFoldPcDefaultsTest \
  --tests app.gamenative.product.GameFoldPcPerGameDefaultsTest \
  --offline --no-daemon --console=plain \
  -PGAMEFOLD_PC_LOCAL_SHELL=true
```

Expected: FAIL because version-three constants and migration do not exist.

- [ ] **Step 3: Implement the minimal defaults and migration**

Add constants for `0.50`, FSR Fit, warn-before-exit, hidden quick menu, `GameFold`, hidden fixed tips, and controller defaults. Version-two migration changes flow scale only when absent/`0.80`, changes screen scaling only when absent/None, writes Steam Input off/BOTH/Standard, and leaves all earlier product defaults untouched. New and pre-version-two containers continue receiving the established V0.6.5 defaults before version-three additions.

- [ ] **Step 4: Re-run the suites and verify GREEN**

Run the Step 2 command. Expected: all selected tests pass.

- [ ] **Step 5: Commit Task 1**

```bash
git add app/src/test/java/app/gamenative/product app/src/main/java/app/gamenative/product \
  app/src/main/java/app/gamenative/utils/LsfgVkManager.kt \
  app/src/main/java/com/winlator/container/ContainerData.kt
git commit -m "feat: update GameFold PC per-game defaults"
```

### Task 2: Quick-menu preference and Back policy

**Files:**
- Create: `app/src/main/java/app/gamenative/product/InGameQuickMenuPolicy.kt`
- Create: `app/src/test/java/app/gamenative/product/InGameQuickMenuPolicyTest.kt`
- Modify: `app/src/main/java/app/gamenative/PrefManager.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/settings/SettingsGroupEmulation.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-zh-rCN/strings.xml`

**Interfaces:**
- Produces: `InGameQuickMenuPolicy.backAction(enabled, visible): BackAction` with `DISMISS`, `OPEN`, and `REQUEST_EXIT` results, plus a shared exit-confirmation policy.
- Produces: `PrefManager.showInGameQuickMenu: Boolean`, defaulting off only for GameFold PC.

- [ ] **Step 1: Write the failing Back-policy test**

Test that a visible menu is dismissed, an enabled hidden menu opens, and a disabled hidden menu requests game exit. Test the exit-confirmation state machine separately: warning disabled exits immediately; warning enabled warns first and exits on the second request.

- [ ] **Step 2: Run the policy suite and verify RED**

```bash
env JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
  ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
  ./gradlew :app:testLegacyReleaseUnitTest \
  --tests app.gamenative.product.InGameQuickMenuPolicyTest \
  --offline --no-daemon --console=plain \
  -PGAMEFOLD_PC_LOCAL_SHELL=true
```

Expected: FAIL because `InGameQuickMenuPolicy` is missing.

- [ ] **Step 3: Implement preference, first-row switch, and policy use**

Add the preference and localized labels. Render the switch before Orientation in Simulation settings. In `gameBack`, dismiss an already visible menu, open only when policy returns `OPEN`, and route `REQUEST_EXIT` through the shared “退出前警告” state machine. Existing keyboard/controller callers continue routing through `gameBack`, so the single policy gates every open route.

- [ ] **Step 4: Re-run the policy test and compile Kotlin**

Run Step 2, then:

```bash
env JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
  ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
  ./gradlew :app:compileLegacyReleaseKotlin \
  --offline --no-daemon --console=plain \
  -PGAMEFOLD_PC_LOCAL_SHELL=true
```

Expected: both commands succeed.

- [ ] **Step 5: Commit Task 2**

```bash
git add app/src/main/java/app/gamenative/PrefManager.kt \
  app/src/main/java/app/gamenative/product/InGameQuickMenuPolicy.kt \
  app/src/test/java/app/gamenative/product/InGameQuickMenuPolicyTest.kt \
  app/src/main/java/app/gamenative/ui/screen/settings/SettingsGroupEmulation.kt \
  app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt \
  app/src/main/res/values/strings.xml app/src/main/res/values-zh-rCN/strings.xml
git commit -m "feat: gate the in-game quick menu"
```

### Task 3: Settings cleanup, GameFold splash, and copy inventory

**Files:**
- Create: `app/src/main/java/app/gamenative/product/GameFoldPcPresentationPolicy.kt`
- Create: `app/src/test/java/app/gamenative/product/GameFoldPcPresentationPolicyTest.kt`
- Create: `docs/GAME_LAUNCH_TIPS.md`
- Modify: `app/src/main/java/app/gamenative/ui/screen/settings/SettingsGroupInterface.kt`
- Modify: `app/src/main/java/app/gamenative/ui/components/BootingSplash.kt`

**Interfaces:**
- Produces: `GameFoldPcPresentationPolicy.forProduct(localShell): Presentation` with brand, fixed-tip visibility, and icon-picker visibility.
- Produces: GameFold-only hidden icon picker, `GameFold` launch brand, hidden fixed tips, and a 25-row bilingual copy inventory.

- [ ] **Step 1: Write and run the failing presentation-policy test**

Test literal consumer-visible results for both product branches: local-shell uses `GameFold`, hides tips, and hides the icon picker; upstream uses `GameNative`, shows tips, and shows the icon picker.

```bash
env JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
  ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
  ./gradlew :app:testLegacyReleaseUnitTest \
  --tests app.gamenative.product.GameFoldPcPresentationPolicyTest \
  --offline --no-daemon --console=plain \
  -PGAMEFOLD_PC_LOCAL_SHELL=true
```

Expected: FAIL before production/documentation changes.

- [ ] **Step 2: Implement the minimal UI changes and copy inventory**

Guard only the icon-style picker, leaving aliases and switching code compiled. Select `GameFold` and fixed-tip visibility only for local-shell builds. Preserve dynamic status/progress. Document all 25 Chinese and English strings without changing their resource values.

- [ ] **Step 3: Re-run the presentation-policy test**

Run Step 1. Expected: PASS.

- [ ] **Step 4: Commit Task 3**

```bash
git add app/src/main/java/app/gamenative/product/GameFoldPcPresentationPolicy.kt \
  app/src/test/java/app/gamenative/product/GameFoldPcPresentationPolicyTest.kt \
  docs/GAME_LAUNCH_TIPS.md \
  app/src/main/java/app/gamenative/ui/screen/settings/SettingsGroupInterface.kt \
  app/src/main/java/app/gamenative/ui/components/BootingSplash.kt
git commit -m "feat: simplify GameFold PC launch presentation"
```

### Task 4: Regression, release build, installation, and evidence

**Files:**
- Build: `app/build/outputs/apk/legacy/release/app-legacy-release.apk`
- Archive: `releases/candidates/V0.6.6/GameFold-PC-V0.6.6-Defaults-RC1-10054.apk`

**Interfaces:**
- Produces: verified and installed V0.6.6 release candidate without modifying baseline pointers.

- [ ] **Step 1: Run focused and supported Legacy tests**

Run the affected product tests, GTA fullscreen regression, and the supported local-shell `testLegacyReleaseUnitTest` suite. Expect zero failures. Inspect the Compose order directly, then verify the rendered order and splash on the installed device.

- [ ] **Step 2: Build the release candidate**

```bash
env JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
  ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
  ./gradlew :app:assembleLegacyRelease \
  --offline --no-daemon --console=plain \
  -PGAMEFOLD_PC_LOCAL_SHELL=true \
  -PGAMEFOLD_PC_APPLICATION_ID=com.gamefold.pc.v04localshell \
  -PGAMEFOLD_PC_VERSION_CODE=10054 \
  -PGAMEFOLD_PC_VERSION_NAME=0.6.6-defaults-rc1
```

- [ ] **Step 3: Verify and archive exact candidate bytes**

Use Android `aapt`, `apksigner`, `stat`, and `shasum -a 256`; copy the verified APK into the V0.6.6 candidate directory and prove both hashes match.

- [ ] **Step 4: Install in place and verify device state**

Use the vivo installer helper. Do not uninstall. Verify `dumpsys package`, update time, installed APK hash, and `MANAGE_EXTERNAL_STORAGE` state.

- [ ] **Step 5: Inspect final diff and hand off for real-device validation**

Run `git diff --check`, `git status --short`, and verify no V0.6.5 baseline file or pointer changed. Ask the user to test settings order/defaults, Back behavior, splash, GTA launch/fullscreen, LSFG, FSR Fit, and controller input.

# GameFold PC Local Legacy Test Contract Implementation Plan

> **Execution note:** Apply this plan on `codex/gamefold-pc-v065-legacy-test-contract`, which starts from the immutable `gamefold-pc-v0.6.5-baseline` tag. Do not modify or retag V0.6.5.

**Goal:** Make `testLegacyReleaseUnitTest` validate only the features retained by the offline GameFold PC product when `GAMEFOLD_PC_LOCAL_SHELL=true`, while preserving the upstream/historical test suite for non-local builds.

**Architecture:** Keep all test source files intact. Add a positive class allowlist to the existing Gradle `Test` task only for the GameFold PC local-shell property and the `testLegacyReleaseUnitTest` task. A positive allowlist makes newly introduced platform/download tests opt-in instead of silently expanding the supported product contract.

**Technology:** Gradle Kotlin DSL, Android Gradle Plugin unit-test tasks, JUnit/Robolectric.

---

## Task 1: Prove the old task violates the offline product boundary

**Files:**

- Read: `app/build.gradle.kts`
- Evidence: `app/build/test-results/testLegacyReleaseUnitTest/`

1. Run the unchanged local-shell Legacy task:

   ```bash
   env JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
     ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
     ./gradlew :app:testLegacyReleaseUnitTest \
     --offline --no-daemon --console=plain \
     -PGAMEFOLD_PC_LOCAL_SHELL=true
   ```

2. Record the expected RED result: 910 tests, 71 failures. Confirm the failures are historical platform/download contracts such as `SteamUtilsFileSearchTest` and the five `*DownloaderTest` classes, rather than V0.6.5 fullscreen/default tests.

## Task 2: Add the local-shell positive allowlist

**Files:**

- Modify: `app/build.gradle.kts`

1. Import `org.gradle.api.tasks.testing.Test`.
2. Define `gameFoldPcSupportedLegacyTestClasses` next to the other GameFold PC product properties. Include only retained areas:

   - local input and path configuration;
   - offline storage and launch actions;
   - GameFold PC policy and defaults;
   - supported local game-fix types;
   - SD-card detection, FPS limiter, container settings, and library sorting;
   - case-insensitive filesystem/path utilities;
   - local scanning, artwork, executable isolation, LSFG DLL lookup, and GTA fullscreen resolution;
   - local pre-install runtime components and Bionic/Proton bootstrap policy;
   - Winlator filesystem, Wine, environment, and ImageFS runtime tests.

3. Configure only `testLegacyReleaseUnitTest` when `gameFoldPcLocalShell` is true:

   ```kotlin
   tasks.withType<Test>().configureEach {
       if (gameFoldPcLocalShell && name == "testLegacyReleaseUnitTest") {
           filter {
               gameFoldPcSupportedLegacyTestClasses.forEach(::includeTestsMatching)
               isFailOnNoMatchingTests = true
           }
       }
   }
   ```

4. Do not delete or rename online test files. Running the same task without `-PGAMEFOLD_PC_LOCAL_SHELL=true` must retain the historical full-suite behavior.

## Task 3: Verify the supported contract and exclusion boundary

**Files:**

- Verify: `app/build/test-results/testLegacyReleaseUnitTest/TEST-*.xml`
- Verify: `app/build.gradle.kts`

1. Run the local-shell Legacy task from Task 1 again. Expect `BUILD SUCCESSFUL` and zero failed tests.
2. Inspect generated XML and confirm representative supported suites ran:

   - `ProductModePolicyTest`
   - `LocalGameExecutableResolverTest`
   - `GtaFullscreenConfigTest`
   - `LsfgDllLocatorTest`
   - `ImageFsInstallerTest`
   - `ShooterModeConfigTest`

3. Confirm representative removed-product suites did not run:

   - `SteamUtilsFileSearchTest`
   - `ContainerFilesDownloaderTest`
   - `NexusApiClientTest`
   - `GOGAuthManagerTest`
   - `HltbServiceTest`

4. Compile the local-shell Legacy release to prove the build configuration remains valid:

   ```bash
   env JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' \
     ANDROID_HOME='/Users/11085429/Library/Android/sdk' \
     ./gradlew :app:compileLegacyReleaseKotlin \
     --offline --no-daemon --console=plain \
     -PGAMEFOLD_PC_LOCAL_SHELL=true
   ```

5. Run `git diff --check`, inspect the diff, and commit only the test-contract implementation. No APK build or device installation is required because production sources and packaged resources are unchanged.

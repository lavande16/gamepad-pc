# GameFold PC Legacy Test Contract Design

## Goal

Make the Legacy unit-test gate reflect the supported GameFold PC offline product instead of the removed GameNative online product. A GameFold PC test run must exercise retained local runtime behavior and must not execute tests whose only subject is Steam/platform accounts, online recommendations, remote manifests, or runtime downloads.

## Root cause

`testLegacyReleaseUnitTest` currently runs the complete historical GameNative test source set even when the build is configured with `GAMEFOLD_PC_LOCAL_SHELL=true`. The production policy disables platform accounts, online updates, analytics, and online runtime downloads, but the Gradle test task does not apply that policy. Consequently, tests for removed assets and remote downloader manifests still run and fail even though those paths are unreachable in the supported product.

The evidence is stable across baselines: V0.6.4 ran 903 tests with 71 failures, while V0.6.5 ran 910 tests with the same 71 failures. The seven added V0.6.5 fullscreen tests passed.

## Chosen approach

Keep the historical test source files for upstream reference, but make the existing Legacy test task product-aware. When and only when `GAMEFOLD_PC_LOCAL_SHELL=true`, Gradle applies a positive allowlist of supported test classes. Without that property, the historical test task keeps its existing behavior.

A positive allowlist is intentional: a newly added online-only test cannot silently become part of the GameFold PC gate merely because it happens to pass today. Adding a new supported capability requires explicitly adding its test class or package to the contract.

## Supported test surface

The GameFold PC Legacy gate covers:

- offline storage permission and local game entry actions;
- product-mode policy and GameFold PC defaults;
- local game scanning, first-scan artwork, executable selection, folder boundaries, and case-insensitive path behavior;
- container storage and container configuration updates;
- GTA fullscreen/resolution synchronization and non-GTA isolation;
- LSFG local DLL resolution;
- local Bionic/Proton bootstrap and ImageFS installation/migration;
- Wine/core filesystem helpers, environment variables, and retained generic game-fix behavior;
- retained local Win-component preparation such as OpenAL, PhysX, VC redist, and XNA;
- controller-facing shooter/touch configuration and FPS-limiter utilities.

## Unsupported test surface

The GameFold PC Legacy gate does not run tests whose subject is:

- Steam account, library, depot, cloud save, collection, workshop, or Steam API replacement;
- GOG, Epic, or Amazon authentication, libraries, downloads, cloud saves, or app screens;
- Nexus, online mod acquisition, workshop, or platform mod synchronization;
- HLTB/network recommendations and online integration tests;
- update/download UI and active download state;
- remote component catalogs, URL formats, cache-download behavior, or remote manifest installation;
- generic launch dependency download orchestration that the local shell explicitly bypasses.

Historical tests remain in the repository and can still run in a non-local GameNative build. They are excluded from the GameFold PC contract because the corresponding product capabilities are unsupported, not because individual assertions happen to fail.

## Gradle contract

- The existing command remains `:app:testLegacyReleaseUnitTest`.
- The GameFold PC gate always supplies `-PGAMEFOLD_PC_LOCAL_SHELL=true`.
- `app/build.gradle.kts` defines the allowlist once and applies it only to `testLegacyReleaseUnitTest` when local-shell mode is enabled.
- `isFailOnNoMatchingTests` remains enabled so a broken or empty allowlist cannot report a false green result.
- The task output and XML reports provide the executed test count and zero-failure evidence.

## Verification

1. Run the current V0.6.5 Legacy task without the local-shell property and reproduce the historical 71 failures.
2. Add the product-aware allowlist.
3. Run `:app:testLegacyReleaseUnitTest -PGAMEFOLD_PC_LOCAL_SHELL=true` and require zero failures.
4. Inspect the XML report to confirm retained GTA, local scanner/artwork, container, LSFG, product policy, input configuration, and ImageFS suites executed.
5. Inspect the XML report to confirm Steam/platform/downloader/Nexus/HLTB suites did not execute.
6. Run `:app:compileLegacyReleaseKotlin -PGAMEFOLD_PC_LOCAL_SHELL=true` to confirm the test-only Gradle configuration does not alter production compilation.

## Baseline boundary

This work starts from `gamefold-pc-v0.6.5-baseline` on a separate branch. It does not change the V0.6.5 APK, baseline tag, current-baseline declaration, product behavior, or device installation. No APK build or phone installation is required because the change is limited to build-time test selection and documentation.

# GameFold PC V0.6.6 baseline

## Status

Frozen on 2026-08-13 after real-device acceptance of the V0.6.6 defaults and presentation update, followed by a focused offline Proton 10 selector correction. `V0.6.6` is the approved rollback point on top of the V0.6.5 GTA fullscreen and folder-scoped multi-game runtime.

V0.6.5, V0.6.4, and V0.6 remain frozen and available. This declaration does not overwrite or rename any earlier artifact.

## Frozen artifact

| Field | Value |
| --- | --- |
| Artifact | `releases/baselines/V0.6.6/GameFold-PC-V0.6.6-Baseline-10057.apk` |
| Build artifact | `app/build/outputs/apk/legacy/release/app-legacy-release.apk` |
| Size | `14,059,761` bytes |
| APK SHA-256 | `3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad` |
| Package | `com.gamefold.pc.v04localshell` |
| versionName | `0.6.6` |
| versionCode | `10057` |
| APK signer SHA-256 | `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4` (Android Debug) |
| Signature scheme | APK Signature Scheme v2 |
| Source commit | `c0320448b6256ada0d6e175385d4c8ca43df64d9` (`fix: keep Proton 10 visible offline`) |
| Git tag | `gamefold-pc-v0.6.6-baseline` |

The build artifact, final candidate, frozen baseline artifact, and APK pulled back from the phone matched the SHA-256 above byte-for-byte.

The frozen `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `4c452ed25047fa224e240aec12211295fb51472336c0dbcc9ac57bb6c8267faf`, identical to V0.6.5.

## V0.6.6 product contract

- Preserve every V0.6.5 local-game scan, folder-scoped executable selection, per-game A: isolation, GTA fullscreen synchronization, `1067x600` runtime, Bionic/Proton, controller bridge, and LSFG-VK injection guarantee.
- The LSFG-VK optical-flow scale defaults to `0.50` for GameFold PC.
- Screen effects default to `FSR Fit` when a game has no existing non-default scaling choice.
- Offline controller defaults disable Steam Input, enable XInput and DInput, use the combined preferred input API, and use the standard DInput mapper.
- The first Emulation setting is `Show in-game quick menu`; it defaults off. With the menu disabled, Back routes to the exit policy instead of opening the menu.
- `Warn before exiting` defaults on: the first Back requests confirmation and the second Back exits. Explicit saved user preferences still win.
- The launch splash uses `GameFold`, hides the 25 fixed rotating tips, and preserves dynamic launch status and progress. The hidden copy inventory remains in `docs/GAME_LAUNCH_TIPS.md`.
- The GameFold PC icon-style picker is hidden without removing the underlying shared icon implementation.
- The global `Modify default config` screen size migrates once from missing or legacy `1280x720` to `1067x600`. Other custom global values, existing per-game resolutions, and later manual choices remain untouched.
- New games inherit the global screen-size template instead of receiving a second hard-coded per-game resolution assignment.
- `proton-10.0-arm64ec-2` is included in the local Bionic Wine selector for GameFold PC, so the offline default-config UI cannot fall back visually to the first Proton 9 entry when the legacy online component manifest is unavailable.

Existing V0.6.5 per-game settings migrate conservatively: the old LSFG flow default `0.80` becomes `0.50`, and no scaling becomes `FSR Fit`; custom flow and scaling choices survive. The migration does not overwrite an existing game's LSFG armed state, language, HUD state, or resolution.

## Automated verification

- The complete supported offline Legacy suite passed `227` tests across `39` classes with `0` failures, `0` errors, and `0` skipped tests.
- The Proton selector regression test was observed failing before the implementation because the local Proton 10 option API was absent, then passed after the minimal product-scoped implementation.
- `:app:testLegacyReleaseUnitTest :app:assembleLegacyRelease` completed successfully with versionCode `10057` and versionName `0.6.6`.
- `git diff --check` passed before freezing.
- The V0.6.5-protected `CustomGameScanner`, `LocalGameExecutableResolver`, `GtaFullscreenConfig`, and `LsfgDllLocator` source files have no V0.6.6 diff.

## Real-device evidence

- Device: vivo `V2436A`, serial `10AF3P0SDY001M6`, arm64-v8a.
- Installed package: versionCode `10057`, versionName `0.6.6`.
- First install time remained `2026-08-12 02:29:45`; accepted update time was `2026-08-12 23:57:31`.
- `MANAGE_EXTERNAL_STORAGE` remained allowed, confirming an in-place upgrade without clearing app data or file authorization.
- The APK pulled back from the installed package matched the frozen SHA-256 byte-for-byte.
- `Advanced settings -> Emulation settings -> Modify default config` rendered `proton-10.0-arm64ec-2` on the device even while the legacy remote component-manifest request was timing out.
- Immediately before the selector correction, live GTA V logs proved the installed V0.6.6 runtime was already using `proton-10.0-arm64ec-2`, `1067x600`, DXVK `2.4.1-gplasync`, and the established LSFG-VK layer. The selector defect was presentation-only until a user actively chose and saved another entry.
- The user explicitly requested promotion of the corrected `0.6.6` build to the baseline.

## Known follow-up boundary

- The shared component-availability screen still contains a legacy remote manifest lookup. V0.6.6 no longer depends on that lookup to display the packaged Proton 10 default, but removing the remaining lookup belongs to a separately scoped offline cleanup and must not mutate this baseline.
- A live GTA V slowdown observed before this freeze was diagnosed from device evidence as severe thermal throttling: system thermal status `3`, skin temperature about `45-46.8 C`, GPU cooling level `9`, and CPU clocks around `0.84-1.13 GHz`. It was not a Proton 9 runtime regression.

## Follow-up rules

- Never overwrite or mutate the V0.6.6 artifact bytes.
- New candidates keep the same package and signer for in-place installation, but must use a versionCode higher than `10057`.
- Do not uninstall, clear app data, or reset file authorization during candidate installation unless the user explicitly requests a clean-install test.
- Do not alter the validated folder-scoped launch paths, GTA fullscreen synchronization, global screen migration, Bionic/Proton runtime, input bridge, LSFG-VK path, or public-resource contract without a separately scoped diagnosis and regression test.
- Keep V0.6.5, V0.6.4, and V0.6 available as previous rollback points.

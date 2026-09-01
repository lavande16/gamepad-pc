# GameFold PC V0.6.5 baseline

## Status

Frozen on 2026-08-12 after real-device validation of the 1067x600 fullscreen contract for GTA V and Black Myth Pixel. `V0.6.5` is the approved rollback point for fullscreen resolution synchronization on top of the V0.6.4 folder-scoped multi-game launcher.

V0.6.4 and V0.6 remain frozen and available. This declaration does not overwrite or rename either earlier artifact.

## Frozen artifact

| Field | Value |
| --- | --- |
| Artifact | `releases/baselines/V0.6.5/GameFold-PC-V0.6.5-Baseline-10053.apk` |
| Build artifact | `app/build/outputs/apk/legacy/release/app-legacy-release.apk` |
| Size | `14,055,549` bytes |
| APK SHA-256 | `92dde6a5ab4bff75554c47f218e7275606ca81b5ecd62c08f9f9446b4c67fe0a` |
| Package | `com.gamefold.pc.v04localshell` |
| versionName | `0.6.5-gta-fullscreen-sync-r2` |
| versionCode | `10053` |
| APK signer SHA-256 | `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4` (Android Debug) |
| Signature scheme | APK Signature Scheme v2 |
| Source commit | `cfad91b266258f6a0a61678d0b41ca3d367d52e5` (`fix: keep GTA fullscreen at a compatible resolution`) |
| Git tag | `gamefold-pc-v0.6.5-baseline` |

The candidate APK, both frozen artifact copies, and APK pulled back from the phone matched the SHA-256 above byte-for-byte.

The frozen `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `4c452ed25047fa224e240aec12211295fb51472336c0dbcc9ac57bb6c8267faf`, identical to V0.6.4.

## Product and runtime contract

- Preserve every V0.6.4 folder-scoped executable-selection and per-game A: isolation guarantee.
- New game containers default to the custom virtual desktop size `1067x600`.
- Existing configured containers keep their user-selected resolution unless GTA V requests an unsupported size below `800x600`.
- `GTAVLauncher.exe` and `GTA5.exe` are the only executables receiving GTA-specific fullscreen handling.
- Before GTA V starts, `commandline.txt` preserves unrelated arguments, removes conflicting fullscreen/window/borderless/width/height flags, and writes `-fullscreen`, `-width`, and `-height` for the current container size.
- A GTA V size below `800` pixels wide or `600` pixels high is normalized to the safe `1067x600` default so that the XServer, Wine desktop, and actual game buffers cannot disagree.
- Supported resolutions at or above `800x600` remain user-selectable and are synchronized exactly.
- Other games do not receive GTA-specific command-line or minimum-size changes.
- Existing Bionic/Proton, public-resource, controller, LSFG-VK, performance HUD, Simplified Chinese, icon, and advanced-settings behavior remains in scope.

## Automated verification

- The directly affected suites passed `13` tests with `0` failures: `GameFoldPcDefaultsTest` (`3`), `GameFoldPcPerGameDefaultsTest` (`3`), and `GtaFullscreenConfigTest` (`7`).
- The regression tests cover new-container defaults, existing-container preservation, GTA executable detection, conflicting flag replacement, unrelated argument preservation, unsupported-size normalization, invalid input, and non-GTA isolation.
- The complete V0.6.5 Legacy task ran `910` tests with `71` historical failures.
- The identical command on frozen V0.6.4 ran `903` tests with the same `71` failures. The seven added V0.6.5 tests all passed, so V0.6.5 introduced no additional Legacy-test failure.
- The historical failures are concentrated in removed Steam/online manifest and downloader assumptions plus one pre-existing Mod materializer case. They are not declared green by this baseline.
- `:app:assembleLegacyRelease` completed successfully with versionCode `10053` and versionName `0.6.5-gta-fullscreen-sync-r2`.

## Real-device evidence

- Device: vivo `V2436A`, serial `10AF3P0SDY001M6`, arm64-v8a.
- Installed package: versionCode `10053`, versionName `0.6.5-gta-fullscreen-sync-r2`.
- First install time remained `2026-08-12 02:29:45`; accepted update time was `2026-08-12 19:09:17`.
- `MANAGE_EXTERNAL_STORAGE` remained allowed, confirming an in-place upgrade without clearing data or authorization.
- Black Myth Pixel launched its own `BlackMythPixel.exe`, not GTA V. XServer, container surface, and all five imported AHB buffers were `1067x600`; LSFG-VK created its Vulkan device layer and swapchain.
- GTA V launched `Grand Theft Auto V/GTAVLauncher.exe`, then `GTA5.exe`. Its command line, XServer, container surface, and all five imported AHB buffers were `1067x600`; LSFG-VK created its Vulkan device layer and swapchain.
- Both game processes remained alive during the observation window. No `FATAL EXCEPTION`, fatal native signal, or application ANR was observed.
- The user explicitly accepted this installed version and requested promotion to the new baseline.

## Follow-up rules

- Never overwrite or mutate the V0.6.5 artifact bytes.
- New candidates keep the same package and signer for in-place installation, but must use a versionCode higher than `10053`.
- Do not uninstall, clear app data, or reset file authorization during candidate installation unless the user explicitly requests a clean-install test.
- Do not alter the validated folder-scoped launch paths, 1067x600 default, GTA fullscreen synchronization, Bionic/Proton runtime, input bridge, LSFG-VK path, or public-resource contract without a separately scoped diagnosis and regression test.
- The next test-maintenance change must start after this baseline and may retire tests only for product capabilities that are explicitly no longer supported. It must not rewrite this baseline tag.
- Keep V0.6.4 and V0.6 available as previous rollback points.

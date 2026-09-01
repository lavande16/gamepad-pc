# GameFold PC V0.6.4 baseline

## Status

Frozen on 2026-08-12 after real-device validation of folder-scoped multi-game launching. `V0.6.4` is the approved rollback point for the local-game executable isolation work.

The previous V0.6 baseline remains frozen and available. This declaration does not overwrite or rename any V0.6 artifact.

## Frozen artifact

| Field | Value |
| --- | --- |
| Artifact | `releases/baselines/V0.6.4/GameFold-PC-V0.6.4-Baseline-10049.apk` |
| Build artifact | `app/build/outputs/apk/legacy/release/app-legacy-release.apk` |
| Size | `14,053,993` bytes |
| APK SHA-256 | `e0ea096b21a1a6b5bda908f64c776876d53e7ade43578564ae77acc9ab0b01a6` |
| Package | `com.gamefold.pc.v04localshell` |
| versionName | `0.6.4-v06-folder-scoped-launch` |
| versionCode | `10049` |
| APK signer SHA-256 | `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4` (Android Debug) |
| Signature scheme | APK Signature Scheme v2 |
| Source commit | `9d2fd4e40a9ed606a3ca36d01b460facd29bd76c` (`fix: harden local game launch boundaries`) |
| Git tag | `gamefold-pc-v0.6.4-baseline` |

The frozen artifact, build artifact, and APK pulled back from the phone matched the SHA-256 above byte-for-byte.

## Product and runtime contract

- Every custom game maps A: to its own registered folder under `/storage/emulated/0/GameFoldPC/game`.
- Persisted executable paths are accepted only when the target is a regular `.exe` canonically contained in the current game's registered folder.
- The shared `/storage/emulated/0/GameFoldPC/game` root must never be used as an individual game's executable-resolution root.
- Empty or stale paths are repaired by bounded, deterministic, folder-local executable scanning.
- GTA V resolves `Grand Theft Auto V/GTAVLauncher.exe` inside its own folder.
- Single-executable local games resolve their own executable without inheriting GTA V state.
- Installer, uninstaller, crash-handler, helper, updater, bootstrapper, patcher, redistributable, and benchmark executables are excluded from automatic selection.
- Existing V0.6 Wine/Bionic/Proton, public-resource, controller, LSFG-VK, performance HUD, Simplified Chinese, icon, and advanced-settings behavior remains in scope and must not be regressed.

## Automated verification

- The focused V0.6.4 suite passed: `11` tests, `0` failures.
- Coverage includes GTA V multi-EXE scoring, unique-EXE selection, sibling-game isolation, stale persisted-path repair, canonical traversal rejection, shared-root A: rejection, helper/crash-handler filtering, valid persisted selection, and deterministic tie-breaking.
- `:app:assembleLegacyRelease` completed successfully with the frozen version parameters.
- Independent code review found no remaining Critical or Important issues after the boundary-hardening follow-up.
- The historical full legacy unit-test task still contains unrelated pre-existing failures tied to removed online manifests/assets and legacy Steam/downloader tests; it is not recorded as green for this baseline.

## Real-device evidence

- Device: vivo `V2436A`, serial `10AF3P0SDY001M6`, arm64-v8a.
- Installed package: versionCode `10049`, versionName `0.6.4-v06-folder-scoped-launch`.
- First install time remained `2026-08-12 02:29:45`; final accepted update time was `2026-08-12 15:26:11`.
- `MANAGE_EXTERNAL_STORAGE` remained allowed, confirming the candidate was installed in place without clearing app data or authorization.
- GTA V final-package logs mapped A: to `/storage/emulated/0/GameFoldPC/game/Grand Theft Auto V`, launched `A:\Grand Theft Auto V\GTAVLauncher.exe`, and produced both `GTAVLauncher.exe` and `GTA5.exe` processes. The user confirmed GTA V gameplay was normal.
- Black Myth Pixel final-package logs mapped A: to `/storage/emulated/0/GameFoldPC/game/黑神话像素版` and launched only `A:\黑神话像素版1.0 电脑版.exe`; the previous GTA path and `Path not found` failure were absent.

## Known limitation outside baseline acceptance

`黑神话像素版1.0 电脑版.exe` is a WinRAR SFX archive, not the inner Unity game executable. The outer SFX process remains on a black screen and does not start the archived `黑神话像素版.exe`/UnityPlayer process. Therefore:

- folder isolation and executable routing are accepted;
- Black Myth Pixel gameplay is **not** accepted or claimed by this baseline;
- the next diagnosis should use a separate offline extracted copy and preserve the original game folder.

## Follow-up rules

- Never overwrite or mutate the V0.6.4 artifact bytes.
- New candidates keep the same package and signer for in-place installation, but must use a higher versionCode than `10049`.
- Do not uninstall, clear app data, or reset file authorization during candidate installation unless the user explicitly requests a clean-install test.
- Do not alter the validated GTA V launch path, per-game A: isolation, Bionic/Proton runtime, input bridge, LSFG-VK path, or public-resource contract without a separately scoped diagnosis and regression test.
- Keep V0.6 available as the previous rollback point.

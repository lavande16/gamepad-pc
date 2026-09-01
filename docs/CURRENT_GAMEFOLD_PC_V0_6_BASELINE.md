# GameFold PC V0.6 baseline

## Status

Frozen on 2026-08-12 after real-device validation. V0.6 is the approved rollback point for the launcher-icon and default-settings work.

`V0.6` is the product baseline label. The Android identity below intentionally records the exact verified APK metadata instead of renaming or rebuilding the accepted bytes.

## Frozen artifact

| Field | Value |
| --- | --- |
| Artifact | `releases/baselines/V0.6/GameFold-PC-V0.6-Baseline-10045.apk` |
| Original artifact | `releases/GameFold-PC-V0.5.5-Arm64-Runtime-Safe-LSFG-Fix-10045.apk` |
| Size | `14,166,434` bytes |
| APK SHA-256 | `31605a8dfee488665239b211a904c68c9d60d8be5666f0dc76cb7fec718d2cc6` |
| Package | `com.gamefold.pc.v04localshell` |
| versionName | `0.5.5-v04-local-shell-arm64-runtime-safe-lsfg-fix` |
| versionCode | `10045` |
| APK signer SHA-256 | `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4` (Android Debug) |
| Source commit | `c177cf8` (`baseline: freeze GameFold PC V0.6 runtime-safe offline shell`) |

## Runtime contract

- Package remains arm64-only and keeps the proven Wine/Bionic/Proton command path.
- Public runtime resources remain under `/storage/emulated/0/GameFoldPC/`.
- LSFG reads `/storage/emulated/0/GameFoldPC/lsfg/inputtest/Lossless.dll`; the DLL is not embedded in the APK.
- `INTERNET` permission is retained only because Wine uses AF_INET for local IPC and interface discovery. Online login, recommendation, and downloader product surfaces remain removed.

## Real-device evidence

- Device: vivo `V2436A`, serial `10AF3P0SDY001M6`, arm64-v8a.
- Installed package metadata matched versionCode `10045` and the versionName above.
- The APK pulled back from the device matched the frozen SHA-256 byte-for-byte.
- First install time was preserved as `2026-08-12 02:29:45`; last update time for this accepted install was `2026-08-12 12:47:29`.
- `MANAGE_EXTERNAL_STORAGE` was allowed.
- GTA5 remained running after startup; controller input remained operational.
- LSFG was saved enabled and runtime logs showed `LSFG armed`, Vulkan instance/device initialization, Android AHB context, and a five-image swapchain.

## Follow-up rules

- Never overwrite or mutate the V0.6 artifact bytes.
- New candidates keep the same package and signature for in-place installation, but use a higher versionCode.
- Do not uninstall, clear app data, or reset file authorization during candidate installation.
- Changes after this baseline must not alter the game-launch, Bionic extraction, Proton runtime, input, or LSFG runtime paths unless a separate diagnosis explicitly requires it.

# Current GameFold PC V0.7.1 baseline

The approved GameFold PC baseline is **V0.7.1-Auto-Launch-R22-10087**.

- APK: `releases/baselines/V0.7.1/GameFold-PC-V0.7.1-Auto-Launch-R22-10087.apk`
- SHA-256: `2bef4beb5e567c43518c17980d7ab33e13c67cd16a9e6b38df2f996b91fbb3ef`
- Package: `com.gamefold.pc.v066nsrc.r22`
- versionName/versionCode: `0.7.1` / `10087`
- Source commit: `c91760fedc77a85597a0bfc153c350258175de72`
- Git tag: `gamefold-pc-v0.7.1-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`.
- Native Vulkan SHA-256: `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`.
- JVM verification: 255 tests across 46 classes; zero failures, errors, or skips.
- Installed APK bytes on device `10AF3P0SDY001M6` match the frozen APK SHA-256.

## Accepted behavior

The offline analyzer chooses launch entry points and DirectX wrappers without per-game name rules. The accepted device results are:

- Cyberpunk 2077: `bin/x64/Cyberpunk2077.exe`, VKD3D 2.14.1.
- Grand Theft Auto V: `Grand Theft Auto V/GTAVLauncher.exe` launching `GTA5.exe`, DXVK 2.4.1-gplasync.
- The Ramp: `The Ramp.exe`, DXVK 2.4.1-gplasync.
- 黑神话像素版: `BlackMythPixel.exe`, DXVK.

The user manually confirmed on 2026-08-13 that all four games run normally. The formal `0.7.1` APK was installed in place over the accepted R22 candidate without clearing data; all four library entries remained present.

## Rollback

The previous frozen `V0.7.0-NS-UI-R21-10085` artifact remains unchanged at:

`releases/baselines/V0.7.0/GameFold-PC-V0.7.0-NS-UI-R21-10085.apk`

Its SHA-256 is `c8b24fe4f9fb6c2662bf0421b17470a1d71afdefc69ae04d5de9ea0127da76a8`.

## Scope

This baseline covers GameFold PC only. GameFold FC is explicitly out of scope and unchanged.

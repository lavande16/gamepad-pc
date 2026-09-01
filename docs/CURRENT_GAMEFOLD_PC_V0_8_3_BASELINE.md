# Current GameFold PC V0.8.3 baseline

The approved GameFold PC baseline is **V0.8.3-DUAL-SCREEN-DRIVER-10139**.

- APK: `releases/baselines/V0.8.3/GameFold-PC-V0.8.3-DUAL-SCREEN-DRIVER-10139.apk`
- SHA-256: `1356aae0361bfaf7598379c70e5756a87f48a89967950a26356b1c8605f59b30`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `0.8.3` / `10139`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `5045338b0a9342918dc00bfa010fa3eef5a87a9a`
- Git tag: `gamefold-pc-v0.8.3-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,301,880` bytes

## Verification evidence

- Full unit regression: `345 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully with Gradle 8.12.1.
- `aapt dump badging` reports package `com.gamefold.pc`, version code `10139`, version name `0.8.3`, minSdk `26`, and targetSdk `28`.
- Signature verification reports APK Signature Scheme v2 `true`; v1/v3/v4 are not used.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`.
- Device `10AF3P0SDY001M6` (vivo V2436A, SM8650) reports `versionName=0.8.3`, `versionCode=10139`, first install time `2026-08-14 14:17:59`, and last update time `2026-08-15 23:51:20`.
- The installed APK was pulled read-only from the device. Its size and SHA-256 match the frozen artifact exactly: `16,301,880` bytes and `1356aae0361bfaf7598379c70e5756a87f48a89967950a26356b1c8605f59b30`.
- The vivo public V25 archive SHA-256 matches the frozen sidecar: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`.
- Installation was in place; app data, permissions, games, and containers were not cleared.
- The user explicitly accepted RC8 on OPPO and vivo and requested promotion to the `0.8.3` baseline on `2026-08-15`.

## Accepted state

- Dual-screen session closes and restores consistently across Home, desktop, background, and foreground transitions.
- Game controller input resumes after returning from the GameFold Home screen to a running game.
- Home/Back input during launch no longer leaves the app trapped in an unresponsive startup state.
- The performance HUD reports the presented/interpolated frame rate rather than an unrelated high-frequency counter.
- Public graphics-driver policy selects V25 for SM8650 and V30 for SM8750/SM8850 while preserving explicit manual choices.
- The OPPO SM8850 rendering checkerboard was resolved with V30; V25/V30 remain outside the APK in the shared GameFoldPC directory.
- Per-game settings show the persisted V30 selection on the first frame rather than briefly falling back to System.
- `设置 -> 模拟设置 -> 修改默认配置` no longer crashes from an infinite Compose height constraint.

## Public driver restore contract

- `releases/baselines/V0.8.3/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V0.8.3/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`
- Runtime destination: `/storage/emulated/0/GameFoldPC/graphics/`.
- The APK contains no V25/V30 driver payload.

## Scope and rollback

- This baseline freezes the complete approved GameFold PC 0.8.3 source/runtime state and its required public driver sidecars.
- GameFold NS is a behavior reference only; GameFold NS and GameFold FC/NES are unchanged and out of scope.
- The previous rollback baseline remains at `releases/baselines/V0.8.1/GameFold-PC-V0.8.1-NS-ABOUT-ALIGN-10130.apk`, SHA-256 `df8aff8e9556cbe69c2f753f3770fa3dc14f3d7b5c831f2dfdb1bf4cc407e870`.
- Rollback tag: `gamefold-pc-v0.8.1-baseline`.
- Future GameFold PC work should branch from `gamefold-pc-v0.8.3-baseline` and use a new candidate/version identity.

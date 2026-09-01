# Current GameFold PC V0.8.6 baseline

The approved GameFold PC baseline is **V0.8.6-ICON-CONTAINER-BACK-10144**.

- APK: `releases/baselines/V0.8.6/GameFold-PC-V0.8.6-ICON-CONTAINER-BACK-10144.apk`
- SHA-256: `7b546a5c9d0205298307f3a1cb82ed0482584dfba51a5498387c5b5f8d38f8f3`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `0.8.6` / `10144`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `7212d923918fc0ac6f0cdb22b816d91b238e7e1d`
- Git tag: `gamefold-pc-v0.8.6-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,357,456` bytes

## Verification evidence

- Full unit regression: `348 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully.
- APK badging reports `com.gamefold.pc`, `0.8.6 / 10144`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 verification passed.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`.
- Device `10AF3P0SDY001M6` (vivo V2436A) reports `0.8.6 / 10144` after an in-place update.
- The installed APK was pulled read-only; its size and SHA-256 match the frozen artifact exactly.
- App data, permissions, games, and containers were not cleared.
- The user explicitly accepted the controller B/Y fix and requested promotion to the `0.8.6` baseline on `2026-08-16`.

## Accepted state

- New GameFold PC application icon is used across launcher, Home, About, and other product surfaces.
- Home title is `GameFold PC`; About includes the Xiaohongshu and Dribbble icons.
- Global and per-game container settings remain manual-save.
- Dirty settings show the original `MessageDialog` on toolbar Back, system Back, and controller B/Y; no silent save or silent exit occurs.
- All functionality frozen in the V0.8.3 dual-screen/runtime baseline remains part of this source state.

## Public driver restore contract

- `releases/baselines/V0.8.6/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V0.8.6/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`
- Runtime destination: `/storage/emulated/0/GameFoldPC/graphics/`.

## Scope and rollback

- GameFold NS is an asset reference only; GameFold NS and GameFold FC/NES are unchanged and out of scope.
- Rollback baseline: `releases/baselines/V0.8.3/GameFold-PC-V0.8.3-DUAL-SCREEN-DRIVER-10139.apk`.
- Rollback tag: `gamefold-pc-v0.8.3-baseline`.
- Future GameFold PC work should branch from `gamefold-pc-v0.8.6-baseline` with a new candidate/version identity.

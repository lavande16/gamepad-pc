# Current GameFold PC V0.8.0 baseline

The approved GameFold PC baseline is **V0.8.0-NS-FRONTEND-ALIGN-10122**.

- APK: `releases/baselines/V0.8.0/GameFold-PC-V0.8.0-NS-FRONTEND-ALIGN-10122.apk`
- SHA-256: `52bf0311e5c1176ba8bcf1337776cc6dcf91d25ed8959d7b70a1e026175c09f0`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `0.8.0` / `10122`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `6fb9b1e5adf5688a45a76d3e2e3139b2c3463f92` (`feat: align GameFold PC 0.8.0 frontend with NS 2.9`)
- Git tag: `gamefold-pc-v0.8.0-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,119,103` bytes

## Verification evidence

- Full unit regression was re-executed with explicit 0.8.0 build properties: `255 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully with Gradle 8.12.1.
- `aapt dump badging` reports package `com.gamefold.pc`, version code `10122`, version name `0.8.0`, minSdk `26`, targetSdk `28`, and `arm64-v8a` native code.
- Signature verification reports v2 `true`; v1/v3/v4 are not used by this local baseline.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`.
- Device `10AF3P0SDY001M6` (vivo V2436A) reports `versionName=0.8.0`, `versionCode=10122`, first install and last update time `2026-08-14 14:17:59`.
- The installed APK was pulled read-only from the device. Its size and SHA-256 match the formal build artifact exactly: `16,119,103` bytes and `52bf0311e5c1176ba8bcf1337776cc6dcf91d25ed8959d7b70a1e026175c09f0`.
- No reinstall, data clearing, permission reset, game-directory change, or container mutation was performed during baseline promotion.

## Scope and rollback

- This baseline covers GameFold PC 0.8.0 only. GameFold NS and GameFold FC/NES are out of scope and unchanged.
- The frozen 0.7.2 rollback baseline remains at `releases/baselines/V0.7.2/GameFold-PC-V0.7.2-NS-UI-R56-10121.apk`, SHA-256 `00a4a5045a257b332c46a1550e418825ede37ebb88bf3db7d2b8d3ec456c06b0`.
- Rollback tag: `gamefold-pc-v0.7.2-baseline`.
- Future GameFold PC design work should branch from `gamefold-pc-v0.8.0-baseline` and use a new candidate/version identity.

## Acceptance boundary

This document freezes the exact 0.8.0 source, APK, build flags, signature, tests, and installed-device identity selected by the user. It does not claim that every GameFold NS 2.9 R2 deep page or platform-specific function is already identical. The known static differences remain recorded in `docs/gamefold/GAMEFOLD_PC_V0_8_0_NS_STATIC_ALIGNMENT.md` and the subsequent full static audit.

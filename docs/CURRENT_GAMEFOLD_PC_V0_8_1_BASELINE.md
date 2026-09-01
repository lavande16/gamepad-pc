# Current GameFold PC V0.8.1 baseline

The approved GameFold PC baseline is **V0.8.1-NS-ABOUT-ALIGN-10130**.

- APK: `releases/baselines/V0.8.1/GameFold-PC-V0.8.1-NS-ABOUT-ALIGN-10130.apk`
- SHA-256: `df8aff8e9556cbe69c2f753f3770fa3dc14f3d7b5c831f2dfdb1bf4cc407e870`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `0.8.1` / `10130`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `963abd06fdd348a37d7eeed755872b0e4a1af312`
- Git tag: `gamefold-pc-v0.8.1-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,291,692` bytes

## Verification evidence

- Full unit regression: `316 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully with Gradle 8.12.1.
- `aapt dump badging` reports package `com.gamefold.pc`, version code `10130`, version name `0.8.1`, minSdk `26`, targetSdk `28`, and `arm64-v8a` native code.
- Signature verification reports v2 `true`; v1/v3/v4 are not used by this local baseline.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`.
- Device `10AF3P0SDY001M6` (vivo V2436A) reports `versionName=0.8.1`, `versionCode=10130`, first install time `2026-08-14 14:17:59`, and last update time `2026-08-15 15:08:09`.
- The installed APK was pulled read-only from the device. Its size and SHA-256 match the frozen artifact exactly: `16,291,692` bytes and `df8aff8e9556cbe69c2f753f3770fa3dc14f3d7b5c831f2dfdb1bf4cc407e870`.
- The user explicitly accepted the final About page on `2026-08-15` and requested that this exact version be recorded as the V0.8.1 baseline.
- Installation was in place; app data and permissions were not cleared.

## Accepted About-page state

- The About page uses the GameFold NS layout and pixel wordmark implementation.
- The title bar spans the full page width and visibly displays `关于`.
- The open-source disclaimer begins with `基于 Wine、Winlator 及相关开源兼容层` and does not identify Eden as the PC runtime basis.
- The original NS About XML remains byte-identical to its source reference; PC-specific text is supplied through resources.

## Scope and rollback

- This baseline freezes the complete GameFold PC source and runtime state that produced the approved 0.8.1 APK.
- GameFold NS and GameFold FC/NES are out of scope and unchanged.
- The previous rollback baseline remains at `releases/baselines/V0.8.0/GameFold-PC-V0.8.0-NS-FRONTEND-ALIGN-10122.apk`, SHA-256 `52bf0311e5c1176ba8bcf1337776cc6dcf91d25ed8959d7b70a1e026175c09f0`.
- Rollback tag: `gamefold-pc-v0.8.0-baseline`.
- Future GameFold PC work should branch from `gamefold-pc-v0.8.1-baseline` and use a new candidate/version identity.

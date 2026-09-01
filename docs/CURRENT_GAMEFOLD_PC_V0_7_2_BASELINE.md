# Current GameFold PC V0.7.2 baseline

The approved GameFold PC baseline is **V0.7.2-NS-UI-R56-10121**.

- APK: `releases/baselines/V0.7.2/GameFold-PC-V0.7.2-NS-UI-R56-10121.apk`
- SHA-256: `00a4a5045a257b332c46a1550e418825ede37ebb88bf3db7d2b8d3ec456c06b0`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `0.7.2` / `10121`
- Build flavor: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `f309b6d` (`feat: finalize GameFold PC 0.7.2 R56 UI baseline`)
- Git tag: `gamefold-pc-v0.7.2-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,112,911` bytes

## Verification evidence

- Release build: `:app:packageLegacyRelease` completed successfully with Gradle 8.12.1.
- BuildConfig verification: `VERSION_NAME="0.7.2"`, `VERSION_CODE=10121`, `GAMEFOLD_PC_LOCAL_SHELL=true`.
- APK metadata: `aapt dump badging` reports package `com.gamefold.pc`, version code `10121`, version name `0.7.2`.
- Signature verification: v2 `true`; v1/v3/v4 are not used by this debug-signed local baseline.
- Targeted regression: `GameFoldPcFrontendPolicyTest`, 14 tests, 0 failures, 0 errors.
- Device install: user-installed on vivo `10AF3P0SDY001M6` (V2436A). `dumpsys package` reports `versionName=0.7.2`, `versionCode=10121`.
- Device artifact verification: pulled installed APK SHA-256 matches the build artifact exactly: `00a4a5045a257b332c46a1550e418825ede37ebb88bf3db7d2b8d3ec456c06b0`.

## R56 correction carried into this baseline

R55 was rejected because startup crashed when Compose `painterResource` attempted to load the NS Android layer-list button drawable. R56 keeps the NS visual asset but renders that layer-list through a native `ImageView`, avoiding the unsupported Compose painter path. The formal 0.7.2 APK was rebuilt from that corrected R56 source with the explicit PC Local Shell build flag.

## Scope and rollback

- This baseline covers GameFold PC only. GameFold FC/NES source and products are out of scope and unchanged.
- The frozen 0.7.1 baseline remains available at `releases/baselines/V0.7.1/GameFold-PC-V0.7.1-Auto-Launch-R22-10087.apk` with SHA-256 `2bef4beb5e567c43518c17980d7ab33e13c67cd16a9e6b38df2f996b91fbb3ef`.
- Rollback tag: `gamefold-pc-v0.7.1-baseline`.
- Do not overwrite either frozen APK. Future UI work should branch from `gamefold-pc-v0.7.2-baseline` and use a new candidate/version identifier.

## Acceptance boundary

This document proves source, build, package, signature, hash, targeted-test, and installation identity. It does not replace the remaining user-facing visual comparison or complete game/hand-controller behavior acceptance recorded in `docs/plans/2026-08-14-gamefold-pc-v071-ui-update-checklist.md`.

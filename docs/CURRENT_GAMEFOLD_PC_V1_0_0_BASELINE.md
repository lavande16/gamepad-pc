# Current GameFold PC V1.0.0 baseline

The approved GameFold PC baseline is **V1.0.0-GTA5-GYRO-AIM-10163**.

- APK: `releases/baselines/V1.0.0/GameFold-PC-V1.0.0-GTA5-GYRO-AIM-10163.apk`
- SHA-256: `10ccf377243cb76dd52218aa036e97b174b05cf3c5e7fb75a5ccd8adbd9b8d4d`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `1.0.0` / `10163`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `6e53da5118ab25c7be9e1d7c1cefc5e68b9fd8fe`
- Git tag: `gamefold-pc-v1.0.0-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,375,857` bytes

## Verification evidence

- Full unit regression: `394 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully.
- APK badging reports `com.gamefold.pc`, `1.0.0 / 10163`, application label `GameFold PC`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 verification passed.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`; required presented-frame symbol is present.
- Device `10AF3P0SDY001M6` (vivo V2436A) reports `1.0.0 / 10163` after an in-place update at `2026-08-16 22:02:26`.
- The installed APK was pulled read-only; its size and SHA-256 match the frozen artifact exactly.
- Existing app data, permissions, games, and containers were not cleared; `MANAGE_EXTERNAL_STORAGE` remains allowed.
- The user explicitly accepted the candidate and requested promotion to the `1.0.0` baseline on `2026-08-16`.

## Accepted state

- GTA5 ZL strictly gates gyro-to-relative-mouse aim; releasing ZL clears pending output.
- Horizontal and vertical directions are accepted. Both axes use `1x` sensitivity with `19.6 counts/degree` calibration.
- Known external requirement is intentionally preserved: GTA5 mouse input must be switched to `Windows` once before the first gyro response; after that, switching to other modes continues to respond. The user accepted this and requested no further change.
- Launcher icons use the approved PC asset and every localized launcher label is `GameFold PC`.
- In landscape, the About brand icon/title top-aligns with the current-version card; portrait retains the original `42dp` design.
- All accepted V0.8.6 container-setting behavior and V0.8.3 dual-screen/runtime functionality remain part of this source state.

## Public driver restore contract

- `releases/baselines/V1.0.0/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V1.0.0/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`
- Runtime destination: `/storage/emulated/0/GameFoldPC/graphics/`.

## Scope and rollback

- GTA5 is the real-device gyro-aim acceptance target; this baseline does not claim equivalent behavior for every PC game.
- GameFold NS and GameFold FC/NES are unchanged and out of scope.
- Rollback baseline: `releases/baselines/V0.8.6/GameFold-PC-V0.8.6-ICON-CONTAINER-BACK-10144.apk`.
- Rollback tag: `gamefold-pc-v0.8.6-baseline`.
- Future GameFold PC work should branch from `gamefold-pc-v1.0.0-baseline` with a new candidate/version identity.

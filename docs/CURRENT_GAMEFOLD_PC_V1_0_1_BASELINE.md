# Current GameFold PC V1.0.1 baseline

The approved GameFold PC baseline is **V1.0.1-PORTRAIT-GYRO-DIRECTION-10164**.

- APK: `releases/baselines/V1.0.1/GameFold-PC-V1.0.1-PORTRAIT-GYRO-DIRECTION-10164.apk`
- SHA-256: `3671ecfabbc8d7892f0ec5eb086449c44fe9b4bcf0de78889efa10fda07d584d`
- Package: `com.gamefold.pc`
- `versionName` / `versionCode`: `1.0.1` / `10164`
- Build variant: `legacyRelease`
- PC shell flags: `GAMEFOLD_PC_LOCAL_SHELL=true`, `GAMEFOLD_PC_LOCAL_WINE_FAST_PATH=false`
- Source commit: `495886971537609fdcb7bafe3948acb31a885bf4`
- Git tag: `gamefold-pc-v1.0.1-baseline`
- Signature: APK Signature Scheme v2; certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`
- APK size: `16,376,037` bytes

## Verification evidence

- Full unit regression: `397 tests / 0 failures / 0 errors / 0 skipped`.
- `:app:assembleLegacyRelease` completed successfully.
- APK badging reports `com.gamefold.pc`, `1.0.1 / 10164`, application label `GameFold PC`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 verification passed.
- `lib/arm64-v8a/libvulkan_renderer.so` SHA-256 is `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`; the required presented-frame symbol is present.
- Device `3B163600D6E00000` (OPPO PLP110) reports `1.0.1 / 10164` after an in-place update at `2026-08-17 00:08:11`.
- The installed APK was pulled read-only; its size and SHA-256 match the frozen artifact exactly.
- Existing app data, permissions, games, and containers were not cleared; `MANAGE_EXTERNAL_STORAGE` remains allowed.
- The user tested the candidate without issue and explicitly requested promotion to the `1.0.1` baseline on `2026-08-17`.

## Accepted state

- GTA5 ZL strictly gates gyro-to-relative-mouse aim; releasing ZL clears pending output.
- Landscape keeps the accepted horizontal and vertical direction mapping unchanged.
- Portrait inverts both gyro mouse output axes before fractional accumulation, correcting the portrait-only reversal without changing sensitivity, timing, calibration, filtering, or ZL behavior.
- Both axes retain `1x` sensitivity with `19.6 counts/degree` calibration.
- The accepted external requirement remains unchanged: GTA5 mouse input must be switched to `Windows` once before the first gyro response; switching back afterward remains responsive.
- All launcher identity, landscape-only About alignment, container-setting, dual-screen, and runtime behavior accepted in V1.0.0 remain part of this source state.

## Public driver restore contract

- `releases/baselines/V1.0.1/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V1.0.1/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`
- Runtime destination: `/storage/emulated/0/GameFoldPC/graphics/`.

## Scope and rollback

- GTA5 is the real-device gyro-aim acceptance target; this baseline does not claim equivalent behavior for every PC game.
- GameFold NS and GameFold FC/NES are unchanged and out of scope.
- Rollback baseline: `releases/baselines/V1.0.0/GameFold-PC-V1.0.0-GTA5-GYRO-AIM-10163.apk`.
- Rollback tag: `gamefold-pc-v1.0.0-baseline`.
- Future GameFold PC work should branch from `gamefold-pc-v1.0.1-baseline` with a new candidate/version identity.

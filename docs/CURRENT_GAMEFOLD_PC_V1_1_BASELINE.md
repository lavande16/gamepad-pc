# Current GameFold PC V1.1 baseline

The approved GameFold PC baseline is **V1.1-GYRO-SENSITIVITY-125-10176**. It promotes the user-accepted `V1.0.11-GYRO-SENSITIVITY-125-RC1` behavior with the formal Android identity `1.1 / 10176`.

- APK: `releases/baselines/V1.1/GameFold-PC-V1.1-GYRO-SENSITIVITY-125-10176.apk`
- Package: `com.gamefold.pc`
- Version: `1.1 / 10176`
- SHA-256: `5bdb1221a88d2c8eb23bbd3851a4d442b7c896284fd4c37637fe99ba59ebbca8`
- APK size: `16,554,211` bytes
- Feature source commit: `7e850d7815530fdd45043346835442907b807571`
- Formal identity source commit: `6311177a47870d31e23a02c711bae061d3a1db4b`
- Git tag: `gamefold-pc-v1.1-baseline`
- Previous formal rollback tag: `gamefold-pc-v1.0.7-baseline`

## Accepted behavior

- PC ABXY layout is top Y, right B, bottom A, and left X; default/pressed assets and touch semantics match.
- Existing enlarged touch geometry remains unchanged.
- GTA5 gyro aim remains strictly ZL-gated and uses `1.25x` horizontal and vertical sensitivity, producing `24.5` effective mouse counts per physical degree.
- Gyro direction, stillness calibration, timing cap, filtering, and fractional accumulation remain unchanged.
- GTA5 and Cyberpunk 2077 save auto-import behavior remains present and no-overwrite.
- Formal baseline builds disable `run-as`; the manifest reports `android:debuggable=false`.

## Verification evidence

- Forced no-cache-equivalent Gradle run executed all `100` tasks and completed successfully.
- `449` Legacy Release unit tests passed with `0 failures / 0 errors / 0 skipped`.
- APK badging reports `com.gamefold.pc`, `1.1 / 10176`, minSdk `26`, and targetSdk `28`.
- APK Signature Scheme v2 verification passed with certificate SHA-256 `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`.
- APK manifest reports `android:debuggable=false`.
- Device `43925730` / vivo `V2436A` reports `1.1 / 10176` after an in-place update at `2026-08-20 20:34:48`; first-install time `2026-08-19 01:15:52` and storage permission were preserved.
- `run-as com.gamefold.pc` is rejected because the package is not debuggable.
- The pulled installed APK matches the frozen baseline APK byte-for-byte and has the same SHA-256.
- User accepted the V1.0.11 GTA5 ZL-held gyro behavior before promotion; the V1.1 formal build changes only release identity and debug access policy.

## Frozen external recovery assets

- `releases/baselines/V1.1/GameFoldPC/graphics/adrenotools-Turnip_Gen8_V25.tzst`
  - Size: `2,529,392` bytes
  - SHA-256: `b8c33c0feae648c0c73533000ed17efdcb653d7dce06a0a5c8d7d0f58b9a0e44`
- `releases/baselines/V1.1/GameFoldPC/graphics/adrenotools-Turnip Gen8 V30.tzst`
  - Size: `1,968,250` bytes
  - SHA-256: `0705aed1537c2df5a61125a1479fc6551ea11f982075ae0a1d6d2b9fecff0f83`

## Restore contract

- Restore code from Git tag `gamefold-pc-v1.1-baseline`.
- Restore the exact APK and external driver assets from `releases/baselines/V1.1/`.
- Verify every SHA-256 before installation.
- Do not replace this directory, pointer, or tag with a later candidate.
- V1.0.7 and earlier baselines remain frozen; use `gamefold-pc-v1.0.7-baseline` for the direct formal rollback.

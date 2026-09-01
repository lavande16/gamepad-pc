# Current GamePad PC V1.4 baseline

The approved long-term personal baseline is **V1.4-RUN-AS-DIAGNOSTIC-10012**.

- APK: `releases/baselines/GamePad-PC-V1.4/GamePad-PC-V1.4-RUN-AS-DIAGNOSTIC-10012.apk`
- Package/version: `com.gamepad.pc`, `1.4 / 10012`
- SHA-256: `f3575ed0362241627c2a21633e1db8e5fac52a3a937d8fd34b96fac6f5a30f3d`
- Functional source commit: `44f1e27835494aeb6b993707842aca74c83de176`
- Git tag: `gamepad-pc-v1.4-baseline`
- Run-as: enabled by default for this personal baseline

This baseline preserves the actual 1.3/10009 diagnostic behavior: ADB and controller quick menus, GamePad screen-size defaults and migration, wide-screen insets, and XServer drawable stability fixes. It contains no NFS11 HF1/HF2 runtime policy.

Verification is frozen in `releases/baselines/GamePad-PC-V1.4/BASELINE.json`: 500 tests passed, the Release build completed, device installation succeeded, run-as entered the app UID, and the pulled installed APK matched the archive byte-for-byte. HF1/HF2 APKs were removed and the NFS11 `.box64rc` is absent from the device.

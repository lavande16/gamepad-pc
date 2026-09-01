# Current GamePad PC V2.0 baseline

The approved long-term personal baseline is **V2.0-NFS11-GYRO-STEERING-10025**.

- APK: `releases/baselines/GamePad-PC-V2.0/GamePad-PC-V2.0-NFS11-GYRO-STEERING-10025.apk`
- Package/version: `com.gamepad.pc`, `2.0 / 10025`
- SHA-256: `992a7ca18afd5c5eac916d9666b75c2d01df19164c02592f8db02a4ed8d27a8a`
- Functional source commit: `5a0dfcf9cd28a90c39ccda10fd6ff94b4b4f3a8c`
- Git tag: `gamepad-pc-v2.0-baseline`
- Run-as: enabled by default for this personal baseline

V2.0 freezes NFS11-only automatic gyro steering with screen-rotation-aware wheel/yaw mapping, accepted steering directions, intent-axis latching, a 1-degree deadzone, and 26-degree full scale. The in-game diagnostic card is no longer attached, so normal play has no W/Y/X readout, mode button, or recenter button; AUTO remains the default and internal `GameFoldSteeringTrace` logs remain available through ADB.

Verification is frozen in `releases/baselines/GamePad-PC-V2.0/BASELINE.json`: 563 tests passed, the Release build completed, APK identity and v2 signature were verified, installation succeeded, `WRITE_SECURE_SETTINGS` and run-as were available, the NFS11 gyro session registered and calibrated, the device screenshot showed no diagnostics card, and the pulled installed APK matched the archive byte-for-byte.

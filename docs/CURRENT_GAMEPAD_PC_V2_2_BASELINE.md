# Current GamePad PC V2.2 baseline

The approved personal baseline is **V2.2-NFS11-GYRO-TOGGLE-10027**.

- APK: `releases/baselines/GamePad-PC-V2.2/GamePad-PC-V2.2-NFS11-GYRO-TOGGLE-10027.apk`
- Package/version: `com.gamepad.pc`, `2.2 / 10027`
- SHA-256: `4b8fc805deffb6559bc506b4f5ab134a9ae1b65718ba50e74d84d30b936213cb`
- Functional source commit: `469ca5dc027dff63984335d73e22bc26ac756344`
- Git tag: `gamepad-pc-v2.2-baseline`
- Run-as: enabled by default for this personal baseline

V2.2 points the add-game dialog at the exact user-approved 3DM URL and adds a per-container “Tilt steering” switch under General settings. The switch is visible only for the original `NFS11.exe`, defaults to enabled for both new and legacy containers, persists across container reload, and prevents creation of the gyro steering controller when disabled. Other games and NFS remastered are excluded.

Verification is frozen in `releases/baselines/GamePad-PC-V2.2/BASELINE.json`: 571 tests passed, the Release build completed, APK identity and v2 signature were verified, installation succeeded, `WRITE_SECURE_SETTINGS` and run-as remain available, and the pulled installed APK matches the archived APK byte-for-byte. Final in-game interaction acceptance of the new switch remains a user-visible gate.

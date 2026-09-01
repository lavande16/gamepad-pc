# GameFold PC V0.6 icon and default-settings implementation plan

**Goal:** Build and install an in-place V0.6-derived candidate with the NS icon, Proton 10/Simplified Chinese container defaults, LSFG on, FPS-only HUD on, and no Information section.

**Architecture:** Keep runtime code untouched. Introduce a small pure product-default policy and make existing default factories/readers consume it. Gate only the Information composable with the existing local-shell build flag. Replace launcher resources without adding bitmap weight.

**Tech stack:** Android/Kotlin/Java, Jetpack Compose, Gradle, shell APK guards, ADB.

---

### Task 1: Freeze V0.6

- Archive the accepted 10045 APK under `releases/baselines/V0.6/`.
- Record exact package, version, signature, SHA-256, source commit, device evidence, and rollback rules.

### Task 2: Add failing policy tests

- Add tests for Proton identifier, Simplified Chinese identifier, LSFG enabled, container FPS enabled, HUD enabled, and FPS-only metrics.
- Extend product-mode tests so local shell hides Information while the upstream mode can still show it.
- Add a resource guard that requires default and alternate launcher definitions to be identical and to reference the NS source vector.

### Task 3: Implement the smallest policy change

- Add the GameFold PC defaults object.
- Use it in `PrefManager`, `ContainerData`, `ContainerUtils`, and container application/creation paths.
- Preserve explicitly persisted user values.
- Hide the Information section only when `GAMEFOLD_PC_LOCAL_SHELL` is true.

### Task 4: Replace launcher artwork

- Copy the NS canonical launcher vector and monochrome mark.
- Make default, round, alternate, and alternate-round adaptive icons resolve to the same artwork.
- Make the two notification icon styles use the same NS monochrome mark.

### Task 5: Verify and package candidate 10046

- Run targeted release unit tests.
- Build legacy release with package `com.gamefold.pc.v04localshell`, versionCode `10046`, and a V0.6-derived versionName.
- Run the arm64/offline/LSFG APK guard and icon/default static guard.
- Record APK metadata, signer, size, and SHA-256.

### Task 6: Install and verify on device

- Install in place; do not uninstall or clear data.
- Verify package/version, pulled-back APK hash, preserved first-install time, and storage authorization.
- Verify launcher/settings UI and check logcat for fatal exceptions.
- Launch the existing GTA5 entry only if needed to confirm HUD/LSFG and runtime stability; do not change the frozen V0.6 artifact.

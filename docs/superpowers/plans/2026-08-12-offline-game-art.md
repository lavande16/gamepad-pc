# Offline Game Art Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Show a locally resolved square game image and title on the GameFold PC local library without network access or changes to the runtime launch path.

**Architecture:** Add a pure folder-based artwork resolver with explicit filename priority, executable-icon extraction fallback, and ambiguity-safe image fallback. Resolve paths on the existing IO refresh path for the local screen, then render them through the existing Coil image stack with a bundled placeholder. Keep tap and long-press callbacks unchanged.

**Tech Stack:** Kotlin, JUnit, Compose Material 3, Landscapist Coil, existing `ExeIconExtractor` and `IconDecoder`.

## Global Constraints

- Only inspect `/storage/emulated/0/GameFoldPC/game/<game>` and its immediate child directories.
- Never download artwork or pass HTTP URLs to the local artwork renderer.
- Supported local artwork extensions are PNG, JPG, JPEG, WebP, and ICO.
- Preserve existing game launch, long-press settings, Bionic, Proton, DXVK, input, and LSFG behavior.
- Use the existing separate `com.gamefold.pc.v04localshell` candidate line; do not modify the frozen V0.4 baseline.

---

### Task 1: Add and test the local artwork priority resolver

**Files:**
- Create: `app/src/main/java/app/gamenative/utils/LocalGameArtworkResolver.kt`
- Create: `app/src/test/java/app/gamenative/utils/LocalGameArtworkResolverTest.kt`

**Interfaces:**
- `LocalGameArtworkResolver.resolve(folder: File, preferredExecutableRelativePath: String? = null): File?`
- Resolution order: `cover_square`, `cover`; then `coverv`, `poster`, `artwork`; then `icon`, `logo`, `steamgriddb_logo`; then preferred executable icon; then unique executable icon; then unique/unambiguous nearby image.

- [ ] **Step 1: Write failing tests** for explicit cover priority, case-insensitive extension preference, executable fallback ordering, unique nearby image fallback, ambiguous nearby images returning null, and missing folders returning null.
- [ ] **Step 2: Run the resolver test and confirm it fails** because the resolver does not exist.
- [ ] **Step 3: Implement the resolver** with root-only named candidates, one-level fallback scanning, cached `.extracted.ico` handling, and no network types or URLs.
- [ ] **Step 4: Run the resolver tests and confirm they pass.**
- [ ] **Step 5: Commit** the resolver and tests with `feat: resolve offline local game artwork`.

### Task 2: Connect artwork resolution to local game cards

**Files:**
- Modify: `app/src/main/java/app/gamenative/utils/CustomGameScanner.kt`
- Modify: `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt`

**Interfaces:**
- Add `CustomGameScanner.findLocalArtworkForCustomGame(context: Context, appId: String): String?`, returning only a `file://` URI or null.
- Keep `LibraryItem` and launch callbacks unchanged.

- [ ] **Step 1: Add a failing integration-level unit test** proving a custom-game app ID resolves the root `cover_square.*` path through the scanner boundary without HTTP fallback.
- [ ] **Step 2: Run the test and confirm it fails** because the scanner boundary is absent.
- [ ] **Step 3: Implement the scanner boundary** by resolving the game folder, reading the configured container executable when available, and delegating to `LocalGameArtworkResolver`.
- [ ] **Step 4: Add an IO-backed `artworkByAppId` refresh map** in `OfflineLibraryScreen`; clear stale entries on refresh and resolve custom-game artwork off the main thread.
- [ ] **Step 5: Run the integration test and existing offline tests.**
- [ ] **Step 6: Commit** the scanner integration with `feat: expose offline game artwork to local library`.

### Task 3: Render square cards and produce the candidate APK

**Files:**
- Modify: `app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt`
- Test: existing offline and resolver test suites

- [ ] **Step 1: Replace the instructional subtitle** with a square `CoilImage` using `ContentScale.Crop`, local file URI only, and `R.drawable.ic_logo_color` as deterministic placeholder.
- [ ] **Step 2: Keep the existing `combinedClickable` tap and long-press callbacks unchanged**, with the game title directly below the image.
- [ ] **Step 3: Run targeted unit tests and the legacy release compile.**
- [ ] **Step 4: Build the separate local-shell release with versionCode `10034` and versionName `0.4.4-v04-local-shell-artwork`.
- [ ] **Step 5: Verify package metadata, v2 signature, APK hash, and confirm runtime implementation files are not changed by this feature.**
- [ ] **Step 6: Uninstall `com.gamefold.pc.v04localshell`, install 10034 with the vivo installer, and verify the installed identity.
- [ ] **Step 7: Device-test GTA5 artwork, placeholder behavior, tap launch, and long-press details; leave the frozen V0.4 baseline untouched.
- [ ] **Step 8: Commit** the UI and packaging changes with `feat: show offline game artwork cards`.

# GameFold PC 0.6.6 × GameFold NS 2.9 R2 Integration Design

## Purpose

Build the next GameFold PC candidate by combining the frozen GameFold PC 0.6.6 runtime with the GameFold NS 2.9 R2 official product design. The resulting application must present the same shell, controller, pixel visual language, colors, library, loading experience, settings presentation, orientation behavior, unsupported-window protection, and vivo rear-touch experience as GameFold NS while continuing to run PC games through the proven GameFold PC runtime.

This design is approved for autonomous execution and autonomous engineering acceptance. User-visible visual and tactile acceptance remains a final promotion gate; an automatically verified candidate must not overwrite or replace a named baseline without explicit user approval.

## Authoritative inputs

### PC runtime baseline

- Git tag: `gamefold-pc-v0.6.6-baseline`
- Tag commit: `139d0d460038c9175f1ef27c4ade5e2189c01729`
- Functional source commit: `c0320448b6256ada0d6e175385d4c8ca43df64d9`
- APK: `releases/baselines/V0.6.6/GameFold-PC-V0.6.6-Baseline-10057.apk`
- Package: `com.gamefold.pc.v04localshell`
- versionCode: `10057`
- versionName: `0.6.6`
- APK SHA-256: `3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad`
- Signer SHA-256: `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`

The integration worktree is `/Users/11085429/Documents/Game Fold/gamefold-pc-ns-ui-integration` on branch `codex/gamefold-pc-ns-ui-integration`. It is based on the frozen 0.6.6 tag above. V0.6.5 remains an immutable historical comparison only.

### Official UI and interaction baseline

- APK: `/Users/11085429/Documents/Game Fold/baselines/GameFold-2.9-Mainline-Baseline-R2/GameFold-2.9-Baseline-R2.apk`
- Package: `com.switch.fold.baseline.lite`
- versionCode: `33466084`
- versionName: `2.9`
- APK SHA-256: `156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292`

The current NS Android working tree is a migration reference, not a proven 2.9 R2 source snapshot: its Git HEAD is the older 2.1 baseline and later UI work is uncommitted. When source, screenshot, and frozen APK disagree, the frozen 2.9 R2 APK and accepted device behavior win.

## Product definition

“GameFold official design” means the complete GameFold NS 2.9 R2 foreground product experience, including:

- controller geometry, artwork, hit regions, pressed states, haptics, sounds, L3/R3 long-press behavior, and navigation semantics;
- pixel palette, typography, borders, focus states, icons, panels, dialogs, loading states, empty states, error states, and progress components;
- the upper 16:9 display aperture and lower controller composition in landscape and portrait;
- home-library cartridge cards with an outer cartridge, square PC artwork, and title below;
- add-game dialog, global advanced settings, per-game settings, game loading, top-left pixel FPS, and runtime transitions;
- explicit orientation switching, multi-window/PiP/small-window blocking, input release on unsupported layouts, and state recovery after returning to a supported layout;
- vivo dual-screen enable/disable lifecycle, rear ZL/ZR touch surface, front edge feedback, and blank-frame shutdown protection.

Unmentioned foreground UI and interaction states inherit NS 2.9 R2 behavior. Switch-specific content and runtime semantics do not: Switch ROM, firmware, keys, driver packages, `NativeConfig`, `NativeLibrary`, `NativeInput`, Yuzu `Game`, and Yuzu Surface code are replaced by PC equivalents.

## Architecture

The PC 0.6.6 repository remains the application and runtime base. A bounded GameFold product-shell layer is added above it. UI code depends on explicit ports instead of reaching into Wine, XServer, Yuzu, or online-platform models.

### Required ports

```kotlin
data class LocalGameSummary(
    val appId: String,
    val title: String,
    val artworkPath: String?,
    val folderAvailable: Boolean,
)

interface GameCatalogPort {
    suspend fun scanGames(): List<LocalGameSummary>
    suspend fun refresh(): List<LocalGameSummary>
}

sealed interface RuntimeLaunchState {
    data object Idle : RuntimeLaunchState
    data class Checking(val message: String) : RuntimeLaunchState
    data class Installing(val progress: Float?, val message: String) : RuntimeLaunchState
    data class Booting(val game: LocalGameSummary, val message: String) : RuntimeLaunchState
    data class Running(val appId: String) : RuntimeLaunchState
    data class Failed(val appId: String?, val error: RuntimeLaunchError) : RuntimeLaunchState
}

data class RuntimeLaunchError(
    val kind: Kind,
    val message: String,
    val recoverable: Boolean,
) {
    enum class Kind { PERMISSION, PUBLIC_PAYLOAD, MISSING_EXECUTABLE, RUNTIME_SETUP, GUEST_EXIT, UNKNOWN }
}

enum class RuntimeLaunchMode { GAME, CONTAINER, GRAPHICS_TEST, DIAGNOSTIC }

@JvmInline
value class GameFoldSessionId(val value: String)

sealed interface RuntimeSessionState {
    data object Idle : RuntimeSessionState
    data class Preparing(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Checking(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Installing(val id: GameFoldSessionId, val appId: String, val progress: Float?) : RuntimeSessionState
    data class Booting(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Running(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Paused(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Stopping(val id: GameFoldSessionId, val appId: String) : RuntimeSessionState
    data class Failed(val id: GameFoldSessionId?, val appId: String?, val error: RuntimeLaunchError) : RuntimeSessionState
}

sealed interface SessionCommandResult {
    data class Applied(val id: GameFoldSessionId) : SessionCommandResult
    data class AlreadyInState(val id: GameFoldSessionId) : SessionCommandResult
    data class Rejected(val reason: String) : SessionCommandResult
    data class Failed(val error: RuntimeLaunchError) : SessionCommandResult
}

interface RuntimeSessionPort {
    val state: StateFlow<RuntimeSessionState>
    suspend fun launch(appId: String, mode: RuntimeLaunchMode = RuntimeLaunchMode.GAME): SessionCommandResult
    suspend fun pause(id: GameFoldSessionId): SessionCommandResult
    suspend fun resume(id: GameFoldSessionId): SessionCommandResult
    suspend fun stop(id: GameFoldSessionId): SessionCommandResult
}

enum class GameFoldButton {
    DPAD_UP, DPAD_RIGHT, DPAD_DOWN, DPAD_LEFT,
    X, A, B, Y, MINUS, PLUS, L, R, ZL, ZR, L3, R3, HOME,
}

enum class GameFoldStick { LEFT, RIGHT }

enum class InputSource { FRONT_TOUCH, REAR_TOUCH, PHYSICAL }

interface RuntimeInputLease : AutoCloseable {
    fun setButton(button: GameFoldButton, pressed: Boolean)
    fun setTrigger(button: GameFoldButton, value: Float)
    fun setStick(stick: GameFoldStick, x: Float, y: Float)
    fun releaseAll()
    override fun close()
}

interface RuntimeInputPort {
    fun attach(source: InputSource, slot: Int = 0): RuntimeInputLease
}

interface PerformanceStatsPort {
    val framesPerSecond: StateFlow<Int>
}

sealed interface GameFoldSettingValue {
    data class Toggle(val enabled: Boolean) : GameFoldSettingValue
    data class Choice(val selectedId: String, val choices: List<String>) : GameFoldSettingValue
    data class Range(val value: Float, val minimum: Float, val maximum: Float, val step: Float) : GameFoldSettingValue
    data class Text(val value: String) : GameFoldSettingValue
    data object Action : GameFoldSettingValue
}

data class GameFoldSettingItem(
    val id: String,
    val title: String,
    val summary: String?,
    val value: GameFoldSettingValue,
    val enabled: Boolean = true,
)

data class GameFoldSettingsSection(val id: String, val title: String, val items: List<GameFoldSettingItem>)
data class GameFoldSettingsSnapshot(val sections: List<GameFoldSettingsSection>)
sealed interface GameFoldSettingsScope {
    data object Global : GameFoldSettingsScope
    data class Game(val appId: String) : GameFoldSettingsScope
}

interface GameFoldSettingsRepository {
    fun observe(scope: GameFoldSettingsScope): Flow<GameFoldSettingsSnapshot>
    suspend fun update(scope: GameFoldSettingsScope, itemId: String, value: GameFoldSettingValue)
}

interface PackagePreflightPort {
    val state: StateFlow<StartupState>
    suspend fun advance()
    suspend fun retry()
}

interface RearDisplayPort {
    val state: StateFlow<RearDisplayState>
    fun isSupported(): Boolean
    suspend fun enable()
    suspend fun disable()
}
```

Names can be adjusted only before their first consumer is committed. UI models must not expose the existing online-heavy `LibraryItem` directly.

### Runtime boundaries that remain intact

- the `CUSTOM_GAME_` plus numeric-ID identity format and appId-to-directory mapping.
- Canonical per-game folder isolation and `A:` drive binding.
- Container persistence, runtime migrations, and per-game defaults.
- Bionic, Proton 10 ARM64EC, Turnip, DXVK/VKD3D, ImageFS, container patterns, and public resource aliases.
- XServer `ScreenInfo`, `XServerView`/`XServerViewGL`, Surface, AHB, LSFG implicit layer, and Wine launch lifecycle.
- GTA-only command-line synchronization and the safe `1067x600` default/minimum behavior.
- `WinHandler`/evshim 64-byte, four-slot shared-memory protocol and XInput/DirectInput DLL behavior.
- Complete pause/resume/teardown behavior in the existing PC runtime.

The original runtime Views are hosted within the GameFold display aperture. They are not reimplemented as an NS Surface.

## First-run startup and public-runtime assembly

The order follows GameFold NS 2.9 R2:

1. Processor/SoC compatibility gate.
2. All-files/storage permission explanation and system authorization.
3. Validation of `/storage/emulated/0/GameFoldPC/manifest.json` and every required public archive by path, size, and SHA-256.
4. Common runtime assembly before the home library opens.
5. Home library.

The processor gate runs before permission. The first implementation preserves the NS unsupported-MediaTek rule as the product behavior and adds arm64/required-capability facts without inventing an unapproved broad device blacklist.

### Scheme A: common runtime assembly

The existing `/storage/emulated/0/GameFoldPC` payload is the source. The application does not download or synthesize a new 600 MB seed package.

Before home:

- unpack and validate `imagefs/imagefs_bionic.txz` into the application-private ImageFS;
- install `proton/proton-10.0-arm64ec.wcp` into the shared private Proton content directory;
- deploy the shared `redirect.tzst`/guest libraries and `wincomponents/extras.tzst` that the existing ImageFS installer already treats as common ImageFS content;
- create and verify the required shared-home and active-Proton links;
- stage the entire managed ImageFS, shared redirect/guest content, common extras and exact Proton entry in sibling private directories on the same filesystem;
- write a commit journal and backup the last valid managed runtime, atomically switch the staged runtime, then validate the switched runtime before deleting the backup;
- write an atomic startup receipt containing the public manifest fingerprint, ImageFS version, Proton version, runtime schema version, and completion status only after the switch and post-inspection succeed;
- validate the resulting private ImageFS and Proton installation.

Validate now but defer extraction until a game/container needs it:

- common and GameNative container patterns;
- FEXCore, PulseAudio and ARM64EC/x86_64 input archives;
- Turnip, DXVK, D8VK, VKD3D, wincomponents, LSFG and configuration files.

Remain per-game:

- create or migrate a concrete game container;
- map its folder to `A:`;
- apply its per-game settings and resolve its executable;
- write GTA-specific launch arguments when applicable;
- activate the container and start XServer/Wine.

Assembly is idempotent. First provision, receipt/manifest metadata change, incomplete private runtime, or an explicit force performs full manifest SHA validation before any private write. A fully matching receipt and valid private runtime may use a cheap path/size/metadata check and skip extraction. An incomplete receipt never counts as success. A failed attempt, ENOSPC or process death must preserve or restore the last valid runtime using the journal/backup on the next launch. Existing user-imported Wine/Proton installations and per-game containers are never deleted by first-run assembly, and the existing per-game full install/validation path remains defense in depth.

### V0.6.6 product defaults retained by the integration base

The integration keeps the frozen V0.6.6 semantics while replacing only its foreground presentation: LSFG flow scale `0.50`, FSR Fit screen effects, Steam Input disabled, XInput and DInput enabled, Preferred API BOTH, Standard DInput mapper, quick menu disabled, exit warning enabled, GameFold splash branding with fixed tips hidden, icon-style entry hidden without deleting icon resources, one-time global `1280x720`/empty migration to `1067x600`, and new games inheriting the global template rather than hard-coding a per-game resolution. Existing custom global values, per-game resolutions and later manual edits are preserved.

## UI implementation

### Official shell and controller

Port the NS 2.9 R2 drawing/layout behavior from `GameFoldShellLayout`, `GameFoldViewportLayout`, and `GameFoldControllerInput` with their resources. Keep the exact visual geometry and input feedback. Replace the Switch bridge with a PC adapter that writes the existing PC `GamepadState`/`WinHandler` path. Every cancel, focus loss, unsupported-window transition, orientation change, background transition, and session stop calls `releaseAll()` to prevent stuck input.

The GameFold shell is a controlled Android View hosted from the PC Compose application where needed. This hybrid boundary maximizes fidelity without replacing PC navigation, DI, persistence, or runtime lifecycle.

### Home library

- Use the NS cartridge proportions, frame, focus treatment, square cover window, two-line title, add card, header, orientation and dual-screen controls.
- Read games and artwork through `GameCatalogPort` backed by `CustomGameScanner` and `LocalGameArtworkResolver`.
- The add-game dialog displays `/storage/emulated/0/GameFoldPC/game/`.
- The existing download URL stays unchanged.
- Click launches the game; long press opens that game's GameFold-styled settings/properties page.
- The home settings icon opens advanced settings directly.

### Loading, errors, and FPS

Map PC prelaunch and runtime events into `RuntimeLaunchState`. The NS pixel loading panel displays cover, title, determinate/indeterminate progress, errors, and retry. No previous game's frame may leak during game switching or failed startup.

Reuse `PixelNumberView` for the top-left FPS presentation. The source remains the PC renderer's real FPS provider. FPS sampling stops when disabled or the session ends.

### Settings

Reuse the NS pixel presentation, navigation, section headers, controls, focus behavior, dialogs and game-properties structure. All values and persistence bind to PC Container/DataStore/Room state.

Switch-only settings are excluded. PC-relevant global and per-game settings remain available, including runtime, Wine/Proton, graphics driver, resolution, LSFG, input and diagnostics. Online-account/store/recommendation/update remnants are not exposed in the official PC foreground UI.

### Orientation and unsupported windows

Orientation is an explicit persisted portrait/landscape choice and does not follow the sensor. The same choice applies to startup, library, settings and runtime. Unsupported multi-window, PiP, or undersized layouts show the NS unsupported state, hide child content, swallow UI touches, and release all controller state. Returning to a supported window restores the session without reconstructing or relaunching the game.

### Dual-screen and rear touch

Port the NS vivo lifecycle and rear-touch visuals through `RearDisplayPort`. The vivo implementation retains the proven device-state/display commands, display selection, independent rear activity, ZL/ZR hit testing, front edge feedback, and committed blank-frame-before-disable rule. Unsupported manufacturers hide the control. The PC generic Presentation path remains a separate fallback and must not masquerade as the validated vivo rear-screen path.

## V0.6.6 baseline provenance

V0.6.6 is already terminal and accepted at source commit `c0320448b6256ada0d6e175385d4c8ca43df64d9`, frozen tag commit `139d0d460038c9175f1ef27c4ade5e2189c01729`, APK `releases/baselines/V0.6.6/GameFold-PC-V0.6.6-Baseline-10057.apk`, SHA-256 `3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad`, package `com.gamefold.pc.v04localshell`, version `0.6.6` / `10057`, signer `24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`, and 227 supported Legacy tests across 39 classes with zero failures/errors/skips. The integration branch is based on this tag, so the 0.6.6 behavior is retained as the runtime/settings input rather than re-imported from an intermediate checkout.

The baseline's known paired semantics remain atomic in later edits: quick-menu gating `c343ccf` and corrected disabled-menu Back/exit routing `6fe04e0`; the final Proton selector correction `c032044` is included by the frozen tag. Presentation changes are superseded by the official NS shell, while settings/migration semantics remain preserved through adapters and tests.

## Autonomous execution policy

The agent may continue across stages without requesting routine approval. It may create commits, run offline tests, build a separately named candidate, install it in place on the currently connected device, use ADB for non-destructive UI/runtime verification, and iterate on failures that are clearly within this design.

The agent must stop and report rather than guess when:

- a product choice has two materially different user-visible outcomes not resolved here;
- an operation would overwrite a frozen artifact, clear app data, uninstall the package, reset file authorization, or delete user games/resources;
- the frozen NS APK and available source cannot resolve a disputed visual/interaction detail;
- runtime regressions persist after isolating and reverting the current stage;
- device confirmation requires a physical fold/rear-screen action or security dialog that ADB cannot safely complete;
- a candidate would require package/signature/data migration outside the existing in-place upgrade contract.

## Verification and promotion

### Automated gates per task

- Start with a failing focused test or executable artifact/resource guard.
- Implement the smallest passing change.
- Run focused tests plus affected 0.6.6 contract suites.
- Run compile/resource linking where UI resources changed.
- Commit one independently reviewable behavior.

### Phase gates

- Baseline: the frozen 0.6.6 tag's 227 supported Legacy tests across 39 classes pass before implementation.
- Runtime: no new failures or errors relative to the exact frozen 0.6.6 Legacy inventory; targeted local-shell/runtime tests are green.
- UI: deterministic screenshot/golden or geometry tests cover startup states, home, loading, settings, portrait, landscape and unsupported mode.
- Input: button press/release pairs, axes, cancel/focus/orientation release, multi-touch, L3/R3 threshold and HOME/session behavior are automatically tested.
- Artifact: Legacy arm64 release builds; package and signer remain unchanged; versionCode is greater than 10057; APK identity and SHA-256 are recorded.
- Device: install is in place with unchanged first-install time and file authorization; startup, library, settings and at least Black Myth Pixel plus GTA V launch are exercised; XServer/Surface/AHB resolution, LSFG, FPS and fatal/ANR logs are checked.
- Foldable: portrait/landscape, unsupported-window recovery and input release are checked on the connected foldable.
- Rear display: vivo enable, rear ZL/ZR, front feedback, Home/return, disable and no-flash shutdown require physical-device observation. Until this is observed, report it as pending physical acceptance rather than complete.

### Baseline preservation

- Never overwrite either frozen APK.
- Produce an independently named release candidate and metadata record.
- Keep package `com.gamefold.pc.v04localshell` and the existing signer for in-place upgrade.
- Do not clear data or storage authorization.
- A successful build/hash/install proves artifact delivery, not visual or tactile acceptance.
- Promote the candidate to a named baseline only after explicit user acceptance.

## Initial verified state

The isolated integration worktree is now based on the frozen 0.6.6 tag commit `139d0d4` (source `c032044`). The 0.6.6 supported Legacy inventory and baseline artifact guard are the pre-implementation evidence; the earlier 0.6.5 direct tests remain historical comparison only. `local.properties` is an ignored machine-local SDK pointer and is not part of source changes.

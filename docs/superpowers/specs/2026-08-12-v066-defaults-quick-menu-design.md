# GameFold PC V0.6.6 Defaults and Quick Menu Design

## Goal

Build a V0.6.6 release candidate from the frozen V0.6.5 product line that updates GameFold PC defaults and presentation without changing the validated local-game scanner, per-game executable isolation, GTA fullscreen synchronization, Bionic/Proton runtime, LSFG injection path, public resource contract, or controller input bridge.

## Baseline and release isolation

- Start from `codex/gamefold-pc-v065-legacy-test-contract`, whose production sources still match the immutable `gamefold-pc-v0.6.5-baseline` tag.
- Work on `codex/gamefold-pc-v066-defaults-rc1`.
- Keep package `com.gamefold.pc.v04localshell` and the existing signer so the candidate installs in place without clearing data or file authorization.
- Use versionCode `10054` and versionName `0.6.6-defaults-rc1`.
- Do not alter or replace the V0.6.5 baseline artifact.

## Global settings

The GameFold PC local-shell build hides the icon-style picker from Interface settings. The launcher/notification icon implementation and aliases remain intact, so an existing installation keeps a valid icon component.

`warnBeforeExit` defaults to `true` for GameFold PC and retains the upstream default for other product modes. An explicit saved user preference continues to win over the fallback.

Simulation settings receive a new first-row switch named “显示游戏内快捷菜单”. Its GameFold PC default is off. When off, the in-game quick menu cannot be opened by Android Back, keyboard menu gestures, or a controller navigation-menu binding. Back is routed to the existing exit-confirmation behavior: with “退出前警告” enabled, the first press shows a warning and a second press exits the game; with the warning disabled, the first press exits. The menu implementation remains available and works unchanged when the switch is enabled.

## Per-game defaults and migration

New GameFold PC containers use:

- LSFG-VK flow scale `0.50`;
- screen scaling mode `FSR Fit` (`SCALING_MODE_FSR_ASPECT`), preserving aspect ratio;
- Steam Input off;
- XInput on;
- DirectInput on;
- DirectInput mapper `Standard`.

Advance the GameFold per-game defaults marker from version `2` to version `3`. For containers already marked version `2`, migrate only old defaults:

- absent or `0.80` LSFG flow scale becomes `0.50`;
- absent or `None` screen scaling becomes `FSR Fit`;
- controller API defaults become Steam Input off, XInput and DirectInput on, Standard mapper.

The migration must not re-enable LSFG, change the game language, performance HUD, user resolution, executable path, GTA flags, or overwrite a non-default LSFG flow scale or non-default screen effect selection.

This BOTH input-API configuration is retained because it is the validated broad-compatibility path for current games. The future GameFold controller UI will feed the existing virtual gamepad/XInput state boundary; that UI integration is outside this candidate.

## Boot splash and copy inventory

For GameFold PC, the boot splash brand text is `GameFold`. The dynamic status text and progress indicator remain visible because they communicate runtime installation and launch stages. The rotating block of 25 fixed tips is hidden only in GameFold PC; other product modes retain existing behavior.

Create `docs/GAME_LAUNCH_TIPS.md` containing the 25 string keys and their Simplified Chinese and English source text. Mark the collection as hidden in GameFold PC V0.6.6 so it remains an auditable copy inventory instead of UI content.

## Verification

- Add unit coverage for new constants, version-three per-game defaults, conservative version-two migration, and the quick-menu opening policy.
- Add executable policy coverage for icon-picker visibility, GameFold splash branding, and hidden fixed tips; verify switch placement and rendered presentation on the device.
- Run the supported local-shell Legacy suite and the V0.6.5 GTA/default regressions.
- Build `assembleLegacyRelease` with the candidate identity.
- Verify package, versionName, versionCode, signer, size, timestamp, and SHA-256.
- Install in place on the connected vivo device without uninstalling or clearing data, then verify installed metadata and permissions before user testing.

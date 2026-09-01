# GamePad PC Home Resize Button Design

## Goal

Embed the proven Resize display-size toggle into the GamePad PC home header, starting from the exact archived `com.gamepad.pc` `1.7 / 10015` APK whose SHA-256 is `7389c647c8dd95e20dbc57b4cf7ceda232513a316a43741a59c2234751a84a6a`.

## Scope

- Add one 48dp header action immediately left of Settings.
- Reuse the Settings button geometry, border, shadow, focus, click, and controller-navigation behavior.
- Give the new action a light-blue face instead of the red Settings action face.
- Use a temporary pixel corner-bracket/resize glyph inspired by `「 」`.
- Render the Resize glyph in white. Center both the Resize and Settings glyphs on the unshadowed 44dp face rather than on the complete 48dp face-plus-shadow canvas; this shifts both glyphs 2dp left and 2dp up without changing their frames or hit regions.
- On every activation, immediately toggle between the physical `1440 x 2304` display size and the forced `1080 x 1920` display size.
- Show no toast, dialog, snackbar, or other user-facing message.
- Port and call the already proven Resize app Binder implementation rather than invoking `/system/bin/wm` or rebuilding the mechanism.
- Declare `android.permission.WRITE_SECURE_SETTINGS`; installation still requires a one-time ADB grant for `com.gamepad.pc`.
- Keep `versionName=1.7` and advance the candidate to `versionCode=10016`.
- Default the global and new-game `Show Game on External Display` switch to enabled for GamePad PC. Existing explicitly saved per-game choices remain untouched.
- After a successful display toggle, synchronize the global game screen-size template and every existing game container: `1920x1080` while the tablet is forced to `1080x1920`, and `2304x1440` after the tablet returns to physical `1440x2304`.
- Keep fail-closed blocking for portrait, split-screen, floating windows, and picture-in-picture. Add exact full-screen `1920x1080` as the sole secondary supported game-window size alongside `2304x1440`, so the game/controller surface remains interactive in the forced 1080p mode. The 1080p profile intentionally does not reuse the native profile's 576dp floor because the tablet's unchanged 400dpi density makes a valid 1080p display report about 432dp; exact pixels plus the platform multi-window/PiP flags are the fail-closed boundary for this profile.

## Interaction

The toggle reads both the initial physical size and current base size. If an override exists, it clears the override so Android restores `1440 x 2304`. If no override exists, it writes `1080 x 1920`. A busy guard prevents overlapping taps. The result is verified by rereading display size. Failures are logged only.

Only after the read-back succeeds does GamePad PC update its game-rendering profile. The update intentionally applies to the global template and all existing containers because the user requested every game to follow the two display modes. Each container is persisted after its screen size changes.

Controller navigation treats Resize as a first-class header action in this order: optional Dual Screen, Resize, Settings. Existing grid-to-header entry behavior stays unchanged.

## Visual Contract

The new action uses the existing 48dp header component and its foreground focus frame. Its new layer-list keeps the same 4dp pixel shadow offset, 2dp ink border, and face geometry as `pixel_action_button_normal`, while substituting light-blue face and shadow colors. Resize uses a white glyph. Both Resize and Settings glyphs are centered at `(22dp, 22dp)`, the center of their unshadowed 44dp face, while the complete button and 48dp hit target remain fixed. No other home layout, spacing, icon, or artwork changes.

## Acceptance

- The archived 1.7 / 10015 source identity remains traceable and unchanged.
- Unit tests cover both toggle directions, verification, busy handling, and header ordering.
- Release tests and build pass for `1.7 / 10016`.
- The APK installs in place as `com.gamepad.pc`.
- After one-time permission grant, two consecutive taps produce `1080x1920` and then restored physical `1440x2304`, with the final device state restored.
- The first tap also produces global/all-game `1920x1080`; the second produces global/all-game `2304x1440`.
- A fresh GamePad PC global controller template and a fresh game container both default external-display swap to enabled.
- The in-game unsupported-mode mask is absent at exact full-screen `1920x1080`, while split-screen, picture-in-picture, portrait, undersized, and off-by-one window sizes remain blocked.

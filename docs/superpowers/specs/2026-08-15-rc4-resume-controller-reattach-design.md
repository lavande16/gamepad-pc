# GameFold PC V0.8.2 RC4 Resume Controller Reattachment Design

## Problem

When a running game returns to Home through the GameFold HOME control and the same game is opened again, the preserved Wine/XServer session resumes rendering but GameFold controller input no longer reaches the guest.

The live RC3 log proves that the cold launch starts the original `WinHandler`, maps the evshim gamepad files, and reports Player 1 connected. A preserved-session re-entry constructs a new `XServerView` over the existing `XServer`, then unconditionally replaces `xServer.winHandler`. Because `PluviaApp.xEnvironment` is already present, the Wine environment setup path does not start or map that replacement handler. The new GameFold shell therefore publishes to an unstarted handler while the guest keeps running through the preserved environment.

## Required behavior

1. A cold launch creates and starts one `WinHandler` exactly as RC3 does.
2. Returning Home preserves the running environment and releases any held GameFold controls.
3. Reopening the same preserved game reuses the existing started `WinHandler` and reattaches it to the newly created renderer view and Activity context.
4. Reattachment re-enables the GameFold virtual controller, publishes a neutral state, and keeps Player 1 connected before accepting new input.
5. Renderer refresh requests and vibration continue to use the current view/context rather than the detached pre-Home view.
6. The change must not alter game-process pause/resume, replacement-game behavior, exit handling, rear-display lifecycle, or the RC3 focus-layer change.

## Design

### Attachment decision

Add a pure session policy that returns either:

- `CREATE_NEW` when no resumable environment/handler exists.
- `REATTACH_EXISTING` when the preserved environment still owns an existing handler.

The policy is covered by JVM tests so the resume path cannot silently return to unconditional replacement.

### WinHandler reattachment

Make the handler's renderer-view reference replaceable through a narrow `reattachRendererView` method. The method updates both the renderer view and its Activity context without restarting the socket thread or remapping gamepad shared memory.

Add a `restoreGameFoldVirtualGamepad` method that enables the virtual pad, writes a neutral Player 1 state to the existing mapped buffer, and marks the slot connected. It is safe to call only after reusing a started handler.

### XServerScreen integration

When a new renderer view is built:

- create a new handler only for a cold XServer;
- otherwise retain `xServer.winHandler`, attach the current renderer view, and restore the GameFold virtual pad;
- build the new shell controller bridge against the selected active handler.

No second handler is allowed to replace the one owned by the preserved environment.

## Error handling

If the environment claims to be resumable but its XServer has no handler, fall back to `CREATE_NEW`; the existing session policy will then prevent treating a half-torn-down environment as a valid resume on the next launch. Reattachment does not create a second socket or memory mapping.

## Verification

Automated:

- attachment policy is RED before implementation and GREEN after it;
- all legacy release unit tests pass;
- legacy release APK builds with RC4 identity.

Device:

1. Cold-start a game and confirm controller input.
2. Press GameFold HOME, reopen the same game, and confirm controller input.
3. Repeat the Home/reopen cycle a second time.
4. Confirm logs show handler reattachment and no second evshim Java mapping during preserved resume.
5. Confirm the game process remains alive, rendering continues, and the rear screen returns to OFF when dual-screen is not enabled.

## Release boundary

Produce an independent `0.8.2-rc4` candidate derived from RC3. Do not modify or promote the V0.8.1 baseline and do not clear app data or permissions during the in-place vivo installation.

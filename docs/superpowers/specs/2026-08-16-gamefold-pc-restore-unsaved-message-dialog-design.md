# GameFold PC 0.8.5 Restore Unsaved MessageDialog Design

## Context

GameFold PC 0.8.5 keeps the accepted PC icon, Home title, and About social-icon changes. Container configuration must remain manual-save. The original 0.8.3 unsaved-changes `MessageDialog` will be restored after the user rejected both auto-save-on-Back and silent discard-on-Back behavior.

## Interaction Contract

- The global container editor and every per-game container editor continue to share `ContainerConfigDialog`.
- Pressing the toolbar Back action, Android system Back, or the platform dialog Back path uses one dismiss-check policy.
- If the current configuration equals `initialConfig`, Back closes the editor immediately.
- If the current configuration differs from `initialConfig`, Back opens the original `MessageDialog` using the existing unsaved-title, unsaved-message, discard, and cancel string resources.
- Cancel closes only the confirmation dialog and keeps the editor open with its unsaved values intact.
- Discard closes the editor without invoking `onSave`.
- The toolbar Save action remains the only path that invokes `onSave(config)`.
- The title's existing `*` dirty marker remains unchanged.

## Implementation Shape

Restore the 0.8.3 `MessageDialogState` and `MessageDialog` block inside `ContainerConfigDialog`. Add a small, pure Back-decision function returning either `DISMISS` or `SHOW_CONFIRMATION`, based on whether the initial and current `ContainerData` values match. The toolbar Back action, local-shell `BackHandler`, and platform `Dialog.onDismissRequest` all call the same handler.

No launcher, Home, About, runtime, driver, container persistence, or explicit Save behavior changes are in scope.

## Verification

- A changed configuration produces `SHOW_CONFIRMATION` and does not invoke Save.
- An unchanged configuration produces `DISMISS`.
- Cancel preserves the editor and current unsaved state.
- Discard invokes only the editor dismissal callback.
- Explicit Save still invokes the existing save callback.
- Run the complete `:app:testLegacyReleaseUnitTest` suite and build `:app:assembleLegacyRelease` before installing a new candidate.
- The new candidate must use a new monotonically increasing `versionCode`; the already installed `10141` bytes are superseded and must not be reused.
- Freeze `gamefold-pc-v0.8.5-baseline` only after the user verifies the newly installed candidate.

# Offline game art on the local library

## Goal

The GameFold PC local-shell home screen should show a square local game image with the game title below it. It must work without network access, preserve the existing tap/long-press behavior, and never block launching when no image can be found.

## Resolution strategy

For each scanned custom game, resolve one local image in this order:

1. Root-level explicit square artwork: `cover_square`, then `cover`.
2. Root-level common artwork: `coverv`, `poster`, then `artwork`.
3. Root-level named icons: `icon`, `logo`, then `steamgriddb_logo`.
4. The configured launch executable's embedded icon.
5. A uniquely detected executable's embedded icon.
6. A unique or unambiguous PNG/JPEG/WebP/ICO in the root or one immediate child directory.
7. A bundled local placeholder when no candidate is available.

Only local files are considered. Supported extensions are PNG, JPG, JPEG, WebP, and ICO. Candidate lookup is case-insensitive and does not recurse through the whole game tree.

## Data flow and caching

The scanner resolves the art path while building `LibraryItem` values on the existing IO refresh path. Embedded executable icons are extracted only when needed and cached beside the executable with a source timestamp check. The UI receives a local file URI/path and uses the existing image-loading stack; no HTTP fallback is permitted.

## UI behavior

The local library card becomes a square image, followed by the title. The existing click-to-launch and long-press-to-details behavior remains unchanged. The instructional subtitle is removed. Missing or unreadable art uses a deterministic bundled placeholder and never changes the launch action.

## Verification

- Unit tests cover priority ordering, case-insensitive extensions, ambiguity fallback, and placeholder selection.
- Existing offline scanner and launch-entry tests remain green.
- A release candidate is built under the existing separate local-shell package line and installed without touching the frozen V0.4 baseline.
- Device verification checks the GTA5 card, a no-art fallback card, tap launch, and long-press details.

# Folder-Scoped Local Game Launch Design

## Goal

Allow GameFold PC to launch multiple offline games from `/GameFoldPC/game` without one game's executable path leaking into another game's container. GTA V must continue to launch through `GTAVLauncher.exe`, while a single-executable game such as `黑神话像素版` must launch its own executable.

## Scope and constraints

- Every custom game is resolved only inside the folder associated with its own `CUSTOM_GAME_<id>`.
- The `/GameFoldPC/game` library root is never treated as a single game's launch root.
- A persisted executable is reused only when it resolves to a regular file inside the current game folder.
- Existing V0.6 runtime, Proton, LSFG-VK, HUD, language, controller, and resource-loading behavior stays unchanged.
- The candidate is an in-place update of `com.gamefold.pc.v04localshell`, preserving app data and storage authorization.
- Candidate identity is `versionName=0.6.4-v06-folder-scoped-launch` and `versionCode=10049`.

## Resolver design

Introduce one folder-scoped resolver as the source of truth for custom-game executable selection.

Resolution order:

1. Validate the persisted executable against the current game folder. Keep it when the canonical target is a regular `.exe` inside that folder.
2. Otherwise scan only the current game folder, with a maximum recursion depth of four.
3. Ignore known non-game executables such as uninstallers, installers, redistributables, crash reporters, helpers, and benchmark tools.
4. If exactly one valid candidate remains, select it.
5. If several candidates remain, apply deterministic filename and location scoring:
   - a filename containing `launcher` has the strongest entry-point signal;
   - `play` and `start` are secondary entry-point signals;
   - a filename related to the game-folder name or ending in `Win64-Shipping` is preferred over generic helpers;
   - shallower paths win otherwise;
   - a case-insensitive relative-path comparison is the final stable tie-breaker.
6. Return a forward-slash relative path. Never return an absolute path or a path outside the game folder.

For the observed folders, this selects:

- `Grand Theft Auto V/GTAVLauncher.exe` inside the GTA V game folder.
- `黑神话像素版1.0 电脑版.exe` inside the `黑神话像素版` folder.

## Container and launch flow

- `getOrCreateContainer()` obtains the exact folder from `CustomGameScanner.getFolderPathFromAppId(appId)` before resolving an executable.
- Remove the current behavior that searches the shared GameFold PC library root and writes a GTA-specific path into every custom container.
- When the stored executable is stale, resolve and save the replacement into that game's container.
- Immediately before launching, resolve again using the current A-drive folder. The launcher uses the resolved result, not merely a non-empty stored value.
- If no safe candidate exists, retain the existing file-manager fallback instead of launching an invalid path.

## Failure handling

- Reject `..` traversal, absolute paths, directories, missing files, and files outside the current game folder.
- A stale path from another game is repaired automatically when a valid local candidate exists.
- Candidate ties are deterministic, so refreshes and APK updates do not randomly change the selected executable.

## Verification

Automated tests cover:

1. A sibling GTA folder cannot influence `黑神话像素版` resolution.
2. GTA V with `GTA5.exe`, `GTAVLauncher.exe`, `sc.exe`, and `unins000.exe` selects `GTAVLauncher.exe`.
3. A folder with one valid executable selects that executable.
4. A stale persisted GTA path is repaired to the current game's executable.
5. A persisted path that escapes the game folder is rejected.
6. A valid persisted per-game selection is preserved.

Device verification covers installation identity, the Black Myth launch command/process, and a GTA V launch-command regression check.

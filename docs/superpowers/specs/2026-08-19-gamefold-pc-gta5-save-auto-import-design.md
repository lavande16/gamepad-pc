# GameFold PC GTA5 Save Auto-Import Design

## Status

Approved in chat on 2026-08-19 for implementation from the frozen
`gamefold-pc-v1.0.3-baseline` tag. This design is limited to GTA5 custom games
in the GameFold PC local-shell product.

## Goal

On the first GTA5 launch for a container, import progression save files from
the game's sibling `saves` directory into the Wine Social Club save directory.
Later launches must never repeat the import or overwrite newer progress.

## Non-goals

- Do not implement a general save-import framework.
- Do not change the GTA5 executable or launcher path.
- Do not launch Wine, `start.exe`, a BAT file, or the container desktop to copy
  files.
- Do not move, rename, modify, or delete files in the public `saves` directory.
- Do not affect Steam, GOG, Epic, Amazon, non-GTA custom games, or the "Open
  container" action.
- Do not automatically re-import when source files change after the first
  handled launch.

## Product Semantics

"First load" means the first normal GTA5 launch after the private container and
runtime dependencies are ready, immediately before container activation and the
handoff to `XServerScreen`. Library scanning only discovers the game; it must not
create a container or import saves.

The importer is silent until it has a result:

- Successful import: continue launch and show one completion message.
- Existing target save: preserve it, record the handled decision, continue
  launch, and show one preservation message.
- Missing or empty source: continue launch without recording a handled decision.
- Eligible import that fails: do not launch GTA5, show a retryable error, and do
  not record completion.

## GTA5 Eligibility

The importer runs only when all of the following are true:

1. `BuildConfig.GAMEFOLD_PC_LOCAL_SHELL` is enabled.
2. The app ID belongs to `GameSource.CUSTOM_GAME`.
3. The launch is a normal game launch (`bootToContainer == false`).
4. The resolved executable basename is `GTA5.exe` or `GTAVLauncher.exe`, using
   the existing `GameFoldPcGameIdentity` case-insensitive rule.
5. The registered custom-game directory exists and contains `socialclub.ini`.
6. `socialclub.ini` resolves to `UserName=RLD!` and `AppId=271590`, both treated
   as fixed whitelisted values for this first GTA5-only implementation.

After the game passes these identity checks, source discovery considers only
direct, regular, non-symbolic-link children of `saves` whose basename matches
`SGTA5` followed by decimal digits, optionally followed by `.bak`,
case-insensitively. An absent directory or no accepted files produces
`NoSource`; it does not make the game `NotApplicable`.

No value read from `socialclub.ini` may be interpolated into a filesystem path.
The destination is fixed in code for this supported GTA5 build.

## Source and Destination

For a registered GTA5 folder `<game>`:

- Source: `<game>/saves/`
- Accepted files: direct children matching `(?i)^SGTA5[0-9]+(?:\.bak)?$`
- Destination:
  `<container>/.wine/drive_c/ProgramData/Socialclub/RLD!/271590/`

The destination directory may be created after the container has been fully
created. The importer copies only accepted save files; settings such as
`cfg.dat` and `pc_settings.bin` remain untouched.

## Container-Scoped State

State lives under `<container>/.gamefold/gta5-save-import-v1/` so it follows the
container lifecycle rather than the APK lifecycle:

- `decision.json`: durable final decision.
- `transaction.json`: crash-recovery journal while an import is committing.
- `staging/`: temporary verified copies.

`decision.json` has schema version `1` and one of two statuses:

- `imported`: source files were verified and committed.
- `skipped_existing_target`: at least one accepted target save existed before
  the importer began, so no source file was copied.

The decision also records app ID, source path, fixed destination relative path,
timestamp, and for `imported`, each file's basename, size, and SHA-256. The
decision is written to a temporary sibling and atomically renamed only after the
operation completes. A valid final decision makes every later launch a no-op.

Deleting or recreating the GTA5 container removes the decision and allows a new
fresh-container import. Updating or reinstalling the APK while preserving app
data does not.

## Transaction and Crash Recovery

When no decision exists and the target contains no accepted GTA5 saves:

1. Enumerate and sort accepted source files by basename.
2. Copy every file into a newly cleaned private `staging` directory.
3. Compute and compare source/staged size and SHA-256.
4. Write `transaction.json` containing the verified file manifest and state
   `prepared`.
5. Move each staged file into the destination on the same filesystem.
6. Verify every destination file against the manifest.
7. Atomically write `decision.json` with status `imported`.
8. Delete the journal and staging directory.

If a prior journal exists without a final decision, the importer owns only the
destination basenames and hashes recorded by that journal. It removes matching
partial destination files, clears staging, and restarts the transaction. It must
not remove a destination file whose hash differs from the journal.

If recovery encounters a differing destination save, it records
`skipped_existing_target` and preserves that file. This converts a possible
post-crash user save into the same non-overwrite protection used for upgrades.

## Upgrade Protection

V1.0.3 users can already have manually imported or newly created GTA5 saves but
no auto-import decision. Therefore, before staging any source data, the importer
checks the destination for accepted GTA5 save basenames.

If any exists, it copies nothing and atomically records
`skipped_existing_target`. This rule takes precedence over source freshness,
file count, and hashes. There is no automatic overwrite path.

## Launch Integration

Add a focused `Gta5SaveAutoImporter` in the GameFold PC product package. Invoke
it from `preLaunchLocalShell` after:

- `ContainerUtils.getOrCreateContainer(...)`
- `ImageFsInstaller.installIfNeededFuture(...)`
- `ImageFsInstaller.ensureLaunchRuntimeDependencies(...)`

and before:

- `containerManager.activateContainer(container)`
- `onSuccess(...)`

The existing local-shell single-flight coordinator prevents concurrent launch
transactions. The importer remains synchronous on the existing IO coroutine so
the process cannot start while the copy is in progress.

Return a sealed result with these meanings:

- `NotApplicable`: not the supported GTA5 build or `bootToContainer` launch.
- `NoSource`: source missing or contains no accepted files.
- `AlreadyHandled`: a valid final decision already exists.
- `Imported(fileCount)`: transaction completed successfully.
- `PreservedExisting(fileCount)`: existing destination save protected.
- `Failed(message)`: eligible import could not complete and launch must stop.

Malformed state files are treated as failed state, not as permission to
overwrite. The user-facing error asks the user to retry or use "Open container"
for manual recovery.

## Tests

Pure JVM tests use temporary directories and real file I/O. Required cases:

- GTA5 identity accepts only the supported executable plus whitelisted INI.
- Non-GTA, non-custom, and "Open container" launches are not applicable.
- Missing or empty `saves` returns `NoSource` without a decision.
- Only valid direct regular `SGTA5*` and `.bak` files are imported.
- A fresh target imports, verifies hashes, and writes `imported` once.
- A second launch leaves source and target mtimes/hashes unchanged.
- An existing target writes `skipped_existing_target` without overwriting.
- A simulated copy or verification failure writes no final decision.
- A prepared journal recovers matching partial output and retries.
- A prepared journal preserves a differing destination file.
- Container deletion naturally removes state and permits a new import.
- Path traversal, nested entries, directories, and symbolic links are rejected.

Integration tests verify that `preLaunchLocalShell` maps `Failed` to a blocking
dialog and that all other results continue to the existing activation handoff.

## Device Acceptance

Build a separately identified candidate from V1.0.3 and install it in place so
current app data, permissions, game registration, and the existing container are
preserved.

On the current phone:

1. Record the public source hashes before launch.
2. Launch GTA5 once. Because the target already contains the manually imported
   save, expect `PreservedExisting`, not an import.
3. Confirm the game still sees the existing save.
4. Launch again and confirm no copy or prompt repeats.
5. Pull and verify installed package name, version name/code, APK hash, install
   time, and preserved permission state.

Fresh-container import is verified with the JVM real-filesystem suite and a
controlled disposable container fixture. The accepted user's live GTA5
container must not be deleted or reset for testing.

## Release and Rollback

- Preserve the frozen V1.0.3 tag and artifacts.
- Develop on a separate branch from `gamefold-pc-v1.0.3-baseline`.
- Use a new candidate identity; the intended next identity is `1.0.4 / 10168`
  unless an existing release allocation conflicts during implementation.
- Do not promote or replace the named baseline until automated verification and
  real-device acceptance are complete and the user explicitly approves it.
- Rollback remains the frozen V1.0.3 APK and tag.

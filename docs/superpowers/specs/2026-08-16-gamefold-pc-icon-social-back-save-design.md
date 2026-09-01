# GameFold PC Icon, Social Links, and Back-Save Design

## Baseline and candidate identity

The implementation starts from the immutable `gamefold-pc-v0.8.3-baseline` tag (`V0.8.3-DUAL-SCREEN-DRIVER-10139`). The baseline tag and archived APK remain untouched. The new candidate is `0.8.4-ICON-BACKSAVE-RC1` with `versionCode` `10140` on branch `codex/gamefold-pc-v0.8.4-icon-backsave-rc1`.

## Product changes

1. Restore the original GameFold NS Xiaohongshu and Dribbble buttons in the About page. Copy the two canonical WebP assets from the current GameFold NS source and retain the existing URLs and click handlers.
2. Make every Back path from `ContainerConfigDialog` save changed data automatically. The toolbar Back button and Android system Back both use the same exit decision. A changed configuration invokes `onSave(config)`; an unchanged configuration invokes `onDismissRequest()`. The explicit Save action remains available. This shared dialog covers both per-game container settings and global default container settings.
3. Replace GameFold PC's visible application icon with the supplied PC icon. The supplied 512 px artwork is the canonical in-app bitmap. Desktop launcher default, round, alternate, and density-specific resources are updated to the same visual. The splash icon uses the same colors and geometry. The notification icon remains a monochrome Android notification-safe silhouette.
4. Change the home and quick-start header wordmark from `GAMEFOLD` to `GAMEFOLD PC`. Add the missing `C` glyph and allocate enough width to preserve the pixel size while leaving the right-side actions intact. Other copy and About-page wordmark text remain unchanged.

## Implementation boundaries

- No changes to GameFold NS or FC/NES sources.
- No container schema, storage format, or save callback changes.
- No changes to social URLs, About copy, runtime payloads, driver sidecars, signing identity, package name, or application data.
- Installation is an in-place upgrade of `com.gamefold.pc`; app data and permissions must be preserved.

## Error handling

Container persistence continues through the existing `onSave` callbacks. The Back decision itself is synchronous and does not create a second persistence path. Resource generation uses the supplied local files only; no network fetch is required.

## Verification

- Robolectric inflation verifies the About social row and icon resources.
- Unit tests verify changed and unchanged Back decisions and the `C` wordmark glyph/width.
- Existing version test verifies `0.8.4-ICON-BACKSAVE-RC1`.
- Run the full Legacy unit suite and a fresh Legacy release assembly.
- Verify package metadata, signature, candidate SHA-256, installed package metadata, and the SHA-256 of the APK pulled back from the device.

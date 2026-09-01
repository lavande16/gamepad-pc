# GameFold PC ARM64 and Online Dependency Prune Design

## Goal

Produce a separately versioned candidate from the installed `10037` offline-cut baseline that is arm64-only and no longer packages `libpatchelf.so`, `libopenxr_loader.so`, JavaSteam, platform login/download/cloud code, or other online-only dependency payloads, while preserving the verified local GTA5 launch path, controller input, advanced container settings, public-directory runtime resources, and LSFG-VK.

## Candidate boundary

- Keep package `com.gamefold.pc.v04localshell` so the candidate can be installed as an in-place upgrade without losing app data or `MANAGE_EXTERNAL_STORAGE` authorization.
- Use versionCode `10038` and versionName `0.5.1-v04-local-shell-arm64-pruned`.
- Keep `10037` and its APK as the rollback baseline.
- Support only `arm64-v8a`; this candidate intentionally drops 32-bit Android devices. Windows x86/x64 execution through ARM64EC/FEX/Box64 remains supported because Android ABI and Windows guest architecture are separate layers.

## Removal boundary

1. Package only `arm64-v8a` JNI libraries.
2. Exclude `libpatchelf.so` and `libopenxr_loader.so` from the local-shell APK. Static inspection must show no loader or source call site that requires either library.
3. Remove JavaSteam and its depot downloader, SpongyCastle, DNS-over-HTTPS, Play Integrity, QR login, online video/recommendation, analytics, account, cloud-save, platform download, and update payloads from the local-shell artifact.
4. Remove the `SteamService` Android component and startup references from the local shell. Online platform source may remain in the upstream source tree only when it is excluded from the local-shell runtime artifact and dependency graph; no matching class may appear in the final DEX.
5. Retain Winlator's local `SteamClientComponent`, `steam_api` compatibility files, and Wine-side local shims when required by Windows games. These are offline compatibility components, not Steam login or store clients.

## Retained runtime

- `/storage/emulated/0/GameFoldPC/game` scanning and offline artwork.
- Public-directory ImageFS, Proton, container pattern, Turnip, DXVK, FEX/Box64/WoW64 resources.
- Bionic launcher, XServer, Vulkan/Wrapper renderer, PulseAudio, controller/evshim pipeline, advanced settings, and diagnostics.
- LSFG-VK layer and external `Lossless.dll` resolution.
- `ACCESS_NETWORK_STATE`, because Winlator reads interface information when creating the local guest environment; `INTERNET` remains absent.

## Verification

- An APK-level guard must fail on `10037` and pass on the new candidate. It checks ABI directories, forbidden native libraries, forbidden DEX class prefixes, forbidden permissions/components, and required LSFG/runtime entries.
- Compile and targeted offline/runtime unit tests must pass.
- APK metadata, signature, hash, size, ABI list, permissions, and class inventory must be inspected.
- Install with `adb install -r` through the vivo helper. Do not uninstall, clear data, or reset app-ops.
- Confirm file authorization remains `allow`, GTA5 is still discovered, advanced settings remain reachable, and GTA5 reaches a rendered Wine window with no fatal exception.

## Rollback

Reinstall `releases/GameFold-PC-V0.5.0-OfflineCut-R2-10037.apk` in place if the candidate fails any device check.

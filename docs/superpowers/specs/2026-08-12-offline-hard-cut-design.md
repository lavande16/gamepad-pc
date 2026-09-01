# GameFold PC Offline Hard-Cut Design

## Goal

Create a separately versioned local-only GameFold PC candidate that contains no platform login, online recommendation, online update, analytics, or runtime-download path, while preserving the verified GameFold PC 0.4.5 local launch behavior.

## Frozen boundary

- The 0.4.5 candidate APK remains untouched and is the rollback artifact.
- The package remains `com.gamefold.pc.v04localshell`; the hard-cut candidate uses versionCode `10036` and versionName `0.5.0-v04-local-shell-offline-cut`.
- The only supported library source remains `/storage/emulated/0/GameFoldPC/game`.
- Advanced settings, container editing, diagnostics, Bionic/Proton/DXVK/Turnip setup from local resources, controller input, LSFG-VK, local artwork resolution, and GTA5 launch remain in scope.

## What is removed

1. Platform account surfaces: Steam login/QR/2FA, GOG/Epic/Amazon OAuth activities, platform service declarations and startup/reconnect code.
2. Online library surfaces: Steam/GOG/Epic/Amazon library/download/recommendation screens and their local-only build entry points.
3. Online background work: update checks, analytics/PostHog/Play Integrity warm-up, remote manifest/download preloading, and online feedback/Nexus callback entry points.
4. Build-only online baggage: browser/auth/JWT/PostHog dependencies and download/update catalog assets that are not read by the local runtime.

## What remains

- The local shell enters `OfflineLibraryScreen` directly after the file-permission gate.
- `preLaunchApp` validates the external manifest and uses only existing public runtime files; it must never call an HTTP client or installer in the local shell.
- The existing LSFG layer, input-control assets, image filesystem installation, container creation, game executable selection, and launch callbacks are not deleted or renamed.
- The launcher keeps a normal Android main activity and local file-provider only if required by the retained settings/runtime code.

## Data flow

```text
MainActivity -> local shell startup -> OfflineLibraryScreen
    -> scan GameFoldPC/game -> resolve local artwork
    -> long-press container settings / tap launch
    -> local manifest validation -> local ImageFS/container setup -> XServer
```

There is no account state transition and no online fallback in this flow. Missing public resources produce a local error explaining the missing path instead of starting a download.

## Safety and verification

- Add a repository surface test that fails while online manifest components, permissions, dependencies, or catalog assets remain.
- Keep targeted regression tests for local startup policy, first-scan artwork resolution, and LSFG path resolution.
- Build and sign a new legacy release, compare its size and ZIP contents with 0.4.5, install it using uninstall-first, and verify package identity and SHA-256.
- On the connected vivo device, verify first-run file permission, direct first-scan cover display, long-press settings, GTA5 launch, and absence of fatal exceptions. Restore the app-op to default after automated permission checks so the user can perform a clean manual test.

## Rollback

If the candidate fails to launch GTA5 or LSFG, reinstall `GameFold-PC-V0.4.5-LocalShell-Artwork-SizeFix-10035.apk`; no changes are made to that APK or its source restore point.

# GameFold PC Cold Start and MTK Gate Design

**Baseline:** GameFold PC V1.0.1 / 10164, commit `d15eb3c01e29f2f33af9dda29f60b199d472e064`

**Candidate:** GameFold PC V1.0.2 / 10165 RC1

## Goal

Make every cold start transition from an NS-style icon-and-wordmark splash directly to one stable destination: the normal Home page, the existing storage-permission page, or an NS-style MTK unsupported page. No permission frame or orientation transition may be visible behind the splash.

## Root causes

- `MainActivity` uses `Theme.Pluvia` directly and never installs the AndroidX splash screen, so Android's starting window does not use the existing GameFold splash artwork.
- `GameFoldPcPermissionProcessorGate` initializes `ready` to `false` and checks permission later in `LaunchedEffect`, so an already-authorized device can compose the permission page for one frame.
- `GameFoldOrientation.apply()` runs after `super.onCreate()`, while the manifest orientation is `unspecified`; Android can therefore create the first window in the physical orientation before the saved GameFold mode is applied.
- `GameFoldPcProcessorPolicy` checks only `arm64-v8a`; MediaTek Dimensity devices are ARM64 and pass the existing gate.

## Architecture

Add a pure startup-route policy whose only outputs are `READY`, `PERMISSION_REQUIRED`, `UNSUPPORTED_MTK`, and `UNSUPPORTED_ABI`. It evaluates processor compatibility before permission, receives explicit device strings for unit testing, and is initialized synchronously before the first Compose branch is selected.

Use AndroidX `core-splashscreen` with the existing `gamefold_splash_icon` vector. `MainActivity` applies the saved GameFold orientation before `super.onCreate()`, installs the splash, and keeps it until the startup gate has successfully composed its stable route. The gate reports that readiness after composition, not after an arbitrary delay.

## Startup flow

1. `attachBaseContext()` initializes preferences and locale as today.
2. `MainActivity.onCreate()` applies the saved GameFold orientation before `super.onCreate()`.
3. The AndroidX splash is installed with `Theme.GameFoldPc.Splash` and held by `splashRouting`.
4. Compose synchronously evaluates the startup route.
5. MTK or non-ARM64 devices receive the unsupported route before any storage check is surfaced.
6. Supported devices without storage access receive the existing permission route.
7. Supported and authorized devices compose Home directly.
8. A successful composition releases the splash; no timer is used.

## MTK compatibility contract

Detection mirrors GameFold NS and checks normalized `Build.SOC_MODEL` plus `Build.HARDWARE` for `MEDIATEK`, `DIMENSITY`, `MTK`, or a bounded `MT` model such as `MT6989`. Common Qualcomm identifiers such as `SM8650` remain supported.

The blocked screen uses the existing GameFold PC controller shell and NS-style assembly surface with this Chinese copy:

- `处理器兼容性`
- `当前设备暂不支持`
- `由于驱动限制，本软件暂不支持 MTK 天玑处理器，敬请期待。`
- `体验FC版GameFold，已完成全平台适配`

The FC action opens the same FC website used by GameFold NS. The unsupported route cannot continue to permission or Home.

## Permission behavior

An already-authorized device never composes the permission route. A device that genuinely lacks access transitions from splash to the stable existing permission page; Android system settings open only after the user activates the authorization action. The existing notification-permission branch remains suppressed for the local offline shell.

## Verification

- Unit tests cover MediaTek aliases, false positives, ABI rejection, and startup-route priority.
- Existing orientation and storage-access tests remain green.
- Full `testLegacyReleaseUnitTest` and `assembleLegacyRelease` gates pass.
- The candidate is installed in place on the connected vivo device, then package identity, install time, permission, pulled APK hash, and byte equality are checked.
- Real-device cold starts are checked from force-stop in saved landscape and saved portrait modes. MTK runtime acceptance remains an external device gate when no MTK phone is connected.

## Non-goals

- No changes to game launch loading overlays, Wine/container provisioning, rear-display behavior, or controller layout.
- No overwrite or retag of the frozen V1.0.1 baseline.

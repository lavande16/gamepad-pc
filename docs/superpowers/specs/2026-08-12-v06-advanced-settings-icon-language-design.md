# GameFold PC V0.6 Advanced Settings, NS Icon, and Per-Game Language Design

## Scope

Create a separately versioned candidate from the frozen V0.6 baseline. Do not alter the baseline APK, GTA5 launch path, LSFG-VK wiring, input bridge, runtime resources, or FPS-only HUD behavior.

## Confirmed root causes

1. Opening Home > Advanced Settings crashes in `IconVariantCard`: Compose `painterResource()` receives `ic_notification.xml`, whose root is a `layer-list`. Compose accepts VectorDrawable or raster assets there, so it throws `IllegalArgumentException`.
2. The V0.6.2 launcher copied `gamefold_launcher_foreground.xml`, whose content is scaled to `0.56` inside an adaptive icon. The GameFold NS 2.9 APK currently installed on the phone instead exposes a full-frame vector with a `M0,0h108v108h-108z` background. The scaled adaptive foreground creates the excessive black margin reported by the user.
3. Global language defaults are `schinese`, but the per-game migration only persists LSFG and FPS HUD. Existing GTA5 containers therefore remain `english`.

## Design

### Advanced Settings

Keep the icon-style UI. Render the notification preview with the canonical `gamefold_notification_mark` VectorDrawable rather than the runtime `layer-list` notification wrapper. Default and alternate preview assets remain visually identical.

### Launcher icon

Add the canonical full-frame `gamefold_system_icon` vector from GameFold NS 2.9. Use that same vector for default, alternate, round, API 26, and API 33 launcher resources. Do not use the scaled adaptive foreground.

### Per-game Simplified Chinese

Raise the GameFold PC per-game defaults schema from `1` to `2`. Move the mutation into one testable helper that applies, exactly once per schema version:

- performance HUD enabled;
- LSFG-VK armed;
- `language = schinese`;
- `LC_ALL = zh_CN.UTF-8`;
- `gamefoldPcDefaults = 2`.

`ContainerUtils` remains responsible for saving the real container after the helper reports a change. New containers continue to inherit the same global defaults.

## Verification

- Run a red-green unit test against a real `Container` for the version-2 migration and idempotence.
- Run the existing GameFold PC targeted unit tests and packaging guards.
- Build an arm64-only release candidate with a new version code/name.
- Install in place on the connected vivo phone without clearing data or permissions.
- Verify Advanced Settings stays open with no fatal exception.
- Verify the launcher icon visually matches the installed GameFold NS 2.9 full-frame icon.
- Open GTA5 independent settings and verify Simplified Chinese, LSFG-VK enabled, and performance HUD enabled.
- Launch GTA5 once and confirm LSFG injection and FPS-only HUD remain functional.

# V0.6 launcher icon and defaults design

## Goal

Starting from the frozen V0.6 runtime, make GameFold PC use the Game Fold NS official launcher icon and ship the requested offline-friendly defaults without changing the proven GTA5, controller, Wine/Bionic, or LSFG runtime pipeline.

## Decisions

1. Copy the canonical Game Fold NS launcher vector and monochrome mark into this app. Default and alternate launcher aliases use the same artwork, so switching icon style cannot reveal the old GameNative artwork.
2. Centralize GameFold PC product defaults as pure constants/data so unit tests can assert the exact policy independently of Android UI state.
3. New/default containers use Bionic with `proton-10.0-arm64ec-2`, game language `schinese`, LSFG enabled, and the container FPS flag enabled.
4. The floating performance HUD defaults to enabled and to FPS-only. Existing stored user preferences continue to win; upgrading must not overwrite an explicit user choice.
5. The entire Information section is absent in local-shell builds. Other build modes retain their existing behavior.
6. Existing containers are not force-reset. Their explicit settings remain intact; missing/default values and newly created containers receive this policy.

## Validation

- Unit tests prove the default policy, FPS-only metric set, and local-shell Information visibility.
- Resource checks prove default and alternate aliases point to the same NS artwork.
- The release APK must remain arm64-only, contain LSFG assets and the exact Android public DLL path, and contain no host-machine path.
- Install in place on the connected vivo, preserve first-install time and storage authorization, then verify package/version/hash, launcher/settings UI, and absence of fatal startup logs.

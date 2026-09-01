# GameFold PC Splash Orientation Readiness Design

## Problem

When GameFold PC is saved in landscape mode but cold-started while the phone is physically upright, Android creates the initial window using the current portrait configuration. `MainActivity` requests landscape before `super.onCreate()`, but the splash is currently released as soon as startup routing is ready. That can expose a portrait application frame before the configuration change reaches landscape.

## Approved design

Keep the existing early orientation request and make splash dismissal depend on two independent readiness signals:

1. startup routing is ready;
2. the activity configuration matches the orientation saved in `GameFoldOrientation`.

The splash stays visible until both signals are true. `onConfigurationChanged` refreshes orientation readiness, so a portrait-to-landscape configuration transition remains covered by the splash. If the saved mode is already aligned with the device, dismissal is unchanged.

## Scope

- Add a small pure readiness gate with unit coverage for both ordering cases.
- Wire `MainActivity` startup routing and configuration changes into the gate.
- Preserve permission routing, MTK blocking, portrait mode, icon aliases, and existing splash visuals.
- Increment the release candidate from `1.0.2 / 10165 RC1` to `1.0.2 / 10166 RC2`.
- Build and install the APK without performing product-side visual acceptance on the user's behalf.

## Success criteria

- The splash cannot dismiss when only routing is ready.
- The splash cannot dismiss when only orientation is ready.
- The splash dismisses when both are ready, regardless of signal order.
- Release tests and APK build pass; the installed package reports `com.gamefold.pc`, `1.0.2`, and `10166`.

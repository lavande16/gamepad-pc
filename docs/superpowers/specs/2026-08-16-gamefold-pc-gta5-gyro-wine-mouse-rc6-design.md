# GameFold PC GTA5 Gyro Wine Mouse RC6 Design

## Field evidence

RC5 is rejected after a live GTA5 test on vivo V2436A. ZL still raises the virtual Xbox left trigger and GTA5 enters aim, Android keeps the gyroscope registered at 10 ms, and live samples reach approximately `+/-7.8 rad/s`, but the camera receives no movement.

The broken boundary is RC5's output callback: it always calls `XServer.injectPointerMoveDelta(dx, dy)`. That method updates the X11 pointer and emits XI2 raw motion. The repository's working game-relative mouse paths instead call `WinHandler.mouseEvent(MouseEventFlags.MOVE, dx, dy, 0)`, which forwards a `MOUSE_EVENT` packet into Wine.

## Decision

RC6 keeps the RC5 GamepadMotionHelpers/JoyShockMapper-derived gyro calculation, stillness calibration, equal `40 counts/degree` axis tuning and strict ZL gate. It changes only the terminal transport from the XServer pointer path to the existing Wine relative-mouse path.

```text
Android gyro -> calibrated angular velocity -> fractional integer delta
             -> ZL gate -> WinHandler MOUSE_EVENT -> Wine/GTA5
```

The transport is wrapped in a small `GyroMouseOutputDispatcher` so its nonzero-only behavior is testable without constructing Android or Wine runtime objects. RC6 also adds release-visible, rate-limited diagnostics for ZL gate transitions and accumulated nonzero mouse output. Diagnostics must not log every 100 Hz sensor sample.

## Constraints

- Branch: `codex/gamefold-pc-v0.9.2-gta5-gyro-wine-mouse-rc6`.
- Package remains `com.gamefold.pc`; candidate identity is `0.9.2 / 10150`.
- Do not change direction, `40 counts/degree`, axis equality, bias calibration or GTA5 executable scope.
- Do not restore gyro-to-right-stick conversion.
- ZL release, lifecycle pause and session teardown continue to clear timing and pending fractions.
- RC5 and every formal baseline remain immutable.
- Build and device identity verification do not replace GTA5 gameplay acceptance.

## Acceptance

Automated tests must prove the output dispatcher suppresses `(0, 0)`, forwards nonzero deltas unchanged exactly once, and keeps the existing gyro/ZL contracts green. Full legacy release unit tests and `assembleLegacyRelease` must pass. The archived and installed APKs must have identical SHA-256. Final camera response remains a user GTA5 test gate.

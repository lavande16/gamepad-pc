# GameFold PC GTA5 Gyro Mouse Port RC5 Design

## Decision

RC4 is rejected and remains archived. RC5 starts from the accepted RC3 source line at commit `b16d418`, but replaces the gyro-to-right-stick path with the MIT-licensed control model used by JibbSmart's GamepadMotionHelpers and JoyShockMapper.

The selected model is calibrated angular velocity integrated into relative mouse movement:

```text
Android calibrated gyro (rad/s)
  -> subtract stillness-proven bias
  -> remap device axes to current display axes
  -> multiply by sensor delta time
  -> radians to degrees
  -> JoyShockMapper real-world calibration (mouse counts / degree)
  -> fractional remainder accumulator
  -> XServer raw relative mouse motion
```

This is deliberately not an absolute-orientation implementation. GamepadMotionHelpers documents that quaternion yaw errors accumulate and recommends calibrated gyro angular velocity for mouse-like aiming. It is also not a virtual-right-stick implementation: joystick saturation, camera acceleration and nonlinear stick response make equal opposite physical rotations produce unequal camera displacement.

## Upstream source and license

- Project: `JibbSmart/GamepadMotionHelpers`
- Source commit inspected: `f1a76007ec058122a4bb6f19c816c6c0c7b7f12a`
- Algorithm version: `GamepadMotion.hpp` Version 10
- License: MIT, copyright Julian "Jibb" Smart
- Companion behavior reference: `JibbSmart/JoyShockMapper`, especially calibrated angular velocity, real-world calibration, ZL-style gyro gating and fractional mouse remainder accumulation

RC5 ports only the required behavior into Kotlin. It does not add a native library or copy unrelated controller support. The upstream MIT notice must ship in the APK and the port must be documented in the repository.

## Behavior contract

1. The experiment remains limited to local-shell `GTA5.exe` and `GTAVLauncher.exe` sessions.
2. ZL continues to publish the normal left trigger to the virtual Xbox controller.
3. Gyro mouse movement is emitted only while ZL is held.
4. Releasing ZL immediately stops gyro output and discards fractional pending movement.
5. The physical/onscreen right stick remains a pure right-stick input and is never mixed with gyro.
6. A clockwise physical rotation followed by an equal counter-clockwise rotation produces an approximately zero net relative mouse delta before game-side mouse acceleration.
7. Horizontal and vertical gyro sensitivity use the same default multiplier.
8. No deadzone and no general low-pass smoothing are applied. This follows JoyShockMapper's defaults and avoids latency or loss of slow intentional tracking.
9. Gyro bias may update only while ZL is released and only after the GamepadMotionHelpers-style stillness window accepts both gyro and accelerometer stability. Bias is frozen for the complete ZL-held aim session.
10. Sensor discontinuities, lifecycle pause and display rotation changes reset timing and pending mouse fractions so they cannot create a jump.

## Initial tuning

- Sensor: Android `TYPE_GYROSCOPE`, requested at 100 Hz
- Stillness reference: Android `TYPE_ACCELEROMETER`, converted from m/s² to g
- Real-world calibration: `40 mouse counts / degree`, JoyShockMapper's documented first calibration value for 3D games
- Horizontal sensitivity: `1.0`
- Vertical sensitivity: `1.0`
- Maximum accepted sample interval: `50 ms`; larger gaps reset the integration reference rather than synthesizing motion

The `40 counts/degree` value is an initial GTA5 experiment value, not a claimed final calibration. GTA5 in-game mouse sensitivity and acceleration affect the resulting camera angle and remain a real-device acceptance item.

## Components

### `GyroMouseProcessor`

Pure Kotlin motion-to-mouse control law. It owns ZL gate state, timestamp continuity, display-axis mapping and conversion to floating-point mouse counts. It consumes a frozen bias while aiming.

### `GamepadMotionStillnessCalibrator`

A focused Kotlin port of the GamepadMotionHelpers stillness window: running min/max/mean for gyro and acceleration, minimum sample/time gates, stability thresholds and bias easing. It only receives samples while ZL is released.

### `FractionalMouseDeltaAccumulator`

Ports JoyShockMapper's `moveMouse` remainder behavior. It accumulates floating-point deltas and emits only the integer portion to XServer, retaining the sub-count remainder for later samples.

### `AndroidGyroMouseController`

Registers gyro and accelerometer listeners on the main looper, feeds samples into the processor, passes integer deltas to the existing XServer `injectPointerMoveDelta`, and clears state on ZL release/stop.

### `GameFoldRuntimeGamepadBridge`

Keeps ZL gate notification but removes all gyro/right-stick mixing. Manual right-stick state remains untouched by gyro callbacks.

## Verification boundary

Unit tests can prove gating, signs, equal axis scaling, arithmetic reversibility, fractional conservation, stillness calibration and right-stick isolation. Build, badging, signing, archive hash and installed APK hash prove artifact identity. Only a GTA5 run on the vivo device can prove final direction, perceived sensitivity, mixed mouse/controller compatibility and absence of game-side mouse acceleration.


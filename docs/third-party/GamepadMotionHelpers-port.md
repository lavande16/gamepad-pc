# JibbSmart gyro mouse implementation provenance

## Sources

GameFold PC GTA5 Gyro Mouse RC5 ports a focused subset of these upstream MIT-licensed projects:

- GamepadMotionHelpers: <https://github.com/JibbSmart/GamepadMotionHelpers>
  - inspected commit: `f1a76007ec058122a4bb6f19c816c6c0c7b7f12a`
  - inspected implementation: `GamepadMotion.hpp`, Version 10
  - ported behavior: three-axis gyro bias representation, min/max/mean stillness window, gyro-plus-accelerometer stability gate and calibrated angular velocity usage
- JoyShockMapper: <https://github.com/JibbSmart/JoyShockMapper>
  - inspected commit: `980dc52c09fbdfb3378b04a8a1895ea3157c29e1`
  - inspected implementations: `include/InputHelpers.h`, Windows/Linux `moveMouse`, gyro section of `src/main.cpp`
  - ported behavior: angular velocity multiplied by elapsed time and real-world calibration, strict button gating, and fractional relative-mouse remainder accumulation

The complete upstream MIT notices ship in:

- `app/src/main/assets/licenses/GamepadMotionHelpers-LICENSE.txt`
- `app/src/main/assets/licenses/JoyShockMapper-LICENSE.txt`

## Local adaptation

The upstream C++ algorithms were adapted to focused Kotlin classes rather than importing controller drivers or native binaries:

- `GamepadMotionStillnessCalibrator.kt`
- `GyroMouseProcessor.kt`
- `AndroidGyroMouseController.kt`

Android `TYPE_GYROSCOPE` is the angular-velocity source and `TYPE_ACCELEROMETER` is used only to validate a stillness window. Output goes to the existing Winlator XServer raw relative mouse path. Calibration is frozen for the entire ZL-held aim session. The RC5 port deliberately omits absolute quaternion orientation, virtual-right-stick conversion, gyro deadzones, smoothing, acceleration curves, flick stick and upstream controller-device support.

No JibbSmart project name or trademark is used as a GameFold product name, and this port does not imply upstream endorsement.

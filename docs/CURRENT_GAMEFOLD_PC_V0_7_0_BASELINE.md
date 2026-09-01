# Current GameFold PC V0.7.0 baseline

The approved GameFold PC frontend baseline is **V0.7.0-NS-UI-R21-10085**.

- APK: `releases/baselines/V0.7.0/GameFold-PC-V0.7.0-NS-UI-R21-10085.apk`
- SHA-256: `c8b24fe4f9fb6c2662bf0421b17470a1d71afdefc69ae04d5de9ea0127da76a8`
- Package: `com.gamefold.pc.v066nsrc.r21`
- versionName/versionCode: `0.7.0` / `10085`
- Native Vulkan SHA-256: `34849ec51e3b7cbbd7aefb43f6d890c1d764deec2e59f3b0939e351260e4141f`
- Required JNI symbol: `Java_com_winlator_renderer_VulkanRenderer_nativeGetPresentedFrameCount`
- Device installation verified on `10AF3P0SDY001M6`.
- R21 contains the native presented-frame counter and the focused JVM/UI contracts pass. GTA5/FPS runtime acceptance is intentionally still pending manual verification on this frozen rollback point.

## Rollback

The previous frozen `V0.6.6-NS-UI-R15-10072` artifact remains unchanged at:

`releases/baselines/V0.6.6-NS-UI-R15-10072/GameFold-PC-V0.6.6-NS-UI-R15-10072.apk`

Its SHA-256 is `dc79f8c3d3e21a9bba841c449c4871a5de5ab5790b2672d49c15e48141ad1e39`.

## Scope

This baseline covers GameFold PC frontend integration only. GameFold FC is explicitly out of scope and no FC path is included in the change set.

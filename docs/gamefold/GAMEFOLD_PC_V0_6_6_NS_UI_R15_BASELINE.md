# GameFold PC 0.6.6 NS UI R15 基线

## 基线身份

- 产品：GameFold PC
- 基线名称：`V0.6.6-NS-UI-R15-10072`
- 对应源码提交：`4e48396da2fc1d7a37cc7e8d855955fa543c778a`
- APK：[GameFold-PC-V0.6.6-NS-UI-R15-10072.apk](../../releases/baselines/V0.6.6-NS-UI-R15-10072/GameFold-PC-V0.6.6-NS-UI-R15-10072.apk)
- SHA-256：`dc79f8c3d3e21a9bba841c449c4871a5de5ab5790b2672d49c15e48141ad1e39`
- 包名：`com.gamefold.pc.v066nsrc`
- versionName：`0.6.6-gf-ns-r15`
- versionCode：`10072`
- minSdk：`26`
- targetSdk：`28`

## 签名

- 签名方案：APK Signature Scheme v2
- 证书 DN：`C=US, O=Android, CN=Android Debug`
- 证书 SHA-256：`24a37be7ed0f0431fde44018312271152545d689bb3d1fbe9fbf92914bdf84b4`

## 使用规则

1. 后续 UI 修改从该源码提交和 APK 身份继续推进。
2. 不覆盖、不重签、不替换该基线 APK。
3. 每个 UI 候选包必须使用独立目录、独立 versionCode 和独立 SHA-256。
4. 若新 UI 验收失败，回退到该 APK，不回退或修改 GameFold FC。
5. GameFold FC 在本次 PC UI 工作中属于明确排除范围。

## 基线核验命令

```bash
shasum -a 256 releases/baselines/V0.6.6-NS-UI-R15-10072/GameFold-PC-V0.6.6-NS-UI-R15-10072.apk
ANDROID_HOME=/Users/11085429/Library/Android/sdk \
  /Users/11085429/Library/Android/sdk/build-tools/37.0.0/aapt \
  dump badging releases/baselines/V0.6.6-NS-UI-R15-10072/GameFold-PC-V0.6.6-NS-UI-R15-10072.apk
```

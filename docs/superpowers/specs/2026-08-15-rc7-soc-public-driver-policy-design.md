# GameFold PC V0.8.2 RC7 SoC Public Driver Policy Design

## Goal

Keep Turnip payloads outside the APK under `/storage/emulated/0/GameFoldPC/graphics/`, while selecting the proven driver automatically for GameFold PC devices:

- `SM8750` and `SM8850` use `Turnip Gen8 V30`.
- `SM8650` uses `Turnip_Gen8_V25`.

## Resource contract

The public payload uses the runtime-native Zstandard tar format:

- `graphics/adrenotools-Turnip Gen8 V30.tzst`
- `graphics/adrenotools-Turnip_Gen8_V25.tzst`

The APK contains neither archive. `GameFoldPcResourceRoot` must resolve an explicitly selected driver by exact semantic name. It must not silently substitute a different Turnip build when the SoC-selected archive is missing.

Public archives are treated as locally available in the settings list. Selecting or automatically choosing one extracts it into the existing private AdrenoTools content directory before Vulkan startup.

## Selection policy

Add a pure SoC policy with case-insensitive, whitespace-tolerant matching. `SM8750` and `SM8850` return V30; `SM8650` returns V25; all other or missing values retain the existing generic default.

The policy applies to fresh GameFold PC containers and to containers that still carry the old automatic driver. An explicit user selection is preserved. RC7 migration therefore changes only blank, `System`, or the previous GameFold PC automatic default; it does not replace V25, V30, T26, or another manually selected driver.

## Failure behavior

If the exact public archive is missing or invalid, log the missing driver and fall back through the existing system-driver behavior. Do not download automatically and do not use a different Turnip archive as a compatibility guess.

## Verification

Automated tests cover SoC mapping, unknown-SoC fallback, exact public-resource resolution, and rejection of wrong-version fallback. The release APK is inspected to prove no Turnip/Adreno archive is packaged. On the OPPO SM8850, RC7 must select V30, resolve the public archive, start GTA5 with LSFG off, and render the previously corrupted scene without checkerboard artifacts.

#!/usr/bin/env bash
set -euo pipefail

# Create a repeatable per-RC evidence bundle. The script deliberately does not clear app data,
# grant permissions, or drive game input: those are device acceptance actions and must preserve the
# user's existing PC library. It records the selected core pages so the next RC can be compared
# with the same NS 2.9 R2 reference set.

usage() {
  echo "Usage: $0 APK OUT_DIR [SERIAL]" >&2
  exit 2
}

[[ $# -ge 2 && $# -le 3 ]] || usage
apk="$1"
out="$2"
serial="${3:-}"
[[ -f "$apk" ]] || { echo "APK not found: $apk" >&2; exit 2; }
mkdir -p "$out/screenshots" "$out/comparisons"

sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$sdk" ]]; then
  echo "ANDROID_HOME or ANDROID_SDK_ROOT is required" >&2
  exit 2
fi
aapt="$(find "$sdk/build-tools" -mindepth 1 -maxdepth 1 -type d -print | sort -V | tail -n1)/aapt"
[[ -x "$aapt" ]] || { echo "aapt not found under $sdk/build-tools" >&2; exit 2; }

sha256="$(shasum -a 256 "$apk" | awk '{print $1}')"
badging="$($aapt dump badging "$apk")"
package="$(sed -n "s/^package: name='\\([^']*\\)'.*/\\1/p" <<<"$badging" | head -n1)"
version_name="$(sed -n "s/^package:.*versionName='\\([^']*\\)'.*/\\1/p" <<<"$badging" | head -n1)"
version_code="$(sed -n "s/^package:.*versionCode='\\([^']*\\)'.*/\\1/p" <<<"$badging" | head -n1)"
printf '%s\n' "$badging" > "$out/aapt-badging.txt"
printf '{\n  "apk": "%s",\n  "sha256": "%s",\n  "package": "%s",\n  "versionName": "%s",\n  "versionCode": "%s",\n  "reference": "GameFold NS 2.9 R2"\n}\n' \
  "$apk" "$sha256" "$package" "$version_name" "$version_code" > "$out/RC-EVIDENCE.json"

if [[ -n "$serial" ]]; then
  adb="${ADB:-adb}"
  "$adb" -s "$serial" get-state >/dev/null
  "$adb" -s "$serial" shell pm path "$package" > "$out/device-package-path.txt"
  "$adb" -s "$serial" shell dumpsys package "$package" > "$out/device-package.txt"
  installed_apk="$($adb -s "$serial" shell pm path "$package" | sed -n 's/^package://p' | tr -d '\r' | head -n1)"
  if [[ -n "$installed_apk" ]]; then
    "$adb" -s "$serial" exec-out screencap -p > "$out/screenshots/current.png"
    "$adb" -s "$serial" shell am stack list > "$out/device-stack.txt" || true
    device_sha="$(mktemp /tmp/gamefold-device-sha.XXXXXX)"
    "$adb" -s "$serial" shell sha256sum "$installed_apk" > "$device_sha" || true
    sed -E 's/[[:space:]].*$//' "$device_sha" > "$out/device-sha256.txt"
    rm -f "$device_sha"
  fi
fi

reference_dir="${GAMEFOLD_NS_REFERENCE_DIR:-}"
if [[ -n "$reference_dir" && -d "$reference_dir" ]]; then
  for page in home add_game settings container_settings loading runtime_fps pause; do
    candidate="$out/screenshots/$page.png"
    reference="$reference_dir/$page.png"
    [[ -f "$candidate" && -f "$reference" ]] || continue
    if command -v compare >/dev/null 2>&1; then
      compare -metric AE "$reference" "$candidate" "$out/comparisons/$page-diff.png" \
        2> "$out/comparisons/$page-metric.txt" || true
    else
      echo "ImageMagick compare unavailable; review $page side-by-side." > "$out/comparisons/$page-metric.txt"
    fi
  done
fi

cat > "$out/SCREENSHOT-MATRIX.txt" <<'EOF'
Capture these pages after installing the RC, without clearing data:
  home              NS header, three actions, cartridge size/spacing, add-game last
  add_game          NS pixel card, path text, Download Game action, no legacy sentence
  settings          advanced settings in NS light palette; controller navigation works
  container_settings per-game settings stays inside display aperture; controller remains visible
  loading           one NS-style wide card, square game cover, dynamic status/progress
  runtime_fps       game surface, upper-left large 8-bit FPS, no arrow/panel, non-zero after frames
  pause             NS pause card inside display aperture; controller remains visible

For each page compare the candidate with the same-named GameFold NS 2.9 R2 reference. Record
layout/color/typography/input differences in the next RC ledger before changing implementation.
EOF
echo "Wrote GameFold PC NS UI evidence bundle: $out"

#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
pc_tag_commit="139d0d460038c9175f1ef27c4ade5e2189c01729"
pc_apk="/Users/11085429/Documents/Game Fold/gamefold-pc-v065-fullscreen-release/releases/baselines/V0.6.6/GameFold-PC-V0.6.6-Baseline-10057.apk"
ns_apk="/Users/11085429/Documents/Game Fold/baselines/GameFold-2.9-Mainline-Baseline-R2/GameFold-2.9-Baseline-R2.apk"

find_aapt() {
    local sdk_root="$1"
    local candidate
    local candidate_version
    local best_path=""
    local best_version=""

    if [[ ! -d "$sdk_root/build-tools" ]]; then
        printf 'ERROR: Android SDK has no build-tools directory: %s\n' "$sdk_root" >&2
        return 1
    fi

    while IFS= read -r -d '' candidate; do
        [[ -x "$candidate/aapt" ]] || continue
        candidate_version="$(basename "$candidate")"
        if [[ -z "$best_path" ]] || version_is_newer "$candidate_version" "$best_version"; then
            best_path="$candidate/aapt"
            best_version="$candidate_version"
        fi
    done < <(find "$sdk_root/build-tools" -mindepth 1 -maxdepth 1 -type d -print0)

    if [[ -z "$best_path" ]]; then
        printf 'ERROR: no executable aapt found under Android SDK build-tools: %s\n' "$sdk_root/build-tools" >&2
        return 1
    fi

    printf '%s\n' "$best_path"
}

version_is_newer() {
    local candidate="$1"
    local current="$2"
    local candidate_major candidate_minor candidate_patch
    local current_major current_minor current_patch

    IFS=. read -r candidate_major candidate_minor candidate_patch <<< "$candidate"
    IFS=. read -r current_major current_minor current_patch <<< "$current"
    [[ "$candidate_major" =~ ^[0-9]+$ && "$candidate_minor" =~ ^[0-9]+$ && "$candidate_patch" =~ ^[0-9]+$ ]] || return 1
    [[ "$current_major" =~ ^[0-9]+$ && "$current_minor" =~ ^[0-9]+$ && "$current_patch" =~ ^[0-9]+$ ]] || return 0

    (( candidate_major > current_major ||
        (candidate_major == current_major && candidate_minor > current_minor) ||
        (candidate_major == current_major && candidate_minor == current_minor && candidate_patch > current_patch) ))
}

require_apk_identity() {
    local apk="$1"
    local expected_package="$2"
    local expected_version_code="$3"
    local expected_version_name="$4"
    local badging

    badging="$("$aapt" dump badging "$apk")"
    [[ "$badging" == *"package: name='$expected_package' versionCode='$expected_version_code' versionName='$expected_version_name'"* ]]
}

cd "$repo_root"

sdk_root="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$sdk_root" ]]; then
    printf 'ERROR: set ANDROID_HOME or ANDROID_SDK_ROOT to an Android SDK containing build-tools/aapt.\n' >&2
    exit 2
fi
aapt="$(find_aapt "$sdk_root")" || exit 2

test "$(git rev-parse gamefold-pc-v0.6.6-baseline^{commit})" = "$pc_tag_commit"

pc_hash="$(shasum -a 256 "$pc_apk" | awk '{print $1}')"
ns_hash="$(shasum -a 256 "$ns_apk" | awk '{print $1}')"
test "$pc_hash" = "3460a103798e2e62c2888cb5e6de0b8cd66f7b8659ddb162d92e1eea7a5268ad"
test "$ns_hash" = "156c340360ea4a988fd8cde5c0282a1ebe3585ecfb77457109b1770ea2768292"

require_apk_identity "$pc_apk" "com.gamefold.pc.v04localshell" "10057" "0.6.6"
require_apk_identity "$ns_apk" "com.switch.fold.baseline.lite" "33466084" "2.9"

printf 'PC 0.6.6 approved SHA-256: %s\n' "$pc_hash"
printf 'NS 2.9 R2 approved SHA-256: %s\n' "$ns_hash"

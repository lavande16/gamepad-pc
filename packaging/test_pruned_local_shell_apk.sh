#!/usr/bin/env bash
set -euo pipefail

APK="${1:?APK path required}"
BUILD_TOOLS="${2:-/Users/11085429/Library/Android/sdk/build-tools/37.0.0}"
AAPT2="$BUILD_TOOLS/aapt2"
DEXDUMP="$BUILD_TOOLS/dexdump"

test -f "$APK"
test -x "$AAPT2"
test -x "$DEXDUMP"

reject_matches() {
    local pattern="$1"
    local file="$2"
    if rg -q "$pattern" "$file"; then
        printf 'forbidden APK content matching %s:\n' "$pattern" >&2
        rg "$pattern" "$file" >&2
        return 1
    fi
}

TMP_DIR="$(mktemp -d /private/tmp/gamefold-pruned-apk.XXXXXX)"
trap 'rm -rf "$TMP_DIR"' EXIT

ZIP_LIST="$TMP_DIR/zip-list.txt"
MANIFEST="$TMP_DIR/manifest.txt"
CLASSES="$TMP_DIR/classes.txt"
DEX_STRINGS="$TMP_DIR/dex-strings.txt"

unzip -Z1 "$APK" > "$ZIP_LIST"
"$AAPT2" dump xmltree "$APK" --file AndroidManifest.xml > "$MANIFEST"

while IFS= read -r dex; do
    unzip -p "$APK" "$dex" > "$TMP_DIR/$dex"
    strings "$TMP_DIR/$dex" >> "$DEX_STRINGS"
    "$DEXDUMP" -l plain "$TMP_DIR/$dex"
done < <(rg '^classes([0-9]+)?\.dex$' "$ZIP_LIST" | sort) | rg 'Class descriptor' > "$CLASSES"
test -s "$CLASSES"
test -s "$DEX_STRINGS"

# Android host ABI is arm64-only. Guest x86/x64 support lives in the external
# Wine/Proton resources and is intentionally independent from this check.
test -n "$(rg '^lib/arm64-v8a/' "$ZIP_LIST")"
reject_matches '^lib/(armeabi-v7a|x86|x86_64)/' "$ZIP_LIST"

reject_matches '(^|/)libpatchelf\.so$|(^|/)libopenxr_loader\.so$' "$ZIP_LIST"

# Login screens and the active JavaSteam client/downloader implementations must
# not be present. A few inert model enums may remain until the upstream database
# schema is migrated, but they have no runtime entry point.
reject_matches "'Lin/dragonbra/javasteam/(steam/authentication|steam/steamclient|depotdownloader)/|'Lapp/gamenative/ui/screen/(login|auth|downloads)/" "$CLASSES"

# Wine uses AF_INET sockets for local IPC and interface discovery. Android gates
# those calls behind INTERNET even when no remote endpoint is contacted.
rg 'android\.permission\.INTERNET' "$MANIFEST"

# Online service roots remain forbidden; socket capability is not an online
# product entry point.
reject_matches 'app\.gamenative\.service\.SteamService|app\.gamenative\.service\.(gog|epic|amazon)' "$MANIFEST"
rg 'android\.permission\.ACCESS_NETWORK_STATE' "$MANIFEST"

# Retained runtime evidence.
rg '^assets/lsfg_vk/android_arm64_v8a/VkLayer_LS_frame_generation\.json$' "$ZIP_LIST"
rg '^assets/lsfg_vk/android_arm64_v8a/liblsfg-vk-layer\.so$' "$ZIP_LIST"
rg '/storage/emulated/0/GameFoldPC/lsfg/inputtest/Lossless\.dll' "$DEX_STRINGS"
reject_matches '/Users/|/home/[^/]+/(Downloads|Documents|Desktop)/|[A-Za-z]:\\\\Users\\\\' "$DEX_STRINGS"
rg '^lib/arm64-v8a/libevshim\.so$' "$ZIP_LIST"
rg '^lib/arm64-v8a/libwinlator\.so$' "$ZIP_LIST"

#!/usr/bin/env bash
set -euo pipefail

ROOT="${1:?repository root required}"
RES="$ROOT/app/src/main/res"

LAUNCHER_VECTOR="$RES/drawable/gamefold_system_icon.xml"
NOTIFICATION_MARK="$RES/drawable/gamefold_notification_mark.xml"

test "$(shasum -a 256 "$LAUNCHER_VECTOR" | awk '{print $1}')" = \
    "6f95c67e2d3fe5efede2bc2470380e21d8b3717f011d6215ef183d682068a042"
test "$(shasum -a 256 "$NOTIFICATION_MARK" | awk '{print $1}')" = \
    "3aff0c2a793df80378e3fff5a7ae494049948976a6e9208eb0035c52122ddf94"

for version_dir in mipmap-anydpi mipmap-anydpi-v26 mipmap-anydpi-v33; do
    for icon in ic_launcher ic_launcher_round ic_launcher_alt ic_launcher_alt_round; do
        cmp "$LAUNCHER_VECTOR" "$RES/$version_dir/$icon.xml"
    done
done

cmp "$RES/mipmap-anydpi/ic_launcher.xml" "$RES/mipmap-anydpi/ic_launcher_alt.xml"
cmp "$RES/mipmap-anydpi/ic_launcher_round.xml" "$RES/mipmap-anydpi/ic_launcher_alt_round.xml"
cmp "$RES/mipmap-anydpi-v26/ic_launcher.xml" "$RES/mipmap-anydpi-v26/ic_launcher_alt.xml"
cmp "$RES/mipmap-anydpi-v26/ic_launcher_round.xml" "$RES/mipmap-anydpi-v26/ic_launcher_alt_round.xml"
cmp "$RES/mipmap-anydpi-v33/ic_launcher.xml" "$RES/mipmap-anydpi-v33/ic_launcher_alt.xml"
cmp "$RES/mipmap-anydpi-v33/ic_launcher_round.xml" "$RES/mipmap-anydpi-v33/ic_launcher_alt_round.xml"

cmp "$RES/drawable-anydpi/ic_notification.xml" "$RES/drawable-anydpi/ic_notification_alt.xml"
rg '@drawable/gamefold_notification_mark' "$RES/drawable-anydpi/ic_notification.xml"

SETTINGS_INTERFACE="$ROOT/app/src/main/java/app/gamenative/ui/screen/settings/SettingsGroupInterface.kt"
test "$(rg -c 'notificationIconRes = R\.drawable\.gamefold_notification_mark' "$SETTINGS_INTERFACE")" -eq 2

rg 'mutableStateOf\(container\.isShowFPS\)' \
    "$ROOT/app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt"
rg 'container\.isShowFPS = enabled' \
    "$ROOT/app/src/main/java/app/gamenative/ui/screen/xserver/XServerScreen.kt"

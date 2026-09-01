#!/usr/bin/env bash
set -euo pipefail

ROOT="${1:?repository root required}"
MANIFEST="$ROOT/app/src/main/AndroidManifest.xml"
BUILD="$ROOT/app/build.gradle.kts"

! rg -n 'GOGOAuthActivity|EpicOAuthActivity|AmazonOAuthActivity|service\.gog\.GOGService|service\.epic\.EpicService|service\.amazon\.AmazonService' "$MANIFEST"
! rg -n 'android\.permission\.INTERNET|ACCESS_WIFI_STATE|REQUEST_INSTALL_PACKAGES' "$MANIFEST"
rg -n 'android\.permission\.ACCESS_NETWORK_STATE' "$MANIFEST"
! rg -n 'posthog|auth0\.android|androidx\.browser:browser' "$BUILD"

for asset in \
    graphics_driver_download.json \
    core_drivers_download.json \
    dxwrapper_download.json \
    wincomponents_download.json \
    container_files_download.json; do
    test ! -e "$ROOT/app/src/main/assets/$asset"
done

test -e "$ROOT/app/src/main/java/app/gamenative/ui/screen/library/OfflineLibraryScreen.kt"
test -e "$ROOT/app/src/main/java/app/gamenative/utils/LsfgDllLocator.kt"

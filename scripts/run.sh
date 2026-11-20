#!/usr/bin/env bash
# Build the debug APK, install it on the connected device and launch it.
set -euo pipefail
source "$(dirname "$0")/env.sh"
require_device

cd "$PROJECT_ROOT"
./gradlew :app:installDebug --quiet
adb shell am start -n "$DEBUG_APP_ID/$MAIN_ACTIVITY" >/dev/null
echo "Started $DEBUG_APP_ID on $(adb shell getprop ro.product.model | tr -d '\r')"

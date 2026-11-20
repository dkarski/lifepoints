#!/usr/bin/env bash
# Print the device facts the project depends on (Android version, API level, screen, OS build).
set -euo pipefail
source "$(dirname "$0")/env.sh"
require_device

prop() { adb shell getprop "$1" | tr -d '\r'; }
echo "Model:        $(prop ro.product.model) ($(prop ro.product.manufacturer))"
echo "OS build:     $(prop ro.build.display.id)"
echo "Android:      $(prop ro.build.version.release) (API $(prop ro.build.version.sdk))"
echo "ABI:          $(prop ro.product.cpu.abi)"
echo "Screen:       $(adb shell wm size | tr -d '\r' | tail -1)"
echo "Density:      $(adb shell wm density | tr -d '\r' | tail -1)"
echo "RAM:          $(adb shell cat /proc/meminfo | tr -d '\r' | awk '/MemTotal/ {printf "%.1f GB", $2/1024/1024}')"
echo "GMS present:  $(adb shell pm path com.google.android.gms >/dev/null 2>&1 && echo yes || echo no)"

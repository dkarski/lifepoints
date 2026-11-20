#!/usr/bin/env bash
# Measure cold start, memory and APK size of an installed app on the device.
# Usage: scripts/bench.sh [package] [activity] [runs]
#   defaults: the release build of this app, 10 runs.
# Compare with a Views baseline by passing its package/activity.
set -euo pipefail
source "$(dirname "$0")/env.sh"
require_device

PKG="${1:-$RELEASE_APP_ID}"
ACT="${2:-$MAIN_ACTIVITY}"
RUNS="${3:-10}"

if ! adb shell pm path "$PKG" >/dev/null 2>&1; then
  echo "Package $PKG is not installed on the device." >&2
  exit 1
fi

echo "== $PKG on $(adb shell getprop ro.product.model | tr -d '\r'), Android $(adb shell getprop ro.build.version.release | tr -d '\r')"

# Cold start: force-stop before every launch, read TotalTime from am start -W.
times=()
for i in $(seq 1 "$RUNS"); do
  adb shell input keyevent KEYCODE_WAKEUP; adb shell wm dismiss-keyguard
  adb shell am force-stop "$PKG"
  sleep 1
  t="$(adb shell am start -W -n "$PKG/$ACT" | tr -d '\r' | awk -F': ' '/TotalTime/ {print $2}')"
  times+=("$t")
  sleep 2
done
sorted="$(printf '%s\n' "${times[@]}" | sort -n)"
median="$(echo "$sorted" | awk '{a[NR]=$1} END {print (NR%2 ? a[(NR+1)/2] : int((a[NR/2]+a[NR/2+1])/2))}')"
echo "Cold start TotalTime ms (n=$RUNS): median=$median  all=[$(echo "$sorted" | paste -sd, -)]"

# Memory after the last launch has settled.
sleep 3
adb shell dumpsys meminfo "$PKG" | tr -d '\r' | awk '/TOTAL PSS:|TOTAL:/ {print "Memory: " $0; exit}'

# Installed APK size.
APK_PATH="$(adb shell pm path "$PKG" | head -1 | tr -d '\r' | sed 's/package://')"
SIZE="$(adb shell stat -c %s "$APK_PATH" | tr -d '\r')"
echo "APK size: $((SIZE / 1024)) KB ($APK_PATH)"

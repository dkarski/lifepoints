#!/usr/bin/env bash
# Shared environment for project scripts. Sourced, not executed.
# Uses Android Studio's bundled JBR (arm64) instead of the system x86_64 JDK,
# and the SDK's platform-tools adb instead of an old one elsewhere on PATH.

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export JAVA_HOME="${KOMPAKT_JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"

BUILD_TOOLS_DIR="$(ls -d "$ANDROID_HOME"/build-tools/* 2>/dev/null | sort -V | tail -1)"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$BUILD_TOOLS_DIR:$PATH"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export PROJECT_ROOT

# App identity. The Kotlin namespace kept the earlier working name; the package is dev.dkarski.lifepoints.
APP_ID="dev.dkarski.lifepoints"
export APP_SLUG="lifepoints"
export DEBUG_APP_ID="$APP_ID.debug"
export RELEASE_APP_ID="$APP_ID"
export MAIN_ACTIVITY="dev.dkarski.tcglivecounter.MainActivity"

# With an emulator and a phone connected at once, target the physical Kompakt unless ANDROID_SERIAL is set.
if [[ -z "${ANDROID_SERIAL:-}" ]]; then
  PHYSICAL="$(adb devices 2>/dev/null | awk 'NR>1 && $2=="device" && $1 !~ /^emulator-/ {print $1; exit}')"
  [[ -n "$PHYSICAL" ]] && export ANDROID_SERIAL="$PHYSICAL"
fi

# Fails with a clear message when no device/emulator is connected.
require_device() {
  if ! adb get-state >/dev/null 2>&1; then
    echo "No device found. Connect the Kompakt over USB with USB debugging enabled (check: adb devices)." >&2
    exit 1
  fi
}

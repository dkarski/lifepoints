#!/usr/bin/env bash
# Take a screenshot of the device so it can be reviewed (also by AI).
# Usage: scripts/shot.sh [name]   -> screenshots/<name or timestamp>.png
set -euo pipefail
source "$(dirname "$0")/env.sh"
require_device

mkdir -p "$PROJECT_ROOT/screenshots"
OUT="$PROJECT_ROOT/screenshots/${1:-$(date +%Y%m%d-%H%M%S)}.png"
adb exec-out screencap -p > "$OUT"
echo "$OUT"

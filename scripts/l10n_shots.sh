#!/usr/bin/env bash
# Screenshots of every screen with text, per language, for the localization review (docs/i18n/review/).
#
# Usage: scripts/l10n_shots.sh [lang ...]      default: the 9 MuditaOS system languages
#   Runs on the Kompakt_K1 emulator (ANDROID_SERIAL=emulator-5554 by default) with the debug build installed.
#   The language is set by writing the app's preferences with run-as (debug builds only), so no test code is
#   needed in the app. Tap coordinates are for Kompakt_K1 (480x800, 213 dpi, 32 px status bar); the layout is
#   the same in every language, so English coordinates work for all.
# Output: docs/i18n/screenshots/l10n/<lang>/NN-screen.png   (emulator screenshots, not E Ink)
set -euo pipefail
source "$(dirname "$0")/env.sh"
export ANDROID_SERIAL="${L10N_SERIAL:-emulator-5554}"
require_device

PKG="$DEBUG_APP_ID"
ACT="$PKG/$MAIN_ACTIVITY"
LANGS=("${@:-en it pl cs pt es de fr nl}")
[[ $# -eq 0 ]] && LANGS=(en it pl cs pt es de fr nl)
OUT_ROOT="${L10N_OUT:-$PROJECT_ROOT/docs/i18n/screenshots/l10n}"
WAIT="${L10N_WAIT:-1.3}"

tap() { adb shell input tap "$1" "$2"; sleep "$WAIT"; }
back() { adb shell input keyevent KEYCODE_BACK; sleep "$WAIT"; }
shot() { adb exec-out screencap -p > "$OUT/$1.png"; }

# Fresh app with the given language (and Light theme), starting on Home.
fresh() {
  adb shell pm clear "$PKG" >/dev/null
  adb shell run-as "$PKG" mkdir -p shared_prefs
  printf '<?xml version="1.0" encoding="utf-8" standalone="yes" ?>\n<map>\n    <string name="language">%s</string>\n    <string name="theme">Light</string>\n</map>\n' "$1" \
    | adb shell run-as "$PKG" sh -c "'cat > shared_prefs/settings.xml'"
  adb shell am start -n "$ACT" >/dev/null
  sleep 2.5
}

# About with one section expanded, scrolled so its text is visible.
about_section() { # $1 lang, $2 y of the section row, $3 file name
  fresh "$1"; tap 400 762; tap 224 323; tap 224 "$2"
  adb shell input swipe 240 700 240 260 400; sleep "$WAIT"
  shot "$3"
}

for L in "${LANGS[@]}"; do
  OUT="$OUT_ROOT/$L"; mkdir -p "$OUT"
  echo "== $L"
  if [[ "${L10N_ONLY:-}" == "home" ]]; then
    # Only the New game screen states (tile captions, custom value): 01, 02 and 17.
    fresh "$L"; shot 01-home
    tap 240 208; shot 02-custom-life
    adb shell input text 25; back; tap 240 470; shot 17-custom-value
    continue
  fi
  if [[ -n "${L10N_QUICK:-}" ]]; then
    # Quick mode: only the screens with buttons and tiles, to catch overflow in many languages.
    GAME=80; SETTINGS=400
    case "$L" in he|fa|ur) GAME=400; SETTINGS=80;; esac  # RTL mirrors the bottom bar
    fresh "$L"; shot 01-home; tap 239 762; shot 04-history-coming-soon; tap $SETTINGS 762; shot 06-settings
    tap $GAME 762; tap 240 632; tap 241 417; shot 12-game-menu
    continue
  fi
  fresh "$L"; shot 01-home
  tap 240 208; shot 02-custom-life
  adb shell input text 25; back; tap 240 470; shot 17-custom-value   # value entered: tile reads like the presets
  fresh "$L"
  tap 239 358; shot 03-players-coming-soon; back
  tap 239 762; shot 04-history-coming-soon
  tap 240 372; tap 240 404; shot 05-contact-no-email-app  # button sits lower when the text above wraps
  tap 400 762; shot 06-settings
  tap 128 248; shot 07-language
  tap 444 75; shot 08-language-search; back; back; back
  tap 224 323; shot 09-about; back
  tap 224 398; shot 10-tip-jar; back
  tap 80 762; tap 240 632
  for _ in 1 2 3; do adb shell input tap 240 739; done
  for _ in 1 2 3 4 5 6 7 8; do adb shell input tap 240 93; done
  sleep "$WAIT"; shot 11-counter
  tap 241 417; shot 12-game-menu
  about_section "$L" 506 13-about-privacy
  about_section "$L" 577 14-about-terms
  about_section "$L" 648 15-about-licenses
  about_section "$L" 719 16-about-legal
done
echo "Saved to $OUT_ROOT"

#!/usr/bin/env bash
# Screenshots of every screen with text, per language, for the localization review (docs/i18n/review/).
#
# Usage: scripts/l10n_shots.sh [lang ...]      default: the 9 MuditaOS system languages
#   Runs on the Kompakt_K1 emulator (L10N_SERIAL, default emulator-5554) or a Kompakt, with the debug build
#   installed. The language is set by writing the app's preferences with run-as (debug builds only), so no test
#   code is needed in the app. Buttons and rows are found by their translated text (from the language's
#   strings.xml) in a UI dump, so the script works for every language, also right-to-left ones.
#   L10N_QUICK=1: only the screens with buttons and tiles, to catch overflow in many languages.
#   L10N_OUT=<dir>: output root instead of docs/i18n/screenshots/l10n.
# Output: <out>/<lang>/NN-screen.png (screenshots are not kept in Git)
set -euo pipefail
source "$(dirname "$0")/env.sh"
export ANDROID_SERIAL="${L10N_SERIAL:-emulator-5554}"
require_device

PKG="$DEBUG_APP_ID"
ACT="$PKG/$MAIN_ACTIVITY"
RES="$PROJECT_ROOT/app/src/main/res"
LANGS=("$@")
[[ $# -eq 0 ]] && LANGS=(en it pl cs pt es de fr nl)
OUT_ROOT="${L10N_OUT:-$PROJECT_ROOT/docs/i18n/screenshots/l10n}"
WAIT="${L10N_WAIT:-1.3}"

back() { adb shell input keyevent KEYCODE_BACK; sleep "$WAIT"; }
shot() { adb exec-out screencap -p > "$OUT/$1.png"; }

# Values folder of a language tag (as in data/AppLanguage.kt).
folder() {
  case "$1" in
    en) echo values ;; he) echo values-iw ;; id) echo values-in ;; fil) echo values-b+fil ;;
    zh-Hans) echo values-b+zh+Hans ;; zh-Hant) echo values-b+zh+Hant ;; *) echo "values-$1" ;;
  esac
}

# Translation of string key $2 in language $1; $3 (optional) fills the %1$d / %1$s placeholder.
text_of() {
  python3 -I - "$RES/$(folder "$1")/strings.xml" "$2" "${3:-}" <<'EOF'
import sys, xml.etree.ElementTree as ET
path, key, arg = sys.argv[1:4]
for s in ET.parse(path).getroot().iter("string"):
    if s.get("name") == key:
        t = "".join(s.itertext()).replace("\\'", "'").replace('\\"', '"')
        print(t.replace("%1$d", arg).replace("%1$s", arg))
EOF
}

# Taps the element whose text or content description is the translation of key $2 in language $1.
tap_key() { tap_text "$(text_of "$@")"; }

# Taps the element whose text or content description is exactly $1.
tap_text() {
  local text="$1"
  adb shell uiautomator dump /sdcard/l10n.xml >/dev/null
  local xy
  xy="$(adb exec-out cat /sdcard/l10n.xml | python3 -I -c '
import re, sys, xml.etree.ElementTree as ET
want = sys.argv[1]
for n in ET.fromstring(sys.stdin.read()).iter("node"):
    if want in (n.get("text"), n.get("content-desc")):
        x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
        print((x1 + x2) // 2, (y1 + y2) // 2); break
' "$text")"
  [[ -n "$xy" ]] || { echo "  not found: $text" >&2; return 1; }
  adb shell input tap $xy; sleep "$WAIT"
}

# Fresh app with the given language (and Light theme): a new 20-20 game on the counter.
fresh() {
  adb shell input keyevent KEYCODE_WAKEUP; adb shell wm dismiss-keyguard
  adb shell pm clear "$PKG" >/dev/null
  adb shell run-as "$PKG" mkdir -p shared_prefs
  printf '<?xml version="1.0" encoding="utf-8" standalone="yes" ?>\n<map>\n    <string name="language">%s</string>\n    <string name="theme">Light</string>\n</map>\n' "$1" \
    | adb shell run-as "$PKG" sh -c "'cat > shared_prefs/settings.xml'"
  adb shell am start -n "$ACT" >/dev/null
  sleep 2.5
}

# Presses Back until the translation of key $2 is on screen (at most 4 times): the number of steps back
# depends on whether the keyboard or the search field is open.
back_to() {
  local text i
  text="$(text_of "$1" "$2")"
  for i in 1 2 3 4; do
    back
    adb shell uiautomator dump /sdcard/l10n.xml >/dev/null
    adb exec-out cat /sdcard/l10n.xml | grep -qF "text=\"$text\"" && return 0
  done
  echo "  not reached: $text" >&2; return 1
}

# Counter -> Menu -> Settings.
settings() { tap_key "$1" counter_open_menu; tap_key "$1" settings_title; }
# Settings scrolled to the bottom (language, about, tip jar rows).
scroll_down() { adb shell input swipe 240 650 240 200 400; sleep "$WAIT"; }
# One life lost on the bottom half, so the game has progress.
progress() { tap_key "$1" counter_decrease "$(text_of "$1" player_name 1)"; }

# About with one section expanded ($2: string key of the section), scrolled so its text is visible.
about_section() {
  fresh "$1"; settings "$1"; scroll_down; tap_key "$1" settings_about; tap_key "$1" "$2"
  adb shell input swipe 240 700 240 260 400; sleep "$WAIT"
  shot "$3"
}

for L in "${LANGS[@]}"; do
  OUT="$OUT_ROOT/$L"; mkdir -p "$OUT"
  echo "== $L"
  fresh "$L"; shot 01-counter
  tap_key "$L" counter_open_menu; shot 02-menu
  tap_key "$L" settings_title; shot 03-settings
  if [[ -n "${L10N_QUICK:-}" ]]; then
    back; back; progress "$L"; tap_key "$L" counter_open_menu; tap_key "$L" menu_restart; shot 04-restart-confirm
    continue
  fi
  tap_key "$L" custom_life; shot 04-custom-edit
  adb shell input text 25; adb shell input keyevent KEYCODE_ENTER; sleep "$WAIT"; shot 05-custom-value
  back; back; progress "$L"
  tap_key "$L" counter_open_menu; tap_key "$L" menu_restart; shot 06-restart-confirm
  tap_key "$L" cancel; tap_key "$L" settings_title
  tap_text "40 $(text_of "$L" home_unit_life)"; shot 07-settings-pending   # the game has progress: pending change
  scroll_down; shot 08-settings-app
  tap_key "$L" settings_language; shot 09-language
  tap_key "$L" language_search; shot 10-language-search; back_to "$L" settings_about   # the About row is only in Settings
  tap_key "$L" settings_about; shot 11-about; back
  tap_key "$L" about_tip_jar; shot 12-tip-jar; back
  adb shell input swipe 240 200 240 650 400; sleep "$WAIT"
  tap_text "3 $(text_of "$L" soon)"; shot 13-players-coming-soon
  tap_key "$L" coming_soon_contact; shot 14-contact-no-email-app   # the emulator has no e-mail app
  about_section "$L" about_privacy 15-about-privacy
  about_section "$L" about_terms 16-about-terms
  about_section "$L" about_licenses 17-about-licenses
  about_section "$L" about_legal 18-about-legal
done
echo "Saved to $OUT_ROOT"

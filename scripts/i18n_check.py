#!/usr/bin/env python3
"""Checks every translation against the English source (app/src/main/res/values/strings.xml).

Errors (exit code 1):
  - a language from data/AppLanguage.kt has no strings.xml, or a strings.xml has no language in the list
  - missing or extra keys, invalid XML
  - placeholders differ from English (%1$s, %2$s, %1$d ...)
  - unescaped apostrophe (Android needs \\' or the whole text in double quotes), stray '@' or '?' at the start
Warnings:
  - very long translations of short UI labels (risk of overflow on the 480x800 screen)
  - text identical to English (possibly untranslated; fine for names and loanwords)

Usage: scripts/i18n_check.py [--quiet]   (standard library only)
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"
LANG_FILE = ROOT / "app/src/main/java/dev/dkarski/tcglivecounter/data/AppLanguage.kt"

# BCP 47 tag (as in AppLanguage.kt) -> values folder qualifier.
SPECIAL = {"he": "iw", "id": "in", "zh-Hans": "b+zh+Hans", "zh-Hant": "b+zh+Hant", "fil": "b+fil"}
# Short labels shown in tiles, tabs and buttons: warn when a translation is much longer than English.
SHORT_LABELS = {
    "soon", "tab_game", "tab_history", "tab_settings", "home_unit_life", "home_custom", "home_unit_players",
    "home_start_game", "menu_continue", "menu_new_game", "settings_theme_light", "settings_theme_dark",
    "coming_soon_copy", "settings_language", "language_system_default", "settings_about", "about_tip_jar",
}
PLACEHOLDER = re.compile(r"%(\d+\$)?[sd]")


def qualifier(tag: str) -> str:
    return "values" if tag == "en" else "values-" + SPECIAL.get(tag, tag)


def load(path: Path) -> dict:
    root = ET.parse(path).getroot()
    out = {}
    for s in root.iter("string"):
        if s.get("translatable") == "false":
            continue
        out[s.get("name")] = "".join(s.itertext())
    return out


def raw_texts(path: Path) -> dict:
    # Raw XML text per key, to check escaping as written in the file.
    text = path.read_text(encoding="utf-8")
    return dict(re.findall(r'<string name="([^"]+)"[^>]*>(.*?)</string>', text, re.S))


def main() -> int:
    quiet = "--quiet" in sys.argv
    tags = re.findall(r'AppLanguage\("([^"]+)"', LANG_FILE.read_text(encoding="utf-8"))
    source = load(RES / "values/strings.xml")
    errors, warnings = [], []

    expected_dirs = {qualifier(t) for t in tags}
    for d in sorted(p.parent.name for p in RES.glob("values-*/strings.xml")):
        if d not in expected_dirs:
            errors.append(f"{d}: strings.xml for a language that is not in AppLanguage.kt")

    for tag in tags:
        if tag == "en":
            continue
        path = RES / qualifier(tag) / "strings.xml"
        if not path.exists():
            errors.append(f"{tag}: missing {path.relative_to(ROOT)}")
            continue
        try:
            strings = load(path)
        except ET.ParseError as e:
            errors.append(f"{tag}: invalid XML: {e}")
            continue
        raw = raw_texts(path)
        missing = source.keys() - strings.keys()
        extra = strings.keys() - source.keys()
        if missing:
            errors.append(f"{tag}: missing keys: {', '.join(sorted(missing))}")
        if extra:
            errors.append(f"{tag}: extra keys: {', '.join(sorted(extra))}")
        for key, en in source.items():
            tr = strings.get(key)
            if tr is None:
                continue
            if sorted(PLACEHOLDER.findall(en)) != sorted(PLACEHOLDER.findall(tr)):
                errors.append(f"{tag}/{key}: placeholders {PLACEHOLDER.findall(tr)} != English {PLACEHOLDER.findall(en)}")
            r = raw.get(key, "")
            if re.search(r"(?<!\\)'", r) and not (r.startswith('"') and r.endswith('"')):
                errors.append(f"{tag}/{key}: unescaped apostrophe (use \\')")
            if r[:1] in ("@", "?"):
                errors.append(f"{tag}/{key}: starts with '{r[:1]}' (Android reads it as a reference)")
            if not tr.strip():
                errors.append(f"{tag}/{key}: empty")
            if key in SHORT_LABELS and len(tr) > max(2 * len(en), len(en) + 10):
                warnings.append(f"{tag}/{key}: long label ({len(tr)} chars vs {len(en)}): {tr}")
            if tr == en and len(en) > 3 and not quiet:
                warnings.append(f"{tag}/{key}: same as English: {tr}")

    for w in warnings:
        print("WARN ", w)
    for e in errors:
        print("ERROR", e)
    print(f"{len(tags)} languages, {len(source)} strings each: {len(errors)} errors, {len(warnings)} warnings")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())

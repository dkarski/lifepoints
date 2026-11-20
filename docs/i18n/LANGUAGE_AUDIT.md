# Language support audit for Lifepoints 1.1.0

Date: 2026-11-20. Device: Mudita Kompakt, MuditaOS K 2.0.0, Android 12 (API 31), 480×800, 213 dpi.
Scope: the 63 languages offered by the Mudita Developer Portal (InkApps). Rendering only; no translations yet.

## Method

1. **App fonts.** The APK ships 10 Lato files from MMD (Regular, Bold, Black, Light, Hairline + italics, ~700 KB).
   Their `cmap` tables were read directly: every file covers the same 263 code points (Basic Latin, Latin-1, Polish
   letters, a few more). No Cyrillic, Greek or non-Latin scripts. The app sets no font of its own.
2. **System fonts.** `/system/fonts` (208 files, 65 MB) and `/system/etc/fonts.xml` were read from the device.
   - MuditaOS ships the full AOSP Noto set: CJK, Hebrew, Arabic (Naskh), Devanagari, Gujarati, Gurmukhi, Kannada,
     Malayalam, Tamil, Telugu, Sinhala, Thai, Lao, Khmer, Myanmar, Georgian and more. No Noto Nastaliq Urdu.
   - **The first (default) family is `lato-mudita.ttf`**, a full Lato (2164 code points: all of Latin Extended-A/B,
     Vietnamese, Greek, Cyrillic). Roboto follows as `sans-serif`. Fallback order is the order in `fonts.xml`.
3. **Rendering test on the device.** A debug-only screen (`app/src/debug/.../ScriptTestActivity.kt`, not in the
   release APK) renders UI phrases plus language-specific letters through the app's MMD typography (body text and
   bold title), with RTL layout for RTL languages and the matching locale for CJK glyph variants. 13 groups covering
   all 63 languages, captured with `adb exec-out screencap` on the Kompakt. Two groups repeated in Dark.
4. **Cost.** Cold start (`am start -W`, median of 6–10 runs) and PSS (`dumpsys meminfo`) of the test screen per script.

## Findings

- **Android font fallback works on the Kompakt with the current MMD typography.** Compose asks for Lato (MMD); any
  character Lato lacks is drawn from the system chain. No tofu, no broken shaping in any of the 63 languages.
- **Latin letters missing from MMD's Lato** (č, ř, ő, ș, ğ, ā, ư…) come from the system Lato (`lato-mudita`), so words
  keep one typeface. The system file has only the 400/500 weights, so these letters can look marginally heavier;
  not noticeable on the screenshots.
- **Complex scripts render correctly**: Indic conjuncts and reph (क्ष, ज्ञ, ್ರ), Thai/Lao/Khmer/Myanmar stacked marks
  (no clipping with MMD line heights), Arabic joining, Hebrew/Persian/Urdu RTL with numbers, Persian digits,
  Simplified vs Traditional Chinese glyph variants, Japanese kana, Hangul.
- **Urdu** is drawn in Naskh (Noto Naskh Arabic) because the device has no Nastaliq font. Correct and readable, but
  Nastaliq is the style Urdu readers expect.
- **Cost of fallback** (debug build, so absolute times are higher than release; the comparison is what matters):

  | Script group | Cold start (median) | PSS |
  |---|---|---|
  | Latin (Lato only) | 2269 ms | 65.2 MB |
  | Indic (Kannada…Sinhala) | 2294 ms | 66.7 MB |
  | Thai/Lao/Khmer/Myanmar | 2215 ms | 65.7 MB |
  | RTL + Georgian | 2234 ms | 65.6 MB |
  | CJK | 2244 ms | 74.5 MB |

  Start time: no measurable difference (within ±50 ms run-to-run noise). Memory: +0.5–1.5 MB for most scripts;
  **+9 MB for CJK** (pages of the large Noto CJK font mapped in), only while CJK text is shown. APK cost: 0 bytes.
- **System language detection.** MuditaOS offers 9 system languages: English, Italiano, Polski, Čeština, Português,
  Español, Deutsch, Français, Nederlands (`system-languages.png`). "System default" can only pick one of these; the
  other 54 languages are reachable only through the in-app language setting.

## Locale decisions

- Default (`values/`) is English and the fallback for everything else.
- Chinese: `values-b+zh+Hans` (Simplified) and `values-b+zh+Hant` (Traditional), so script decides, not region.
- Legacy Android codes: Hebrew `values-iw`, Indonesian `values-in`.
- Norwegian: Bokmål, `values-nb`.
- Filipino: `values-fil`. Resource resolution for `fil` must be confirmed in the implementation (fallback: `values-tl`).
- Serbian: Cyrillic (`values-sr`, Android's default script for Serbian). Mongolian, Kazakh, Kyrgyz: Cyrillic.
- Punjabi: Gurmukhi (`values-pa`).
- Portuguese and Spanish: one base resource each (`values-pt`, `values-es`), written in neutral European usage to match
  the MuditaOS system languages. Regional variants (pt-BR, es-419) fall back to them.

## Results (63 languages)

Status: **A** = supported with existing app and device resources, rendering confirmed on the device;
**B** = renders, but needs verification or has a known limitation; **C** = not supported without changes.
Font support: *MMD Lato* = all letters in the app's Lato; *MMD Lato + system Lato* = some letters from `lato-mudita`;
otherwise the system font that draws the script. Screenshots are in `docs/i18n/screenshots/`.

| # | Language | Locale | Script | Font support | Status | Notes / screenshot |
|---|---|---|---|---|---|---|
| 1 | Belarusian | be | Cyrillic | system Lato | A | cyr-a |
| 2 | Bulgarian | bg | Cyrillic | system Lato | A | cyr-a |
| 3 | Burmese | my | Myanmar | Noto Sans Myanmar | A | sea; Unicode text (not Zawgyi) |
| 4 | Catalan | ca | Latin | MMD Lato + system Lato (ŀ) | A | latin-c |
| 5 | Chinese Simplified | b+zh+Hans | Han (Simplified) | Noto Sans CJK | A | cjk; +9 MB PSS while shown |
| 6 | Chinese Traditional | b+zh+Hant | Han (Traditional) | Noto Sans CJK | A | cjk; distinct glyph variants confirmed |
| 7 | Croatian | hr | Latin | MMD Lato + system Lato (č, đ) | A | latin-b |
| 8 | Czech | cs | Latin | MMD Lato + system Lato | A | latin-a; also a system language |
| 9 | Danish | da | Latin | MMD Lato | A | latin-c |
| 10 | Dutch | nl | Latin | MMD Lato + system Lato (ĳ) | A | latin-c; system language |
| 11 | English | (default) | Latin | MMD Lato | A | latin-d; fallback |
| 12 | Estonian | et | Latin | MMD Lato | A | latin-c |
| 13 | Filipino | fil | Latin | MMD Lato | A | latin-e; confirm `fil` resource resolution |
| 14 | Finnish | fi | Latin | MMD Lato | A | latin-c |
| 15 | French | fr | Latin | MMD Lato | A | latin-ok; system language |
| 16 | Galician | gl | Latin | MMD Lato | A | latin-d |
| 17 | Georgian | ka | Georgian | Noto Sans Georgian | A | rtl group |
| 18 | German | de | Latin | MMD Lato | A | latin-ok; system language; long words, check UI fit |
| 19 | Greek | el | Greek | system Lato | A | cyr-b |
| 20 | Gujarati | gu | Gujarati | Noto Sans Gujarati | A | indic-a |
| 21 | Hebrew | iw | Hebrew (RTL) | Noto Sans Hebrew | A | rtl |
| 22 | Hindi | hi | Devanagari | Noto Sans Devanagari | A | indic-a |
| 23 | Hungarian | hu | Latin | MMD Lato + system Lato (ő, ű) | A | latin-a |
| 24 | Icelandic | is | Latin | MMD Lato | A | latin-ok |
| 25 | Indonesian | in | Latin | MMD Lato | A | latin-e |
| 26 | Italian | it | Latin | MMD Lato | A | latin-d; system language |
| 27 | Japanese | ja | Han + Kana | Noto Sans CJK | A | cjk; +9 MB PSS while shown |
| 28 | Kannada | kn | Kannada | Noto Sans Kannada | A | indic-b |
| 29 | Kazakh | kk | Cyrillic | system Lato | A | cyr-b |
| 30 | Khmer | km | Khmer | Noto Sans Khmer | A | sea |
| 31 | Korean | ko | Hangul | Noto Sans CJK | A | cjk |
| 32 | Kyrgyz | ky | Cyrillic | system Lato | A | cyr-b |
| 33 | Lao | lo | Lao | Noto Sans Lao | A | sea |
| 34 | Latvian | lv | Latin | MMD Lato + system Lato | A | latin-b |
| 35 | Lithuanian | lt | Latin | MMD Lato + system Lato | A | latin-b |
| 36 | Macedonian | mk | Cyrillic | system Lato | A | cyr-a |
| 37 | Malay | ms | Latin | MMD Lato | A | latin-e |
| 38 | Malayalam | ml | Malayalam | Noto Sans Malayalam | A | indic-b; long words, check UI fit |
| 39 | Marathi | mr | Devanagari | Noto Sans Devanagari | A | indic-a |
| 40 | Mongolian | mn | Cyrillic | system Lato | A | cyr-b |
| 41 | Nepali | ne | Devanagari | Noto Sans Devanagari | A | indic-a |
| 42 | Norwegian | nb | Latin | MMD Lato | A | latin-d |
| 43 | Persian | fa | Arabic (RTL) | Noto Naskh Arabic | A | rtl; Persian digits in translated text |
| 44 | Polish | pl | Latin | MMD Lato | A | latin-ok; system language; already translated |
| 45 | Portuguese | pt | Latin | MMD Lato | A | latin-ok; system language |
| 46 | Punjabi | pa | Gurmukhi | Noto Sans Gurmukhi | A | indic-a |
| 47 | Romanian | ro | Latin | MMD Lato + system Lato (ș, ț, ă) | A | latin-a |
| 48 | Romansh | rm | Latin | MMD Lato | A | latin-d |
| 49 | Russian | ru | Cyrillic | system Lato | A | cyr-a |
| 50 | Serbian | sr | Cyrillic | system Lato | A | cyr-b |
| 51 | Sinhala | si | Sinhala | Noto Sans Sinhala | A | indic-b |
| 52 | Slovak | sk | Latin | MMD Lato + system Lato | A | latin-a |
| 53 | Slovenian | sl | Latin | MMD Lato + system Lato (č) | A | latin-b |
| 54 | Spanish | es | Latin | MMD Lato | A | latin-d; system language |
| 55 | Swahili | sw | Latin | MMD Lato | A | latin-e |
| 56 | Swedish | sv | Latin | MMD Lato | A | latin-e |
| 57 | Tamil | ta | Tamil | Noto Sans Tamil | A | indic-b; long words, check UI fit |
| 58 | Telugu | te | Telugu | Noto Sans Telugu | A | indic-b |
| 59 | Thai | th | Thai | Noto Sans Thai | A | sea; stacked marks not clipped |
| 60 | Turkish | tr | Latin | MMD Lato + system Lato (ğ, ş, İ) | A | latin-a |
| 61 | Ukrainian | uk | Cyrillic | system Lato | A | cyr-a |
| 62 | Urdu | ur | Arabic (RTL) | Noto Naskh Arabic | B | rtl; Naskh instead of the expected Nastaliq |
| 63 | Vietnamese | vi | Latin | MMD Lato + system Lato (102 letters) | A | latin-b |

## Summary

1. **Without extra fonts: 62 languages fully, 63 including Urdu** (Urdu with a typographic limitation).
2. **Needs verification:** Urdu (Naskh vs Nastaliq: acceptable to readers?). During implementation: `fil` resource
   resolution, and text fit for long translations (German, Finnish, Tamil, Malayalam…) in tiles and the bottom bar.
3. **Needs technical changes:** none for rendering. The only real change is the in-app language setting, because the
   OS offers just 9 system languages.
4. **Standard Android font fallback is enough.** MuditaOS puts a full Lato first in the fallback chain, so even the
   Latin letters missing from MMD's Lato stay in the same typeface.
5. **Cheap extensions:** none needed. Proper Urdu Nastaliq would mean bundling Noto Nastaliq Urdu (~1 MB) and is not
   worth it. Unrelated cheap win: MMD bundles Hairline, Light and italic Lato files the app never uses (~0.35 MB).
6. **Recommendation for 1.1.0:** all 63 languages. Urdu ships in Naskh, with a note in the release; if a native reader
   finds it unacceptable, Urdu is the one to drop.

The test screen stays in the debug source set so the audit can be repeated on future MuditaOS versions:
`adb shell am start -n dev.dkarski.lifepoints.debug/dev.dkarski.tcglivecounter.debugtools.ScriptTestActivity --es group <name>`
(groups: latin-ok, latin-a…latin-e, cyr-a, cyr-b, rtl, indic-a, indic-b, sea, cjk; add `--ez dark true` for Dark).

## Results with the implemented app (1.1.0, 2026-11-20)

The finished app (debug build, language set through the app preference) was opened on the Kompakt in 21 languages
covering every script group: Filipino, Hebrew, Persian, Urdu, Japanese, Chinese Simplified and Traditional, Korean,
Thai, Hindi, Tamil, Malayalam, Russian, Greek, Georgian, Burmese, Khmer, German, Finnish (Home and Settings each),
plus a Hebrew game counter and Persian in Dark. Screenshots: `docs/i18n/screenshots/device/`.

- Every language rendered; no clipped labels in tiles, buttons or the bottom bar (long cases: Tamil, Malayalam,
  Georgian, German, Finnish).
- RTL (Hebrew, Persian, Urdu): the whole layout mirrors (tiles 40-30-20, bottom bar, row chevrons); life totals stay
  in Latin digits on purpose.
- Filipino resolves from `values-b+fil`.
- The 9 system languages were captured on every screen with text (emulator, `docs/i18n/screenshots/l10n/`) and
  reviewed in context; see `docs/i18n/review/index.html`.
- Release build on the Kompakt, median of 9-10 cold starts: 1.0.0 = 647 ms, 1.1.0 = 650 ms. PSS: 36.6 MB vs 37.0 MB.
  Installed APK: 2252 KB vs 2593 KB (+341 KB for 62 translations).

Status in the table above is unchanged: 62 languages A, Urdu B (Naskh instead of Nastaliq).

# Translations

Lifepoints ships in 63 languages (the InkApps Developer Portal list). English (`app/src/main/res/values/strings.xml`)
is the source and the fallback; every other language lives in its own `values-*/strings.xml`. Everything is in the
APK and works offline; Android reads only the strings of the active language.

## How the language is chosen

- **System default** (the default): the app follows the phone's language. MuditaOS K 2.0 offers 9 system languages
  (English, Italiano, Polski, Čeština, Português, Español, Deutsch, Français, Nederlands); any other phone language
  falls back to English.
- **Settings > Language**: any of the 63 languages, saved on the phone. Implementation: `data/AppLocale.kt`
  (wraps the Activity context; no AppCompat, LocaleManager needs API 33) and `data/AppLanguage.kt` (the list, in the
  order shown: System default, the 9 system languages, then the rest grouped by script). Names are each language's
  own name, so every speaker finds their language whatever language the app is in.

## Resource folders (non-obvious codes)

| Language | Folder | Why |
|---|---|---|
| Hebrew | `values-iw` | Android's legacy code for `he` |
| Indonesian | `values-in` | legacy code for `id` |
| Filipino | `values-b+fil` | three-letter code needs the BCP 47 form; verified on the Kompakt |
| Chinese Simplified / Traditional | `values-b+zh+Hans` / `values-b+zh+Hant` | script, not region |
| Norwegian | `values-nb` | Bokmål |
| Serbian | `values-sr` | Cyrillic (Android's default script for Serbian) |
| Portuguese, Spanish | `values-pt`, `values-es` | European usage; regional variants fall back to them |

`app/build.gradle.kts` limits the APK to these languages (`resourceConfigurations`), so AndroidX texts never appear in
a language the app does not offer.

## Terminology: life

“Life” is the player's life total, as in Magic: The Gathering (the 20/30/40 presets are MTG formats). Where MTG is
printed in the language, use its official term (checked on printed cards via Scryfall): DE Lebenspunkte, FR points de
vie, IT punti vita, PT pontos de vida, ES vidas, RU жизни, JA ライフ, KO 생명. The small caption under 20/30/40 is
narrow, so DE and FR/PT use the usual TCG abbreviations LP and PV there; the full term appears in the input hint.
Polish has no official MTG edition: “życia”, as players say it (“mam 20 życia”), fits any number.

The Custom tile has its own caption before a value is entered (“Custom life”, PL “Inna wartość”), because label and
caption are read together; once a value is entered it uses the life caption like the presets (“25 życia”).

## Adding or changing a text

1. Edit English in `values/strings.xml`. Add a `<!-- comment -->` with where the text appears and any length limit.
2. Run `scripts/i18n_check.py`: it lists every language missing the key.
3. Translate the key into all languages (AI is fine), keeping placeholders (`%1$s`, `%1$d`) and escaping (`\'`, `\n`).
4. Run `scripts/i18n_check.py` again (must report 0 errors) and `./gradlew :app:lintDebug`
   (MissingTranslation and ExtraTranslation are errors).
5. Check the UI: `scripts/l10n_shots.sh` captures every screen of the 9 system languages on the Kompakt_K1 emulator
   into `docs/i18n/screenshots/l10n/`; open `docs/i18n/review/index.html` to compare them.
6. For a new script or language, check rendering on the Kompakt itself (see `docs/i18n/LANGUAGE_AUDIT.md`).

## Quality status

- All 62 translations were generated with AI.
- The 9 MuditaOS system languages were reviewed in UI context (screenshots of every screen) and improved where the
  wording was literal, long or inconsistent; see the review page. A native speaker review is still recommended.
- The other 54 languages are technically complete and checked (keys, placeholders, escaping, XML) and their main
  screens were captured on the emulator to catch overflow (`L10N_QUICK=1 L10N_OUT=/tmp/x scripts/l10n_shots.sh <langs>`),
  but not reviewed for style. Treat them as good first drafts.
- The legal texts (Privacy, Terms, Legal notes) are translated for convenience; the English versions in `docs/` are
  the reference.

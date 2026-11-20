# Translations

Lifepoints ships in the 63 languages offered by InkApps. English in `app/src/main/res/values/strings.xml` is the
source and the fallback. Each other language has its own `values-*/strings.xml`. All texts are in the APK and work
offline.

## How the language is chosen

- **System default**: the app follows the phone's language. MuditaOS K 2.0 has 9 system languages (English,
  Italiano, Polski, Čeština, Português, Español, Deutsch, Français, Nederlands). Any other phone language falls back
  to English.
- **Settings > Language**: any of the 63 languages, saved on the phone. The list shows System default, the 9 system
  languages, then the rest grouped by script. Each language is shown in its own name.

The code is in `data/AppLanguage.kt` (the list) and `data/AppLocale.kt` (applies the language to the Activity
context). It does not use AppCompat, and `LocaleManager` needs API 33.

## Resource folders

Most folders use the plain language code. These do not:

| Language | Folder | Why |
|---|---|---|
| Hebrew | `values-iw` | Android's legacy code for `he` |
| Indonesian | `values-in` | Android's legacy code for `id` |
| Filipino | `values-b+fil` | three-letter codes need the BCP 47 form |
| Chinese Simplified, Traditional | `values-b+zh+Hans`, `values-b+zh+Hant` | script, not region |
| Norwegian | `values-nb` | Bokmål |
| Serbian | `values-sr` | Cyrillic, Android's default script for Serbian |
| Portuguese, Spanish | `values-pt`, `values-es` | European usage; regional variants fall back to them |

`resourceConfigurations` in `app/build.gradle.kts` limits the APK to these languages, so library texts never appear
in a language the app does not offer.

## Terminology: life

"Life" is the player's life total, as in Magic: The Gathering, whose formats the 20, 30 and 40 presets follow.
Where Magic is printed in a language, use its official term, checked on printed cards via Scryfall:

| DE | FR | IT | PT | ES | RU | JA | KO |
|---|---|---|---|---|---|---|---|
| Lebenspunkte | points de vie | punti vita | pontos de vida | vidas | жизни | ライフ | 생명 |

The caption under 20, 30 and 40 is narrow, so German uses LP and French and Portuguese use PV there, as card
players do. The full term appears in the input hint. Polish has no official Magic edition and uses "życia", as
players say it ("mam 20 życia").

Before a value is entered, the Custom tile has its own caption ("Custom life", in Polish "Inna wartość"), because
the label and caption are read together. After a value is entered, it uses the life caption like the presets.

## Adding or changing a text

1. Edit English in `values/strings.xml`. Add a comment saying where the text appears and any length limit.
2. Run `scripts/i18n_check.py`. It lists every language that is missing the key.
3. Translate the key into all languages. Keep placeholders (`%1$s`, `%1$d`) and escaping (`\'`, `\n`).
4. Run `scripts/i18n_check.py` again (0 errors) and `./gradlew :app:lintDebug`, where `MissingTranslation` and
   `ExtraTranslation` are errors.
5. Check the screens with `scripts/l10n_shots.sh`. It captures every screen in the 9 system languages on the
   Kompakt_K1 emulator into `docs/i18n/screenshots/l10n/`. Compare them in `docs/i18n/review/index.html`.
   Screenshots are not kept in Git.
6. For a new script or language, check rendering on a Kompakt. See [LANGUAGE_AUDIT.md](i18n/LANGUAGE_AUDIT.md).

## Quality

- All translations were generated with AI.
- The 9 system languages were reviewed on screenshots of every screen and reworded where needed. A review by native
  speakers is still recommended.
- The other 54 languages pass all checks (keys, placeholders, escaping, XML) and their main screens were checked
  for overflow with `L10N_QUICK=1 L10N_OUT=<dir> scripts/l10n_shots.sh <languages>`. Their style was not reviewed.
- The in-app privacy, terms and legal texts are translated for convenience. The English versions in `docs/` are the
  reference.

# Contributing

Bug reports and ideas are welcome as [issues](https://github.com/dkarski/lifepoints/issues). For larger changes,
open an issue first.

## Build

Requirements: JDK 17 and the Android SDK with Platform 35 and Build-Tools 35. Set `ANDROID_HOME`, or `sdk.dir` in
`local.properties`.

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/lifepoints-*-debug.apk
```

The debug build uses the package `dev.dkarski.lifepoints.debug`, so it installs next to the store version.

Before opening a pull request, run the same checks as CI:

```sh
python3 scripts/i18n_check.py
./gradlew :app:lintDebug :app:assembleDebug :app:assembleRelease
```

The app targets the Mudita Kompakt (Android 12, API 31, 480×800 E Ink) and uses the
[Mudita Mindful Design](https://central.sonatype.com/artifact/com.mudita/MMD) components. Keep it light: no new
libraries or permissions without discussion first.

English in `app/src/main/res/values/strings.xml` is the source for all 63 languages. To add or change a text, see
[docs/TRANSLATIONS.md](docs/TRANSLATIONS.md).

## Pull requests

Pull requests are squash-merged. The title becomes the commit on `main` and, for `feat` and `fix`, a line in the
changelog. Titles follow [Conventional Commits](https://www.conventionalcommits.org/), checked by CI:

| Type | Use for | Version |
|---|---|---|
| `feat` | a new or improved feature | minor |
| `fix` | a bug in a published version | patch |
| `perf` | a speed or memory improvement | patch |
| `docs`, `ci`, `build`, `chore`, `refactor`, `test` | everything else | none, not in the changelog |

`feat!:` marks a breaking change and makes a major release.

Write `feat` and `fix` titles for users: `feat: three-player games were added`, not `feat: add PlayerCount enum`.
Judge them against the latest published release. Clearer texts are `feat`, not `fix`. A change to something that
is new in the same unreleased version is part of that `feat` and gets no line of its own.

If a pull request has several user-visible changes, list them at the end of its description. Each line becomes a
changelog entry:

```text
BEGIN_COMMIT_OVERRIDE
feat: three-player games were added
fix: the counter no longer resets after a theme change
END_COMMIT_OVERRIDE
```

## Releasing

Releases are made by GitHub Actions only. The version lives in `version.txt`. `versionName` is read from it and
`versionCode` is derived from it (`1.2.3` becomes `1002003`). Do not edit it by hand.

1. After each merge to `main`, the Release workflow updates a pull request named `chore(release): vX.Y.Z` with
   the next version and the new `CHANGELOG.md` section.
2. Review the version and the changelog. The section becomes the release notes.
3. Squash-merge the release pull request. CI does not run on it, so use the administrator bypass.
4. The workflow tags the commit, creates a draft release, builds and signs the APK, and verifies the signing
   certificate, package name, `versionName` and `versionCode`. It then attaches `lifepoints-X.Y.Z.apk` with its
   `.sha256` file and publishes the release.
5. Optionally, name the release after what users gain, and use the same name in InkApps:
   `gh release edit vX.Y.Z --title "vX.Y.Z — Play in your language"`.

If the APK job fails, the release stays a draft. Fix the cause on `main`, then run *Actions > Release > Run
workflow* with the tag, for example `v1.1.0`. It builds the tagged commit, verifies it and publishes the draft.
A published release is never rebuilt. If the app code has to change, delete the draft release and its tag, and
release a new version.

### Publishing in InkApps

1. Download `lifepoints-X.Y.Z.apk` from the [release](https://github.com/dkarski/lifepoints/releases/latest).
   To check it: `shasum -a 256 -c lifepoints-X.Y.Z.apk.sha256`.
2. In the InkApps Developer Portal, add a new version of Lifepoints and upload this APK. Do not rebuild it.
3. Use the release notes, shortened if needed, as the changelog.
4. Submit it for review. When the status is *Accepted*, release the version.

### Signing

The signing key is not in the repository. GitHub stores it in the `release` environment, which only `main` can
use, so pull request builds cannot read it.

| Name | Kind | Content |
|---|---|---|
| `RELEASE_KEYSTORE_BASE64` | secret | the keystore file, base64-encoded |
| `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` | secrets | keystore credentials |
| `RELEASE_CERT_SHA256` | variable | SHA-256 of the signing certificate |

Every version must be signed with the same key. Android rejects an update signed with another key, so users would
have to uninstall the app and lose their data. Never generate a new release key, and keep an offline backup of the
keystore and its passwords.

To sign a release build locally, add these to `local.properties`, which Git ignores:

```properties
release.storeFile=/absolute/path/to/keystore.jks
release.storePassword=...
release.keyAlias=...
release.keyPassword=...
```

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE).

# Contributing

Thanks for your interest in Lifepoints. Bug reports and ideas are welcome as
[issues](https://github.com/dkarski/lifepoints/issues); for larger changes, please open an issue first.

## Build

Requirements: JDK 17 and the Android SDK (Platform 35, Build-Tools 35). Set `ANDROID_HOME`, or `sdk.dir` in
`local.properties`.

```sh
./gradlew :app:lintDebug :app:assembleDebug   # what CI checks on every pull request
./gradlew :app:assembleRelease                # unsigned unless signing is configured (below)
```

APKs are written to `app/build/outputs/apk/`. The debug build uses the separate `dev.dkarski.lifepoints.debug`
package, so it can be installed next to the store version:

```sh
adb install -r app/build/outputs/apk/debug/lifepoints-*-debug.apk
```

The app targets the Mudita Kompakt (MuditaOS K, Android 12 / API 31, 480×800 E Ink) and uses the
[Mudita Mindful Design](https://central.sonatype.com/artifact/com.mudita/MMD) components. Keep it light: no new
libraries or permissions without discussion. The app icon artwork is `assets/lifepoints.svg`.

## Translations

English (`app/src/main/res/values/strings.xml`) is the source; the app ships in 63 languages. How languages work,
how to add or change a text and how to check it: [docs/TRANSLATIONS.md](docs/TRANSLATIONS.md).

## Version

`version.txt` is the only place with the version. `versionName` is read from it and `versionCode` is derived from it
(`1.2.3` → `1002003`). Do not edit it by hand: the release pull request updates it.

## Commits and pull requests

Pull requests are merged with **squash**, so the pull request title becomes the commit on `main` and the line in the
changelog. Titles follow [Conventional Commits](https://www.conventionalcommits.org/) and CI checks them:

| Type | Use for | Release |
|---|---|---|
| `feat` | something new for users | minor (1.1.0 → 1.2.0) |
| `fix` | a bug fix users notice | patch (1.1.0 → 1.1.1) |
| `perf` | faster or lighter | patch |
| `docs`, `ci`, `build`, `chore`, `refactor`, `test` | everything else | none, hidden from the changelog |

A breaking change (`feat!:`) makes a major release. Write `feat` and `fix` titles for users, for example
`feat: three-player games were added`, not `feat: add PlayerCount enum`.

When one pull request brings several user-visible changes, list them at the end of its description; they become
separate changelog lines:

```text
BEGIN_COMMIT_OVERRIDE
feat: the app was translated into 63 languages
feat: a language can be chosen in Settings > Language
fix: long button labels no longer overflow
END_COMMIT_OVERRIDE
```

## Releasing

Releases are made only by GitHub Actions, never from a local machine.

1. **Merge pull requests into `main`.** The *Release* workflow keeps a pull request named `chore(release): vX.Y.Z`
   up to date with the next version (from the commit types) and the new `CHANGELOG.md` section.
2. **Review the release pull request.** Check the version and the changelog wording; it becomes the release notes.
3. **Merge it** (squash). CI does not run on this pull request, so merge it with the administrator bypass.
4. The *Release* workflow then:
   - tags the commit (`vX.Y.Z`) and creates a draft GitHub Release with the changelog section;
   - builds the release APK and signs it with the production key;
   - verifies the signing certificate (`RELEASE_CERT_SHA256`), the package name, `versionName` and `versionCode`;
   - attaches `lifepoints-X.Y.Z.apk` and its `.sha256`, and publishes the release.

If the APK job fails, the release stays a draft. Fix the cause (for example a secret) and use *Re-run failed jobs*.
If code must change, delete the draft release and its tag, merge the fix and let the workflow release again.

### Publishing in InkApps (manual)

1. Download `lifepoints-X.Y.Z.apk` from the [release](https://github.com/dkarski/lifepoints/releases/latest).
   Optionally check it: `shasum -a 256 -c lifepoints-X.Y.Z.apk.sha256`.
2. In the InkApps Developer Portal, open Lifepoints and add a new version: upload the APK (do not rebuild it).
3. Paste the release notes, shortened if needed, as the changelog.
4. Submit for review. After the status changes to *Accepted*, release the version.

### Signing

The production key never enters the repository. In GitHub it is stored in the `release` environment, which only
`main` can use, so pull request builds have no access to it:

| Name | Kind | Content |
|---|---|---|
| `RELEASE_KEYSTORE_BASE64` | secret | the keystore file, base64-encoded |
| `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` | secrets | keystore credentials |
| `RELEASE_CERT_SHA256` | variable | expected SHA-256 of the signing certificate |

Every version must be signed with the same key: Android refuses an update signed with a different one, and users
would have to uninstall the app and lose their data. Never generate a new production key. Keep an offline backup of
the keystore and its passwords.

To build a signed release locally, add to `local.properties` (not tracked by Git):

```properties
release.storeFile=/absolute/path/to/keystore.jks
release.storePassword=...
release.keyAlias=...
release.keyPassword=...
```

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE).

# Lifepoints

An offline life counter for trading card games on Mudita Kompakt, built with Kotlin,
Jetpack Compose and Mudita Mindful Design (MMD).

## Features

- Two-player counter with starting life 20, 30, 40 or a custom value.
- Saved game and Light/Dark themes.
- English and Polish interfaces.
- Offline operation with no requested permissions.
- Developer contact from Settings.
- About, privacy information, terms and open source licenses.
- Optional support through a QR code.

Three- and four-player games and game history are currently unavailable.

## Build

Requirements: JDK 17, Android SDK Platform 35 and Android SDK Build-Tools 35.
Set `ANDROID_HOME` to your SDK directory, or set `sdk.dir` in your local `local.properties`.

```sh
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

APKs are written to `app/build/outputs/apk/`. Debug builds use a separate `.debug` package.
Release builds are unsigned unless the following local properties are configured:

```properties
release.storeFile=/absolute/path/to/signing-key.jks
release.storePassword=YOUR_STORE_PASSWORD
release.keyAlias=YOUR_KEY_ALIAS
release.keyPassword=YOUR_KEY_PASSWORD
```

Keep signing keys and `local.properties` outside version control.

## App icon

The SVG artwork is in `assets/lifepoints.svg`; Android launcher resources are in `app/src/main/res/`.

## Contact

Daniel Karski — daniel.karski5q@gmail.com

## Privacy and terms

See [Privacy Policy](docs/PRIVACY_POLICY.md) and [Terms of Use](docs/TERMS_OF_USE.md).

## License

Source code: https://github.com/dkarski/lifepoints

Copyright 2026 Daniel Karski. Licensed under the [Apache License 2.0](LICENSE).
Third-party license texts are included in `app/src/main/res/raw/` and available in the app.

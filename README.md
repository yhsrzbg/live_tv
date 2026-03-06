# live_tv

Kotlin + Jetpack Compose Android TV rebuild of dart_simple_live simple_live_tv_app.

## Modules
- `:app-tv` Android TV app (Compose UI, remote control behavior, player shell)
- `:core` Live site contracts + first-phase site implementations

## Compatibility
- minSdk 30 (Android 11)

## Build
- CI verification command: `./gradlew :core:test :app-tv:lint :app-tv:testDebugUnitTest :app-tv:assembleRelease`
- GitHub Actions workflow uploads ARM release APKs:
  - `app-tv-arm64-v8a-release.apk`
  - `app-tv-armeabi-v7a-release.apk`

# Repository Guidelines

## Project Structure & Module Organization
This repository is a Kotlin multi-module Android TV project.
- `app-tv/`: Android TV app (Compose UI, navigation, Room/DataStore, input mapping, player shell).
- `core/`: shared live-site contracts, parser/signing implementations, danmaku clients, and protocol/resources (`src/main/proto`, JS assets).
- `docs/`: project status and planning notes (see `docs/phase-status.md`).
- `.github/workflows/`: CI build and release artifact workflow.

Use package roots under `com.yhsrzbg.live_tv` and keep feature code grouped by layer (`ui/`, `data/`, `core/site/`, etc.).

## Build, Test, and Development Commands
Run from repo root:
- `./gradlew :app-tv:assembleDebug` (or `gradlew.bat ...` on Windows): local debug APK.
- `./gradlew :app-tv:lint :app-tv:testDebugUnitTest :app-tv:assembleRelease`: same validation path as CI.
- `./gradlew :core:test`: runs parser/signing and contract tests in the core module.
- `./gradlew test`: runs all unit tests across modules.

If you need signed release output locally, provide `signing.properties` at the root (do not commit secrets/keystores).

## Coding Style & Naming Conventions
- Language/toolchain: Kotlin, Java 17, Compose, Gradle Kotlin DSL.
- Indentation: 4 spaces; keep files UTF-8.
- Types use `PascalCase`; functions/properties use `camelCase`; constants use `UPPER_SNAKE_CASE`.
- Compose screens end with `Screen` (for example, `LiveRoomScreen`); view models end with `ViewModel`; site providers end with `Site`.
- Prefer small, focused files in existing layer folders over cross-layer utility dumping.

## Testing Guidelines
- Framework: JUnit4 with `kotlinx-coroutines-test` where needed.
- Test files end with `Test.kt` and mirror source package paths.
- Add/adjust tests with parser, signing, model, key mapping, or contract changes.
- Minimum pre-PR checks: `:app-tv:testDebugUnitTest` and `:core:test`.

## Commit & Pull Request Guidelines
Recent history follows Conventional-style prefixes: `feat:`, `fix:`, `chore:`, `ci:` (optionally scoped, e.g., `fix(android): ...`).
- Keep commit messages imperative and focused on one change.
- PRs should include: summary, impacted modules, test commands run, and screenshots/video for UI/focus behavior updates.
- Link related issues/tasks and call out signing/ABI/release-impact changes explicitly.

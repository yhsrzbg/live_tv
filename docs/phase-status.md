# Phase 1 Status and Deferred Scope

## Implemented in this phase
- Multi-module Android project: `:app-tv` + `:core`
- Android 11+ baseline (`minSdk=30`)
- Compose navigation and main routes: Home/Hot/Category/Search/LiveRoom
- Remote key mapping aligned with legacy intent:
  - OK/Enter: toggle controls
  - Left: follow list intent
  - Right/Menu: settings intent
  - Up/Down: prev/next channel intent
  - Back: exit live room
- Core site registry with four site providers (`bilibili`, `douyu`, `huya`, `douyin`)
- DataStore + Room foundation (settings/history/follow)
- GitHub Actions build pipeline for release arm64-v8a APK only

## Deferred (not implemented in Phase 1)
- Full account system (Bilibili QR login, cookie/session management)
- Full sync workflow (discovery, protocol, conflict handling)
- Full settings parity with Flutter TV app
- Full follow/history management UX parity
- Full visual/animation 1:1 parity with Flutter UI
- Site-specific production danmaku protocol implementation
- Site-specific production parser/signing implementation parity

## Next phases
- Phase 2: complete account + sync + full settings/follow/history
- Phase 3: strengthen parser resilience, player recovery, expand automated test coverage
- Phase 4: release governance (tag/release strategy, rollback, changelog discipline)

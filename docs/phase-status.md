# Phase Status

Last updated: 2026-03-06

## Goal Alignment (against original reconstruction target)

### 1) Project shape and platform baseline
- Status: Done
- Evidence:
  - Multi-module project exists: `:app-tv` + `:core`
  - `minSdk=30` in both modules
  - Kotlin + Compose + Media3 stack in `app-tv`

### 2) `simple_live_core` -> `:core` migration
- Status: Mostly done (mainline complete)
- Done:
  - Unified `LiveSite` contract (suspend-based)
  - Unified `LiveDanmaku` contract (Flow-based)
  - Unified models for list/detail/play/search/danmaku
  - Four site providers: `bilibili`, `douyu`, `huya`, `douyin`
  - Site parser/signing and danmaku protocol implementations present
  - Super Chat contract and Bilibili implementation are in place
- Known gap kept intentionally in current phase:
  - Old Flutter `CoreLog` logging abstraction not ported to `:core`

### 3) `simple_live_tv_app` -> `:app-tv` (Phase 1 mainline)
- Status: In progress (main route available, parity pending)
- Baseline matrix:
  - Added: `docs/plans/2026-03-06-simple-live-tv-app-gap-matrix.md`
  - Note: old source path exists in primary workspace (`E:\work\live_tv\old\simple_live_tv_app\lib\modules`) but is not visible inside isolated git worktree; mapping baseline uses validated old-module list + current app state
- Done:
  - Compose routes: Home/Hot/Category/Search/LiveRoom
  - Remote key intent mapping aligned to legacy intent:
    - OK/Enter -> controls
    - Left -> follow list intent
    - Right/Menu -> settings intent
    - Up/Down -> prev/next channel intent
    - Back -> exit live room
  - DataStore + Room foundation (settings/history/follow storage)
- Not yet at old app parity:
  - Full UI/UX parity for follow/history/settings flows
  - Full live-room interaction parity and TV focus polish

### 4) CI/CD and release packaging target
- Status: Partially done (release pipeline definition exists, release build stability pending)
- Done:
  - GitHub Actions workflow exists
  - Uses signing secrets and builds release
  - Uploads ARM APK artifacts (`arm64-v8a` + `armeabi-v7a`)
- Pending / to tighten:
  - Workflow currently runs `lint/test/assembleRelease` on `:app-tv` only; `:core` checks are not explicitly included
  - App module ABI split still includes `x86/x86_64` for local builds; CI upload currently keeps ARM-only outputs (`arm64-v8a` + `armeabi-v7a`)
  - Repo bootstrap requirement via fixed `gh` path is process-level and not tracked in this repo file

### 5) Release minify blocker (resolved on 2026-03-06)
- Status: Done
- Root cause:
  - `:app-tv:minifyReleaseWithR8` failed due to Rhino references to `java.beans.*` APIs not present on Android.
- Fix:
  - Added targeted `-dontwarn java.beans.*` suppressions in `app-tv/proguard-rules.pro`.
  - Added a release config smoke test to guard the rule presence.
- Checklist:
  - [x] R8 missing-class root cause identified
  - [x] keep rules or dependency fix chosen
  - [x] local `:app-tv:assembleRelease` passes after fix

## Deferred Scope (confirmed)
- Full account system (Bilibili QR login, cookie/session management UX)
- Full multi-device sync workflow
- Full settings/follow/history UX parity with old Flutter TV app
- Full visual/animation 1:1 parity with old Flutter UI
- Broader automated testing matrix (parser edge cases, player state machine, TV UI focus E2E)

## Suggested Next Milestones
- Phase 2: app-tv parity pass (follow/history/settings/live-room controls + focus behavior)
- Phase 3: robustness pass (player recovery, parser fallback, wider test coverage including `:core`)
- Phase 4: release governance (tags/changelog/rollback discipline)

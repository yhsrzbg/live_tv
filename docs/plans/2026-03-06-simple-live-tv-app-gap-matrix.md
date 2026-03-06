# simple_live_tv_app -> app-tv Gap Matrix Baseline

Date: 2026-03-06

## Checklist (failing baseline)

- [ ] Every old route has target screen in app-tv
- [ ] Every old remote key action has mapped behavior in app-tv
- [ ] Every old persisted setting has DataStore key + UI binding

## Source validation

- Expected old path: `old/simple_live_tv_app/lib/modules`
- Actual status:
  - Present in primary workspace: `E:\work\live_tv\old\simple_live_tv_app\lib\modules`
  - Not visible in current git worktree checkout (`.worktrees/simple-live-tv-migration`)
- Handling: old-module list is validated from primary workspace path; migration status is evaluated against current `app-tv` implementation state

## Feature migration matrix

| Feature | Old Path | New Path | Status | Gap |
|---|---|---|---|---|
| Home | `modules/home` | `ui/feature/home` | In Progress | currently in `ui/screen/HomeScreen`, not yet moved to feature slice |
| Hot | `modules/hot_live` | `ui/feature/hot` | In Progress | currently in `ui/screen/HotScreen`, no feature package split yet |
| Category | `modules/category` | `ui/feature/category` | In Progress | currently in `ui/screen/CategoryScreen`, category data still uses hot list flow |
| CategoryDetail | `modules/category/detail` | `ui/feature/category/CategoryDetailScreen` | Missing | route + screen + repository flow not wired |
| SearchRoom | `modules/search/room` | `ui/feature/search` | In Progress | room search exists but still in shared listing screen file |
| SearchAnchor | `modules/search/anchor` | `ui/feature/search/SearchAnchorScreen` | Missing | route + screen + repository method missing |
| Follow | `modules/follow_user` | `ui/feature/follow` | Missing | screen + nav + state/tests missing |
| History | `modules/history` | `ui/feature/history` | Missing | screen + nav + clear behavior/tests missing |
| Settings | `modules/settings` | `ui/feature/settings` | Missing | settings screen/actions parity subset not implemented |
| LiveRoom | `modules/live_room` | `ui/feature/live_room` | In Progress | key intent mapping exists, behavior wiring still partial |

## Remote key behavior matrix

| Key action | Expected behavior | Current status | Gap |
|---|---|---|---|
| OK / Enter | Toggle controls | Done | none |
| LEFT | Open follow screen | In Progress | currently triggers follow-current action, not follow route |
| RIGHT / MENU | Open settings screen | In Progress | currently toggles controls state instead of navigating to settings |
| UP | Previous channel | Missing | callback is wired but no channel switch behavior in app shell |
| DOWN | Next channel | Missing | callback is wired but no channel switch behavior in app shell |
| BACK | Exit live room | Done | none |

## Persisted settings matrix (parity subset baseline)

| Setting | DataStore key exists | UI binding exists | Status | Gap |
|---|---|---|---|---|
| Danmaku enabled | Yes (`danmaku_enabled`) | Yes (LiveRoom overlay) | Done | none |
| Quality level | Yes (`quality_level`) | No | In Progress | missing visible setting flow and interaction |
| Scale mode | No | No | Missing | key/flow/setter/UI missing |
| Player compat mode | No | No | Missing | key/flow/setter/UI missing |

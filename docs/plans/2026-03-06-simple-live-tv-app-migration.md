# simple_live_tv_app -> app-tv Migration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Migrate old Flutter `old/simple_live_tv_app` mainline user flows into native Kotlin `:app-tv` with feature/behavior parity, stable data flow, and release-ready quality gates.

**Architecture:** Keep `:core` as cross-site protocol/data provider and evolve `:app-tv` into feature-layered Compose screens (`ui/feature/*`) backed by a single repository + focused use-cases. Migrate in vertical slices (feature by feature) with TDD on state and mapping logic first, then UI behavior and focus navigation. Keep deferred scope (account/sync full parity) explicitly behind feature flags or documented out-of-scope markers.

**Tech Stack:** Kotlin, Jetpack Compose (TV), Navigation Compose, Room, DataStore, Media3, JUnit4, kotlinx-coroutines-test.

---

## Execution Constraints

- Do not intentionally break the build as part of the migration workflow.
- For move-only or package-layout refactors, keep the project compiling after each small edit batch and use compile checks as guardrails, not as deliberately failing milestones.

## Scope and Non-Goals

- Migration principle (must follow):
  - Feature behavior must be consistent with old app.
  - Implementation details, architecture patterns, and dependencies are allowed to differ if behavior and acceptance criteria stay equivalent.

- In scope:
  - Home/Hot/Category/CategoryDetail/SearchRoom/SearchAnchor/LiveRoom/Follow/History/Settings parity pass
  - TV remote key behavior parity (OK/Menu/Left/Up/Down/Back)
  - Persistent settings/follow/history behavior parity
  - Player interaction parity baseline (quality, danmaku switch, channel switch intent)
- Out of scope for this phase:
  - Full Bilibili QR login UX parity
  - Full sync workflow parity
  - Full Flutter visual 1:1 animation parity

## Migration Mapping (old -> new)

- `old/simple_live_tv_app/lib/modules/home` -> `app-tv/ui/feature/home`
- `old/simple_live_tv_app/lib/modules/hot_live` -> `app-tv/ui/feature/hot`
- `old/simple_live_tv_app/lib/modules/category(+detail)` -> `app-tv/ui/feature/category`
- `old/simple_live_tv_app/lib/modules/search/room|anchor` -> `app-tv/ui/feature/search`
- `old/simple_live_tv_app/lib/modules/follow_user` -> `app-tv/ui/feature/follow`
- `old/simple_live_tv_app/lib/modules/history` -> `app-tv/ui/feature/history`
- `old/simple_live_tv_app/lib/modules/settings` -> `app-tv/ui/feature/settings`
- `old/simple_live_tv_app/lib/modules/live_room` -> `app-tv/ui/feature/live_room`

## Delivery Milestones

1. Foundation and route skeleton complete
2. Category and repository parity baseline complete
3. Follow/History/Settings parity complete
4. LiveRoom behavior parity baseline complete
5. Release verification unblocked, CI hardened, and docs updated

---

### Task 1: Build Migration Baseline Matrix

**Files:**
- Create: `docs/plans/2026-03-06-simple-live-tv-app-gap-matrix.md`
- Modify: `docs/phase-status.md`

**Step 1: Write failing documentation checklist**

```markdown
- [ ] Every old route has target screen in app-tv
- [ ] Every old remote key action has mapped behavior in app-tv
- [ ] Every old persisted setting has DataStore key + UI binding
```

**Step 2: Validate current gaps manually**

Run: `Get-ChildItem -Recurse old/simple_live_tv_app/lib/modules -Directory`
Expected: old feature modules list available for mapping.

**Step 3: Fill matrix with status (Done/In Progress/Missing)**

```markdown
| Feature | Old Path | New Path | Status | Gap |
|---|---|---|---|---|
| Follow | modules/follow_user | ui/feature/follow | Missing | screen + nav + tests |
```

**Step 4: Commit**

```bash
git add docs/plans/2026-03-06-simple-live-tv-app-gap-matrix.md docs/phase-status.md
git commit -m "docs: add simple_live_tv_app migration gap matrix"
```

### Task 2: Refactor app-tv package layout for feature slices

**Files:**
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/home/HomeScreen.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/hot/HotScreen.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/category/CategoryScreen.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/search/SearchScreen.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/screen/ListingScreens.kt`

**Step 1: Capture a compile baseline before refactor**

Run: `./gradlew :app-tv:compileDebugKotlin --console=plain`
Expected: PASS.

**Step 2: Move screen composables into feature packages with same signatures in small batches**

```kotlin
@Composable
fun HotScreen(
    siteId: String,
    load: suspend () -> List<LiveRoomItem>,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit,
)
```

**Step 3: Repoint imports, remove the old container file, and keep compile green after each batch**

Run: `./gradlew :app-tv:compileDebugKotlin --console=plain`
Expected: PASS.

**Step 4: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui
git commit -m "refactor(app-tv): split listing screens into feature packages"
```

### Task 3: Expand navigation routes to old-app parity baseline

**Files:**
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/navigation/Route.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/LiveTvApp.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/follow/FollowScreen.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/history/HistoryScreen.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/settings/SettingsScreen.kt`

**Step 1: Write failing nav tests for new routes**

- Test: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/navigation/RouteTest.kt`

```kotlin
@Test
fun followRoute_hasExpectedPattern() {
    assertEquals("follow", Route.Follow.value)
}
```

**Step 2: Run tests and verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*RouteTest" --console=plain`
Expected: FAIL (missing route entries).

**Step 3: Add routes and NavHost entries**

```kotlin
data object Follow : Route("follow")
data object History : Route("history")
data object Settings : Route("settings")
```

**Step 4: Re-run tests**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*RouteTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/navigation/RouteTest.kt
git commit -m "feat(app-tv): add follow/history/settings navigation routes"
```

### Task 4: Wire real category and category-detail data flows early

**Files:**
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data/LiveRepository.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModel.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/LiveTvApp.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/navigation/Route.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/category/CategoryDetailScreen.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/CategoryRepositoryTest.kt`

**Step 1: Write failing repository tests for category list/detail lookups**

```kotlin
@Test
fun categoryRooms_delegatesToSiteCategoryApi() = runTest { /* assert categoryRooms() result */ }
```

**Step 2: Run tests and verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*CategoryRepositoryTest" --console=plain`
Expected: FAIL (missing repository methods and route wiring).

**Step 3: Add repository methods and route/screen wiring for category detail**

```kotlin
suspend fun categoryRooms(siteId: String, categoryId: String, parentId: String, page: Int = 1) =
    site(siteId).categoryRooms(categoryId, parentId, page)
```

**Step 4: Re-run tests and compile check**

Run:
- `./gradlew :app-tv:testDebugUnitTest --tests "*CategoryRepositoryTest" --console=plain`
- `./gradlew :app-tv:compileDebugKotlin --console=plain`

Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/CategoryRepositoryTest.kt
git commit -m "feat(app-tv): wire category and category detail data flows"
```

### Task 5: Add repository use-cases for follow/history management

**Files:**
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data/LiveRepository.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data/db/Daos.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/LiveRepositoryFollowHistoryTest.kt`

**Step 1: Write failing tests for read/delete flows**

```kotlin
@Test
fun removeFollow_deletesItem() = runTest { /* insert -> delete -> assert not exists */ }
```

**Step 2: Run test to verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*LiveRepositoryFollowHistoryTest" --console=plain`
Expected: FAIL (missing DAO/repository methods).

**Step 3: Implement minimal DAO/repository methods**

```kotlin
@Query("DELETE FROM history")
suspend fun clearHistory()

suspend fun clearHistory() = database.historyDao().clearHistory()
```

**Step 4: Re-run test**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*LiveRepositoryFollowHistoryTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/LiveRepositoryFollowHistoryTest.kt
git commit -m "feat(app-tv): complete follow and history repository operations"
```

### Task 6: Migrate settings schema from minimal to parity subset

**Files:**
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data/settings/SettingsStore.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/settings/SettingsStoreContractTest.kt`

**Step 1: Write failing contract tests for added keys**

```kotlin
@Test
fun defaultScaleMode_isContain() { assertEquals(0, store.scaleMode.first()) }
```

**Step 2: Run tests and verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*SettingsStoreContractTest" --console=plain`
Expected: FAIL.

**Step 3: Add keys + flows + setters**

```kotlin
val scaleMode = intPreferencesKey("scale_mode")
val playerCompatMode = booleanPreferencesKey("player_compat_mode")
```

**Step 4: Re-run tests**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*SettingsStoreContractTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data/settings app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/settings/SettingsStoreContractTest.kt
git commit -m "feat(app-tv): extend settings store for tv parity subset"
```

### Task 7: Implement Follow screen with focus-safe grid

**Files:**
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/follow/FollowScreen.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModel.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/feature/follow/FollowStateTest.kt`

**Step 1: Write failing state test for follows stream mapping**

```kotlin
@Test
fun follows_areOrderedByAddTimeDesc() = runTest { /* assert order */ }
```

**Step 2: Run test and verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*FollowStateTest" --console=plain`
Expected: FAIL.

**Step 3: Implement UI + state bindings**

```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(3)) { /* follow cards */ }
```

**Step 4: Re-run test**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*FollowStateTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/follow app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModel.kt app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/feature/follow/FollowStateTest.kt
git commit -m "feat(app-tv): add follow screen and state wiring"
```

### Task 8: Implement History screen with clear action

**Files:**
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/history/HistoryScreen.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModel.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/feature/history/HistoryStateTest.kt`

**Step 1: Write failing test for clear history command**

```kotlin
@Test
fun clearHistory_removesAllItems() = runTest { /* assert empty after clear */ }
```

**Step 2: Run test to verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*HistoryStateTest" --console=plain`
Expected: FAIL.

**Step 3: Implement ViewModel + UI clear trigger**

```kotlin
fun clearHistory() = viewModelScope.launch { repository.clearHistory() }
```

**Step 4: Re-run test**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*HistoryStateTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/history app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModel.kt app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/feature/history/HistoryStateTest.kt
git commit -m "feat(app-tv): add history screen with clear behavior"
```

### Task 9: Implement Settings screen (player/danmaku/follow subset)

**Files:**
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/settings/SettingsScreen.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/LiveTvApp.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/feature/settings/SettingsActionTest.kt`

**Step 1: Write failing tests for settings action dispatch**

```kotlin
@Test
fun clickDanmakuToggle_updatesStore() = runTest { /* verify setDanmakuEnabled called */ }
```

**Step 2: Run tests and verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*SettingsActionTest" --console=plain`
Expected: FAIL.

**Step 3: Implement tabbed settings UI + handlers**

```kotlin
enum class SettingsTab { Player, Danmaku, Follow, Account, About }
```

**Step 4: Re-run tests**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*SettingsActionTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/settings app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/LiveTvApp.kt app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/feature/settings/SettingsActionTest.kt
git commit -m "feat(app-tv): add settings screen parity subset"
```

### Task 10: Complete LiveRoom key-action parity behavior

**Files:**
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/screen/LiveRoomScreen.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/LiveTvApp.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModel.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/input/RemoteKeyMapperTest.kt`

**Step 1: Write failing tests for action execution hooks (not only key mapping)**

```kotlin
@Test
fun openFollowIntent_navigatesToFollowRoute() = runTest { /* assert nav target */ }

@Test
fun prevChannelIntent_callsSwitchChannelWithMinusOne() = runTest { /* assert vm hook */ }
```

**Step 2: Run tests and verify fail for missing behavior hooks**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*RemoteKeyMapperTest" --console=plain`
Expected: FAIL for behavior hooks.

**Step 3: Implement actions**

```kotlin
onPrevChannel = { vm.switchChannel(-1) }
onNextChannel = { vm.switchChannel(+1) }
onShowFollow = { navController.navigate(Route.Follow.value) }
onShowSettings = { navController.navigate(Route.Settings.value) }
```

**Step 4: Re-run tests**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*RemoteKeyMapperTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui app-tv/src/test/kotlin/com/yhsrzbg/live_tv/input/RemoteKeyMapperTest.kt
git commit -m "feat(app-tv): wire live room key actions to real navigation and channel switch"
```

### Task 11: Add search anchor route and data flow

**Files:**
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/navigation/Route.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/search/SearchAnchorScreen.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data/LiveRepository.kt`
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/SearchAnchorRepositoryTest.kt`

**Step 1: Write failing repository test for anchor search**

```kotlin
@Test
fun searchAnchors_returnsAnchorItems() = runTest { /* assert mapped list */ }
```

**Step 2: Run test and verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*SearchAnchorRepositoryTest" --console=plain`
Expected: FAIL.

**Step 3: Implement repository + screen + route**

```kotlin
suspend fun searchAnchors(siteId: String, keyword: String, page: Int = 1) =
    site(siteId).searchAnchors(keyword, page).items
```

**Step 4: Re-run tests**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*SearchAnchorRepositoryTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui app-tv/src/main/kotlin/com/yhsrzbg/live_tv/data/LiveRepository.kt app-tv/src/test/kotlin/com/yhsrzbg/live_tv/data/SearchAnchorRepositoryTest.kt
git commit -m "feat(app-tv): add search anchor flow"
```

### Task 12: Build reusable TV focus components

**Files:**
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/component/TvActionButton.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/component/TvCard.kt`
- Create: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/component/TvTopBar.kt`
- Modify: `app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature/**/*.kt`
- Modify: `app-tv/build.gradle.kts`

**Step 1: Add test dependencies required for Compose UI behavior tests**

Run: add dependencies in `app-tv/build.gradle.kts`
- `testImplementation("org.robolectric:robolectric:<version>")` (if unit-test route is chosen)
- or `androidTestImplementation("androidx.compose.ui:ui-test-junit4")` + `debugImplementation("androidx.compose.ui:ui-test-manifest")` (if instrumentation route is chosen)

Expected: Gradle sync/compile succeeds with selected strategy.

**Step 2: Write snapshot-ish behavior tests for focused state styles**

- Test: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/component/TvComponentsTest.kt`

```kotlin
@Test
fun tvActionButton_exposesFocusedStyleState() { /* verify semantics/state */ }
```

**Step 3: Run test to verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*TvComponentsTest" --console=plain`
Expected: FAIL.

**Step 4: Implement reusable components and replace ad-hoc buttons/cards**

```kotlin
@Composable
fun TvActionButton(text: String, focused: Boolean, onClick: () -> Unit)
```

**Step 5: Re-run tests**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*TvComponentsTest" --console=plain`
Expected: PASS.

**Step 6: Commit**

```bash
git add app-tv/build.gradle.kts app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/component app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/feature app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/component/TvComponentsTest.kt
git commit -m "refactor(app-tv): unify tv focus components"
```

### Task 13: Add integration-level ViewModel flow tests

**Files:**
- Create: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModelMigrationTest.kt`

**Step 1: Write failing tests for migrated critical behaviors**

```kotlin
@Test
fun loadRoom_writesHistoryAndSelectsPreferredQuality() = runTest { /* assert state + repo writes */ }
```

**Step 2: Run tests to verify fail**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*MainViewModelMigrationTest" --console=plain`
Expected: FAIL.

**Step 3: Add minimal state/use-case glue needed by tests**

```kotlin
fun switchChannel(direction: Int) { /* follow/history list based channel traversal */ }
```

**Step 4: Re-run tests**

Run: `./gradlew :app-tv:testDebugUnitTest --tests "*MainViewModelMigrationTest" --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/src/main/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModel.kt app-tv/src/test/kotlin/com/yhsrzbg/live_tv/ui/state/MainViewModelMigrationTest.kt
git commit -m "test(app-tv): add migration-critical viewmodel behavior tests"
```

### Task 14: Fix release minify and local release verification blockers

**Files:**
- Modify: `app-tv/build.gradle.kts`
- Modify: `app-tv/proguard-rules.pro`
- Modify: `docs/phase-status.md`
- Create or Modify: `app-tv/src/test/kotlin/com/yhsrzbg/live_tv/build/ReleaseConfigSmokeTest.kt`

**Step 1: Reproduce the known release failure**

Run: `./gradlew :app-tv:assembleRelease --console=plain`
Expected: FAIL at `:app-tv:minifyReleaseWithR8` with the current missing-class error, or PASS if already fixed by prior work.

**Step 2: Document the exact blocker before changing config**

```markdown
- [ ] R8 missing-class root cause identified
- [ ] keep rules or dependency fix chosen
- [ ] local assembleRelease passes after fix
```

**Step 3: Apply the minimal release fix**

```proguard
# Keep only the classes actually required by the embedded Rhino path, or exclude the unused path entirely.
```

**Step 4: Re-run release build**

Run: `./gradlew :app-tv:assembleRelease --console=plain`
Expected: PASS.

**Step 5: Commit**

```bash
git add app-tv/build.gradle.kts app-tv/proguard-rules.pro docs/phase-status.md app-tv/src/test/kotlin/com/yhsrzbg/live_tv/build/ReleaseConfigSmokeTest.kt
git commit -m "fix(app-tv): unblock release minification"
```

### Task 15: CI parity and release verification updates

**Files:**
- Modify: `.github/workflows/android-tv-arm64-release.yml`
- Modify: `README.md`
- Modify: `docs/phase-status.md`

**Step 1: Write failing policy checklist in docs**

```markdown
- [ ] workflow includes :core:test explicitly
- [ ] release artifact list is documented accurately
- [ ] local release command result is documented
```

**Step 2: Update workflow**

Run command target to include core test:
`./gradlew :core:test :app-tv:lint :app-tv:testDebugUnitTest :app-tv:assembleRelease`

**Step 3: Verify workflow-equivalent locally**

Run: `./gradlew :core:test :app-tv:lint :app-tv:testDebugUnitTest :app-tv:assembleRelease --console=plain`
Expected: PASS.

**Step 4: Commit**

```bash
git add .github/workflows/android-tv-arm64-release.yml README.md docs/phase-status.md
git commit -m "ci: align workflow checks with migration quality gates"
```

### Task 16: Final migration acceptance pass

**Files:**
- Modify: `docs/phase-status.md`
- Create: `docs/plans/2026-03-06-simple-live-tv-app-migration-acceptance.md`

**Step 1: Execute full verification suite**

Run:
- `./gradlew :core:test --console=plain`
- `./gradlew :app-tv:testDebugUnitTest --console=plain`
- `./gradlew :app-tv:lint --console=plain`
- `./gradlew :app-tv:assembleDebug --console=plain`
- `./gradlew :app-tv:assembleRelease --console=plain`

Expected: all PASS.

**Step 2: Manual TV behavior checklist**

```markdown
- [ ] LiveRoom OK toggles controls
- [ ] LEFT opens follow screen
- [ ] RIGHT/MENU opens settings screen
- [ ] UP/DOWN switches channel candidate
- [ ] BACK exits LiveRoom
```

**Step 3: Update migration acceptance doc with evidence**

```markdown
## Evidence
- Command outputs (date/time)
- Screens tested
- Remaining known gaps
```

**Step 4: Commit**

```bash
git add docs/phase-status.md docs/plans/2026-03-06-simple-live-tv-app-migration-acceptance.md
git commit -m "docs: record migration acceptance results"
```

---

## Risk Register

- Risk: focus navigation regressions across screens
- Mitigation: reusable focus components + per-screen manual TV checklist

- Risk: release minification failure blocks CI completion
- Mitigation: treat R8 issue as explicit blocker and track missing rules in dedicated fix task before final status flip

- Risk: scope creep into full account/sync parity
- Mitigation: hard non-goal list and phase boundary in docs

## Definition of Done

- All in-scope routes migrated and reachable from Compose nav graph
- Remote key mapping actions are wired to actual behavior (not placeholders)
- Follow/history/settings parity subset is functional with persistence
- Verification commands pass or blockers are explicitly documented with owner and next action
- `docs/phase-status.md` and migration acceptance docs reflect reality

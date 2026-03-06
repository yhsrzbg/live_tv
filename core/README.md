# core

`core` 是 `live_tv` 的平台能力层，负责统一接入多直播站点的数据与弹幕能力，供 `app-tv` 直接调用。

目标是把“站点差异”收敛在 `core` 内部，让上层页面只面向统一模型与接口开发。

## 当前状态

- 模块类型：Android Library（Kotlin）
- 命名空间：`com.yhsrzbg.live_tv.core`
- 最低系统：`minSdk 30`
- 默认内置站点：`bilibili`、`douyu`、`huya`、`douyin`

## 对外入口

- 站点注册表：`SiteRegistry.default()`
- 站点能力接口：`api/LiveSite.kt`
- 弹幕能力接口：`api/LiveDanmaku.kt`
- 统一数据模型：`model/Models.kt`

## LiveSite 能力（统一接口）

每个站点实现都遵循 `LiveSite`：

- 分类：`categories()`
- 推荐：`recommendRooms(page)`
- 分类房间：`categoryRooms(categoryId, parentId, page)`
- 搜索房间：`searchRooms(keyword, page)`
- 搜索主播：`searchAnchors(keyword, page)`
- 房间详情：`roomDetail(roomId)`
- 清晰度：`playQualities(detail)`
- 播放地址：`playUrls(detail, quality)`
- 开播状态：`liveStatus(roomId)`
- SC 消息：`superChatMessages(roomId)`（无能力站点返回空列表）

## LiveDanmaku 能力（统一接口）

`LiveDanmaku` 通过 `Flow<LiveMessage>` 持续输出弹幕事件：

- `start(args: Map<String, String>)`
- `stop()`
- `heartbeatTimeSeconds`
- `messages`

`LiveMessage.type` 当前支持：

- `Chat`
- `Gift`
- `Online`
- `SuperChat`

## 快速接入（给 app-tv）

### 1) 获取站点并加载列表

```kotlin
val registry = SiteRegistry.default()
val site = requireNotNull(registry.site("bilibili"))

val categories = site.categories()
val recommend = site.recommendRooms(page = 1)
```

### 2) 获取房间详情与播放地址

```kotlin
val detail = site.roomDetail(roomId = "12345")
val qualities = site.playQualities(detail)
val selected = qualities.firstOrNull()

if (selected != null) {
    val play = site.playUrls(detail, selected)
    // play.urls: 可用播放地址列表
    // play.headers: 播放所需请求头（如 referer / user-agent）
}
```

### 3) 接入弹幕流

```kotlin
val danmaku = site.danmaku()

// detail.danmakuData 来自 roomDetail，按站点约定透传
// 例如 bilibili 需要 roomId/token/serverHost/buvid 等

danmaku.start(detail.danmakuData)

// 在协程中收集
// danmaku.messages.collect { msg -> ... }

// 页面销毁时
// danmaku.stop()
```

> 注意：上面的 `detail.danmakuData` 是 `Map<String, String>`，由各站点 `roomDetail` 负责产出，`app-tv` 不应自己拼装站点参数。

### 4) 拉取 SC（如果站点支持）

```kotlin
val scList = site.superChatMessages(roomId = detail.roomId)
```

## 迁移映射（old -> core）

从 `old/simple_live_core` 迁移到本模块时，主要映射如下：

- `LiveSite.getCategores` -> `LiveSite.categories`
- `LiveSite.getRecommendRooms` -> `LiveSite.recommendRooms`
- `LiveSite.getCategoryRooms` -> `LiveSite.categoryRooms`
- `LiveSite.getRoomDetail` -> `LiveSite.roomDetail`
- `LiveSite.getPlayQualites` -> `LiveSite.playQualities`
- `LiveSite.getPlayUrls` -> `LiveSite.playUrls`
- `LiveSite.getLiveStatus` -> `LiveSite.liveStatus`
- `LiveSite.getSuperChatMessage` -> `LiveSite.superChatMessages`
- `LiveDanmaku.onMessage/onReady/onClose` 事件风格 -> `Flow<LiveMessage>` 流风格

## app-tv 开发建议

- 上层只依赖 `LiveSite` + `Models`，不要直接引用具体站点类。
- 播放器请求头统一使用 `LivePlayUrl.headers`。
- 弹幕参数统一使用 `LiveRoomDetail.danmakuData`。
- 以 `site.id` 作为持久化与路由键，避免用展示名。
- 对 `superChatMessages` 做“可选能力”处理（空列表即不支持）。

## 新增站点规范

新增站点时请至少实现：

- 一个 `XxxSite : LiveSite`
- 一个 `XxxDanmaku : LiveDanmaku`（若站点无弹幕可返回空流或 no-op）
- 在 `site/DefaultSites.kt` 注册
- 补充对应单测

## 测试

在项目根目录执行：

```bash
./gradlew :core:testDebugUnitTest
```

当前单测覆盖包括：站点注册、解析器、签名器与接口/模型兼容性。

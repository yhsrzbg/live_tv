package com.yhsrzbg.live_tv.ui.navigation

sealed class Route(val value: String) {
    data object Home : Route("home")
    data object Hot : Route("hot/{siteId}")
    data object Category : Route("category/{siteId}")
    data object CategoryDetail : Route("category/{siteId}/{categoryId}/{parentId}")
    data object Search : Route("search/{siteId}")
    data object LiveRoom : Route("live-room/{siteId}/{roomId}")
    data object Follow : Route("follow")
    data object History : Route("history")
    data object Settings : Route("settings")

    companion object {
        fun hot(siteId: String) = "hot/$siteId"
        fun category(siteId: String) = "category/$siteId"
        fun categoryDetail(siteId: String, categoryId: String, parentId: String) =
            "category/$siteId/$categoryId/$parentId"
        fun search(siteId: String) = "search/$siteId"
        fun room(siteId: String, roomId: String) = "live-room/$siteId/$roomId"
    }
}

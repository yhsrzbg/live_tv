package com.yhsrzbg.live_tv.ui.navigation

sealed class Route(val value: String) {
    data object Home : Route("home")
    data object Hot : Route("hot/{siteId}")
    data object Category : Route("category/{siteId}")
    data object Search : Route("search/{siteId}")
    data object LiveRoom : Route("live-room/{siteId}/{roomId}")

    companion object {
        fun hot(siteId: String) = "hot/$siteId"
        fun category(siteId: String) = "category/$siteId"
        fun search(siteId: String) = "search/$siteId"
        fun room(siteId: String, roomId: String) = "live-room/$siteId/$roomId"
    }
}

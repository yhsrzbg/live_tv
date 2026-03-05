package com.yhsrzbg.live_tv.core

import com.yhsrzbg.live_tv.core.api.LiveSite
import com.yhsrzbg.live_tv.core.site.BilibiliSite
import com.yhsrzbg.live_tv.core.site.DouyinSite
import com.yhsrzbg.live_tv.core.site.DouyuSite
import com.yhsrzbg.live_tv.core.site.HuyaSite

class SiteRegistry private constructor(
    private val sites: Map<String, LiveSite>,
) {
    fun site(id: String): LiveSite? = sites[id]

    fun supportedSites(): List<LiveSite> = sites.values.sortedBy { it.id }

    companion object {
        fun default(): SiteRegistry {
            val all = listOf(
                BilibiliSite(),
                DouyuSite(),
                HuyaSite(),
                DouyinSite(),
            )
            return SiteRegistry(all.associateBy { it.id })
        }
    }
}

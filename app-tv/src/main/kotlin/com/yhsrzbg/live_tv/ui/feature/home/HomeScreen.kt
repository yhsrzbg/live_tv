package com.yhsrzbg.live_tv.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yhsrzbg.live_tv.ui.component.TvActionButton

@Composable
fun HomeScreen(
    sites: List<String>,
    onOpenHot: (String) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101820))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Live TV", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        sites.forEach { siteId ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvActionButton(text = "$siteId Hot") { onOpenHot(siteId) }
                TvActionButton(text = "$siteId Category") { onOpenCategory(siteId) }
                TvActionButton(text = "$siteId Search") { onOpenSearch(siteId) }
            }
        }
    }
}

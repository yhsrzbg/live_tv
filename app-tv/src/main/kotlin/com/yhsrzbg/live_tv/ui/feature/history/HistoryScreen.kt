package com.yhsrzbg.live_tv.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yhsrzbg.live_tv.ui.component.TvActionButton
import com.yhsrzbg.live_tv.ui.component.TvCard
import com.yhsrzbg.live_tv.ui.component.TvTopBar
import com.yhsrzbg.live_tv.ui.state.HistoryItemUiState

@Composable
fun HistoryScreen(
    items: List<HistoryItemUiState>,
    onClear: () -> Unit,
    onOpenRoom: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TvTopBar(title = "History") {
            TvActionButton(text = "Back", onClick = onBack)
            TvActionButton(text = "Clear", onClick = onClear)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { item ->
                HistoryCard(item = item, onOpenRoom = onOpenRoom)
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItemUiState,
    onOpenRoom: (String, String) -> Unit,
) {
    TvCard(
        onClick = { onOpenRoom(item.siteId, item.roomId) },
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(item.userName, style = MaterialTheme.typography.titleMedium)
            Text("${item.siteId} · room=${item.roomId}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

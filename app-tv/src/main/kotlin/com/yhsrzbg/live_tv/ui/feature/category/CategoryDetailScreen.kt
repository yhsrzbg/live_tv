package com.yhsrzbg.live_tv.ui.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yhsrzbg.live_tv.core.model.LiveRoomItem
import com.yhsrzbg.live_tv.ui.component.TvActionButton
import com.yhsrzbg.live_tv.ui.component.TvCard
import com.yhsrzbg.live_tv.ui.component.TvTopBar

@Composable
fun CategoryDetailScreen(
    siteId: String,
    categoryId: String,
    parentId: String,
    load: suspend () -> List<LiveRoomItem>,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit,
) {
    val rooms = remember { mutableStateListOf<LiveRoomItem>() }

    LaunchedEffect(siteId, categoryId, parentId) {
        rooms.clear()
        rooms.addAll(load())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TvTopBar(title = "$siteId Category Detail") {
            TvActionButton(text = "Back", onClick = onBack)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rooms, key = { it.roomId }) { item ->
                RoomCard(item = item, onOpenRoom = onOpenRoom)
            }
        }
    }
}

@Composable
private fun RoomCard(item: LiveRoomItem, onOpenRoom: (String) -> Unit) {
    TvCard(
        onClick = { onOpenRoom(item.roomId) },
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.size(88.dp, 50.dp).background(Color(0xFF263238)))
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("${item.userName} · Online ${item.online}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

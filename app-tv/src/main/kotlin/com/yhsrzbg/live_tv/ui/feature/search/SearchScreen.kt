package com.yhsrzbg.live_tv.ui.feature.search

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yhsrzbg.live_tv.core.model.LiveRoomItem
import com.yhsrzbg.live_tv.ui.component.TvActionButton
import com.yhsrzbg.live_tv.ui.component.TvCard
import com.yhsrzbg.live_tv.ui.component.TvTopBar
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    siteId: String,
    search: suspend (String) -> List<LiveRoomItem>,
    onOpenAnchorSearch: () -> Unit,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit,
) {
    var keyword by remember { mutableStateOf("") }
    val result = remember { mutableStateListOf<LiveRoomItem>() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1F2A))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TvTopBar(title = "$siteId Search") {
            TvActionButton(text = "Back", onClick = onBack)
            TvActionButton(text = "Anchor Search", onClick = onOpenAnchorSearch)
        }

        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Keyword") },
            modifier = Modifier.fillMaxWidth(),
        )

        TvActionButton(text = "Search") {
            scope.launch {
                result.clear()
                result.addAll(search(keyword))
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(result, key = { it.roomId }) { item ->
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

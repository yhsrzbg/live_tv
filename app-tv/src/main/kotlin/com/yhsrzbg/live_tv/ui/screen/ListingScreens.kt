package com.yhsrzbg.live_tv.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.launch

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
                ActionButton(text = "$siteId Hot") { onOpenHot(siteId) }
                ActionButton(text = "$siteId Category") { onOpenCategory(siteId) }
                ActionButton(text = "$siteId Search") { onOpenSearch(siteId) }
            }
        }
    }
}

@Composable
fun HotScreen(
    siteId: String,
    load: suspend () -> List<LiveRoomItem>,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit,
) {
    RoomListScreen(title = "$siteId Hot Live", load = load, onOpenRoom = onOpenRoom, onBack = onBack)
}

@Composable
fun CategoryScreen(
    siteId: String,
    load: suspend () -> List<LiveRoomItem>,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit,
) {
    RoomListScreen(title = "$siteId Category", load = load, onOpenRoom = onOpenRoom, onBack = onBack)
}

@Composable
fun SearchScreen(
    siteId: String,
    search: suspend (String) -> List<LiveRoomItem>,
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(text = "Back", onClick = onBack)
            Text("$siteId Search", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }

        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Keyword") },
            modifier = Modifier.fillMaxWidth(),
        )

        ActionButton(text = "Search") {
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
private fun RoomListScreen(
    title: String,
    load: suspend () -> List<LiveRoomItem>,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit,
) {
    val rooms = remember { mutableStateListOf<LiveRoomItem>() }

    LaunchedEffect(Unit) {
        rooms.clear()
        rooms.addAll(load())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ActionButton(text = "Back", onClick = onBack)
            Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenRoom(item.roomId) }
            .focusable(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(88.dp, 50.dp).background(Color(0xFF263238)))
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text("${item.userName} ¡¤ Online ${item.online}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(text) }
}

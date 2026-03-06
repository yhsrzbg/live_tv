package com.yhsrzbg.live_tv.ui.feature.search

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
import com.yhsrzbg.live_tv.core.model.LiveAnchorItem
import kotlinx.coroutines.launch

@Composable
fun SearchAnchorScreen(
    siteId: String,
    searchAnchors: suspend (String) -> List<LiveAnchorItem>,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit,
) {
    var keyword by remember { mutableStateOf("") }
    val result = remember { mutableStateListOf<LiveAnchorItem>() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1F2A))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Text("$siteId Anchor Search", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }

        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("Anchor Keyword") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = {
            scope.launch {
                result.clear()
                result.addAll(searchAnchors(keyword))
            }
        }) {
            Text("Search Anchor")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(result, key = { "${it.userName}-${it.roomId}" }) { item ->
                AnchorCard(item = item, onOpenRoom = onOpenRoom)
            }
        }
    }
}

@Composable
private fun AnchorCard(item: LiveAnchorItem, onOpenRoom: (String) -> Unit) {
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.size(88.dp, 50.dp).background(Color(0xFF263238)))
            Column {
                Text(item.userName, style = MaterialTheme.typography.titleMedium)
                Text(
                    "room=${item.roomId} · ${if (item.liveStatus) "LIVE" else "OFFLINE"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

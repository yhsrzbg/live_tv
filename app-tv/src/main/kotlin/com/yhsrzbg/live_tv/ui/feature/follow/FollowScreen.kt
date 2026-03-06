package com.yhsrzbg.live_tv.ui.feature.follow

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yhsrzbg.live_tv.ui.state.FollowItemUiState

@Composable
fun FollowScreen(
    items: List<FollowItemUiState>,
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
        TvTopBar(title = "Follow") {
            TvActionButton(text = "Back", onClick = onBack)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item ->
                FollowCard(item = item, onOpenRoom = onOpenRoom)
            }
        }
    }
}

@Composable
private fun FollowCard(
    item: FollowItemUiState,
    onOpenRoom: (String, String) -> Unit,
) {
    TvCard(
        onClick = { onOpenRoom(item.siteId, item.roomId) },
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF24313C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(item.siteId.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.userName, style = MaterialTheme.typography.titleMedium)
                Text("room=${item.roomId}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

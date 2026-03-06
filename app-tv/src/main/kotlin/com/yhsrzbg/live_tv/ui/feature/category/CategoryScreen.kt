package com.yhsrzbg.live_tv.ui.feature.category

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yhsrzbg.live_tv.core.model.LiveSubCategory
import com.yhsrzbg.live_tv.ui.component.TvActionButton
import com.yhsrzbg.live_tv.ui.component.TvCard
import com.yhsrzbg.live_tv.ui.component.TvTopBar

@Composable
fun CategoryScreen(
    siteId: String,
    load: suspend () -> List<LiveSubCategory>,
    onOpenCategoryDetail: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    CategoryListScreen(
        title = "$siteId Category",
        load = load,
        onOpenCategoryDetail = onOpenCategoryDetail,
        onBack = onBack,
    )
}

@Composable
private fun CategoryListScreen(
    title: String,
    load: suspend () -> List<LiveSubCategory>,
    onOpenCategoryDetail: (String, String) -> Unit,
    onBack: () -> Unit,
) {
    var categories by remember { mutableStateOf<List<LiveSubCategory>>(emptyList()) }

    LaunchedEffect(Unit) {
        categories = load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TvTopBar(title = title) {
            TvActionButton(text = "Back", onClick = onBack)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(categories, key = { "${it.parentId}-${it.id}" }) { item ->
                CategoryCard(item = item, onOpenCategoryDetail = onOpenCategoryDetail)
            }
        }
    }
}

@Composable
private fun CategoryCard(item: LiveSubCategory, onOpenCategoryDetail: (String, String) -> Unit) {
    TvCard(
        onClick = { onOpenCategoryDetail(item.id, item.parentId) },
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("category=${item.id} parent=${item.parentId}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

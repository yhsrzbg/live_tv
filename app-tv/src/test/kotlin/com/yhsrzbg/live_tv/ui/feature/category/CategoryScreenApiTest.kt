package com.yhsrzbg.live_tv.ui.feature.category

import androidx.compose.runtime.Composable
import com.yhsrzbg.live_tv.core.model.LiveSubCategory
import org.junit.Test

class CategoryScreenApiTest {
    @Test
    fun categoryScreen_exposesCategoryDetailCallback() {
        val apiCheck: @Composable (
            String,
            suspend () -> List<LiveSubCategory>,
            (String, String) -> Unit,
            () -> Unit,
        ) -> Unit = { siteId, load, onOpenCategoryDetail, onBack ->
            CategoryScreen(
                siteId = siteId,
                load = load,
                onOpenCategoryDetail = onOpenCategoryDetail,
                onBack = onBack,
            )
        }

        check(apiCheck != null)
    }
}

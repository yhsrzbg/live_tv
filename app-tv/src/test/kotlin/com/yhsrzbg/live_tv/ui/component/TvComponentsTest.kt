package com.yhsrzbg.live_tv.ui.component

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TvComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tvActionButton_exposesFocusedStyleState() {
        composeRule.setContent {
            TvActionButton(
                text = "Play",
                focused = true,
                onClick = {},
            )
        }

        composeRule.onNodeWithText("Play")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "focused"))
    }
}

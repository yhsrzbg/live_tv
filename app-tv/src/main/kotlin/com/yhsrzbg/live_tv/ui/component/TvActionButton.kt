package com.yhsrzbg.live_tv.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

@Composable
fun TvActionButton(
    text: String,
    focused: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            stateDescription = if (focused) "focused" else "unfocused"
        },
    ) {
        Text(text)
    }
}

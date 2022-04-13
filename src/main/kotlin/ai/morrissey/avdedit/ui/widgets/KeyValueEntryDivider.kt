package ai.morrissey.avdedit.ui.widgets

import ai.morrissey.avdedit.ui.dividerColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KeyValueEntryDivider() {
    Box(
        Modifier
            .fillMaxHeight()
            .padding(start = 4.dp, end = 4.dp)
            .width(2.dp)
            .background(color = dividerColor)
    )
}

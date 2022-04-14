package ai.morrissey.avdedit.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun <T> DropDownList(
    items: List<T>,
    selected: MutableState<T>,
    itemLabelBuilder: (T) -> String = { it.toString() },
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    contentTextStyle: TextStyle = LocalTextStyle.current,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors()
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.wrapContentSize(Alignment.TopStart)) {
        Button(
            modifier = Modifier.wrapContentWidth(),
            onClick = { expanded = true },
            colors = buttonColors,
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 4.dp,
                end = 4.dp,
                bottom = 4.dp
            )
        ) {
            Text(modifier = Modifier.wrapContentWidth(), text = itemLabelBuilder(selected.value), style = textStyle)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "dropdown")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(IntrinsicSize.Min)
        ) {
            items.forEach { item: T ->
                DropdownMenuItem(
                    modifier = Modifier.height(36.dp),
                    onClick = {
                        selected.value = item
                        expanded = false
                    }
                ) {
                    Text(
                        text = itemLabelBuilder(item),
                        style = contentTextStyle
                    )
                }
            }
        }
    }
}

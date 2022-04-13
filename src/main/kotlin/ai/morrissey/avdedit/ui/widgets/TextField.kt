package ai.morrissey.avdedit.ui.widgets

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ReadOnlyTextField(text: String, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    val textStyle: TextStyle = textAlign?.let { LocalTextStyle.current.copy(textAlign = it) } ?: LocalTextStyle.current
    BasicTextField(
        modifier = modifier,
        value = text,
        onValueChange = { },
        readOnly = true,
        singleLine = true,
        textStyle = textStyle
    )
}

@Composable
fun TextField(text: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, textAlign: TextAlign? = null) {
    val textStyle: TextStyle = textAlign?.let { LocalTextStyle.current.copy(textAlign = it) } ?: LocalTextStyle.current
    BasicTextField(
        modifier = modifier,
        value = text,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle
    )
}

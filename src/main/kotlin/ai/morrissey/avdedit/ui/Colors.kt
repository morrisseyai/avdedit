package ai.morrissey.avdedit.ui

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val backgroundColor = Color(0xff353535)
val floatedItemBackgroundColor = Color(0xff454545)
val itemTextColor = Color(0xffdddddd)
val dividerColor = Color(0x19dddddd)

val checkboxColors: @Composable () -> CheckboxColors = {
    CheckboxDefaults.colors(
        checkmarkColor = Color.White
    )
}

// just use a greenish colour everywhere
val colors = darkColors(
    primary = Color(0xff415940),
    primaryVariant = Color(0xff415940),
    secondary = Color(0xff377737),
    secondaryVariant = Color(0xff377737),
    onPrimary = Color.White,
    onSecondary = Color.White
)

val textSelectionColors = TextSelectionColors(
    handleColor = colors.secondary,
    backgroundColor = colors.secondaryVariant
)

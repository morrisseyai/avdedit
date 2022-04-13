package ai.morrissey.avdedit.ui

import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val backgroundColor = Color(0xff353535)
val floatedItemBackgroundColor = Color(0xff454545)
val itemTextColor = Color(0xffdddddd)
val dividerColor = Color(0x19dddddd)
val buttonColor = Color(0xff1d6b9f)


val buttonColors: @Composable () -> ButtonColors = {
    ButtonDefaults.buttonColors(
        backgroundColor = buttonColor,
        contentColor = Color.White
    )
}

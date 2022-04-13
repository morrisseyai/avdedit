package ai.morrissey.avdedit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import java.io.FileFilter

@Composable
@Preview
fun App() {
    var avdDirectory = homeDirectory.resolve(".android").resolve("avd")

    MaterialTheme(
        colors = darkColors()
    ) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle.Default.copy(color = itemTextColor)) {
            Column(modifier = Modifier.fillMaxSize().background(color = backgroundColor).padding(16.dp)) {
                val currentConfigMap = mutableStateOf(LinkedHashMap<String, String>())
                if (avdDirectory.exists()) {
                    val dumpCurrentConfigToConsole: () -> Unit = {
                        currentConfigMap.value.entries.forEach { println("${it.key}=${it.value}") }
                    }
                    Button(
                        colors = buttonColors(),
                        onClick = dumpCurrentConfigToConsole
                    ) {
                        Text("dump current config to console")
                    }
                    Text("Found AVD directory: $avdDirectory")

                    val avdList = avdDirectory.listFiles(FileFilter { it.isDirectory })
                    val avd = avdList?.first()
                    if (avd != null) {
                        Text("Found AVD: $avd")
                        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace
                        )) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val state = rememberLazyListState()
                                val avdConfig = avd.resolve("config.ini")
                                if (avdConfig.isFile) {
                                    currentConfigMap.value = avdConfig.toAvdConfigMap()

                                    LazyColumn(modifier = Modifier.padding(end = 8.dp), state = state) {
                                        currentConfigMap.value.entries.forEach { entry ->
                                            item {
                                                when (HandledType.forKey(entry.key)) {
                                                    HandledType.RealBoolean -> RealBooleanEntryView(entry)
                                                    HandledType.YesNoBoolean -> YesNoBooleanEntryView(entry)
                                                    null -> TextEntryView(entry)
                                                }
                                            }
                                        }
                                    }
                                    VerticalScrollbar(
                                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                        adapter = rememberScrollbarAdapter(
                                            scrollState = state
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealBooleanEntryView(entry: MutableMap.MutableEntry<String, String>) {
    EntryRow {
        ReadOnlyTextField(
            modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp),
            text = entry.key
        )
        KeyValueEntryDivider()
        val checked = mutableStateOf(entry.value.toBoolean())
        Box(modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp)) {
            Checkbox(
                modifier = Modifier.align(Alignment.CenterEnd).size(24.dp),
                checked = checked.value,
                onCheckedChange = {
                    entry.setValue(it.toString())
                    checked.value = it
                }
            )
        }
    }
}

@Composable
fun YesNoBooleanEntryView(entry: MutableMap.MutableEntry<String, String>) {
    EntryRow {
        ReadOnlyTextField(
            modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp),
            text = entry.key
        )
        KeyValueEntryDivider()
        val checked = mutableStateOf(when (entry.value) {
            "yes" -> true
            else -> false
        })
        Box(modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp)) {
            Checkbox(
                modifier = Modifier.align(Alignment.CenterEnd).size(24.dp),
                checked = checked.value,
                onCheckedChange = {
                    val value = if (it) "yes" else "no"
                    entry.setValue(value)
                    checked.value = it
                }
            )
        }
    }
}

@Composable
fun TextEntryView(entry: MutableMap.MutableEntry<String, String>) {
    EntryRow {
        ReadOnlyTextField(
            modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp),
            text = entry.key
        )
        KeyValueEntryDivider()
        ReadOnlyTextField(
            modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp),
            text = entry.value,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun EntryRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(32.dp)
            .background(color = floatedItemBackgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

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

@Composable
fun ReadOnlyTextField(modifier: Modifier = Modifier, text: String, textAlign: TextAlign? = null) {
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

enum class HandledType {
    RealBoolean,
    YesNoBoolean;

    companion object {
        fun forKey(key: String) = when (key) {
            "PlayStore.enabled" -> RealBoolean
            "hw.arc" -> RealBoolean
            "fastboot.forceChosenSnapshotBoot" -> YesNoBoolean
            "fastboot.forceColdBoot" -> YesNoBoolean
            "fastboot.forceFastBoot" -> YesNoBoolean
            "hw.accelerometer" -> YesNoBoolean
            "hw.audioInput" -> YesNoBoolean
            "hw.audioOutput" -> YesNoBoolean
            "hw.battery" -> YesNoBoolean
            "hw.dPad" -> YesNoBoolean
            "hw.gps" -> YesNoBoolean
            "hw.gpu.enabled" -> YesNoBoolean
            "hw.keyboard" -> YesNoBoolean
            "hw.mainKeys" -> YesNoBoolean
            "hw.sdCard" -> YesNoBoolean
            "hw.sensors.orientation" -> YesNoBoolean
            "hw.sensors.proximity" -> YesNoBoolean
            "hw.trackBall" -> YesNoBoolean
            "showDeviceFrame" -> YesNoBoolean
            "skin.dynamic" -> YesNoBoolean
            else -> null
        }
    }
}

private fun File.toAvdConfigMap() = this.useLines { lineSequence ->
    val configValuesMap = LinkedHashMap<String, String>()
    lineSequence.associateTo(configValuesMap) {
        val (key, value) = it.split("=")
        key to value
    }
    configValuesMap
}

fun main() = application {
    val windowState = rememberWindowState(
        width = 900.dp,
        height = 750.dp,
        position = WindowPosition(Alignment.Center)
    )
    Window(
        state = windowState,
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}

private val homeDirectory by lazy { java.io.File(System.getProperty("user.home")) }

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

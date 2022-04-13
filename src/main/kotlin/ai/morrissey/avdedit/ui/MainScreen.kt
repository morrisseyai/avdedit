package ai.morrissey.avdedit.ui

import ai.morrissey.avdedit.HandledType
import ai.morrissey.avdedit.toAvdConfigMap
import ai.morrissey.avdedit.ui.widgets.KeyValueEntryDivider
import ai.morrissey.avdedit.ui.widgets.ReadOnlyTextField
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.FileFilter

private val homeDirectory by lazy { java.io.File(System.getProperty("user.home")) }

@Composable
@Preview
fun App() {
    MaterialTheme(
        colors = darkColors()
    ) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle.Default.copy(color = itemTextColor)) {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    var avdDirectory = homeDirectory.resolve(".android").resolve("avd")

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
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(
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
        val textValue = mutableStateOf(entry.value)
        ai.morrissey.avdedit.ui.widgets.TextField(
            modifier = Modifier.weight(1f).padding(start = 4.dp, end = 4.dp),
            text = textValue.value,
            onValueChange = { value ->
                entry.setValue(value)
                textValue.value = value
            },
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

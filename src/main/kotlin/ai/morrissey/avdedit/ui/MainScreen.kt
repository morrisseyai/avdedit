package ai.morrissey.avdedit.ui

import ai.morrissey.avdedit.HandledType
import ai.morrissey.avdedit.saveToFile
import ai.morrissey.avdedit.toAvdConfigMap
import ai.morrissey.avdedit.ui.widgets.DropDownList
import ai.morrissey.avdedit.ui.widgets.KeyValueEntryDivider
import ai.morrissey.avdedit.ui.widgets.ReadOnlyTextField
import ai.morrissey.avdedit.ui.widgets.TextField
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileFilter

private val homeDirectory by lazy { File(System.getProperty("user.home")) }

@Composable
@Preview
fun App() {
    MaterialTheme(
        colors = colors
    ) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle.Default.copy(color = itemTextColor)) {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val avdDirectory = homeDirectory.resolve(".android").resolve("avd")

    val avdList = avdDirectory.listFiles(FileFilter { it.isDirectory })?.toList() ?: emptyList()

    val selectedAvd = remember { mutableStateOf(avdList.firstOrNull()) }
    val currentConfigMap = remember { mutableStateOf(LinkedHashMap<String, String>()) }

    Column(modifier = Modifier.fillMaxSize().background(color = backgroundColor).padding(16.dp)) {
        if (avdDirectory.exists()) {
            val dumpCurrentConfigToConsole: () -> Unit = {
                currentConfigMap.value.entries.forEach { println("${it.key}=${it.value}") }
            }
            Button(
                onClick = dumpCurrentConfigToConsole
            ) {
                Text("dump current config to console")
            }
            Text("Found AVD directory: $avdDirectory")

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(end = 16.dp),
                    text = "Select an AVD from the list:",
                    fontSize = 16.sp
                )
                DropDownList(
                    items = avdList,
                    selected = selectedAvd,
                    itemLabelBuilder = { it?.nameWithoutExtension ?: "" },
                    textStyle = MaterialTheme.typography.button,
                    contentTextStyle = MaterialTheme.typography.button.copy(fontWeight = FontWeight.Normal)
                )
            }

            selectedAvd.value?.let { avd ->
                val avdConfig = avd.resolve("config.ini")
                if (avdConfig.isFile) {
                    currentConfigMap.value = avdConfig.toAvdConfigMap()

                    ConfigEntries(currentConfigMap = currentConfigMap.value)

                    Row {
                        Button(
                            onClick = { currentConfigMap.value.saveToFile(avdConfig) }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ConfigEntries(currentConfigMap: LinkedHashMap<String, String>) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontFamily = FontFamily.Monospace
        )) {
        Box(
            modifier = Modifier.weight(1f).fillMaxSize()
        ) {
            val state = rememberLazyListState()

                LazyColumn(modifier = Modifier.padding(end = 10.dp), state = state) {
                    currentConfigMap.entries.forEach { entry ->
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
                },
                colors = checkboxColors()
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
                },
                colors = checkboxColors()
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
        TextField(
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

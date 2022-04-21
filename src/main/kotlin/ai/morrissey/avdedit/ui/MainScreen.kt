package ai.morrissey.avdedit.ui

import ai.morrissey.avdedit.HandledType
import ai.morrissey.avdedit.saveToFile
import ai.morrissey.avdedit.toAvdConfigMap
import ai.morrissey.avdedit.ui.widgets.DropDownList
import ai.morrissey.avdedit.ui.widgets.KeyValueEntryDivider
import ai.morrissey.avdedit.ui.widgets.ReadOnlyTextField
import ai.morrissey.avdedit.ui.widgets.TextField
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileFilter

private val homeDirectory by lazy { File(System.getProperty("user.home")) }
private val mainTextStyle = TextStyle.Default.copy(color = itemTextColor)
private val editTextStyle = TextStyle.Default.copy(color = itemTextColor, fontFamily = FontFamily.Monospace)

@Composable
@Preview
fun App() {
    MaterialTheme(
        colors = colors
    ) {
        CompositionLocalProvider(LocalTextStyle provides mainTextStyle) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp)
    ) {
        if (avdDirectory.exists()) {
            Text(
                text = "Found AVD directory: $avdDirectory",
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
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
                    LaunchedEffect(selectedAvd.value) {
                        currentConfigMap.value = avdConfig.toAvdConfigMap()
                    }

                    val listScrollCoroutineScope = rememberCoroutineScope()
                    val listState = rememberLazyListState()
                    val scrollToEndOfList: () -> Unit = {
                        listScrollCoroutineScope.launch {
                            // hacky, but looks nice
                            val scrollBy = listState.layoutInfo.totalItemsCount *
                                    (listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0)
                            listState.animateScrollBy(scrollBy.toFloat(), tween(2000))
                        }
                    }
                    ConfigEntries(currentConfigMap = currentConfigMap.value, listState)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            val placeholder = "setting.name=value"
                            val adding = remember { mutableStateOf(false) }
                            val addingInput = remember { mutableStateOf(placeholder) }
                            if (adding.value) {
                                val animatedProgress = remember { Animatable(initialValue = 0f) }
                                LaunchedEffect(Unit) {
                                    animatedProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(800)
                                    )
                                }

                                CompositionLocalProvider(LocalTextStyle provides editTextStyle) {
                                    TextField(
                                        modifier = Modifier
                                            .width(364.dp * animatedProgress.value)
                                            .padding(end = 4.dp)
                                            .height(32.dp)
                                            .background(
                                                color = floatedItemBackgroundColor,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(8.dp),
                                        text = addingInput.value,
                                        onValueChange = { addingInput.value = it },
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    if (adding.value) {
                                        // save the thing
                                        val inputItems = addingInput.value.split("=")
                                        if (inputItems.size > 1) {
                                            val (key, value) = inputItems
                                            if (key.isNotEmpty() && value.isNotEmpty()) {
                                                currentConfigMap.value[key] = value
                                                addingInput.value = placeholder
                                                scrollToEndOfList()
                                                adding.value = false
                                            }
                                        }
                                    } else {
                                        // show the input
                                        adding.value = true
                                        scrollToEndOfList()
                                    }
                                }
                            ) {
                                val buttonText = if (adding.value) "Done" else "Add New"
                                Text(buttonText)
                            }
                        }

                        Button(
                            modifier = Modifier.padding(end = 10.dp),
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
fun ColumnScope.ConfigEntries(currentConfigMap: LinkedHashMap<String, String>, listState: LazyListState) {
    CompositionLocalProvider(LocalTextStyle provides editTextStyle) {
        Box(
            modifier = Modifier.weight(1f).fillMaxSize()
        ) {
                LazyColumn(modifier = Modifier.padding(end = 10.dp), state = listState) {
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
                        scrollState = listState
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

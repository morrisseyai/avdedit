package ai.morrissey.avdedit

import java.io.File

internal fun File.toAvdConfigMap() = this.useLines { lineSequence ->
    val configValuesMap = LinkedHashMap<String, String>()
    lineSequence.associateTo(configValuesMap) {
        val (key, value) = it.split("=")
        key to value
    }
    configValuesMap
}

internal fun LinkedHashMap<String, String>.saveToFile(file: File) {
    file.writeText(
        text = map { "${it.key}=${it.value}" }
            .sorted()
            .joinToString("\n")
    )
}

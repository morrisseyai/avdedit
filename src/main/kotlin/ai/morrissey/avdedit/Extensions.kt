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

package ai.morrissey.avdedit

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

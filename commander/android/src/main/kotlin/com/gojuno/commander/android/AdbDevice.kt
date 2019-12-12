@file:Suppress("unused")
package com.gojuno.commander.android

data class AdbDevice(
        val id: String,
        val model: String = "unknown",
        val online: Boolean
) {
    val isEmulator = id.startsWith("emulator-")
}

@file:Suppress("unused")
package com.gojuno.commander.android

import java.io.File

data class AdbDevice(
        val id: String,
        val model: String = "unknown",
        val online: Boolean
) {
    val isEmulator = id.startsWith("emulator-")
    val lockFile by lazy {
        File(lockFileDir, "$id.lock").also { lock ->
            lock.parentFile.mkdirs()
            lock.createNewFile()
            lock.deleteOnExit()
        }
    }
}

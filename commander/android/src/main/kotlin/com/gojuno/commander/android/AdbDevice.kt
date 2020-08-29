@file:Suppress("unused")

package com.gojuno.commander.android

import com.gojuno.commander.os.process
import com.gojuno.commander.os.trimmedOutput
import io.reactivex.Single
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

fun AdbDevice.log(message: String) = com.gojuno.commander.os.log("[$id] $message")

fun AdbDevice.deviceModel(): Single<String> =
    process(listOf(adb.absolutePath, "-s", id, "shell", "getprop ro.product.model undefined"))
        .trimmedOutput()
        .doOnError { log("Could not get model name of device $id, error = $it") }

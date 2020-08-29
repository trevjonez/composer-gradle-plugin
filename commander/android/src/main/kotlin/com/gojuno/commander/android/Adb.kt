@file:Suppress("MagicNumber")

package com.gojuno.commander.android

import com.gojuno.commander.os.Notification
import com.gojuno.commander.os.log
import com.gojuno.commander.os.process
import io.reactivex.Single

fun connectedAdbDevices(): Single<Set<AdbDevice>> =
    process(
        listOf(adb.absolutePath, "devices"),
        unbufferedOutput = true
    )
        .ofType(Notification.Exit::class.java)
        .map { it.output.readText() }
        .map {
          it.takeIf { it.contains("List of devices attached") }
          ?: throw IllegalStateException("Adb output is not correct: $it.")
        }
        .retry { retryCount, exception ->
          val shouldRetry = retryCount < 5 && exception is IllegalStateException
          if (shouldRetry) {
            log("connectedAdbDevices: retrying $exception.")
          }

          shouldRetry
        }
        .flatMapIterable { adbStdOut ->
          adbStdOut
              .substringAfter("List of devices attached")
              .split(System.lineSeparator())
              .map { it.trim() }
              .filter { it.isNotEmpty() }
              .filter { it.contains("online") || it.contains("device") }
        }
        .map { line ->
          val serial = line.substringBefore("\t")
          val online = when {
            line.contains("offline", ignoreCase = true) -> false
            line.contains("device", ignoreCase = true) -> true
            else -> throw IllegalStateException("Unknown adb output for device: $line")
          }
          AdbDevice(id = serial, online = online)
        }
        .flatMapSingle { device ->
          device.deviceModel().map { model ->
            device.copy(model = model)
          }
        }
        .toList()
        .map { it.toSet() }
        .doOnError { log("Error during getting connectedAdbDevices, error = $it") }

@file:Suppress("MagicNumber")

/*
 *    Copyright 2020 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.gojuno.commander.android

import com.gojuno.commander.os.Notification
import com.gojuno.commander.os.process
import com.gojuno.commander.os.trimmedOutput
import io.reactivex.Single
import java.io.File
import java.util.concurrent.TimeUnit

fun AdbDevice.externalStorage(): Single<String> =
    process(
        listOf(
            adb.absolutePath,
            "-s",
            id,
            "shell",
            "printenv",
            "EXTERNAL_STORAGE"
        )
    )
        .trimmedOutput()
        .doOnError {
          log("Could not get external storage of device $id, error = $it")
        }

fun AdbDevice.pullFolder(
    folderOnDevice: String,
    folderOnHostMachine: File,
    logErrors: Boolean,
    timeout: Pair<Int, TimeUnit> = 60 to TimeUnit.SECONDS
): Single<Boolean> {
  val pullFiles = process(
      commandAndArgs = listOf(
          adb.absolutePath,
          "-s",
          id,
          "pull",
          folderOnDevice,
          folderOnHostMachine.absolutePath
      ),
      timeout = timeout,
      unbufferedOutput = true
  )

  return pullFiles
      .ofType(Notification.Exit::class.java)
      .retry(3)
      .doOnError { error ->
        if (logErrors) {
          log("Failed to pull files from $folderOnDevice to $folderOnHostMachine: $error")
        }
      }
      .map { true }
      .onErrorReturn { false }
      .singleOrError()
}

fun AdbDevice.deleteFolder(
    folderOnDevice: String,
    logErrors: Boolean,
    timeout: Pair<Int, TimeUnit> = 60 to TimeUnit.SECONDS
): Single<Boolean> {
  val deleteFolder = process(
      commandAndArgs = listOf(
          adb.absolutePath,
          "-s",
          id,
          "shell",
          "rm",
          "-r",
          folderOnDevice
      ),
      timeout = timeout,
      unbufferedOutput = true
  )

  return deleteFolder
      .ofType(Notification.Exit::class.java)
      .retry(3)
      .doOnError { error ->
        if (logErrors) {
          log("Failed to delete directory $folderOnDevice: $error")
        }
      }
      .map { true }
      .onErrorReturn { false }
      .singleOrError()
}

fun AdbDevice.deleteFile(
    fileOnDevice: String,
    logErrors: Boolean,
    timeout: Pair<Int, TimeUnit> = 60 to TimeUnit.SECONDS
): Single<Boolean> {
  val deleteFile = process(
      commandAndArgs = listOf(
          adb.absolutePath,
          "-s",
          id,
          "shell",
          "rm",
          fileOnDevice
      ),
      timeout = timeout,
      unbufferedOutput = true
  )

  return deleteFile
      .ofType(Notification.Exit::class.java)
      .retry(3)
      .doOnError { error ->
        if (logErrors) {
          log("Failed to delete file $fileOnDevice: $error")
        }
      }
      .map { true }
      .onErrorReturn { false }
      .singleOrError()
}

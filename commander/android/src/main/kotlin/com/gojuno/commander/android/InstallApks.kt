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
import com.gojuno.commander.os.nanosToHumanReadableTime
import com.gojuno.commander.os.process
import com.gojuno.commander.os.waitForSuccess
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun AdbDevice.installMultipleApks(
    paths: List<String>,
    timeout: Pair<Int, TimeUnit> = 2 to TimeUnit.MINUTES,
    print: Boolean = false
): Observable<Unit> {
  val installMultiple = process(
      commandAndArgs = listOf(adb.absolutePath, "-s", id, "install-multiple", "-r") + paths,
      timeout = timeout,
      print = print,
      unbufferedOutput = true
  )

  return Observable
      .fromCallable { System.nanoTime() }
      .flatMap { startTimeNanos ->
        installMultiple.ofType(Notification.Exit::class.java)
            .map { it to startTimeNanos }
      }
      .map { (exit, startTimeNanos) ->
        val success = exit.waitForSuccess()

        val duration = System.nanoTime() - startTimeNanos

        if (success) {
          log("Successfully install-multiple in ${duration.nanosToHumanReadableTime()}, apkPaths = $paths")
        } else {
          log("Failed to install-multiple, apkPaths = $paths")
          exitProcess(1)
        }
      }
      .doOnSubscribe { log("Installing apks... apkPaths = $paths") }
      .doOnError { log("Error during install-multiple: $it, pathToApk = $paths") }
}

fun AdbDevice.installApk(
    pathToApk: String,
    timeout: Pair<Int, TimeUnit> = 2 to TimeUnit.MINUTES,
    print: Boolean = false
): Observable<Unit> {
  val installApk = process(
      commandAndArgs = listOf(adb.absolutePath, "-s", id, "install", "-r", pathToApk),
      timeout = timeout,
      print = print,
      unbufferedOutput = true
  )

  return Observable
      .fromCallable { System.nanoTime() }
      .flatMap { startTimeNanos ->
        installApk.ofType(Notification.Exit::class.java)
            .map { it to startTimeNanos }
      }
      .map { (exit, startTimeNanos) ->
        val success = exit.waitForSuccess()

        val duration = System.nanoTime() - startTimeNanos

        if (success) {
          log("Successfully installed apk in ${duration.nanosToHumanReadableTime()}, pathToApk = $pathToApk")
        } else {
          log("Failed to install apk $pathToApk")
          exitProcess(1)
        }
      }
      .doOnSubscribe { log("Installing apk... pathToApk = $pathToApk") }
      .doOnError { log("Error during installing apk: $it, pathToApk = $pathToApk") }
}

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
import io.reactivex.Observable
import java.io.File

fun AdbDevice.redirectLogcatToFile(file: File): Observable<Process> =
    process(
        listOf(adb.absolutePath, "-s", id, "logcat"),
        redirectOutputTo = file,
        timeout = null,
        destroyOnUnsubscribe = true
    )
        .doOnSubscribe { file.parentFile.mkdirs() }
        .ofType(Notification.Start::class.java)
        .doOnError { this.log("Error during redirecting logcat to file $file, error = $it") }
        .map { it.process }

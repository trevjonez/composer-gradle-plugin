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

package com.trevjonez

import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import java.io.File

abstract class RemoveApkTask : AbstractExecTask<RemoveApkTask>(RemoveApkTask::class.java) {

  @get:Input
  abstract val packageName: Property<String>

  @get:[Input Optional]
  abstract val adbPath: Property<String>

  private val androidHome by lazy {
    requireNotNull(System.getenv("ANDROID_SDK_ROOT")
                   ?: System.getenv("ANDROID_HOME"))
  }

  private val adb by lazy {
    "$androidHome${File.separator}platform-tools${File.separator}adb"
  }

  override fun exec() {
    isIgnoreExitValue = true
    commandLine(
        adbPath.orNull ?: adb,
        "shell", "pm", "uninstall",
        packageName.get()
               )
    super.exec()
  }
}
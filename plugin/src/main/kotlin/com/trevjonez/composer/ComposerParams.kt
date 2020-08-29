/*
 *    Copyright 2017 Trevor Jones
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

package com.trevjonez.composer

import org.gradle.api.file.FileCollection
import java.io.File

data class ComposerParams(
    val apk: File,
    val testApk: File,
    val withOrchestrator: Boolean?,
    val extraApks: FileCollection,
    val multiApks: FileCollection,
    val shard: Boolean?,
    val outputDirectory: File?,
    val instrumentationArguments: List<Pair<String, String>>,
    val verboseOutput: Boolean?,
    val devices: List<String>,
    val devicePattern: String?,
    val keepOutput: Boolean?,
    val apkInstallTimeout: Int?
) {

  init {
    if (devicePattern != null && devices.isNotEmpty()) {
      throw IllegalArgumentException(
          "devices and devicePattern can not be used together. " +
          "devices: [${devices.joinToString()}], " +
          "devicePattern: $devicePattern"
      )
    }
  }

  @Suppress("ComplexMethod")
  fun toCliArgs(): List<String> {
    return buildList {
      add("--apk"); add(apk.absolutePath)

      add("--test-apk"); add(testApk.absolutePath)

      withOrchestrator?.let {
        add("--with-orchestrator"); add("$it")
      }

      if (!extraApks.isEmpty) {
        add("--extra-apks"); extraApks.forEach { add(it.absolutePath) }
      }

      if (!multiApks.isEmpty) {
        add("--multi-apks"); multiApks.forEach { add(it.absolutePath) }
      }

      shard?.let {
        add("--shard"); add("$it")
      }

      outputDirectory?.let {
        add("--output-directory"); add("$it")
      }

      if (instrumentationArguments.isNotEmpty()) {
        add("--instrumentation-arguments")
        instrumentationArguments.forEach { (key, value) ->
          add(key); add(value)
        }
      }

      verboseOutput?.let {
        add("--verbose-output"); add("$it")
      }

      if (devices.isNotEmpty()) {
        add("--devices"); devices.forEach { device -> add(device) }
      }

      if (devicePattern?.isNotBlank() == true) {
        add("--device-pattern"); add(devicePattern)
      }

      if (keepOutput == true) {
        add("--keep-output-on-exit")
      }

      if ((apkInstallTimeout ?: 0) > 0) {
        add("--install-timeout"); add("$apkInstallTimeout")
      }
    }
  }
}

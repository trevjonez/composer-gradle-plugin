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
        val apkInstallTimeout: Int?) {

    init {
        if (devicePattern != null && devices.isNotEmpty())
            throw IllegalArgumentException("devices and devicePattern can not be used together. " +
                                           "devices: [${devices.joinToString()}], " +
                                           "devicePattern: $devicePattern")
    }

    fun toCliArgs(): List<String> {
      return listOf(
                "--apk", apk.absolutePath,
                "--test-apk", testApk.absolutePath)
                .let { params ->
                  withOrchestrator?.let {
                    params + arrayOf("--with-orchestrator", "$it")
                  } ?: params
                }
                .let { params ->
                    extraApks.takeIf { !it.isEmpty }?.let {
                      val apks = extraApks.map { file -> file.absolutePath }.toTypedArray()
                      params + arrayOf("--extra-apks", *apks)
                    } ?: params
                }
                .let { params ->
                    multiApks.takeIf { !it.isEmpty }?.let {
                      val apks = multiApks.map { file -> file.absolutePath }.toTypedArray()
                      params + arrayOf("--multi-apks", *apks)
                    } ?: params
                }
                .let { params ->
                    shard?.let {
                        params + arrayOf("--shard", "$it")
                    } ?: params
                }
                .let { params ->
                    outputDirectory?.let {
                        params + arrayOf("--output-directory", it.absolutePath)
                    } ?: params
                }
                .let { params ->
                    instrumentationArguments.takeIf { it.isNotEmpty() }?.let {
                        params + arrayOf("--instrumentation-arguments",
                                         *it.flatMap { (key, value) -> listOf(key, value) }.toTypedArray())
                    } ?: params
                }
                .let { params ->
                    verboseOutput?.let {
                        params + arrayOf("--verbose-output", "$it")
                    } ?: params
                }
                .let { params ->
                    devices.takeIf { it.isNotEmpty() }?.let {
                        params + arrayOf("--devices", *it.toTypedArray())
                    } ?: params
                }
                .let { params ->
                    devicePattern?.takeIf { it.isNotBlank() }?.let {
                        params + arrayOf("--device-pattern", it)
                    } ?: params
                }
                .let { params ->
                    keepOutput?.takeIf { it }?.let {
                        params + arrayOf("--keep-output-on-exit")
                    } ?: params
                }
                .let { params ->
                    apkInstallTimeout?.takeIf { it > 0 }?.let {
                        params + arrayOf("--install-timeout", "$it")
                    } ?: params
                }
    }
}

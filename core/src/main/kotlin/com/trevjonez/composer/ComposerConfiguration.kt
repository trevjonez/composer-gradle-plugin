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

import java.io.File

interface ComposerConfiguration {
    val apk: File
    val testApk: File
    val shard: Boolean?
    val outputDirectory: File?
    val instrumentationArguments: List<Pair<String, String>>
    val verboseOutput: Boolean?
    val devices: List<String>
    val devicePattern: String?
    val keepOutput: Boolean?
    val apkInstallTimeout: Int?

    fun toCliArgs(): List<String> {
        return listOf(
                "--apk", apk.absolutePath,
                "--test-apk", testApk.absolutePath)
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
                                         *it.flatMap { listOf(it.first, it.second) }.toTypedArray())
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
                    devicePattern?.let {
                        params + arrayOf("--device-pattern", it)
                    } ?: params
                }
                .let { params ->
                    keepOutput?.let {
                        params + arrayOf("--keep-output-on-exit")
                    } ?: params
                }
                .let { params ->
                    apkInstallTimeout?.let {
                        params + arrayOf("--install-timeout", "$it")
                    } ?: params
                }
    }

    data class DefaultImpl(
            override val apk: File,
            override val testApk: File,
            override val shard: Boolean?,
            override val outputDirectory: File?,
            override val instrumentationArguments: List<Pair<String, String>>,
            override val verboseOutput: Boolean?,
            override val devices: List<String>,
            override val devicePattern: String?,
            override val keepOutput: Boolean?,
            override val apkInstallTimeout: Int?)
        : ComposerConfiguration {
        init {
            if (devicePattern != null && devices.isNotEmpty())
                throw IllegalArgumentException("devices and devicePattern can not be used together. devices: [${devices.joinToString()}], devicePattern: $devicePattern")
        }
    }
}
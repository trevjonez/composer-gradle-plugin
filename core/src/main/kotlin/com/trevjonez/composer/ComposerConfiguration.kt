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
    val testPackage: String
    val testRunner: String
    val shard: OptionalWorkerParam<Boolean>
    val outputDirectory: OptionalWorkerParam<File>
    val instrumentationArguments: Map<String, String>
    val verboseOutput: OptionalWorkerParam<Boolean>

    fun toCliArgs(): Array<String> {
        return arrayOf(
                "--apk", apk.absolutePath,
                "--test-apk", testApk.absolutePath,
                "--test-package", testPackage,
                "--test-runner", testRunner)
                .let { params ->
                    shard.takeIf { it.isPresent() }?.let {
                        params + arrayOf("--shard", "${it.get()}")
                    } ?: params
                }
                .let { params ->
                    outputDirectory.takeIf { it.isPresent() }?.let {
                        params + arrayOf("--output-directory", it.get().absolutePath)
                    } ?: params
                }
                .let { params ->
                    instrumentationArguments.takeIf { it.isNotEmpty() }?.let {
                        params + arrayOf("--instrumentation-arguments",
                                         it.map { "${it.key} ${it.value}" }.joinToString(separator = " ") { it })
                    } ?: params
                }
                .let { params ->
                    verboseOutput.takeIf { it.isPresent() }?.let {
                        params + arrayOf("--verbose-output", "${it.get()}")
                    } ?: params
                }
    }
}
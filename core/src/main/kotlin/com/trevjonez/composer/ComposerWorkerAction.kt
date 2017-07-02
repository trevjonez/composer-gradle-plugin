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

import com.gojuno.composer.Exit
import com.gojuno.composer.run
import java.io.File
import javax.inject.Inject

class ComposerWorkerAction @Inject constructor(
        override val apk: File,
        override val testApk: File,
        override val testPackage: String,
        override val testRunner: String,
        override val shard: OptionalWorkerParam<Boolean>,
        override val outputDirectory: OptionalWorkerParam<File>,
        override val instrumentationArguments: Map<String, String>,
        override val verboseOutput: OptionalWorkerParam<Boolean>)
    : Runnable, ComposerConfiguration {

    override fun run() {
        val result = run(toCliArgs())
        when (result) {
            Exit.Ok -> return
            else -> throw ComposerTask.Error(result.message ?: "")
        }
    }
}
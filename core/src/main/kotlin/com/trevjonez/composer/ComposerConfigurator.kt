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

interface ComposerConfigurator {
    var apk: File?
    var testApk: File?
    var testPackage: String?
    var testRunner: String?
    var shard: Boolean?
    var outputDirectory: File
    val instrumentationArguments: MutableList<Pair<String, String>>
    var verboseOutput: Boolean?
    var devices: MutableList<String>
    var devicePattern: String?
    var keepOutput: Boolean?

    fun apk(value: File)
    fun apk(value: String)
    fun testApk(value: File)
    fun testApk(value: String)
    fun testPackage(value: String)
    fun testRunner(value: String)
    fun shard(value: Boolean)
    fun outputDirectory(value: File)
    fun outputDirectory(value: String)
    fun instrumentationArgument(key: String, value: String)
    fun instrumentationArguments(vararg values: Pair<String, String>)
    fun verboseOutput(value: Boolean)
    fun device(value: String)
    fun devices(vararg values: String)
    fun devicePattern(value: String)
    fun keepOutput(value: Boolean)
}
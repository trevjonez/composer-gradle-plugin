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

import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File
import kotlin.properties.Delegates

open class ConfiguratorDomainObj(val name: String, project: Project) : ComposerConfigurator {
    override var apk: File? = null
    override var testApk: File? = null
    override var shard: Boolean? = null
    override var outputDirectory: File = project.file(ComposerTask.DEFAULT_OUTPUT_DIR)
    override val instrumentationArguments: MutableList<Pair<String, String>> = mutableListOf()
    override var verboseOutput: Boolean? = null
    override var devices: MutableList<String> by Delegates.observable(mutableListOf()) { _, _, newValue ->
        if (devicePattern != null && newValue.isNotEmpty())
            throw IllegalArgumentException("devices and devicePattern can not be used together. devices: [${newValue.joinToString()}], devicePattern: $devicePattern")
    }
    override var devicePattern: String? by Delegates.observable<String?>(null) { _, _, newValue ->
        if (devices.isNotEmpty() && newValue != null)
            throw IllegalArgumentException("devices and devicePattern can not be used together. devices: [${devices.joinToString()}], devicePattern: $newValue")
    }
    override var keepOutput: Boolean? = null

    var configureTask: Action<ComposerTask>? = null

    override var apkInstallTimeout: Int? = null

    override fun apk(value: File) {
        apk = value
    }

    override fun apk(value: String) {
        apk(File(value))
    }

    override fun testApk(value: File) {
        testApk = value
    }

    override fun testApk(value: String) {
        testApk(File(value))
    }

    override fun shard(value: Boolean) {
        shard = value
    }

    override fun outputDirectory(value: File) {
        outputDirectory = value
    }

    override fun outputDirectory(value: String) {
        outputDirectory(File(value))
    }

    override fun instrumentationArguments(vararg values: Pair<String, String>) {
        values.forEach { instrumentationArguments.add(it) }
    }

    override fun instrumentationArgument(key: String, value: String) {
        instrumentationArguments.add(key to value)
    }

    override fun verboseOutput(value: Boolean) {
        verboseOutput = value
    }

    fun configureTask(action: Action<ComposerTask>) {
        configureTask = action
    }

    override fun device(value: String) {
        devices.add(value)
    }

    override fun devices(vararg values: String) {
        values.forEach { devices.add(it) }
    }

    override fun devicePattern(value: String) {
        devicePattern = value
    }

    override fun keepOutput(value: Boolean) {
        keepOutput = value
    }

    override fun apkInstallTimeout(value: Int) {
        apkInstallTimeout = value
    }
}
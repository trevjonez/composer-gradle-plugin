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

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import java.io.File
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

open class ComposerTask : JavaExec(), ComposerConfigurator {

    companion object {
        private const val MAIN_CLASS = "com.gojuno.composer.MainKt"
        private const val COMPOSER = "composer"
        private const val ARTIFACT_DEP = "com.gojuno.composer:composer:0.3.3"
        const val DEFAULT_OUTPUT_DIR = "composer-output"

        fun createComposerConfiguration(project: Project) {
            if (project.configurations.findByName(COMPOSER) == null) {
                project.configurations.create(COMPOSER)
                project.dependencies.add(COMPOSER, ARTIFACT_DEP)
            }
        }
    }

    val apkProp = newInputFile()
    val testApkProp = newInputFile()
    val outputDirProp = newOutputDirectory()

    override var apk: File? by apkProp
    override var testApk: File? by testApkProp

    @get:[Input Optional]
    override var shard: Boolean? = null

    override var outputDirectory: File by outputDirProp

    @Input
    override val instrumentationArguments = mutableListOf<Pair<String, String>>()

    @get:[Input Optional]
    override var verboseOutput: Boolean? = null

    @get:[Input Optional]
    override var devices: MutableList<String> by Delegates.observable(mutableListOf()) { _, _, newValue ->
        if (devicePattern != null && newValue.isNotEmpty())
            throw IllegalArgumentException("devices and devicePattern can not be used together. devices: [${newValue.joinToString()}], devicePattern: $devicePattern")
    }

    @get:[Input Optional]
    override var devicePattern: String? by Delegates.observable<String?>(null) { _, _, newValue ->
        if (devices.isNotEmpty() && newValue != null)
            throw IllegalArgumentException("devices and devicePattern can not be used together. devices: [${devices.joinToString()}], devicePattern: $newValue")
    }

    @get:[Input Optional]
    override var keepOutput: Boolean? = null

    @get:[Input Optional]
    override var apkInstallTimeout: Int? = null

    init {
        createComposerConfiguration(project)
        @Suppress("LeakingThis")
        outputDirectory = project.file(DEFAULT_OUTPUT_DIR)
    }

    override fun exec() {
        if (outputDirectory.exists()) {
            if (!outputDirectory.deleteRecursively()) {
                throw IllegalStateException("Failed to remove existing outputs")
            }
        }
        val config = ComposerConfiguration.DefaultImpl(
                apkProp.asFile.get(), testApkProp.asFile.get(),
                shard, outputDirectory, instrumentationArguments, verboseOutput,
                devices, devicePattern, keepOutput, apkInstallTimeout)
        args = config.toCliArgs()
        main = MAIN_CLASS
        classpath = project.configurations.getByName(COMPOSER)
        try {
            super.exec()
        } finally {
            val htmlReportIndex = File(outputDirectory, "${File.separator}html-report${File.separator}index.html")
            if (htmlReportIndex.exists()) {
                println("\nComposer Html Report: file://${htmlReportIndex.absolutePath}\n")
            }
        }
    }

    override fun apk(value: File) {
        apk = value
    }

    override fun apk(value: String) {
        apk(project.file(value))
    }

    override fun testApk(value: File) {
        testApk = value
    }

    override fun testApk(value: String) {
        testApk(project.file(value))
    }

    override fun shard(value: Boolean) {
        shard = value
    }

    override fun outputDirectory(value: File) {
        outputDirectory = value
    }

    override fun outputDirectory(value: String) {
        outputDirectory(project.file(value))
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

    private operator fun RegularFileProperty.setValue(composerTask: ComposerTask, property: KProperty<*>, file: File?) {
        file?.let { set(it) }
    }

    private operator fun RegularFileProperty.getValue(composerTask: ComposerTask, property: KProperty<*>): File? {
        return asFile.orNull
    }

    private operator fun DirectoryProperty.setValue(composerTask: ComposerTask, property: KProperty<*>, file: File) {
        set(file)
    }

    private operator fun DirectoryProperty.getValue(composerTask: ComposerTask, property: KProperty<*>): File {
        require(isPresent)
        return asFile.get()
    }
}

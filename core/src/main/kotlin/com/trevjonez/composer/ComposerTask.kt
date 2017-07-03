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

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.properties.Delegates

open class ComposerTask : JavaExec(), ComposerConfigurator {

    companion object {
        const val COMPOSER = "composer"
        const val ARTIFACT_DEP = "com.gojuno.composer:composer:0.2.3"
    }

    private val logger: Logger = LoggerFactory.getLogger(ComposerTask::class.java)

    init {
        if (project.configurations.findByName(COMPOSER) == null) {
            project.configurations.create(COMPOSER)
            project.dependencies.add(COMPOSER, ARTIFACT_DEP)
        }
    }

    @get:InputFile
    override var apk: File? by Delegates.observable<File?>(null) { prop, old, new ->
        logger.info("$name: apk: ${new?.absolutePath}")
    }

    @InputFile
    override var testApk: File? = null

    @Input
    override var testPackage: String? = null

    @Input
    override var testRunner: String? = null

    @Input
    @Optional
    override var shard: Boolean? = null

    @Input
    @OutputDirectory
    override var outputDirectory: File? = File("composer-output")

    @Input
    override var instrumentationArguments = mutableMapOf<String, String>()

    @Input
    @Optional
    override var verboseOutput: Boolean? = null

    override fun exec() {
        val config = ComposerConfiguration.DefaultImpl(
                apk!!, testApk!!, testPackage!!, testRunner!!,
                shard, outputDirectory, instrumentationArguments, verboseOutput)
        args = config.toCliArgs()
        main = "com.gojuno.composer.MainKt"
        classpath = project.configurations.getByName("composer")
        super.exec()
    }

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

    override fun testPackage(value: String) {
        testPackage = value
    }

    override fun testRunner(value: String) {
        testRunner = value
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

    override fun instrumentationArguments(value: MutableMap<String, String>) {
        instrumentationArguments = value
    }

    override fun instrumentationArguments(vararg values: Pair<String, String>) {
        instrumentationArguments(mutableMapOf(*values))
    }

    override fun addInstrumentationArgument(key: String, value: String) {
        instrumentationArguments.put(key, value)
    }

    override fun addInstrumentationArgument(value: Pair<String, String>) {
        instrumentationArguments.put(value.first, value.second)
    }

    override fun verboseOutput(value: Boolean) {
        verboseOutput = value
    }

}
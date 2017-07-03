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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import java.io.File
import kotlin.reflect.KClass

class ComposerPlugin : Plugin<Project> {

    companion object {
        const val GROUP = "Composer Plugin"
    }

    lateinit var config: ConfigExtension

    override fun apply(project: Project) {
        ComposerTask.createComposerConfiguration(project)
        config = project.extensions.create("composer", ConfigExtension::class.java, project)
        project.afterEvaluate(this::observeVariants)
    }

    private fun observeVariants(project: Project) {
        val androidExtension = project.extensions.getByName("android")

        @Suppress("FoldInitializerAndIfToElvis")
        if (androidExtension == null)
            throw NullPointerException("Android configuration not found on the current project. \n" +
                                       "composer is only applicable to android application builds")

        if (androidExtension !is AppExtension) {
            throw IllegalStateException("composer plugin only applicable to android application projects, open an issue if you need something else.")
        }

        androidExtension.applicationVariants.all {
            if (config.variants.isEmpty() || config.variants.contains(it.name)) {
                val assembleTask = project.tasks.findByName("assemble${it.name.capitalize()}AndroidTest")
                val configurator: ConfiguratorDomainObj? = config.configs.findByName(it.name)
                project.createTask(
                        type = ComposerTask::class,
                        name = "test${it.name.capitalize()}Composer",
                        description = "Run composer for ${it.name} variant",
                        dependsOn = listOf(assembleTask))
                        .apply {
                            apk = project.getApk(it, configurator)
                            testApk = project.getTestApk(it, configurator)
                            testPackage = getTestPackage(it, configurator)
                            testRunner = getTestRunner(it, configurator)
                            shard = configurator?.shard
                            outputDirectory = getOutputDirectory(it, configurator)
                            instrumentationArguments = collectInstrumentationArgs(configurator)
                            verboseOutput = configurator?.verboseOutput
                            configurator?.configureTask?.execute(this)
                        }
            }
        }
    }

    private fun <T : DefaultTask> Project.createTask(type: KClass<T>,
                                                     name: String,
                                                     group: String = GROUP,
                                                     description: String? = null,
                                                     dependsOn: List<Task>? = null)
            : T {
        return type.java.cast(project.tasks.create(LinkedHashMap<String, Any>().apply {
            put("name", name)
            put("type", type.java)
            put("group", group)
            description?.let { put("description", it) }
            dependsOn?.let { put("dependsOn", it) }
        }))
    }

    private fun collectInstrumentationArgs(configurator: ComposerConfigurator?): MutableMap<String, String> {
        return mutableMapOf<String, String>().apply {
            putAll(config.instrumentationArguments)
            configurator?.let { putAll(it.instrumentationArguments) }
        }
    }

    private fun Project.getApk(variant: ApplicationVariant,
                               configurator: ComposerConfigurator?): File {
        try {
            val apks = apksForTask(assemble(variant))
            return configurator?.apk ?: apks.singleFile
        } catch (multipleFiles: IllegalStateException) {
            throw IllegalStateException("Multiple APK outputs found, " +
                                        "You must define the apk to use for composer task on variant ${variant.name}", multipleFiles)
        }

    }

    private fun Project.getTestApk(variant: ApplicationVariant,
                                   configurator: ComposerConfigurator?): File {
        try {
            val apks = apksForTask(assembleAndroidTest(variant))
            return configurator?.testApk ?: apks.singleFile
        } catch (multipleFiles: IllegalStateException) {
            throw IllegalStateException("Multiple APK outputs found, " +
                                        "You must define the testApk to use for composer task on variant ${variant.name}", multipleFiles)
        }
    }

    private fun getTestPackage(variant: ApplicationVariant, configurator: ComposerConfigurator?): String {
        return configurator?.testPackage ?: variant.testPackage()
    }

    private fun getTestRunner(variant: ApplicationVariant, configurator: ComposerConfigurator?): String {
        return configurator?.testRunner ?: variant.mergedFlavor.testInstrumentationRunner
    }

    private fun getOutputDirectory(variant: ApplicationVariant, configurator: ConfiguratorDomainObj?): File {
        return if (configurator == null || configurator.outputDirectory == ComposerTask.DEFAULT_OUTPUT_DIR) {
            File("build/reports/composer/${variant.name}")
        } else {
            configurator.outputDirectory
        }
    }

    private fun apksForTask(task: Task): FileCollection {
        return task.outputs.files.filter { it.extension == "apk" }
    }

    private fun Project.assemble(variant: ApplicationVariant) =
            tasks.getByName("assemble${variant.name.capitalize()}")

    private fun Project.assembleAndroidTest(variant: ApplicationVariant) =
            tasks.getByName("assemble${variant.name.capitalize()}AndroidTest")

    private fun ApplicationVariant.testPackage(): String = "$applicationId.test"
}
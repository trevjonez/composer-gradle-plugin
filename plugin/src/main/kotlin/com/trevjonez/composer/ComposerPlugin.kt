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
import org.gradle.api.tasks.TaskProvider
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
        val androidExtension = project.extensions.findByName("android")

        @Suppress("FoldInitializerAndIfToElvis")
        if (androidExtension == null)
            throw NullPointerException("Android configuration not found on the current project. \n" +
                                       "composer is only applicable to android application builds")

        if (androidExtension !is AppExtension) {
            throw IllegalStateException("composer plugin only applicable to android application projects, open an issue if you need something else.")
        }

        androidExtension.applicationVariants.all { variant ->
            if (config.variants.isEmpty() || config.variants.contains(variant.name)) {
                if (variant.testVariant == null) return@all
                val assembleTask = project.tasks.named("assemble${variant.name.capitalize()}")
                                   ?: throw IllegalStateException("Couldn't find registered assemble task for variant ${variant.name}")

                val assembleTestTask = project.tasks.named("assemble${variant.name.capitalize()}AndroidTest")
                                             ?: throw IllegalStateException("Couldn't find registered assemble android test task for variant ${variant.name}")

                val configurator: ConfiguratorDomainObj? = config.configs.findByName(variant.name)
                project.registerTask(
                        type = ComposerTask::class,
                        name = "test${variant.name.capitalize()}Composer",
                        description = "Run composer for ${variant.name} variant",
                        dependsOn = listOf(assembleTask, assembleTestTask))
                        .configure { task ->
                            task.apply {
                                apk = getApk(variant, configurator)
                                testApk = getTestApk(variant, configurator)
                                shard = configurator?.shard
                                outputDirectory = getOutputDirectory(variant, configurator, project)
                                instrumentationArguments.addAll(collectInstrumentationArgs(configurator))
                                verboseOutput = configurator?.verboseOutput
                                configurator?.configureTask?.execute(this)
                                environment("ANDROID_HOME", androidExtension.sdkDirectory.absolutePath)
                                devices = configurator?.devices ?: mutableListOf()
                                devicePattern = configurator?.devicePattern
                                keepOutput = configurator?.keepOutput
                                apkInstallTimeout = configurator?.apkInstallTimeout
                            }
                        }
            }
        }
    }

    private fun <T : DefaultTask> Project.registerTask(type: KClass<T>,
                                                       name: String,
                                                       group: String = GROUP,
                                                       description: String? = null,
                                                       dependsOn: List<Any>? = null)
            : TaskProvider<T> {
        return project.tasks.register(name, type.java) { task ->
            task.group = group
            description?.let { task.description = it }
            dependsOn?.let { task.dependsOn(*it.toTypedArray()) }
        }
    }

    private fun collectInstrumentationArgs(configurator: ComposerConfigurator?): List<Pair<String, String>> {
        return mutableListOf<Pair<String, String>>().apply {
            addAll(config.instrumentationArguments)
            configurator?.let { addAll(it.instrumentationArguments) }
        }
    }

    private fun getApk(variant: ApplicationVariant,
                       configurator: ComposerConfigurator?): File {
        try {
            return configurator?.apk ?: apkForVariant(variant)
        } catch (multipleFiles: IllegalArgumentException) {
            throw IllegalStateException("Multiple APK outputs found, " +
                                        "You must define the apk to use for composer task on variant ${variant.name}", multipleFiles)
        } catch (noFiles: NoSuchElementException) {
            throw IllegalStateException("No APK output found," +
                                        "You must define the testApk to use for composer task on variant ${variant.name}", noFiles)
        }

    }

    private fun getTestApk(variant: ApplicationVariant,
                           configurator: ComposerConfigurator?): File {
        try {
            return configurator?.testApk ?: testApkForVariant(variant)
        } catch (multipleFiles: IllegalArgumentException) {
            throw IllegalStateException("Multiple APK outputs found, " +
                                        "You must define the testApk to use for composer task on variant ${variant.name}", multipleFiles)
        } catch (noFiles: NoSuchElementException) {
            throw IllegalStateException("No APK output found," +
                                        "You must define the testApk to use for composer task on variant ${variant.name}", noFiles)
        }
    }

    private fun apkForVariant(variant: ApplicationVariant): File {
        return variant.outputs.single().outputFile
    }

    private fun testApkForVariant(variant: ApplicationVariant): File {
        return variant.testVariant.outputs.single().outputFile
    }

    private fun getOutputDirectory(variant: ApplicationVariant, configurator: ConfiguratorDomainObj?, project: Project): File {
        return if (configurator == null || configurator.outputDirectory == project.file(ComposerTask.DEFAULT_OUTPUT_DIR)) {
            File(project.buildDir, "reports/composer/${variant.name}")
        } else {
            configurator.outputDirectory
        }
    }
}

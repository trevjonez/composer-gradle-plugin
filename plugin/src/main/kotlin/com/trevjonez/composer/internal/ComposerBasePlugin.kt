/*
 *    Copyright 2018 Trevor Jones
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

package com.trevjonez.composer.internal

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.trevjonez.composer.ComposerConfigurator
import com.trevjonez.composer.ComposerPlugin
import com.trevjonez.composer.ComposerTask
import com.trevjonez.composer.ConfigExtension
import com.trevjonez.composer.ConfiguratorDomainObj
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import java.io.File

abstract class ComposerBasePlugin<T> : Plugin<Project>
        where T : BaseVariant, T : TestedVariant {

    lateinit var project: Project
    lateinit var config: ConfigExtension

    override fun apply(target: Project) {
        this.project = target
        ComposerTask.createComposerConfiguration(target)
        config = target.extensions.create(ConfigExtension.DEFAULT_NAME, ConfigExtension::class.java, target)
        target.afterEvaluate { observeVariants() }
    }

    abstract val sdkDir: File
    abstract val testableVariants: DomainObjectCollection<T>

    private fun observeVariants() {
        testableVariants.all { variant ->
            if (config.variants.isEmpty() || config.variants.contains(variant.name)) {
                if (variant.testVariant == null) return@all

                val configurator: ConfiguratorDomainObj? = config.configs.findByName(variant.name)
                project.registerTask<ComposerTask>(
                        name = "test${variant.name.capitalize()}Composer",
                        description = "Run composer for ${variant.name} variant")
                        .configure { task ->
                            task.apply {
                                val mainPackage = getApk(variant, configurator)
                                val testPackage = getTestApk(variant, configurator)

                                dependsOn(mainPackage, testPackage)

                                apkProp.set(project.layout.file(project.provider {
                                    mainPackage.singleFile
                                }))

                                testApkProp.set(project.layout.file(project.provider {
                                    testPackage.singleFile
                                }))

                                shard = configurator?.shard
                                outputDirectory = getOutputDirectory(variant, configurator, project)
                                instrumentationArguments.addAll(collectInstrumentationArgs(configurator))
                                verboseOutput = configurator?.verboseOutput
                                configurator?.configureTask?.execute(this)
                                environment("ANDROID_HOME", sdkDir.absolutePath)
                                devices = configurator?.devices ?: mutableListOf()
                                devicePattern = configurator?.devicePattern
                                keepOutput = configurator?.keepOutput
                                apkInstallTimeout = configurator?.apkInstallTimeout
                            }
                        }
            }
        }
    }

    private inline fun <reified T : DefaultTask> Project.registerTask(
            name: String,
            description: String? = null,
            dependsOn: List<Any>? = null)
            : TaskProvider<T> {
        return project.tasks.register(name, T::class.java) { task ->
            task.group = ComposerPlugin.GROUP
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

    private fun getApk(variant: T, configurator: ComposerConfigurator?): FileCollection {
        return configurator?.apk?.let { project.files(it) }
               ?: apkForVariant(variant)
    }

    private fun getTestApk(variant: T, configurator: ComposerConfigurator?): FileCollection {
        return configurator?.testApk?.let { project.files(it) }
               ?: testApkForVariant(variant)
    }

    private fun apkForVariant(variant: T): FileCollection {
        return project.files().apply {
            variant.outputs.all {
                builtBy(it.assemble)
                from(it.outputFile)
            }
        }
    }

    private fun testApkForVariant(variant: T): FileCollection {
        return project.files().apply {
            builtBy(variant.assemble)
            variant.testVariant.outputs.all {
                builtBy(it.assemble)
                from(it.outputFile)
            }
        }
    }

    private fun getOutputDirectory(variant: T, configurator: ConfiguratorDomainObj?, project: Project): File {
        return if (configurator == null || configurator.outputDirectory == project.file(ComposerTask.DEFAULT_OUTPUT_DIR)) {
            File(project.buildDir, "reports/composer/${variant.name}")
        } else {
            configurator.outputDirectory
        }
    }
}
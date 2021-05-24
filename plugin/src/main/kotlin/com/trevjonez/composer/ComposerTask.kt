@file:Suppress("TooManyFunctions")

/*
 *    Copyright 2019 Trevor Jones
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

import com.trevjonez.composer.ComposerConfig.MAIN_CLASS
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import java.io.File

@CacheableTask
abstract class ComposerTask : JavaExec(), ComposerConfigurator, ComposerTaskDsl {

  @get:[Classpath InputFile]
  abstract override val testApk: RegularFileProperty

  @get:[Classpath InputFile]
  abstract override val apk: RegularFileProperty

  @get:[Optional Input]
  abstract override val withOrchestrator: Property<Boolean>

  @get:[Optional Input]
  abstract override val shard: Property<Boolean>

  @Suppress("UNCHECKED_CAST")
  @get:[Optional Input]
  abstract override val instrumentationArguments: ListProperty<Pair<String, String>>

  @get:[Optional Input]
  abstract override val verboseOutput: Property<Boolean>

  @get:[Optional Input]
  abstract override val devices: ListProperty<String>

  @get:[Optional Input]
  abstract override val devicePattern: Property<String>

  @get:[Optional Input]
  abstract override val keepOutput: Property<Boolean>

  @get:[Optional Input]
  abstract override val apkInstallTimeout: Property<Int>

  @get:[Classpath InputFiles]
  abstract override val extraApks: ConfigurableFileCollection

  @get:[Classpath InputFiles]
  abstract override val multiApks: ConfigurableFileCollection

  @get:OutputDirectory
  abstract override val outputDir: DirectoryProperty

  @get:OutputDirectory
  abstract override val workDir: DirectoryProperty

  init {
    apk.convention(testApk)
    val buildDirectory = project.layout.buildDirectory
    outputDir.convention(buildDirectory.dir(ComposerConfig.DEFAULT_OUTPUT_DIR))
    workDir.convention(buildDirectory.dir(ComposerConfig.DEFAULT_WORK_DIR))
    setWorkingDir(workDir.map { it.also { it.asFile.mkdirs() } })
    mainClass.set(MAIN_CLASS)
    classpath = project.composerConfig()
  }

  override fun exec() {
    val outputDir = outputDir.get().asFile

    if (outputDir.exists()) {
      if (!outputDir.deleteRecursively()) {
        throw IllegalStateException("Failed to remove existing outputs")
      }
    }

    val config = ComposerParams(
        apk.asFile.get(),
        testApk.asFile.get(),
        withOrchestrator.orNull,
        extraApks,
        multiApks,
        shard.orNull,
        outputDir,
        instrumentationArguments.getOrElse(emptyList()),
        verboseOutput.orNull ?: providerFactory.gradleProperty("composerVerbose").isPresent,
        devices.getOrElse(emptyList()),
        devicePattern.orNull,
        keepOutput.orNull,
        apkInstallTimeout.orNull)

    args = config.toCliArgs().also {
      logger.info(
          it.joinToString(prefix = "ComposerTask: args: =`", postfix = "`")
      )
    }

    try {
      super.exec()
    } finally {
      val htmlReportIndex = File(
          outputDir,
          "${File.separator}html-report${File.separator}index.html"
      )

      if (htmlReportIndex.exists()) {
        println(
            "\nComposer Html Report: file://${htmlReportIndex.absolutePath}\n"
        )
      }
    }
  }

  override fun apk(path: Any) {
    apk.set(project.file(path))
  }

  override fun testApk(path: Any) {
    testApk.set(project.file(path))
  }

  override fun extraApks(paths: Any) {
    extraApks.from(paths)
  }

  override fun multiApks(paths: Any) {
    multiApks.from(paths)
  }

  override fun outputDirectory(path: Any) {
    outputDir.set(project.file(path))
  }

  override fun workDirectory(path: Any) {
    TODO("Not yet implemented")
  }

  override fun withOrchestrator(value: Any) {
    withOrchestrator.eval(value)
  }

  override fun shard(value: Any) {
    shard.eval(value)
  }

  override fun instrumentationArgument(value: Any) {
    instrumentationArguments.eval(value)
  }

  override fun instrumentationArgument(key: CharSequence, value: CharSequence) {
    instrumentationArgument(key.toString() to value.toString())
  }

  override fun instrumentationArguments(value: Any) {
    instrumentationArguments.evalAll(value)
  }

  override fun verboseOutput(value: Any) {
    verboseOutput.eval(value)
  }

  override fun device(value: Any) {
    devices.eval(value)
  }

  override fun devices(value: Any) {
    devices.evalAll(value)
  }

  override fun devices(vararg values: CharSequence) {
    devices.evalAll(values.toList())
  }

  override fun devicePattern(value: Any) {
    devicePattern.eval(value)
  }

  override fun keepOutput(value: Any) {
    keepOutput.eval(value)
  }

  override fun apkInstallTimeout(value: Any) {
    apkInstallTimeout.eval(value)
  }
}

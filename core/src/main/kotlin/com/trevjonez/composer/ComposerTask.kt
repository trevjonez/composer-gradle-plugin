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

import com.trevjonez.composer.ComposerConfig.MAIN_CLASS
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.listProperty
import java.io.File

//TODO: use the worker api not JavaExec
@CacheableTask
open class ComposerTask : JavaExec(), ComposerConfigurator, ComposerTaskDsl {

  override val configuration = project.composerConfig()

  @InputFile
  override val testApk = this.newInputFile()

  @InputFile
  override val apk = this.newInputFile().apply { set(testApk) }

  @OutputDirectory
  override val outputDir = this.newOutputDirectory().apply {
    set(project.file(ComposerConfig.DEFAULT_OUTPUT_DIR))
  }

  @get:[Optional Input]
  override val withOrchestrator = project.emptyProperty<Boolean>()

  @get:[Optional Input]
  override val shard = project.emptyProperty<Boolean>()

  @get:[Optional Input]
  override val instrumentationArguments =
      project.objects.listProperty<Pair<String, String>>()

  @get:[Optional Input]
  override val verboseOutput = project.emptyProperty<Boolean>()

  @get:[Optional Input]
  override val devices = project.objects.listProperty<String>()

  @get:[Optional Input]
  override val devicePattern = project.emptyProperty<String>()

  @get:[Optional Input]
  override val keepOutput = project.emptyProperty<Boolean>()

  @get:[Optional Input]
  override val apkInstallTimeout = project.emptyProperty<Int>()

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
        shard.orNull,
        outputDir,
        instrumentationArguments.orEmpty,
        verboseOutput.orNull,
        devices.orEmpty,
        devicePattern.orNull,
        keepOutput.orNull,
        apkInstallTimeout.orNull)

    args = config.toCliArgs().also {
      project.logger.info(
          it.joinToString(prefix = "ComposerTask: args: =`", postfix = "`")
      )
    }
    main = MAIN_CLASS
    classpath = configuration

    try {
      super.exec()
    } finally {
      val htmlReportIndex = File(
          outputDir,
          "${File.separator}html-report${File.separator}index.html")

      if (htmlReportIndex.exists()) {
        println("\nComposer Html Report: file://${htmlReportIndex.absolutePath}\n")
      }
    }
  }

  override fun apk(path: Any) {
    apk.set(project.file(path))
  }

  override fun testApk(path: Any) {
    testApk.set(project.file(path))
  }

  override fun outputDirectory(path: Any) {
    outputDir.set(project.file(path))
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

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
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

open class ConfiguratorDomainObj(val name: String, val project: Project) :
    ComposerTaskDsl,
    ComposerConfigurator {

  override val testApk = project.layout.fileProperty()
  override val apk = project.layout.fileProperty()
  override val outputDir = project.layout.directoryProperty()

  override val configuration: Configuration = project.composerConfig()
  override val shard = project.objects.property<Boolean>()
  override val instrumentationArguments =
    project.objects.listProperty<Pair<String, String>>()
  override val verboseOutput = project.objects.property<Boolean>()
  override val devices = project.objects.listProperty<String>()
  override val devicePattern = project.objects.property<String>()
  override val keepOutput = project.objects.property<Boolean>()
  override val apkInstallTimeout = project.objects.property<Int>()

  override fun apk(path: Any) {
    apk.set(project.file(path))
  }

  override fun testApk(path: Any) {
    testApk.set(project.file(path))
  }

  override fun outputDirectory(path: Any) {
    outputDir.set(project.file(path))
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
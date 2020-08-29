@file:Suppress("TooManyFunctions")

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

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class ConfigExtension(project: Project) : ComposerDsl, ComposerConfigurator {

  val configs: NamedDomainObjectContainer<ConfiguratorDomainObj> =
      project.container(ConfiguratorDomainObj::class.java)

  val variants = mutableListOf<String>()

  fun variants(vararg values: String) {
    variants.addAll(values)
  }

  fun configs(closure: Closure<Any>) {
    configs.configure(closure)
  }

  fun configs(action: Action<NamedDomainObjectContainer<ConfiguratorDomainObj>>) {
    action.execute(configs)
  }

  abstract override val withOrchestrator: Property<Boolean>
  abstract override val shard: Property<Boolean>
  abstract override val instrumentationArguments: ListProperty<Pair<String, String>>
  abstract override val verboseOutput: Property<Boolean>
  abstract override val devices: ListProperty<String>
  abstract override val devicePattern: Property<String>
  abstract override val keepOutput: Property<Boolean>
  abstract override val apkInstallTimeout: Property<Int>

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

  companion object {
    const val DEFAULT_NAME = "composer"
  }
}

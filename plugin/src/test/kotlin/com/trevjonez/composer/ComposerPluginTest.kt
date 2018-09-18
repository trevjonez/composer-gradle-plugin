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

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ComposerPluginTest {
  private val ANDROID_HOME by environmentVariable

  private val buildDir by systemProperty

  private val andApp by systemProperty
  private val andLib by systemProperty

  @get:Rule
  val testProjectDir = TemporaryFolder()

  fun gradleRunner(projectDir: File, vararg args: String): GradleRunner {
    val argList = args.toMutableList().apply {
      add("--info")
      add("--stacktrace")
    }

    return GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(argList)
        .forwardOutput()
  }

  fun File.writeLocalProps() {
    File(this, "local.properties").writeText("sdk.dir=$ANDROID_HOME")
  }

  /**
   * Run with at least one device/emulator connected
   */
  @Test
  fun `basic application plugin integration`() {
    val projectDir = testProjectDir.newFolder("basicApp").apply {
      andApp.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir, "testDebugComposer")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, 0 passed, 0 failed",
                                       "Error: 0 tests were run.")
  }

  /**
   * Run with at least one device/emulator connected
   */
  @Test
  fun `basic library plugin integration`() {
    val projectDir = testProjectDir.newFolder("basicLib").apply {
      andLib.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir, "testDebugComposer")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, 0 passed, 0 failed",
                                       "Error: 0 tests were run.")
  }

  /**
   * Run with at least one device/emulator connected
   */
  @Test
  fun `custom task finds apk`() {
    val projectDir = testProjectDir.newFolder("customTask").apply {
      andApp.copyRecursively(this)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir,
                              "-b", "build-custom-task.gradle", "customTask")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, 0 passed, 0 failed",
                                       "Error: 0 tests were run.")
  }

  private val environmentVariable: ReadOnlyProperty<Any, String>
    get() {
      return object : ReadOnlyProperty<Any, String> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String {
          return System.getenv(property.name).also { Assume.assumeNotNull(it) }
        }
      }
    }

  private val systemProperty: ReadOnlyProperty<Any, File>
    get() {
      return object : ReadOnlyProperty<Any, File> {
        override fun getValue(thisRef: Any, property: KProperty<*>): File {
          return File(System.getProperty(property.name))
        }
      }
    }
}
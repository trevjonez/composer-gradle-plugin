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

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ComposerPluginTest {
  private val ANDROID_HOME by environmentVariable

  private val buildDir by systemProperty

  private val andApp by systemProperty
  private val andLib by systemProperty
  private val andDyn by systemProperty

  @get:Rule
  val testProjectDir = BuildDir(buildDir)

  fun gradleRunner(projectDir: File, vararg args: String): GradleRunner {
    val argList = args.toList() + listOf("--stacktrace", "--no-build-cache")

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
  fun `application plugin integration`() {
    val projectDir = testProjectDir.newFolder("basicApp").apply {
      andApp.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir, "testDebugComposer")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, total passed = 1, total failed = 1, total ignored = 1",
                                       "Error: There were failed tests.")
  }

  @Test
  fun `plugin registered tasks listed in tasks command`() {
    val projectDir = testProjectDir.newFolder("basicApp_taskList").apply {
      andApp.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir, "tasks")
        .build()

    assertThat(result.output).contains("Composer Plugin tasks",
                                       "testDebugComposer - Run composer for debug variant")
  }

  /**
   * Run with at least one device/emulator connected
   */
  @Test
  fun `library plugin integration`() {
    val projectDir = testProjectDir.newFolder("basicLib").apply {
      andLib.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir, "testDebugComposer")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, total passed = 1, total failed = 1, total ignored = 1",
                                       "Error: There were failed tests.")
  }

  /**
   * Run with at least one device/emulator connected
   */
  @Test
  fun `application plugin with dynamic feature integration`() {
    val projectDir = testProjectDir.newFolder("dynamicApp").apply {
      andDyn.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir, "base:testDebugComposer")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, total passed = 1, total failed = 1, total ignored = 1",
                                       "Error: There were failed tests.")
  }

  /**
   * Run with at least one device/emulator connected
   */
  @Test
  fun `dynamic feature plugin integration`() {
    val projectDir = testProjectDir.newFolder("dynamicApp").apply {
      andDyn.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir, "atInstall:testDebugComposer")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, total passed = 2, total failed = 1, total ignored = 1",
                                       "Error: There were failed tests.")
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
                              "-b", "build-custom-task.gradle",
                              "customTask")
        .buildAndFail()

    assertThat(result.output).contains("Successfully installed apk",
                                       "Starting tests",
                                       "Test run finished, total passed = 1, total failed = 1, total ignored = 1",
                                       "Error: There were failed tests.")
  }


  /**
   * Run with at least one device/emulator connected
   *
   * This test is non exhaustive.
   * Might be nice to do a parameterized setup to ensure it collects things correctly?
   *
   */
  @Test
  fun `plugin cascades and collects dsl inputs`() {
    val projectDir = testProjectDir.newFolder("basicApp").apply {
      andApp.copyRecursively(this, true)
      writeLocalProps()
    }

    val result = gradleRunner(projectDir,
                              "--info",
                              "-b", "build-cascade-dsl.gradle",
                              "testDebugComposer")
        .buildAndFail()

    assertThat(result.output).contains(
        "--instrumentation-arguments, screenshotsDisabled, false, screenshotsEngine, uiAutomator",
        "--verbose-output, true",
        "--keep-output-on-exit",
        "--install-timeout, 10")
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

    class BuildDir(val parentDir: File): TestRule {
        lateinit var root: File
        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    root = File(parentDir, "testDir-${description.methodName.replace("\\s+".toRegex(), "_")}")
                    if(root.exists()) root.deleteRecursively()
                    root.mkdirs()
                    base.evaluate()
                }
            }
        }

        fun newFolder(name: String) = File(root, name).apply { mkdirs() }
    }
}
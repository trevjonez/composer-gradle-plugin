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

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assume.assumeThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

annotation class FileName(val value: String)

class ComposerTaskTest {

  @get:Rule
  val testProjectDir = TemporaryFolder()

  private val ANDROID_SDK_ROOT by environmentVariable

  @FileName("build.gradle")
  private val buildFile by testProjectDir.makeFile()

  @FileName("app.apk")
  private val appApk by testProjectDir.makeFile()

  @FileName("test.apk")
  private val testApk by testProjectDir.makeFile()

  private val defaultConfig by lazy {
    //language=Groovy
    """
    repositories {
      mavenLocal()
      mavenCentral()
    }
    """.trimIndent()
  }

  private val defaultTaskDsl by lazy {
    //language=Groovy
    """
      apk "$appApk"
      testApk "$testApk"
    """.trimIndent()
  }

  private fun makeBuildFile(
      buildScriptConfig: String = defaultConfig,
      taskDsl: String = defaultTaskDsl,
      dependencyDsl: String = ""
  ): String {
    //language=Groovy
    return """
      plugins {
        id "com.trevjonez.composer"
      }

      import com.trevjonez.composer.ComposerTask

      $buildScriptConfig

      task runComposer(type: ComposerTask) {
        environment.put("ANDROID_HOME", "$ANDROID_SDK_ROOT")
        $taskDsl
      }

      dependencies {
        $dependencyDsl
      }
      """.trimIndent()
  }

  private fun buildRunner(): GradleRunner {
    return GradleRunner.create()
        .withPluginClasspath()
        .withProjectDir(testProjectDir.root)
        .withArguments("runComposer", "--stacktrace")
        .forwardOutput()
  }

  /**
   * The apk is fake so AAPT fails with this error.
   * It is an indicator that the plugin task invoked composer
   */
  private val dumpFailedError =
      "ERROR: dump failed because no AndroidManifest.xml found"

  @Test
  fun defaultConfig() {
    makeBuildFile().writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun singleApk() {
    makeBuildFile(taskDsl = """
      testApk "$testApk"
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL devicePattern`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      devicePattern("somePattern")
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL deviceDsl`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      device("dev1")
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL devicesDsl`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      devices(["dev1", "dev2"])
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL devicesVarArgDsl`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      devices("dev1", "dev2")
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL verboseOutput`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      verboseOutput(true)
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL withOrchestrator`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      withOrchestrator(true)
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL instArg`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      instrumentationArgument("foo", "bar")
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL keepOutput`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      keepOutput(true)
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  @Test
  fun `Task DSL apkInstallTimeout`() {
    //language=Groovy
    makeBuildFile(taskDsl = """
      $defaultTaskDsl
      apkInstallTimeout(42)
    """.trimIndent()).writeTo(buildFile)

    val runResult = buildRunner().buildAndFail()

    assertThat(runResult.output).contains(dumpFailedError)
  }

  private fun String.writeTo(file: File) =
      BufferedWriter(FileWriter(file)).use {
        it.write(this)
      }

  private val environmentVariable: ReadOnlyProperty<Any, String>
    get() {
      return object : ReadOnlyProperty<Any, String> {
        override fun getValue(thisRef: Any, property: KProperty<*>): String {
          return System.getenv(property.name).also { assumeThat("Env variable '${property.name}' was null", it, `is`(notNullValue())) }
        }
      }
    }

  private fun TemporaryFolder.makeFile(): ReadOnlyProperty<Any, File> {
    return object : ReadOnlyProperty<Any, File> {
      private var file: File? = null
      override fun getValue(thisRef: Any, property: KProperty<*>): File {
        file?.let { return it }

        val name = requireNotNull(property.findAnnotation<FileName>()) {
          "FileName Annotation Required"
        }

        return newFile(name.value).also { file = it }
      }
    }
  }
}

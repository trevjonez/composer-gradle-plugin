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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ComposerPluginTest {
    @Rule @JvmField val testProjectDir = TemporaryFolder()


    /**
     * Run with an emulator connected on port 5554
     */
    @Test
    fun happyPath() {
        val projectDir = testProjectDir.newFolder("vanilla").also {
            FileUtils.copyDirectory(File(javaClass.classLoader.getResource("vanilla").path), it)
            File(it, "local.properties").writeText("sdk.dir=${System.getenv("HOME")}/Library/Android/sdk", Charsets.UTF_8)
            File(it, "libs").also {
                it.mkdir()
                FileUtils.copyFileToDirectory(File(".", "build/libs/plugin.jar"), it)
                FileUtils.copyFileToDirectory(File(".", "../core/build/libs/core.jar"), it)
            }
        }

        val result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("testDebugComposer")
                .withDebug(true)
                .forwardOutput()
                .buildAndFail()

        assertThat(result.output).contains("Successfully installed apk",
                                                      "Starting tests",
                                                      "Test run finished, 0 passed, 0 failed",
                                                      "Error: 0 tests were run.")
    }
}
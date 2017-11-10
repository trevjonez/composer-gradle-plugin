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
import org.junit.Assume.assumeNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class ComposerTaskTest {
    @Rule @JvmField val testProjectDir = TemporaryFolder()
    val buildFile: File by lazy { testProjectDir.newFile("build.gradle") }
    
    @Before
    fun setUp() {
        File(testProjectDir.root, "app.apk").createNewFile()
        File(testProjectDir.root, "app-test.apk").createNewFile()
        File(testProjectDir.root, "libs").also {
            it.mkdir()
            FileUtils.copyFileToDirectory(File(".", "build/libs/core.jar"), it)
        }
    }

    /**
     * Run without any android devices connected or emulators running
     */
    @Test
    fun functionalCheck() {
        val androidHome: String? = System.getenv("ANDROID_HOME")
        assumeNotNull(androidHome)
        """
buildscript {
    dependencies {
        classpath files("libs/core.jar")
    }
}

repositories {
    jcenter()
}

import com.trevjonez.composer.ComposerTask

task runComposer(type: ComposerTask) {
    apk "${testProjectDir.root.absolutePath}/app.apk"
    testApk "${testProjectDir.root.absolutePath}/app-test.apk"
    testPackage "com.nope.test"
    testRunner "com.nope.Runner"
    environment.put("ANDROID_HOME", "$androidHome")
    devicePattern "notArealPattern"
}

dependencies {
    //optional classpath config
    composer "com.gojuno.composer:composer:0.2.8"
}
""".writeTo(buildFile)

        val runResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("runComposer")
                .forwardOutput()
                .buildAndFail()

        assertThat(runResult.output).contains("Error: No devices available for tests.")
    }

    private fun String.writeTo(file: File) {
        println(this)

        var output: BufferedWriter? = null
        try {
            output = BufferedWriter(FileWriter(file))
            output.write(this)
        } finally {
            if (output != null) {
                output.close()
            }
        }
    }

}
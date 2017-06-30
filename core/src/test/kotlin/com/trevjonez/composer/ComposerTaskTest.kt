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
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter


class ComposerTaskTest {
    @Rule @JvmField val testProjectDir = TemporaryFolder()
    val buildFile: File by lazy { testProjectDir.newFile("build.gradle") }
    
    @Before
    fun setUp() {
        File(testProjectDir.root, "libs").also {
            it.mkdir()
            FileUtils.copyFileToDirectory(File(".", "build/libs/core.jar"), it)
        }
    }

    @Test
    fun functionalCheck() {
        //language=Groovy
        "buildscript {\n    dependencies {\n        classpath files(\"libs/core.jar\")\n    }\n}\n\nimport com.trevjonez.composer.ComposerTask\n\ntask runComposer(type: ComposerTask) {\n    apk \"app.apk\"\n    testApk \"app-test.apk\"\n    testPackage \"com.nope\"\n    testRunner \"com.nope.Runner\"\n}".writeTo(buildFile)

        val runResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("runComposer", "--stacktrace")
                .forwardOutput()
                .build()
    }

    private fun String.writeTo(file: File) {
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
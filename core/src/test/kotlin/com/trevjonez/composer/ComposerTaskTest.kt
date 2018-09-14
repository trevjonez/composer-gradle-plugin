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
import org.junit.Assume.assumeNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

//TODO break the test here into pieces and verify more about how this works
@Suppress("PrivatePropertyName")
class ComposerTaskTest {
    val buildDir by systemProperty { File(it) }
    private val ANDROID_HOME by environmentVariable

    @get:Rule
    val testProjectDir = TemporaryFolder()

    private val buildFile: File by lazy {
        testProjectDir.newFile("build.gradle")
    }

    private val classpathManifest by lazy {
        javaClass.classLoader.getResource("classpath-manifest.txt")
                .openStream().use { inStream ->
                    inStream.reader().readLines().map { File(it) }
                }
    }

    //language=Groovy
    private val buildScriptConfig = """
buildscript {
    dependencies {
        classpath(files(${classpathManifest.joinToString { "\"$it\"" }}))
    }
}

repositories {
    jcenter()
}"""

    @Before
    fun setUp() {
        File(testProjectDir.root, "app.apk").createNewFile()
        File(testProjectDir.root, "app-test.apk").createNewFile()
    }

    /**
     * Device config doesn't matter for this test. Our fake apk file will make composer puke.
     * All this does is verifies we are getting to the invocation of composer without hick-up, not much else.
     */
    @Test
    fun functionalCheck() {
        //language=Groovy
        """
import com.trevjonez.composer.ComposerTask

$buildScriptConfig

task runComposer(type: ComposerTask) {
    apk "${testProjectDir.root.absolutePath}/app.apk"
    testApk "${testProjectDir.root.absolutePath}/app-test.apk"
    environment.put("ANDROID_HOME", "$ANDROID_HOME")
    devicePattern "fakePattern"
}

dependencies {
    //optional classpath config
    composer "com.gojuno.composer:composer:0.3.2"
}
""".writeTo(buildFile)

        val runResult = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("runComposer", "--stacktrace")
                .forwardOutput()
                .buildAndFail()

        assertThat(runResult.output).contains("ERROR: dump failed because no AndroidManifest.xml found")
    }

    private fun String.writeTo(file: File) =
            BufferedWriter(FileWriter(file)).use {
                it.write(this)
            }

    private inline fun <R, T : Any> systemProperty(crossinline conversion: (String) -> T)
            : ReadOnlyProperty<R, T> {
        return object : ReadOnlyProperty<R, T> {
            override fun getValue(thisRef: R, property: KProperty<*>): T {
                return conversion(System.getProperty(property.name)!!)
            }
        }
    }

    private val environmentVariable: ReadOnlyProperty<Any, String>
        get() {
            return object : ReadOnlyProperty<Any, String> {
                override fun getValue(thisRef: Any, property: KProperty<*>): String {
                    return System.getenv(property.name).also { assumeNotNull(it) }
                }
            }
        }
}
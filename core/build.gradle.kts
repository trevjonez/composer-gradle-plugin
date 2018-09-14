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

plugins {
    kotlin("jvm")
    `maven-publish`
    `kotlin-dsl`
}

val classpathManifest = tasks.register("createClasspathManifest") {
    val outputDir = file("$buildDir/$name")

    val runtimeClasspath = sourceSets.getByName("main").runtimeClasspath
    inputs.files(runtimeClasspath)
    outputs.dir(outputDir)

    doLast {
        outputDir.mkdirs()
        file("$outputDir/classpath-manifest.txt")
                .writeText(runtimeClasspath.joinToString(separator = "\n"))
    }
}

dependencies {
    compile(gradleApi())

    testCompile(gradleTestKit())
    testCompile("junit:junit:4.12")
    testCompile("org.assertj:assertj-core:3.5.2")
    testCompile("commons-io:commons-io:2.5")

    testRuntime(files(classpathManifest))
}

tasks.named("test").configure {
    this as Test

    systemProperty("buildDir", buildDir.absolutePath)
}
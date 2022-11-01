@file:Suppress("LocalVariableName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
*    Copyright 2018 Trevor Jones
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

buildscript {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  dependencies {
    val KOTLIN_VERSION: String by rootProject
    classpath("org.gradle.kotlin:gradle-kotlin-dsl-plugins:2.1.4")
    classpath("com.gradle.publish:plugin-publish-plugin:0.11.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION")
    classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
  }
}

plugins {
  `build-dashboard`
  id("io.gitlab.arturbosch.detekt").version("1.14.2").apply(false)
}

allprojects {
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
      freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
    }
  }

  apply(plugin = "io.gitlab.arturbosch.detekt")
  dependencies {
    add("detektPlugins", "io.gitlab.arturbosch.detekt:detekt-formatting:1.14.2")
  }
}

tasks.register("setupGradleCredentials") {
  onlyIf {
    !properties.containsKey("gradle.publish.key") && !properties.containsKey("gradle.publish.secret")
  }
  doLast {
    System.setProperty(
        "gradle.publish.key",
        requireNotNull(System.getenv("GRADLE_PUBLISH_KEY"))
    )
    System.setProperty(
        "gradle.publish.secret",
        requireNotNull(System.getenv("GRADLE_PUBLISH_SECRET"))
    )
  }
}
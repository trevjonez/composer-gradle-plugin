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

rootProject.name = "composer-gradle-plugin"
include("core", "plugin")

val CGP_VERSION: String by settings
val AGP_VERSION: String by settings
val KOTLIN_VERSION: String by settings

pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android"))
                useModule("com.android.tools.build:gradle:$AGP_VERSION")
            if (requested.id.id.startsWith("org.jetbrains.kotlin"))
                useVersion(KOTLIN_VERSION)
        }
    }
}

gradle.allprojects {
    group = "com.github.trevjonez.composer-gradle-plugin"
    version = CGP_VERSION

    repositories {
        google()
        jcenter()
    }

    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "com.android.tools.build" && requested.name == "gradle")
                    useVersion(AGP_VERSION)
            }
        }
    }
}

enableFeaturePreview("STABLE_PUBLISHING")
enableFeaturePreview("IMPROVED_POM_SUPPORT")

buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
    }
    dependencies {
        val GRADLE_SCAN_VERSION: String by settings
        classpath("com.gradle:build-scan-plugin:$GRADLE_SCAN_VERSION")
    }
}

gradle.rootProject {
    apply<com.gradle.scan.plugin.BuildScanPlugin>()

    configure<com.gradle.scan.plugin.BuildScanExtension> {
        setTermsOfServiceUrl("https://gradle.com/terms-of-service")
        setTermsOfServiceAgree("yes")
        publishAlways()
    }
}
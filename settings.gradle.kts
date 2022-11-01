@file:Suppress("PropertyName", "LocalVariableName")

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

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  resolutionStrategy {
    val AGP_VERSION: String by settings
    eachPlugin {
      if (requested.id.id.startsWith("com.android"))
        useModule("com.android.tools.build:gradle:$AGP_VERSION")
    }
  }
}

plugins {
  id("com.gradle.enterprise") version "3.4.1"
}

gradleEnterprise {
  buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    publishAlways()
  }
}

rootProject.name = "composer-gradle-plugin"

include(":plugin")
include(":commander", ":commander:os", ":commander:android", ":commander:testSupport")
include("composer")

project(":composer").projectDir = file("composer/composer")

val CGP_VERSION: String by settings
gradle.allprojects {
  version = CGP_VERSION

  repositories {
    google()
    mavenCentral()
    mavenLocal()
  }
}

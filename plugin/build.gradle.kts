@file:Suppress("LocalVariableName")

plugins {
  `java-gradle-plugin`
  id("org.gradle.kotlin.kotlin-dsl")
  id("maven-publish")
  id("com.gradle.plugin-publish")
  id("com.trevjonez.adb-uninstall")
}

ext["artifactId"] = "plugin"

gradlePlugin {
  plugins {
    create("composer") {
      displayName = "com.trevjonez.composer"
      description = "Gradle task type and plugin for interacting with https://github.com/gojuno/composer"
      id = "com.trevjonez.composer"
      implementationClass = "com.trevjonez.composer.ComposerPlugin"
    }
  }
}

pluginBundle {
  website = "https://github.com/trevjonez/composer-gradle-plugin"
  vcsUrl = "https://github.com/trevjonez/composer-gradle-plugin.git"
  tags = listOf("android", "composer", "test", "orchestrator", "report")
}

dependencies {
  val AGP_VERSION: String by project
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  api("com.android.tools.build:gradle:$AGP_VERSION")
  api(gradleApi())

  testImplementation(gradleTestKit())
  testImplementation("junit:junit:4.12")
  testImplementation("org.assertj:assertj-core:3.5.2")
  testImplementation("commons-io:commons-io:2.5")
  testImplementation("org.jetbrains.kotlin:kotlin-reflect")
}

adbUninstall.packages(
    "com.trevjonez.andapp",
    "com.trevjonez.andapp.test",

    "com.trevjonez.anddyn",
    "com.trevjonez.anddyn.test",
    "com.trevjonez.atinstall.test",

    "com.trevjonez.andlib.test",

    "com.trevjonez.testapp",
    "com.trevjonez.testapp.test"
)

tasks.test {
  systemProperty("buildDir", buildDir.absolutePath)
  systemProperty("andApp", file("../and-app").absolutePath)
  systemProperty("andLib", file("../and-lib").absolutePath)
  systemProperty("andDyn", file("../and-dyn").absolutePath)
  systemProperty("andTest", file("../and-test").absolutePath)

  systemProperty("org.gradle.testkit.debug", false)

  outputs.apply {
    dir("$buildDir/tests")
    dir("../and-app/build")
    dir("../and-lib/build")
    dir("../and-dyn/build")
    dir("../and-dyn/base/build")
    dir("../and-dyn/atInstall/build")
  }

  inputs.apply {
    files("gradle.properties")

    dir("../and-app/src")
    files(
        "../and-app/build.gradle",
        "../and-app/build-cascade-dsl.gradle",
        "../and-app/build-custom-task.gradle",
        "../and-app/settings.gradle"
    )

    dir("../and-lib/src")
    files(
        "../and-lib/build.gradle",
        "../and-lib/settings.gradle"
    )

    dir("../and-dyn/base/src")
    dir("../and-dyn/atInstall/src")
    files(
        "../and-dyn/build.gradle",
        "../and-dyn/settings.gradle",
        "../and-dyn/base/build.gradle",
        "../and-dyn/atInstall/build.gradle"
    )

    dir("../and-test/app/src")
    dir("../and-test/test/src")
    files(
        "../and-test/app/build.gradle",
        "../and-test/test/build.gradle",
        "../and-test/build.gradle",
        "../and-test/settings.gradle"
    )

    dir("../commander/os/src/main")
    dir("../commander/android/src/main")
    dir("../composer/composer/src/main")
    dir("../composer/html-report")
    files(
        "../commander/os/build.gradle",
        "../commander/android/build.gradle",
        "../composer/composer/build.gradle"
    )
  }

  dependsOn(":composer:publishToMavenLocal")
  dependsOn(":commander:os:publishToMavenLocal")
  dependsOn(":commander:android:publishToMavenLocal")
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
  dependsOn(sourceSets["main"].classesTaskName)
}

publishing {
  publications {
    register<MavenPublication>("plugin") {
      from(components["java"])
      artifact(sourcesJar)

      pom {
        inceptionYear.set("2017")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
          }
        }
      }
    }
  }
}
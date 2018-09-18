plugins {
  `java-gradle-plugin`
  kotlin("jvm")
  `maven-publish`
  `kotlin-dsl`
}

gradlePlugin {
  plugins {
    create("composer") {
      id = "composer"
      implementationClass = "com.trevjonez.composer.ComposerPlugin"
    }
  }
}

val AGP_VERSION: String by project

dependencies {
  compile(project(":core"))
  compile("com.android.tools.build:gradle:$AGP_VERSION")
}

dependencies {
  testCompile("junit:junit:4.12")
  testCompile("org.assertj:assertj-core:3.5.2")
  testCompile("commons-io:commons-io:2.5")
}

tasks.named("test").configure {
  this as Test
  systemProperty("buildDir", buildDir.absolutePath)
  systemProperty("andApp", File(rootProject.projectDir, "and-app").absolutePath)
  systemProperty("andLib", File(rootProject.projectDir, "and-lib").absolutePath)
  systemProperty("org.gradle.testkit.debug", false)

  outputs.dir("$buildDir/tests")
  outputs.dir(File(rootProject.projectDir, "and-app/build"))
  outputs.dir(File(rootProject.projectDir, "and-lib/build"))

  inputs.files("gradle.properties")
  inputs.dir(File(rootProject.projectDir, "and-app/src"))
  inputs.files(File(rootProject.projectDir, "and-app/build.gradle"))
  inputs.files(File(rootProject.projectDir, "and-app/build-cascade-dsl.gradle"))
  inputs.files(File(rootProject.projectDir, "and-app/build-custom-task.gradle"))
  inputs.files(File(rootProject.projectDir, "and-app/settings.gradle"))

  inputs.dir(File(rootProject.projectDir, "and-lib/src"))
  inputs.files(File(rootProject.projectDir, "and-lib/build.gradle"))
  inputs.files(File(rootProject.projectDir, "and-lib/settings.gradle"))
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
  classifier = "sources"
  from(sourceSets["main"].allSource)
  dependsOn(sourceSets["main"].classesTaskName)
}

publishing {
  publications {
    register("plugin", MavenPublication::class.java) {
      from(components["java"])
      artifact(sourcesJar.get())

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
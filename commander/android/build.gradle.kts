plugins {
  kotlin("jvm")
  id("com.trevjonez.adb-uninstall")
}

apply(from = "${rootProject.projectDir}/publish.gradle")

ext["artifactId"] = "commander-android"
description = "Functions to work with Android SDK Tools like adb, avdmanager, sdkmanager."

dependencies {
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  api(project(":commander:os"))

  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.5")
  testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.5")
  testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect")
}

adbUninstall.packages("com.trevjonez.commander.testsupport")

tasks.test {
  useJUnitPlatform {
    includeEngines("spek2")
  }

  dependsOn(":commander:testSupport:assembleDebug")
  val apkDir = file("${project(":commander:testSupport").buildDir.absolutePath}/outputs/apk")
  inputs.dir(apkDir)

  systemProperty("oldApk", "$apkDir/older/debug/testSupport-older-debug.apk")
  systemProperty("newApk", "$apkDir/newer/debug/testSupport-newer-debug.apk")
}
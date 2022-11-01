plugins {
  kotlin("jvm")
  `application`
}

apply(from = "${rootProject.projectDir}/publish.gradle")

ext.set("artifactId", "composer")
description = "Reactive Android Instrumentation Test Runner."

application {
  mainClassName = "com.gojuno.composer.MainKt"
}

dependencies {
  api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  api(project(":commander:android"))
  api("com.beust:jcommander:1.81")
  api("commons-io:commons-io:2.6")
  api("org.apache.commons:commons-text:1.6")
  api("com.google.code.gson:gson:2.8.5")
  api("com.linkedin.dextestparser:parser:2.3.4") {
      attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
      }
  }

  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.7")
  testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.7")
  testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.jar {
  manifest {
    attributes(mapOf("Main-Class" to application.mainClassName))
  }
}

tasks.test {
  useJUnitPlatform {
    includeEngines("spek2")
  }
}

tasks.register<Exec>("npmInstall") {
  setWorkingDir("../html-report")
  setCommandLine("npm", "install")
  inputs.file("../html-report/package.json")
  outputs.dir("../html-report/node_modules")
}

tasks.register<Exec>("npmRunBuild") {
  setWorkingDir("../html-report")
  setCommandLine("npm", "run", "build")
  inputs.apply {
    dir("../html-report/layout")
    dir("../html-report/src")
    dir("../html-report/styles")
    file("../html-report/.babelrc")
    file("../html-report/postcss.config.js")
    file("../html-report/webpack.config.dev.js")
    file("../html-report/webpack.config.js")
    file("../html-report/webpack.config.prod.js")
  }
  outputs.dir("../html-report/build")

  dependsOn("npmInstall")
}

tasks.named("processResources") {
  dependsOn("npmRunBuild")
}

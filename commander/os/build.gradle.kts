plugins {
    kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/publish.gradle")

ext["artifactId"] = "commander-os"
description = "Functions to work with processes and files on different Operating Systems."

dependencies {
    api( "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api( "io.reactivex.rxjava2:rxjava:2.2.10")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.5")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.5")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.test {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

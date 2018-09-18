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

dependencies {
    compile(project(":core"))
    compile("com.android.tools.build:gradle")
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

    inputs.files("gradle.properties")
    outputs.dir("$buildDir/tests")
}
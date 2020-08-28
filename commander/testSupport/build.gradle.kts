plugins {
    id("com.android.application")
}

android {
    val MIN_SDK: String by project
    val COMPILE_SDK: String by project

    compileSdkVersion(COMPILE_SDK.toInt())
    defaultConfig {
        applicationId = "com.trevjonez.commander.testsupport"
        minSdkVersion(MIN_SDK.toInt())
        targetSdkVersion(COMPILE_SDK.toInt())
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions("version")
    variantFilter {
        ignore = buildType.name == "release"
    }
    productFlavors {
        create("older") {
            dimension = "version"
        }
        create("newer") {
            dimension = "version"
            versionCode = 2
        }
    }
    sourceSets.named("main") {
        manifest.srcFile(file("AndroidManifest.xml"))
    }
}

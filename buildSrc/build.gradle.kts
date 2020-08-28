plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

repositories {
  jcenter()
  google()
}

gradlePlugin {
  plugins {
    create("adb-uninstall") {
      id = "com.trevjonez.adb-uninstall"
      implementationClass = "com.trevjonez.AdbUninstallPlugin"
    }
  }
}

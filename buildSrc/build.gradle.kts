import io.gitlab.arturbosch.detekt.detekt

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  id("io.gitlab.arturbosch.detekt").version("1.12.0")
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

dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.12.0")
}

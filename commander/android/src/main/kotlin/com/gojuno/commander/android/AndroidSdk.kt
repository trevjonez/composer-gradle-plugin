package com.gojuno.commander.android

import com.gojuno.commander.os.log
import java.io.File

val androidSdkRoot by lazy {
  val sdkRoot = System.getenv("ANDROID_SDK_ROOT")
  val value = if (sdkRoot == null) {
    log("ANDROID_SDK_ROOT not defined, checking for ANDROID_HOME")
    val andHome = System.getenv("ANDROID_HOME")
    if (andHome?.isNotBlank() == true) {
      log(
          """
          ANDROID_HOME was used as a fallback. 
          Please be advised that it is deprecated.
          For more information see: 
            https://developer.android.com/studio/command-line/variables
          """.trimIndent()
      )
      andHome
    } else null
  } else sdkRoot

  checkNotNull(value) { "Please specify ANDROID_SDK_ROOT env variable" }

  File(value)
}

val buildToolsDir by lazy {
  File(androidSdkRoot, "build-tools")
}

val latestBuildTools by lazy {
  requireNotNull(buildToolsDir.listFiles()?.sortedArray()?.lastOrNull()) {
    "Unable to find latest build-tools installation in ${buildToolsDir.absolutePath}"
  }
}

val aapt by lazy {
  File(latestBuildTools, "aapt")
}

val platformToolsDir by lazy {
  File(androidSdkRoot, "platform-tools")
}

val adb by lazy {
  File(platformToolsDir, "adb")
}

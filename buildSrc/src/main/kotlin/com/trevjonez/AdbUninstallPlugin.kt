/*
 *    Copyright 2020 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.trevjonez

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

abstract class AdbUninstallPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    val extension = target.extensions.create<AdbUninstallExtension>("adbUninstall")

    val lifecycleTask = target.tasks.register("adbUninstallAll") {
      description = "Run all adb uninstall tasks"
      group = "Adb Uninstall"
    }

    target.afterEvaluate {
      extension.packageNames.orNull.orEmpty().forEach { pkgName ->
        val nameSuffix = pkgName.split('.')
            .joinToString(separator = "") { it.capitalize() }

        val uninstall = tasks.register<RemoveApkTask>("adbUninstall$nameSuffix") {
          packageName.set(pkgName)
          description = "Uninstall '$pkgName'"
          group = "Adb Uninstall"
        }

        lifecycleTask.configure {
          dependsOn(uninstall)
        }
      }
    }
  }
}

abstract class AdbUninstallExtension {
  abstract val packageNames: SetProperty<String>

  fun packages(vararg packages: String) {
    packageNames.addAll(packages.toList())
  }
}

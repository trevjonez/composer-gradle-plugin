/*
 *    Copyright 2018 Trevor Jones
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

package com.trevjonez.composer.internal

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.trevjonez.composer.ComposerTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

class ComposerApplicationPlugin : ComposerBasePlugin<ApplicationVariant>() {
  private val androidExtension by lazy(LazyThreadSafetyMode.NONE) {
    requireNotNull(project.findExtension<BaseAppModuleExtension>("android")) {
      "Failed to find android application extension"
    }
  }

  private val androidTestUtil by lazy(LazyThreadSafetyMode.NONE) {
    project.configurations.findByName("androidTestUtil")
  }

  override val sdkDir: File
    get() = androidExtension.sdkDirectory

  override val testableVariants: DomainObjectCollection<ApplicationVariant>
    get() = androidExtension.applicationVariants

  override fun ApplicationVariant.getApk(task: ComposerTask): Provider<RegularFile> {
    task.dependsOn(assembleProvider)
    return project.layout.file(project.provider {
      outputs.single().outputFile
    })
  }

  override fun ApplicationVariant.getTestApk(task: ComposerTask): Provider<RegularFile> {
    task.dependsOn(testVariant.assembleProvider)
    return project.layout.file(project.provider {
      testVariant.outputs.single().outputFile
    })
  }

  override fun ApplicationVariant.getExtraApks(task: ComposerTask): ConfigurableFileCollection {
    return project.objects.fileCollection().also {
      it.from(project.provider {
        androidTestUtil?.resolvedConfiguration?.files?.toList().orEmpty()
      })
    }
  }

  override fun ApplicationVariant.getMultiApks(task: ComposerTask): ConfigurableFileCollection {
    return project.objects.fileCollection()
  }
}

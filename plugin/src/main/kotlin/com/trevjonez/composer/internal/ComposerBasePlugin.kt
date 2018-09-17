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

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.trevjonez.composer.ComposerConfig
import com.trevjonez.composer.ComposerTask
import com.trevjonez.composer.ConfigExtension
import com.trevjonez.composer.ConfiguratorDomainObj
import com.trevjonez.composer.composerConfig
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File

abstract class ComposerBasePlugin<T> : Plugin<Project>
    where T : BaseVariant, T : TestedVariant {

  abstract val sdkDir: File
  abstract val testableVariants: DomainObjectCollection<T>

  lateinit var project: Project
  lateinit var config: ConfigExtension

  override fun apply(target: Project) {
    this.project = target
    target.composerConfig()
    config = target.extensions.create(ConfigExtension.DEFAULT_NAME,
                                      ConfigExtension::class.java,
                                      target)
    target.afterEvaluate { observeVariants() }
  }

  private fun observeVariants() {
    testableVariants.all {
      if (config.variants.isEmpty() || config.variants.contains(name)) {
        if (testVariant == null) return@all

        val configurator: ConfiguratorDomainObj? =
          config.configs.findByName(name)

        project.tasks.register(
            "test${name.capitalize()}Composer", ComposerTask::class.java
        ) {
          description = "Run composer for $name variant"
          apk.set(getApk(configurator).singleFile)
          testApk.set(getTestApk(configurator).singleFile)
        }
      }
    }
  }

  private fun T.getApk(configurator: ConfiguratorDomainObj?): FileCollection {
    return if (configurator != null && configurator.apk.isPresent) {
      project.files(configurator.apk)
    } else {
      apkForVariant(this)
    }
  }

  private fun T.getTestApk(configurator: ConfiguratorDomainObj?): FileCollection {
    return if (configurator != null && configurator.testApk.isPresent) {
      project.files(configurator.testApk)
    } else {
      testApkForVariant(this)
    }
  }

  private fun apkForVariant(variant: T): FileCollection {
    return project.files().apply {
      variant.outputs.all {
        builtBy(assemble)
        from(outputFile)
      }
    }
  }

  private fun testApkForVariant(variant: T): FileCollection {
    return project.files().apply {
      builtBy(variant.assemble)
      variant.testVariant.outputs.all {
        builtBy(assemble)
        from(outputFile)
      }
    }
  }
}
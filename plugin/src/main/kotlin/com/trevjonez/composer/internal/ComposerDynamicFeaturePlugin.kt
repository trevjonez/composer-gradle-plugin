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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.trevjonez.composer.ComposerTask
import com.trevjonez.composer.withIssuePrompt
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

class ComposerDynamicFeaturePlugin : ComposerBasePlugin<ApplicationVariant>() {
  private val androidExtension by lazy(LazyThreadSafetyMode.NONE) {
    requireNotNull(project.findExtension<AppExtension>("android")) {
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
    androidTestUtil?.let { task.dependsOn(it) }
    return project.objects.fileCollection().also {
      it.from(project.provider {
        androidTestUtil?.resolvedConfiguration?.files?.toList().orEmpty()
      })
    }
  }

  override fun ApplicationVariant.getMultiApks(task: ComposerTask): ConfigurableFileCollection {
    val consumers = findApplicationProjects().thatUseThisFeature()
    if (consumers.isEmpty()) {
      throw UnsupportedOperationException(
        "Unable to find base module for feature module".withIssuePrompt())
    }
    if (consumers.size > 1) {
      throw UnsupportedOperationException(
        "Multiple consuming base modules is not supported".withIssuePrompt())
    }
    val baseModule = consumers.single()
    val matchedBaseVariant = requireNotNull(baseModule.findExtension<BaseAppModuleExtension>("android")) {
      "Failed to find base module android application extension"
    }.findMatchingVariant(this)

    val featureApk = getApk(task)
    task.dependsOn(matchedBaseVariant.assembleProvider)
    return project.objects.fileCollection().also {
      it.from(project.provider {
        val baseAssembleProvider = baseModule.layout.file(baseModule.provider {
          matchedBaseVariant.outputs.single().outputFile
        })
        listOf(featureApk, baseAssembleProvider)
      })
    }
  }

  private fun findApplicationProjects(): List<Project> {
    return project.rootProject.allprojects.filter {
      it.pluginManager.hasPlugin("com.android.application")
    }
  }

  private fun List<Project>.thatUseThisFeature() = filter { it.usesThisFeature() }

  private fun Project.usesThisFeature(): Boolean {
    val appExtension = findExtension<BaseAppModuleExtension>("android")
    val features = appExtension?.dynamicFeatures
    return features?.contains(this@ComposerDynamicFeaturePlugin.project.path) == true
  }

  private fun BaseAppModuleExtension.findMatchingVariant(featureVariant: ApplicationVariant): ApplicationVariant {
    val withMatchingType = applicationVariants.filter { it hasTheSameBuildTypeNameAs featureVariant }
    if (withMatchingType.size == 1) return withMatchingType.single()

    val withMatchingFlavors = applicationVariants.filter { it hasTheSameFlavorsAs featureVariant }

    val perfectOverlaps = withMatchingType.intersect(withMatchingFlavors)
    if (perfectOverlaps.size == 1) return perfectOverlaps.single()

    throw UnsupportedOperationException(
        """Unable to select the correct base module variant for ${featureVariant.name}
      |from: ${applicationVariants.joinToString(prefix = "[", postfix = "]") { "`${it.name}`" }}
    """.trimMargin().withIssuePrompt())
  }

  private infix fun BaseVariant.hasTheSameBuildTypeNameAs(other: BaseVariant): Boolean {
    return this.buildType.name == other.buildType.name
  }

  private infix fun BaseVariant.hasTheSameFlavorsAs(other: BaseVariant): Boolean {
    return this.productFlavors.map { it.name }.sorted() ==
        other.productFlavors.map { it.name }.sorted()
  }
}

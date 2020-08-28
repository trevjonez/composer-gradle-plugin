package com.trevjonez.composer.internal

import com.android.build.gradle.AppExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.ApplicationVariant
import com.trevjonez.composer.ComposerTask
import org.gradle.api.DomainObjectCollection
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

class ComposerTestPlugin : ComposerBasePlugin<ApplicationVariant>() {
  private val testExtension by lazy(LazyThreadSafetyMode.NONE) {
    requireNotNull(project.findExtension<TestExtension>("android")) {
      "Failed to find android test extension"
    }
  }

  private val appExtension by lazy(LazyThreadSafetyMode.NONE) {
    val targetPath = requireNotNull(testExtension.targetProjectPath) {
      "Target project not set"
    }
    val project = requireNotNull(project.findProject(targetPath)) {
      "Failed to find target project ${testExtension.targetProjectPath}"
    }
    requireNotNull(project.findExtension<AppExtension>("android")) {
      "Failed to find android app extension"
    }
  }

  private val androidTestUtil by lazy(LazyThreadSafetyMode.NONE) {
    project.configurations.findByName("androidTestUtil")
  }

  override val sdkDir: File
    get() = testExtension.sdkDirectory

  override val testableVariants: DomainObjectCollection<ApplicationVariant>
    get() = testExtension.applicationVariants

  override fun ApplicationVariant.getApk(task: ComposerTask): Provider<RegularFile> {
    val variant = appExtension.applicationVariants.find { it.name == name }
      ?: error("Failed to find application variant")

    task.dependsOn(variant.assembleProvider)
    return project.layout.file(project.provider {
      variant.outputs.single().outputFile
    })
  }

  override fun ApplicationVariant.getTestApk(task: ComposerTask): Provider<RegularFile> {
    task.dependsOn(assembleProvider)
    return project.layout.file(project.provider {
      outputs.single().outputFile
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

  override fun ApplicationVariant.isTestable(): Boolean {
    return true
  }
}

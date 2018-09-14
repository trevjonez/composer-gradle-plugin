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

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.DomainObjectCollection
import java.io.File

class ComposerLibraryPlugin: ComposerBasePlugin<LibraryVariant>() {
    private val androidExtension by lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(project.extensions.findByName("android") as? LibraryExtension) {
            "Failed to find android library extension"
        }
    }

    override val sdkDir: File
        get() = androidExtension.sdkDirectory

    override val testableVariants: DomainObjectCollection<LibraryVariant>
        get() = androidExtension.libraryVariants
}
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

package com.trevjonez.composer

import com.trevjonez.composer.ComposerConfig.ARTIFACT_DEP
import com.trevjonez.composer.ComposerConfig.COMPOSER
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

object ComposerConfig {
    const val MAIN_CLASS = "com.gojuno.composer.MainKt"
    const val COMPOSER = "composer"
    const val COMPOSER_VER = "0.3.3"
    const val ARTIFACT_DEP = "com.gojuno.composer:composer:$COMPOSER_VER"
    const val DEFAULT_OUTPUT_DIR = "composer-output"
}

fun Project.composerConfig(): Configuration {
    val defaultComposerArtifact = dependencies.create(ARTIFACT_DEP)
    return configurations.findByName(COMPOSER)
           ?: configurations.create(COMPOSER) {
               defaultDependencies {
                   add(defaultComposerArtifact)
               }
           }
}

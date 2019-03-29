/*
 *    Copyright 2019 Trevor Jones
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

import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface ComposerConfigurator: ComposerDsl {
    val configuration: Configuration

    val withOrchestrator: Property<Boolean>
    val shard: Property<Boolean>
    val instrumentationArguments: ListProperty<Pair<String, String>>
    val verboseOutput: Property<Boolean>
    val devices: ListProperty<String>
    val devicePattern: Property<String>
    val keepOutput: Property<Boolean>
    val apkInstallTimeout: Property<Int>
}

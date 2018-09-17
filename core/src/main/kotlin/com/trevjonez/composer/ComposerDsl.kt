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

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

/**
 * Global configuration options
 */
interface ComposerDsl {
    /**
     * @param value evaluated as per [org.gradle.api.provider.Property.set].
     */
    fun shard(value: Any)

    /**
     * Add one instrumentation argument to the configuration.
     *
     * @param value Accepts the following argument types:
     * - [org.gradle.api.provider.Provider]
     * of type [Pair] of [String]'s
     *
     * - [Pair] of [String]'s
     */
    fun instrumentationArgument(value: Any)

    /**
     * Add one instrumentation argument to the configuration.
     * Same as [instrumentationArgument] with single pair argument
     *
     * @param key converted via [Any.toString]
     * @param value converted via [Any.toString]
     */
    fun instrumentationArgument(key: CharSequence, value: CharSequence)


    /**
     * Add many instrumentation arguments to the configuration.
     *
     * @param value Accepts the following argument types:
     * - [org.gradle.api.provider.Provider]
     * of type [Iterable] of [Pair] of [String]'s
     *
     * - [Iterable] of [Pair] of [String]'s
     */
    fun instrumentationArguments(value: Any)

    /**
     * @param value evaluated as per [org.gradle.api.provider.Property.set].
     */
    fun verboseOutput(value: Any)

    /**
     * Add one device ID to the configuration.
     *
     * @param value Accepts the following argument types:
     * - [org.gradle.api.provider.Provider] of type [String]
     *
     * - [String]
     */
    fun device(value: Any)

    /**
     * Add one or many device ID's to the configuration.
     *
     * @param value Accepts the following argument types:
     * - [org.gradle.api.provider.Provider] of type [Iterable] of [String]
     *
     * - [Iterable] of [String]
     */
    fun devices(value: Any)

    /**
     * @param value evaluated as per [org.gradle.api.provider.Property.set].
     */
    fun devicePattern(value: Any)

    /**
     * @param value evaluated as per [org.gradle.api.provider.Property.set].
     */
    fun keepOutput(value: Any)

    /**
     * @param value evaluated as per [org.gradle.api.provider.Property.set].
     */
    fun apkInstallTimeout(value: Any)
}

/**
 *
 */
interface ComposerTaskDsl : ComposerDsl {

    val testApk: RegularFileProperty
    val apk: RegularFileProperty
    val outputDir: DirectoryProperty

    /**
     * @param path evaluated as per [org.gradle.api.Project.file].
     */
    fun testApk(path: Any)

    /**
     * @param path evaluated as per [org.gradle.api.Project.file].
     */
    fun apk(path: Any)

    /**
     * @param path evaluated as per [org.gradle.api.Project.file].
     */
    fun outputDirectory(path: Any)
}
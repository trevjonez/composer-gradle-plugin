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

import org.gradle.api.Project
import org.gradle.api.internal.provider.DefaultPropertyState
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.PropertyState
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.property

/**
 * This is using internal API.
 * Probably shouldn't but need better optional prop support on primitives
 */
inline fun <reified T> Project.emptyProperty(): Property<T> {
  return DefaultPropertyState<T>(T::class.java)
}

inline val <T> ListProperty<T>.orEmpty: List<T>
  get() {
    return orNull ?: emptyList()
  }

inline fun <reified T> Property<T>.eval(value: Any) {
  @Suppress("UNCHECKED_CAST")
  when (value) {
    is T -> set(value)
    is Provider<*> -> set(value as Provider<out T>)
    else ->
      throw IllegalArgumentException("Unsupported type: ${value.javaClass}, expecting ${T::class.java} or ${Provider::class.java}")
  }
}

inline fun <reified T> ListProperty<T>.eval(value: Any) {
  @Suppress("UNCHECKED_CAST")
  when (value) {
    is T -> add(value)
    is Provider<*> -> add(value as Provider<out T>)
    else ->
      throw IllegalArgumentException("Unsupported type: ${value.javaClass}, expecting ${T::class.java} or ${Provider::class.java}")
  }
}

inline fun <reified T> ListProperty<T>.evalAll(value: Any) {
  @Suppress("UNCHECKED_CAST")
  when (value) {
    is Iterable<*> -> addAll(value as Iterable<T>)
    is Provider<*> -> addAll(value as Provider<out Iterable<T>>)
    else ->
      throw IllegalArgumentException("Unsupported type: ${value.javaClass}, expecting ${Iterable::class.java} or ${Provider::class.java}")
  }
}
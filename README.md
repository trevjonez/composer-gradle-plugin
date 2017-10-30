composer-gradle-plugin
====
[![](https://jitpack.io/v/trevjonez/composer-gradle-plugin.svg)](https://jitpack.io/#trevjonez/composer-gradle-plugin)

Gradle task type and plugin for running [gojuno/composer](https://github.com/gojuno/composer) from gradle.

Installation & Usage
--------------------
In the appropriate `build.gradle` file add the jitpack repository and classpath dependency.
```groovy
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.trevjonez.composer-gradle-plugin:plugin:0.4.1'
    }
}
```

This can be consumed in two ways, via a android variant aware plugin or as a pre-provided task type.

Plugin
----
```groovy
apply plugin: 'composer'
```
The above should be all you need to get started and will create a task for each testable variant in the project.

The tasks that are created will be of the form `testFlavorTypeComposer`

If you want to limit the variants that get tasks or provide custom configuration you can do so via the `composer` DSL block:
```groovy
composer {
  variants "redDebug" // optional, variant names to create composer tasks for. If empty all testable variants will receive a task.
  instrumentationArgument('key1', 'value1') //optional. args that apply to all created tasks
  instrumentationArgument('key2', 'value2')
  instrumentationArgument('keyN', 'valueN')

  configs { 
    redDebug {
      apk 'app/build/outputs/apk/example-debug.apk' //optional override
      testApk 'build/outputs/apk/example-debug-androidTest.apk' //optional override
      testPackage 'com.example.test' //optional override
      testRunner 'com.example.test.ExampleTestRunner' //optional override
      shard true //optional. default true
      outputDirectory 'artifacts/composer-output' //optional override. default 'build/reports/composer/redDebug'
      instrumentationArgument('key1', 'value1') //optional
      instrumentationArgument('key2', 'value2')
      instrumentationArgument('keyN', 'valueN')
      verboseOutput false //optional default false
      device 'emulator-5554' //optional, additive
      device 'emulator-5558'
      devices('emulator-5554', 'emulator-5558') //optional, additive
      devicePattern 'somePattern' //optional
    }
  }
}
```

Core
----
Manual task creation looks something like this:
```groovy
task customTaskName(type: ComposerTask) {
  apk 'app/build/outputs/apk/example-debug.apk' //required
  testApk 'build/outputs/apk/example-debug-androidTest.apk' //required
  testPackage 'com.example.test' //required
  testRunner 'com.example.test.ExampleTestRunner' //required
  shard true //optional
  outputDirectory 'artifacts/composer-output' //optional
  instrumentationArgument('key1', 'value1') //optional
  instrumentationArgument('key2', 'value2')
  instrumentationArgument('keyN', 'valueN')
  verboseOutput false //optional
  device 'emulator-5554' //optional
  device 'emulator-5558'
  devices('emulator-5554', 'emulator-5558') //optional
  devicePattern 'somePattern' //optional
}
```

Dependency Configuration
----
If you need to use a different version of the composer jar than this plugin uses by default, you can modify the composer configuration with normal gradle strategies.
The `composer` configuration is added to your project once a `ComposerTask` has been created or the plugin has been applied to the project.

```groovy
dependencies {
 composer "com.gojuno.composer:composer:0.2.7"
}
```

Disclaimer
----
So far this plugin has had light use in the wild and the test cases only verify that composer is ran.
Please give any feedback possible if you have issues so I can make things work as expected.

Notes on Compatibility
----

The plugin is developed against specific version of gradle and the android gradle plugin.
In most cases using the latest version of gradle is safe but the minimum supported 
version of gradle is 4.0 or whatever minimum is mandated by the android gradle plugin. 

Composer plugin version | Gradle version | Android plugin version
----- | ---- | -----
0.4.1 | 4.2.1  | 3.0.0 

License
-------
    Copyright 2017 Trevor Jones

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
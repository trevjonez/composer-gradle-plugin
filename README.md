composer-gradle-plugin [![](https://jitpack.io/v/trevjonez/composer-gradle-plugin.svg)](https://jitpack.io/#trevjonez/composer-gradle-plugin)
====
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
        classpath 'com.github.trevjonez.composer-gradle-plugin:plugin:0.1.2'
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
}
```

Dependency Configuration
----
If you need to use a different version of the composer jar than this plugin uses by default, you can modify the composer configuration with normal gradle strategies.
The `composer` configuration is added to your project once a `ComposerTask` has been created or the plugin has been applied to the project.

```groovy
dependencies {
 composer "com.gojuno.composer:composer:0.2.3"
}
```

Disclaimer
----
I have not used this thing in the real world yet and the test cases only verify that composer is ran.
Please give any feedback possible if you have issues so I can make things work as expected.

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
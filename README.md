# composer-gradle-plugin

[![](https://jitpack.io/v/trevjonez/composer-gradle-plugin.svg)](https://jitpack.io/#trevjonez/composer-gradle-plugin)

Gradle task type and plugin for running [gojuno/composer](https://github.com/gojuno/composer) from gradle.

## Installation & Usage

In the appropriate `build.gradle` file add the jitpack repository and classpath dependency.
```groovy
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.trevjonez.composer-gradle-plugin:plugin:0.8.0'
    }
}
```

This repo can be consumed in two ways, via a android variant aware plugin or as a pre-provided task type.

##### Plugin Usage

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
      apk "build/outputs/apk/debug/example-debug.apk" //optional override, string paths are evaluated as per {@link org.gradle.api.Project#file(Object)}.
      testApk = new File(buildDir, "outputs/apk/androidTest/debug/example-debug-androidTest.apk") //optional override
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
      apkInstallTimeout 90 //optional, timeout in seconds default 120
    }
  }
}
```

##### Core Usage

Manual task creation looks something like this:
```groovy
task customTaskName(type: ComposerTask) {
  apk "build/outputs/apk/example-debug.apk" //required
  testApk "build/outputs/apk/example-debug-androidTest.apk" //required
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
  apkInstallTimeout 90 //optional
}
```

## Advanced Configuration

#### Instrumentation Arguments

In some cases you may want to only run a sub set of tests or filter based on annotations etc.
Composer already supports passing custom arguments to the instrumentation runner 
and the configuration DSL for both core and plugin use cases can take advantage of that.

Lets say you want to add the ability to pass gradle a property for a class name to run:

```groovy
composer {
  if(project.hasProperty("composerClassTarget")) {
    instrumentationArgument('class', project.getProperty("composerClassTarget"))
  }
}
```

Invocation should look something like this:
```bash
./gradlew app:testCiDebugComposer -PcomposerClassTarget=com.foo.your.test.FullClassName
``` 

Another common use case is to always have the test runner ignore tests that should not be
ran by handled by composer. An example of this would be to ignore [Kontrast screenshot tests:](https://github.com/trevjonez/Kontrast)

```groovy
composer {
  instrumentationArgument('notAnnotation', 'com.trevjonez.kontrast.KontrastTest')
}
```

As always I recommend you read the wealth of information available on [d.android.com](https://developer.android.com/).

Or specifically the [documentation for `InstrumentationTestRunner`](https://developer.android.com/reference/android/test/InstrumentationTestRunner)    

#### Dependency Configuration

If you need to use a different version of the composer jar than this plugin uses by default, 
you can modify the composer configuration with normal gradle strategies.

The `composer` configuration is automatically added to your project once a 
`ComposerTask` has been created or the plugin has been applied to the project.

```groovy
dependencies {
 composer "com.gojuno.composer:composer:0.3.3"
}
```

## Notes on Compatibility

The plugin is developed against specific version of gradle and the android gradle plugin.
In most cases using the latest version of gradle is safe but the minimum supported 
version of gradle is 4.0 or whatever minimum is mandated by the android gradle plugin. 

Composer plugin version | Gradle version | Android plugin version
----- | ---- | -----
0.8.0 | 4.10  | 3.1.4

## License

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

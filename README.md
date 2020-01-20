# composer-gradle-plugin

[ ![Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/trevjonez/composer/com.trevjonez.composer.gradle.plugin/maven-metadata.xml.svg?label=Plugin%20Portal) ](https://plugins.gradle.org/plugin/com.trevjonez.composer)
[ ![JCenter](https://api.bintray.com/packages/trevorjones141/maven/composer/images/download.svg) ](https://bintray.com/trevorjones141/maven/composer/_latestVersion)

Gradle task type and plugin for running [gojuno/composer](https://github.com/gojuno/composer) from gradle.

## Project Status

After Juno was consumed by Lyft the upstream composer and commander projects 
became unmaintained as a result. While unfortunate that those projects have 
concluded I want to try and provide at least the same baseline of features.

To do so I will provide releases based on hard forks (new gradle modules here) 
of the upstream projects. The commander and composer artifacts will be published 
to jcenter and be used as the default dependency when applying the plugin to your projects.

Composer from this project will not be published as a fat jar for standalone consumption.

## Installation & Usage

##### Via Gradle Plugin Portal:

The plugin is available via the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.trevjonez.composer)

```groovy
plugins {
  id "com.trevjonez.composer" version "$version"
}
```

or

```groovy
buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath "com.trevjonez.composer:plugin:$version"
  }
}

apply plugin: "com.trevjonez.composer"
```

This repo can be consumed in two ways, via the android variant aware plugin or as a pre-provided task type.

##### Plugin Usage

```groovy
apply plugin: 'com.trevjonez.composer'
```
The above should be all you need to get started and will create a task for each testable variant in the project.

The tasks that are created will be of the form `testFlavorTypeComposer`

If you want to limit the variants that get tasks or provide custom configuration you can do so via the `composer` DSL block:
```groovy
composer {
  variants "redDebug" // optional, variant names to create composer tasks for. If empty all testable variants will receive a task.
  
  //These dsl functions are combined with variant specific config additively
  instrumentationArgument('key1', 'value1') 
  instrumentationArgument('key2', 'value2')
  instrumentationArgument('keyN', 'valueN')
  
  device 'emulator-5558'
  devices(['emulator-5558', 'emulator-5559'])
      
  //These dsl functions are overwritten by variant specific config if any exists
  withOrchestrator true
  shard false
  verboseOutput false
  keepOutput true
  devicePattern 'somePattern'
  apkInstallTimeout 90
 

  configs { 
    redDebug {
      apk file("build/outputs/apk/debug/example-debug.apk") //optional override
      testApk = new File(buildDir, "outputs/apk/androidTest/debug/example-debug-androidTest.apk") //optional override
      outputDirectory 'artifacts/composer-output' //optional override. default 'build/reports/composer/redDebug'
      withOrchestrator false // optional, default false
      shard true //optional. default true
      instrumentationArgument('key1', 'value1') //optional, additive
      instrumentationArgument('key2', 'value2')
      instrumentationArgument('keyN', 'valueN')
      verboseOutput false //optional default false
      keepOutput true //optional, default false
      device 'emulator-5554' //optional, additive
      device 'emulator-5558'
      devices(['emulator-5554', 'emulator-5558']) //optional, additive
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
  apk file("build/outputs/apk/example-debug.apk") //required
  testApk file("build/outputs/apk/example-debug-androidTest.apk") //required
  withOrchestrator true // optional
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
and the configuration DSL for both manually registered tasks and the plugin use cases can take advantage of that.

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

When you use it with Orchestrator you may want to enable clearPackageData, you can do it adding an 
instrumentationArgument like this: 
```groovy
composer {
  withOrchestrator true
  instrumentationArgument("clearPackageData","true")
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
 composer "com.trevjonez.composer:composer:$version" //This is the default dependency path used.
}
```

The default is added via the [Configuration.defaultDependencies api](https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/Configuration.html#defaultDependencies-org.gradle.api.Action-) meaning any dependency changes will result in the default not being added to the configuration.

## Notes on Compatibility

The plugin is developed against specific version of gradle and the android gradle plugin.
In most cases using the latest version of gradle and agp is safe but the minimum supported 
version of gradle is 5.0 or higher if mandated by the android gradle plugin. 

Composer plugin version | Gradle version | Android plugin version
| ------     | ------             | ------ |
| 0.10.0     | 5.0, 5.1, 5.1.1    | 3.3.0, 3.4.0-beta01<sup>\*</sup>, 3.5.0-alpha01<sup>\*</sup> |
| 0.11.*     | 5.3.1              | 3.3.2, 3.5.0-alpha09<sup>\*</sup> |
| 0.12.0     | 5.4.1              | 3.4.1, 3.5.0-beta03<sup>\*</sup>, 3.6.0-alpha02<sup>\*</sup> |
| 0.13.0     | 5.6                | 3.4.2, 3.5.0-rc01<sup>\*</sup>, 3.6.0-alpha05<sup>\*</sup> |
| 1.0.0-rc04 | 5.6.4              | 3.5.3, 3.6.0-rc01<sup>\*</sup> |

\* Alpha, Beta and RC versions of the android plugin are quickly tested by building against them.
This usually means the published composer plugin will work with those version 
however the lite smoke testing done will not find binary incompatibilities.

## License

    Copyright 2019 Trevor Jones

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

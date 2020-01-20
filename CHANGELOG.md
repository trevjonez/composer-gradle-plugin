# Changelist
Items listed here may not be exhaustive, if you are seeing issues, check the git commits for more specific change information &| open an issue.

## 1.0.0-rc04
Force process death on unsubscribe for processes redirecting to kept files. (logcat & instrumentation)
This should resolve issues with files continuing to change after gradle tasks have completing and breaks gradle caching.

## 1.0.0-rc03
Fix deadlocking in device install/test stream when using device locks.
Only invoke adb install for unique APK's

## 1.0.0-rc2
Add device locks for commander/composer device observables.
Add dynamic feature module support to the plugin (via application plugin impl)

## 1.0.0-rc1
Republish commander and composer from this repo. based on my changes here:
 https://github.com/gojuno/commander/pull/28
 https://github.com/gojuno/composer/pull/172

## 0.13.1
Bug fix for #54 don't force task dependency. Thanks for the find and fix [@dkostyrev](https://github.com/dkostyrev)
 
## 0.13.0
Requires gradle 5.6
Use of internal gradle API's broke the plugin, this refactors to use the automatic instantiation features of gradle.
Breaking changes of DSL for apk, testApk, outputDirectory. Error messages will prompt with fixes. 

## 0.12.0
Gradle cache support thanks to [@CristianGM](https://github.com/CristianGM) via #05fa40275bf3958495a1d1f5207e8acd23279e6a (pending gradle 5.6?)

## 0.11.1
"--with-orchestrator" arity consistency fix, thank you for the find and fix. [@JKMirko](https://github.com/JKMirko)

## 0.11.0
Mostly bug fixes and quality of life items.

**BREAKING CHANGE**: plugin id changed from `composer` to `com.trevjonez.composer` in order to be gradle plugin portal compliant.
 
 - update to Composer 0.6.0 (androidx orchestrator support) 0.5 -> 0.6 has cli breaking change so you must be at 0.6 or greater.
 - possible breaking change: core and plugin modules were merged to plugin to be plugin portal compliant
 - target latest gradle version: 5.3.1
 
## 0.10.0
Gradle 5.0 support

## 0.9.0
Support Orchestrator
 - update to Composer 0.5.0
 - add withOrchestrator
 - install APKs declared on AndroidTestUtils

## 0.8.1
Regression fix for ANDROID_HOME being set on ComposerTask instances that are created via plugin.

## 0.8.0
Large implementation overhaul:
- User facing plugin delegates to AGP specific plugin.
- Support for android library module integration.
- Default output directory has changed to use the `dirName` provided on android variant objects.
- DSL api's have been rewritten. *Should* be source compatible.
- The composer dsl block now has more global options. See readme for details specific behaviors

## 0.7.0
Composer gradle plugin requires gradle 4.10 or newer:
- Use new lazy task registration/configuration API to minimize config time overhead.

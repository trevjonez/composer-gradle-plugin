# Changelist
Items listed here may not be exhaustive, if you are seeing issues, check the git commits for more specific change information &| open an issue.

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

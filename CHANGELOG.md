# Changelist
Items listed here may not be exhaustive, if you are seeing issues, check the git commits for more specific change information &| open an issue.

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

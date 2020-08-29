package com.gojuno.composer

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import java.util.concurrent.TimeUnit

data class Args(
    @Parameter(
        names = ["--apk"],
        required = true,
        description = "Path to application apk that needs to be tested.",
        order = 0
    )
    var appApkPath: String = "",

    @Parameter(
        names = ["--test-apk"],
        required = true,
        description = "Path to apk with tests.",
        order = 1
    )
    var testApkPath: String = "",

    @Parameter(
        names = ["--test-runner"],
        required = false,
        description = "Fully qualified name of test runner class you're using. " +
                      "Will be parsed from test APK if not specified explicitly.",
        order = 2
    )
    var testRunner: String = "",

    @Parameter(
        names = ["--shard"],
        required = false,
        arity = 1,
        description = "Either `true` or `false` to enable/disable test " +
                      "sharding which runs tests in parallel on available " +
                      "devices/emulators. `true` by default.",
        order = 3
    )
    var shard: Boolean = true,

    @Parameter(
        names = ["--output-directory"],
        required = false,
        description = "Either relative or absolute path to directory for " +
                      "output: reports, files from devices and so on. " +
                      "`composer-output` by default.",
        order = 4
    )
    var outputDirectory: String = "composer-output",

    @Parameter(
        names = ["--instrumentation-arguments"],
        required = false,
        variableArity = true,
        description = "Key-value pairs to pass to Instrumentation Runner. " +
                      "Usage example: `--instrumentation-arguments myKey1 myValue1 " +
                      "myKey2 myValue2`.",
        listConverter = InstrumentationArgumentsConverter::class,
        order = 5
    )
    var instrumentationArguments: List<String> = listOf(),

    @Parameter(
        names = ["--verbose-output"],
        required = false,
        arity = 1,
        description = "Either `true` or `false` to enable/disable verbose " +
                      "output for Composer. `false` by default.",
        order = 6
    )
    var verboseOutput: Boolean = false,

    @Parameter(
        names = ["--keep-output-on-exit"],
        required = false,
        description = "Either `true` or `false` to keep/clean output on exit. " +
                      "`false` by default.",
        order = 7
    )
    var keepOutputOnExit: Boolean = false,

    @Parameter(
        names = ["--devices"],
        required = false,
        variableArity = true,
        description = "Connected devices/emulators that will be used to run " +
                      "tests against. If not passed — tests will run on all " +
                      "connected devices/emulators. Specifying both `--devices` " +
                      "and `--device-pattern` will result in an error. " +
                      "Usage example: `--devices emulator-5554 emulator-5556`.",
        order = 8
    )
    var devices: List<String> = emptyList(),

    @Parameter(
        names = ["--device-pattern"],
        required = false,
        description = "Connected devices/emulators that will be used to run " +
                      "tests against. If not passed — tests will run on all " +
                      "connected devices/emulators. Specifying both " +
                      "`--device-pattern` and `--devices` will result in an " +
                      "error. Usage example: `--device-pattern \"somePatterns\"`.",
        order = 9
    )
    var devicePattern: String = "",

    @Parameter(
        names = ["--install-timeout"],
        required = false,
        description = "APK installation timeout in seconds. If not passed " +
                      "defaults to 120 seconds (2 minutes). Applicable to both " +
                      "test APK and APK under test.",
        order = 10
    )
    var installTimeoutSeconds: Int = TimeUnit.MINUTES.toSeconds(2).toInt(),

    @Parameter(
        names = ["--fail-if-no-tests"],
        required = false,
        arity = 1,
        description = "Either `true` or `false` to enable/disable error on " +
                      "empty test suite. True by default.",
        order = 11
    )
    var failIfNoTests: Boolean = true,

    @Parameter(
        names = ["--with-orchestrator"],
        required = false,
        arity = 1,
        description = "Either `true` or `false` to enable/disable running " +
                      "tests via AndroidX Test Orchestrator. False by default.",
        order = 12
    )
    var runWithOrchestrator: Boolean = false,

    @Parameter(
        names = ["--extra-apks"],
        required = false,
        variableArity = true,
        description = "Extra APKs you would usually put on androidTestUtil",
        order = 13
    )
    var extraApks: List<String> = emptyList(),

    @Parameter(
        names = ["--multi-apks"],
        required = false,
        variableArity = true,
        description = "APKs that represent an application with dynamic features",
        order = 14
    )
    var multiApks: List<String> = emptyList()
)

// No way to share array both for runtime and annotation without reflection.
private val PARAMETER_HELP_NAMES = setOf("--help", "-help", "help", "-h")

private fun validateArguments(args: Args) {
  if (args.devicePattern.isNotEmpty() && args.devices.isNotEmpty()) {
    throw IllegalArgumentException(
        "Specifying both --devices and --device-pattern is prohibited."
    )
  }
}

fun parseArgs(rawArgs: Array<String>) = Args().also { args ->
  if (PARAMETER_HELP_NAMES.any { rawArgs.contains(it) }) {
    JCommander(args).usage()
    exit(Exit.Ok)
  }

  @Suppress("SpreadOperator")
  JCommander.newBuilder()
      .addObject(args)
      .build()
      .parse(*rawArgs)
  validateArguments(args)
}

private class InstrumentationArgumentsConverter : IStringConverter<List<String>> {
  override fun convert(argument: String): List<String> = listOf(argument)
}

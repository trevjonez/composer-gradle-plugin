@file:Suppress("unused")

package com.gojuno.commander.android

import com.gojuno.commander.os.Notification
import com.gojuno.commander.os.home
import com.gojuno.commander.os.log
import com.gojuno.commander.os.nanosToHumanReadableTime
import com.gojuno.commander.os.process
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.system.exitProcess

val androidHome: String by lazy { requireNotNull(System.getenv("ANDROID_HOME")) { "Please specify ANDROID_HOME env variable" } }
val adb: String by lazy { "$androidHome${File.separator}platform-tools${File.separator}adb" }
private val buildTools: String? by lazy {
    requireNotNull(File(androidHome, "build-tools")
                       .listFiles()
                       ?.sortedArray()
                       ?.lastOrNull()
                       ?.absolutePath) { "" }
}
val aapt: String by lazy {
    buildTools?.let { "$buildTools${File.separator}aapt" } ?: ""
}
val lockFileDir by lazy {
    File("$home${File.separator}.android${File.separator}commander")
}

internal fun Observable<Notification>.trimmedOutput() = this
    .ofType(Notification.Exit::class.java)
    .singleOrError()
    .map { it.output.readText().trim() }

fun AdbDevice.externalStorage(): Single<String> = process(listOf(adb, "-s", id, "shell", "printenv", "EXTERNAL_STORAGE"))
    .trimmedOutput()
    .doOnError { log("Could not get external storage of device $id, error = $it") }

fun AdbDevice.deviceModel(): Single<String> = process(listOf(adb, "-s", id, "shell", "getprop ro.product.model undefined"))
    .trimmedOutput()
    .doOnError { log("Could not get model name of device $id, error = $it") }

fun connectedAdbDevices(): Single<Set<AdbDevice>> = process(listOf(adb, "devices"), unbufferedOutput = true)
    .ofType(Notification.Exit::class.java)
    .map { it.output.readText() }
    .map {
        it.takeIf { it.contains("List of devices attached") }
                ?: throw IllegalStateException("Adb output is not correct: $it.")
    }
    .retry { retryCount, exception ->
        val shouldRetry = retryCount < 5 && exception is IllegalStateException
        if (shouldRetry) {
            log("connectedAdbDevices: retrying $exception.")
        }

        shouldRetry
    }
    .flatMapIterable { adbStdOut ->
        adbStdOut
            .substringAfter("List of devices attached")
            .split(System.lineSeparator())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { it.contains("online") || it.contains("device") }
    }
    .map { line ->
        val serial = line.substringBefore("\t")
        val online = when {
            line.contains("offline", ignoreCase = true) -> false
            line.contains("device", ignoreCase = true) -> true
            else -> throw IllegalStateException("Unknown adb output for device: $line")
        }
        AdbDevice(id = serial, online = online)
    }
    .flatMapSingle { device ->
        device.deviceModel().map { model ->
            device.copy(model = model)
        }
    }
    .toList()
    .map { it.toSet() }
    .doOnError { log("Error during getting connectedAdbDevices, error = $it") }

fun AdbDevice.log(message: String) = com.gojuno.commander.os.log("[$id] $message")

fun AdbDevice.installApk(pathToApk: String, timeout: Pair<Int, TimeUnit> = 2 to MINUTES, print: Boolean = false): Observable<Unit> {
    val installApk = process(
        commandAndArgs = listOf(adb, "-s", id, "install", "-r", pathToApk),
        timeout = timeout,
        print = print,
        unbufferedOutput = true
    )

    return Observable
        .fromCallable { System.nanoTime() }
        .flatMap { startTimeNanos -> installApk.ofType(Notification.Exit::class.java).map { it to startTimeNanos } }
        .map { (exit, startTimeNanos) ->
            val success = exit
                .output
                .readText()
                .split(System.lineSeparator())
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .firstOrNull { it.equals("Success", ignoreCase = true) } != null

            val duration = System.nanoTime() - startTimeNanos

            if (success)
                log("Successfully installed apk in ${duration.nanosToHumanReadableTime()}, pathToApk = $pathToApk")
            else {
                log("Failed to install apk $pathToApk")
                exitProcess(1)
            }
        }
        .doOnSubscribe { log("Installing apk... pathToApk = $pathToApk") }
        .doOnError { log("Error during installing apk: $it, pathToApk = $pathToApk") }
}

fun <T> Single<T>.acquireDeviceLock(adbDevice: AdbDevice): Single<T> {
    return this.compose { upstream ->
        Single.create<T> { emitter ->
            adbDevice.log("Acquiring lock")
            try {
                val startTime = System.currentTimeMillis()
                val waitLogging = Observable.interval(5L, 30L, SECONDS)
                    .subscribe {
                        adbDevice.log("Waiting for device to be available")
                        if (it == 1L) {
                            adbDevice.log("An early exit of composer can cause stale lock files to be life in place")
                            adbDevice.log("LockFile: ${adbDevice.lockFile.path}")
                        }
                    }

                val lock = adbDevice.lockFile.lock()!!
                waitLogging.dispose()
                adbDevice.log("Lock acquired")
                emitter.setDisposable(Disposables.fromAction {
                    waitLogging.dispose()
                    lock.close()
                    adbDevice.lockFile.delete()
                    adbDevice.log("Lock released")
                })
                upstream.subscribe(emitter::onSuccess, emitter::onError)
            } catch (error: Throwable) {
                emitter.onError(IllegalStateException("Device lock failed", error))
            }
        }
    }
}

private tailrec fun File.lock(): FileLock? {
    val lock = RandomAccessFile(this, "rws").channel.tryLock()
    if (lock == null) Thread.sleep(500L)
    return lock ?: lock()
}

fun AdbDevice.pullFolder(folderOnDevice: String, folderOnHostMachine: File, logErrors: Boolean, timeout: Pair<Int, TimeUnit> = 60 to SECONDS): Single<Boolean> {
    val pullFiles = process(
        commandAndArgs = listOf(adb, "-s", id, "pull", folderOnDevice, folderOnHostMachine.absolutePath),
        timeout = timeout,
        unbufferedOutput = true)

    return pullFiles
        .ofType(Notification.Exit::class.java)
        .retry(3)
        .doOnError { error -> if (logErrors) log("Failed to pull files from $folderOnDevice to $folderOnHostMachine: $error") }
        .map { true }
        .onErrorReturn { false }
        .singleOrError()
}

fun AdbDevice.deleteFolder(folderOnDevice: String, logErrors: Boolean, timeout: Pair<Int, TimeUnit> = 60 to SECONDS): Single<Boolean> {
    val deleteFolder = process(
        commandAndArgs = listOf(adb, "-s", id, "shell", "rm", "-r", folderOnDevice),
        timeout = timeout,
        unbufferedOutput = true)

    return deleteFolder
        .ofType(Notification.Exit::class.java)
        .retry(3)
        .doOnError { error -> if (logErrors) log("Failed to delete directory $folderOnDevice: $error") }
        .map { true }
        .onErrorReturn { false }
        .singleOrError()
}

fun AdbDevice.deleteFile(fileOnDevice: String, logErrors: Boolean, timeout: Pair<Int, TimeUnit> = 60 to SECONDS): Single<Boolean> {
    val deleteFile = process(
        commandAndArgs = listOf(adb, "-s", id, "shell", "rm", fileOnDevice),
        timeout = timeout,
        unbufferedOutput = true)

    return deleteFile
        .ofType(Notification.Exit::class.java)
        .retry(3)
        .doOnError { error -> if (logErrors) log("Failed to delete file $fileOnDevice: $error") }
        .map { true }
        .onErrorReturn { false }
        .singleOrError()
}

fun AdbDevice.redirectLogcatToFile(file: File): Observable<Process> =
    process(
        listOf(adb, "-s", id, "logcat"),
        redirectOutputTo = file,
        timeout = null,
        destroyOnUnsubscribe = true
    )
        .doOnSubscribe { file.parentFile.mkdirs() }
        .ofType(Notification.Start::class.java)
        .doOnError { this.log("Error during redirecting logcat to file $file, error = $it") }
        .map { it.process }

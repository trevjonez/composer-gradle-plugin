package com.gojuno.commander.android

import com.gojuno.commander.os.Notification
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.channels.OverlappingFileLockException
import java.util.concurrent.TimeUnit.SECONDS

class AdbSpec : Spek({

                         describe("process exit notification output") {

                             val file = createTempFile().apply {
                                 deleteOnExit()
                                 writeText("\ttest \n")
                             }

                             it("is trimmed") {
                                 Observable.just<Notification>(Notification.Exit(file))
                                         .trimmedOutput()
                                         .test()
                                         .assertValue("test")
                             }

                         }

                         describe("system level device lock") {
                             var deviceStream: Disposable = Disposables.disposed()
                             beforeEach {
                                 val device: AdbDevice = try {
                                     connectedAdbDevices()
                                             .map { it.first() }
                                             .blockingGet()
                                 } catch (e: Throwable) {
                                     throw IllegalStateException("Test assumes attached device(s)", e)
                                 }

                                 deviceStream = Single.never<Unit>()
                                         .acquireDeviceLock(device)
                                         .subscribe()
                             }
                             it("blocks on device lock") {
                                 connectedAdbDevices()
                                         .map { it.single() }
                                         .flatMap { device ->
                                             Single.just(Unit).acquireDeviceLock(device)
                                         }
                                         .onErrorResumeNext { error: Throwable ->
                                             if (error is IllegalStateException &&
                                                 error.cause is OverlappingFileLockException) {
                                                 Single.just(Unit)
                                             } else Single.error(AssertionError("expected error causal chain not received", error))
                                         }
                                         .blockingGet()
                             }
                             afterEach { deviceStream.dispose() }
                         }

                         describe("adb install") {
                             beforeEach {
                                 val device: AdbDevice = try {
                                     connectedAdbDevices()
                                             .map { it.first() }
                                             .blockingGet()
                                 } catch (e: Throwable) {
                                     throw IllegalStateException("Test assumes attached device(s)", e)
                                 }

                                 device.installApk(System.getProperty("newApk"), 5 to SECONDS, true)
                                         .ignoreElements()
                                         .blockingGet(10, SECONDS)
                             }
                             it("prints the error when it fails to install") {
                                 val originalOut = System.out
                                 val byteStreamOut = ByteArrayOutputStream()
                                 PrintStream(byteStreamOut).use { outPrinter ->
                                     try {
                                         System.setOut(outPrinter)
                                         connectedAdbDevices()
                                                 .map { it.first() }
                                                 .flatMapObservable { it.installApk(System.getProperty("oldApk"), 5 to SECONDS, true) }
                                                 .ignoreElements()
                                                 .blockingGet(10, SECONDS)
                                     } finally {
                                         System.out.flush()
                                         System.setOut(originalOut)
                                     }
                                 }

                                 val capturedOutput = byteStreamOut.toString()
                                 print(capturedOutput)
                                 assertThat(capturedOutput).contains("testSupport-older-debug.apk: Failure [INSTALL_FAILED_VERSION_DOWNGRADE]")
                             }
                         }
                     })

package com.gojuno.commander.android

import com.gojuno.commander.os.Notification
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.channels.OverlappingFileLockException

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
                                         }
                                         else Single.error(AssertionError("expected error causal chain not received", error))
                                     }
                                     .blockingGet()
                             }
                             afterEach { deviceStream.dispose() }
                         }
                     })

@file:Suppress("MagicNumber")

/*
 *    Copyright 2020 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.gojuno.commander.android

import com.gojuno.commander.os.home
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposables
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import java.util.concurrent.TimeUnit

val lockFileDir by lazy {
  File("$home${File.separator}.android${File.separator}commander")
}

fun <T> Single<T>.acquireDeviceLock(adbDevice: AdbDevice): Single<T> {
  return this.compose { upstream ->
    Single.create<T> { emitter ->
      adbDevice.log("Acquiring lock")
      runCatching {
        val waitLogging = Observable.interval(5L, 30L, TimeUnit.SECONDS)
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
        emitter.setDisposable(
            Disposables.fromAction {
              waitLogging.dispose()
              lock.close()
              adbDevice.lockFile.delete()
              adbDevice.log("Lock released")
            }
        )
        upstream.subscribe(emitter::onSuccess, emitter::onError)
      }.onFailure {
        emitter.onError(IllegalStateException("Device lock failed", it))
      }
    }
  }
}

private tailrec fun File.lock(): FileLock? {
  val lock = RandomAccessFile(this, "rws").channel.tryLock()
  if (lock == null) Thread.sleep(500L)
  return lock ?: lock()
}

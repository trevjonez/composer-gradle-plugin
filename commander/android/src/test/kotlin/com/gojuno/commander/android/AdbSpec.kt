package com.gojuno.commander.android

import com.gojuno.commander.os.Notification
import io.reactivex.Observable
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

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

})

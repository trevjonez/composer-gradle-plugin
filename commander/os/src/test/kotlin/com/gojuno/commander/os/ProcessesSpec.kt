package com.gojuno.commander.os

import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

class ProcessesSpec : Spek({

    describe("null device file") {

        val result = os().nullDeviceFile()

        it("is writable") {
            assertThat(result).canWrite()
        }
    }

    describe("nanosToHumanReadableTime") {

        context("convert 1 second") {

            val result by memoized { SECONDS.toNanos(1).nanosToHumanReadableTime() }

            it("converts it to 1 second") {
                assertThat(result).isEqualTo("1 second")
            }
        }

        context("convert 59 seconds") {

            val result by memoized { SECONDS.toNanos(59).nanosToHumanReadableTime() }

            it("converts it to 59 seconds") {
                assertThat(result).isEqualTo("59 seconds")
            }
        }

        context("convert 60 seconds") {

            val result by memoized { SECONDS.toNanos(60).nanosToHumanReadableTime() }

            it("converts it to 1 minute 0 seconds") {
                assertThat(result).isEqualTo("1 minute 0 seconds")
            }
        }

        context("convert 61 seconds") {

            val result by memoized { SECONDS.toNanos(61).nanosToHumanReadableTime() }

            it("converts it to 1 minute 1 second") {
                assertThat(result).isEqualTo("1 minute 1 second")
            }
        }

        context("convert 62 seconds") {

            val result by memoized { SECONDS.toNanos(62).nanosToHumanReadableTime() }

            it("converts it to 1 minute 2 seconds") {
                assertThat(result).isEqualTo("1 minute 2 seconds")
            }
        }

        context("convert 60 minutes") {

            val result by memoized { MINUTES.toNanos(60).nanosToHumanReadableTime() }

            it("converts it to 1 hour 0 seconds") {
                assertThat(result).isEqualTo("1 hour 0 minutes 0 seconds")
            }
        }

        context("convert 61 minutes") {

            val result by memoized { MINUTES.toNanos(61).nanosToHumanReadableTime() }

            it("converts it to 1 hour 1 minute 0 seconds") {
                assertThat(result).isEqualTo("1 hour 1 minute 0 seconds")
            }
        }
    }
})

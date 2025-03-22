package com.rejeq.cpcam.benchmarks.baseline

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        val pkgName = InstrumentationRegistry
            .getArguments()
            .getString("targetAppId")

        checkNotNull(pkgName) {
            "targetAppId not passed as instrumentation runner arg"
        }

        rule.collect(
            packageName = pkgName,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
        }
    }
}

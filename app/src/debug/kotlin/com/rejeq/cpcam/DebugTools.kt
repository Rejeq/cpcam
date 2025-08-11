package com.rejeq.cpcam

import android.os.StrictMode
import com.github.anrwatchdog.ANRWatchDog

fun initDebugTools() {
    ANRWatchDog().start()

    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .build(),
    )

    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectAll()
            .build(),
    )
}

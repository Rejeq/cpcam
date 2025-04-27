package com.rejeq.cpcam.core.common

import android.content.Context
import android.content.Intent

interface MainActivityContract {
    fun createIntent(context: Context): Intent
}

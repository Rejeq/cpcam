package com.rejeq.cpcam.ui

import android.content.Context
import android.content.Intent
import com.rejeq.cpcam.core.common.MainActivityContract

class MainActivityContractImpl : MainActivityContract {
    override fun createIntent(context: Context): Intent =
        Intent(context, MainActivity::class.java)
}

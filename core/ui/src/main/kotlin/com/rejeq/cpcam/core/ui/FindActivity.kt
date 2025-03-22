package com.rejeq.cpcam.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Traverses the context hierarchy to find the hosting Activity instance.
 *
 * This utility function walks up the Context wrapper chain until it finds
 * an Activity context or reaches the root. This is useful when you need
 * an Activity reference from a deeply nested context.
 *
 * @return The hosting [Activity] instance, or null if no activity is found
 */
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }

        context = context.baseContext
    }

    return null
}

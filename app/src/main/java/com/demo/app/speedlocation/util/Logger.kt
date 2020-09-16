package com.demo.app.speedlocation.util

import android.util.Log

object Logger {
    fun error(message: Any?) {
            Log.e("MY_LOG", "$message")
    }

    fun error(reference: Any, message: Any?) {
            val tag = reference::class.java.simpleName
            Log.e(
                "MY_LOG",
                "$tag: $message"
            )
    }
}
package com.demo.app.speedlocation.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Activity.doRequestPermissionsInternal(
    permissions: Array<out String>,
    requestCode: Int
): Boolean {
    if (Build.VERSION.SDK_INT >= 23) {
        if (!checkPermissionGranted(this, permissions)) {
            if (shouldShowPermissionSetting(
                    this,
                    permissions
                )
            ) {
                ActivityCompat.requestPermissions(this, permissions, requestCode)
            } else {
                ActivityCompat.requestPermissions(this, permissions, requestCode)
            }
            return false
        }
    }
    return true // is granted or not need to check permission
}

fun Context.isLocationPermissionGranted(): Boolean {
    return checkPermissionGranted(this, getLocationPermissions())
}

fun checkPermissionGranted(activity: Context, permissions: Array<out String>): Boolean {
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        )
            return false
    }
    return true
}

private fun shouldShowPermissionSetting(
    activity: Activity,
    permissions: Array<out String>
): Boolean {
    for (permission in permissions) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            return true
    }
    return false
}

fun isGranted(grantResults: IntArray): Boolean {
    if (grantResults.isEmpty()) return false
    for (result in grantResults) {
        if (result != PackageManager.PERMISSION_GRANTED) return false
    }
    return true
}

package com.demo.app.speedlocation.helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

const val GMS_ACTIVITY_RECOGNITION_PERMISSION =
    "com.google.android.gms.permission.ACTIVITY_RECOGNITION"

fun getLocationPermissions(): Array<out String> {

    return when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q -> {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.FOREGROUND_SERVICE
            )
        }
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P -> {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.FOREGROUND_SERVICE
            )
        }
        else -> {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
}

fun Context.isLocationServiceEnabled(): Boolean {
    val locationManager: LocationManager =
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun Context.openLocationSetting() {
    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}
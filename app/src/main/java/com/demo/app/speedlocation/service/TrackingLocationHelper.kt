package com.demo.app.speedlocation.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

private fun makeLocationRequest(): LocationRequest = LocationRequest()
    .setInterval(300)
    .setFastestInterval(300)
    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

fun Context.getLocationClient(): FusedLocationProviderClient =
    LocationServices.getFusedLocationProviderClient(this)

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.requestLocationUpdateWithCallBack(callback: LocationCallback) {
    Handler(Looper.getMainLooper()).post {
        requestLocationUpdates(
            makeLocationRequest(),
            callback,
            Looper.myLooper()
        )
    }
}
package com.demo.app.speedlocation.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.demo.app.speedlocation.MyApplication
import com.demo.app.speedlocation.data.RecordHistory
import com.demo.app.speedlocation.data.RunningSession
import com.demo.app.speedlocation.data.SharedPrefsStore
import com.demo.app.speedlocation.helper.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*


class TrackingLocationService : MyForegroundService() {
    companion object {
        var isRunning = false
        var isPause = true
        var lastLocation: LatLng? = null
        var startTimeMilis = 0L

        fun startTrackingLocation() {
            ContextCompat.startForegroundService(
                MyApplication.GLOBAL_CONTEXT,
                Intent(MyApplication.GLOBAL_CONTEXT, TrackingLocationService::class.java)
            )
        }
    }

    private lateinit var session: RunningSession
    private lateinit var history: RecordHistory
    private var locationClient: FusedLocationProviderClient? = null
    private var durationJob: Job? = null

    private lateinit var mActivityTransitionsPendingIntent: PendingIntent
    private lateinit var mTransitionsReceiver: TransitionsReceiver
    private lateinit var sensorIntent:Intent

    private var isMoving = false

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        sensorIntent=Intent(this,SensorService::class.java)
        initTransitionRecognition()
        prepareData()
        startLocationListener()
    }

    private fun prepareData() {
        session = RunningSession.start()
        history = SharedPrefsStore.getRecordHistory()
        startTimeMilis = session.time
    }

    override fun defineNotification() = makeForegroundNotification(CHANNEL_ID)

    override fun onDestroy() {
        super.onDestroy()
        stopLocationListener()
        isRunning = false
        startTimeMilis = 0L
        saveData()
    }

    private fun saveData() {
        session.stop()
        if (session.isNotEmpty()) {
            history.addNewRecord(session.time)
            SharedPrefsStore.saveRunningSession(session)
            SharedPrefsStore.saveRecordHistory(history)
        }
    }

    override fun onReceivedEvent(event: Any?) {
        if (event is EventStopTrackingLocation) endServiceNow()
        if (event is EventPauseTrackingLocation) {
            stopLocationListener()
            session.lastRoute().stop()
        }
        if (event is EventResumeTrackingLocation) {
            startLocationListener()
            session.makeNewRoute(System.currentTimeMillis())
        }
        if (event is EventActivityMovementChange) {
            isMoving = event.isMoving
        }

    }

    private fun endServiceNow() {
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationListener() {
        if (locationClient == null)
            locationClient = getLocationClient()

        if (isLocationPermissionGranted()) {
            isPause = false
            postEvent(EventTrackingLocationRunning(session))
            listenDurationChange()
            locationClient?.requestLocationUpdateWithCallBack(locationCallback)
            startTrackingActivity(mTransitionsReceiver, mActivityTransitionsPendingIntent)
            startService(sensorIntent)
        }
    }

    private fun stopLocationListener() {
        locationClient?.removeLocationUpdates(locationCallback)
        stopListenDurationChange()
        stopTrackingActivity(mTransitionsReceiver, mActivityTransitionsPendingIntent)
        stopService(sensorIntent)
        isPause = true
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (isMoving) {
                val newLocation = locationResult.locations[0]
                lastLocation = LatLng(newLocation.latitude, newLocation.longitude)
                session.lastRoute().makeNewStep(lastLocation!!, newLocation.speed)
                postEvent(EventTrackingUpdateLocation(session))
            }

        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)
            Log.e(
                this::class.java.name,
                "Location:${p0?.isLocationAvailable}"
            )
            if (p0?.isLocationAvailable == false) {
                session.lastRoute().stop()
            }
            if (p0?.isLocationAvailable == false && !isLocationServiceEnabled()) {
                session.lastRoute().stop()
                postEvent(EventLocationServiceNotAvailable())
            }
        }
    }

    private fun listenDurationChange() {
        durationJob = CoroutineScope(Dispatchers.Default).launch {
            while (durationJob?.isActive == true) {
                delay(500)
                postEvent(EventTrackingUpdateDuration(session.getRunningDurationMilis()))
            }
        }
    }

    private fun stopListenDurationChange() {
        durationJob?.cancel()
    }

    private fun initTransitionRecognition() {
        mActivityTransitionsPendingIntent = makePendingIntent()
        mTransitionsReceiver = TransitionsReceiver()
    }

}


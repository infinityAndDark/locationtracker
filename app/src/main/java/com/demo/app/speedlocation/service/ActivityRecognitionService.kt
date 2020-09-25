package com.demo.app.speedlocation.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import com.demo.app.speedlocation.helper.EventActivityMovementChange
import com.demo.app.speedlocation.helper.postEvent
import kotlinx.coroutines.*


class ActivityRecognitionService : Service() {
    private lateinit var mActivityTransitionsPendingIntent: PendingIntent
    private lateinit var mTransitionsReceiver: TransitionsReceiver

    override fun onCreate() {
        super.onCreate()
        initTransitionRecognition()
        startTrackingActivity(mTransitionsReceiver, mActivityTransitionsPendingIntent)
    }
    private fun initTransitionRecognition() {
        mActivityTransitionsPendingIntent = makePendingIntent()
        mTransitionsReceiver = TransitionsReceiver()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTrackingActivity(mTransitionsReceiver, mActivityTransitionsPendingIntent)
    }
}
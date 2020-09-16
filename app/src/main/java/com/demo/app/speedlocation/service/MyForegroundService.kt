package com.demo.app.speedlocation.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.demo.app.speedlocation.helper.checkPermissionGranted
import com.demo.app.speedlocation.helper.registerEventBus
import com.demo.app.speedlocation.helper.unRegisterEventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


abstract class MyForegroundService : Service() {
    protected val CHANNEL_ID = "RunningTrackerChannel"
    private val CHANNEL_NAME = "Running Tracker Channel"
    override fun onCreate() {
        super.onCreate()
        registerEventBus(this)
        makeForeground()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun makeForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (checkPermissionGranted(this, arrayOf(Manifest.permission.FOREGROUND_SERVICE))) {
                    doForeground()
                }
            } else {
                doForeground()
            }

        }

    }

    private fun doForeground() {
        startForeground(1, defineNotification())
    }

    protected abstract fun defineNotification(): Notification

    override fun onDestroy() {
        super.onDestroy()
        unRegisterEventBus(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    fun onMessageEvent(message: Any?) {
        onReceivedEvent(message)
    }

    protected open fun onReceivedEvent(event: Any?) {}
}
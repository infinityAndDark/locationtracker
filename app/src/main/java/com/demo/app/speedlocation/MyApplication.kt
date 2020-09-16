package com.demo.app.speedlocation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.demo.app.speedlocation.helper.EventStartTrackingLocation
import com.demo.app.speedlocation.helper.registerEventBus
import com.demo.app.speedlocation.helper.unRegisterEventBus
import com.demo.app.speedlocation.service.TrackingLocationService
import com.google.android.gms.maps.MapsInitializer
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var GLOBAL_CONTEXT: Context
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        unRegisterEventBus(this)
    }

    override fun onCreate() {
        super.onCreate()
        registerEventBus(this)
        MapsInitializer.initialize(this)
        GLOBAL_CONTEXT = this.applicationContext
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    fun onMessageEvent(message: Any?) {
        if (message is EventStartTrackingLocation) {
            TrackingLocationService.startTrackingLocation()
        }
    }
}
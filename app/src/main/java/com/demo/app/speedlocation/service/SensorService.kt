package com.demo.app.speedlocation.service

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


class SensorService : Service(), SensorEventListener {

    private lateinit var sensorMan: SensorManager
    private lateinit var stepDetector: Sensor
    private var detectStillJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        sensorMan = getSystemService(SENSOR_SERVICE) as SensorManager
        stepDetector = sensorMan.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sensorMan.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorMan.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            postEvent(EventActivityMovementChange(true))
            detectStillEvent()
        }
    }

    private fun detectStillEvent() {
        detectStillJob?.cancel()
        detectStillJob = CoroutineScope(Dispatchers.Default).launch {
            delay(2000L)
            postEvent(EventActivityMovementChange(false))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}
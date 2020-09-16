package com.demo.app.speedlocation.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.demo.app.speedlocation.MyApplication
import com.demo.app.speedlocation.helper.EventActivityMovementChange
import com.demo.app.speedlocation.helper.postEvent
import com.demo.app.speedlocation.util.Logger
import com.google.android.gms.location.*
import java.util.*

const val TRANSITIONS_RECEIVER_ACTION: String =
    com.demo.app.speedlocation.BuildConfig.APPLICATION_ID + ".TRANSITIONS_RECEIVER_ACTION"

fun makePendingIntent(): PendingIntent {
    val intent = Intent()
    intent.action = TRANSITIONS_RECEIVER_ACTION
    return PendingIntent.getBroadcast(
        MyApplication.GLOBAL_CONTEXT,
        1,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}

fun makeActivityTransitionList(): List<ActivityTransition> {
    val activityTransitionList: MutableList<ActivityTransition> = ArrayList()
    activityTransitionList.add(
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build()
    )
    activityTransitionList.add(
        ActivityTransition.Builder()
            .setActivityType(DetectedActivity.STILL)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
            .build()
    )
    return activityTransitionList
}

class TransitionsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TRANSITIONS_RECEIVER_ACTION) return
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            for (event in result!!.transitionEvents) {
                if (event.activityType == DetectedActivity.STILL) {
                    if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        postEvent(EventActivityMovementChange(false))
                    } else if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        postEvent(EventActivityMovementChange(true))
                    }
                }
            }
        }
    }
}

fun Context.startTrackingActivity(
    receiver: BroadcastReceiver,
    pendingIntent: PendingIntent
) {
    registerReceiver(
        receiver,
        IntentFilter(TRANSITIONS_RECEIVER_ACTION)
    )
    val request = ActivityTransitionRequest(makeActivityTransitionList())
    val task = ActivityRecognition.getClient(this)
        .requestActivityTransitionUpdates(request, pendingIntent)
    task.addOnSuccessListener {
        Logger.error("requestActivityTransitionUpdates-Success")
    }
    task.addOnFailureListener { e ->
        Logger.error("requestActivityTransitionUpdates:${e.message}")
    }
}

fun Context.stopTrackingActivity(
    receiver: BroadcastReceiver,
    pendingIntent: PendingIntent
) {
    unregisterReceiver(receiver)
    ActivityRecognition.getClient(this)
        .removeActivityTransitionUpdates(pendingIntent)
        .addOnSuccessListener {
            Logger.error("removeActivityTransitionUpdates-Success")
        }
        .addOnFailureListener { e ->
            Logger.error("removeActivityTransitionUpdates:${e.message}")
        }
}
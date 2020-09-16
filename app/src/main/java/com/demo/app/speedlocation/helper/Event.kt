package com.demo.app.speedlocation.helper

import com.demo.app.speedlocation.data.RunningSession
import org.greenrobot.eventbus.EventBus

class EventRequestLocationPermission
class EventLocationPermissionGranted
class EventLocationPermissionDeny
class EventPauseTrackingLocation
class EventStartTrackingLocation
class EventStopTrackingLocation
class EventResumeTrackingLocation
class EventTrackingLocationRunning(val session: RunningSession)
class EventTrackingUpdateLocation(val session: RunningSession)
class EventTrackingUpdateDuration(val durationMilis: Long)
class EventLocationServiceNotAvailable
class EventActivityMovementChange(val isMoving: Boolean)

fun registerEventBus(scene: Any?) {
    try {
        EventBus.getDefault().register(scene)
    } catch (e: Exception) {
    }
}

fun unRegisterEventBus(scene: Any?) {
    try {
        EventBus.getDefault().unregister(scene)
    } catch (e: Exception) {
    }
}

fun removeStickyEvent(event: Any?) {
    try {
        EventBus.getDefault().removeStickyEvent(event)
    } catch (e: Exception) {
    }
}

fun postEvent(event: Any?, isSticky: Boolean = false) {
    if (isSticky)
        EventBus.getDefault().postSticky(event)
    else
        EventBus.getDefault().post(event)
}
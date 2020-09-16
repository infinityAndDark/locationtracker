package com.demo.app.speedlocation.data

import android.location.Location
import com.demo.app.speedlocation.helper.getLatLngBounds
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

data class Step(
    val location: LatLng,
    var time: Long,
    var speedMS: Float = 0f,
    var distance: Float = 0f
) {
    fun getDistanceMeter(previousStep: Step?): Float {
        if (previousStep == null) return 0f
        val current = Location("")
        current.latitude = location.latitude
        current.longitude = location.longitude
        val previous = Location("")
        previous.latitude = previousStep.location.latitude
        previous.longitude = previousStep.location.longitude
        return previous.distanceTo(current)
    }

    fun getDurationMilis(previousStep: Step?): Long {
        if (previousStep == null) return 0L
        var value = time - previousStep.time
        if (value < 0) value = 0
        return value
    }

    fun getSpeedMeter(previousStep: Step?): Float {
        if (previousStep == null) return 0f
        return getDistanceMeter(previousStep) / getDurationMilis(previousStep) * 1000
    }
}

fun Float.convertMSToKmH(): Float = this * 18 / 5
fun Float.getKmHText(noTale: Boolean = false): String =
    String.format("%.2f${if (noTale) "" else "(km/h)"}", this)

fun Float.convertMeterToKm(): Float = this / 1000
fun Float.getKmText(): String = String.format("%.2f(km)", this)
fun Long.getDurationMilisText(): String {
    val totalSec = (this / 1000f).roundToInt()
    val totalMin = totalSec / 60
    val leftSec = totalSec % 60
    val hour = totalMin / 60
    val leftMin = totalMin % 60
    return String.format("%02d:%02d:%02d", hour, leftMin, leftSec)
}

data class Route(
    val steps: MutableList<Step>,
    var startTimeMilis: Long,
    var endTimeMilis: Long,
    var distanceMeter: Float
) {
    fun lastStep(): Step? {
        if (steps.isEmpty()) return null
        return steps.last()
    }

    fun makeNewStep(location: LatLng, gpsSpeedMeter: Float): Route {
        val time = System.currentTimeMillis()
        val lastStep = lastStep()
        val newStep = Step(location, time)
        newStep.speedMS = newStep.getSpeedMeter(lastStep)
        if (gpsSpeedMeter < newStep.speedMS && gpsSpeedMeter > 0f) newStep.speedMS = gpsSpeedMeter
        if (newStep.speedMS == 0f) newStep.distance = 0f
        else newStep.distance = newStep.getDistanceMeter(lastStep)
        steps.add(newStep)
        distanceMeter += newStep.distance
        return this
    }

    fun getLocations() = steps.map { item -> item.location }
    fun getDurationMilis(): Long = endTimeMilis - startTimeMilis
    fun getAverageSpeedMS(): Float = distanceMeter / (getDurationMilis() / 1000f) // v=s/t
    fun getFastestSpeedMS(): Float = steps.maxOf { item -> item.speedMS }

    fun isNotEmpty(): Boolean = steps.isNotEmpty()
    fun isEmpty(): Boolean = steps.isEmpty()
    fun isEnd() = endTimeMilis != startTimeMilis
    fun stop() {
        endTimeMilis = System.currentTimeMillis()
    }

    fun firstLocation(): LatLng? = if (steps.size == 0) null else steps[0].location
    fun lastLocation(): LatLng? = if (steps.size == 0) null else steps[steps.size - 1].location
}

data class RunningSession(
    val routes: MutableList<Route>,
    val time: Long,
    var centerPoint: LatLng? = null
) {
    companion object {
        fun start(): RunningSession {
            val startTime = System.currentTimeMillis()
            return RunningSession(
                ArrayList(),
                startTime
            ).makeNewRoute(startTime)
        }
    }

    fun makeNewRoute(startTime: Long): RunningSession {
        routes.add(
            Route(
                ArrayList(),
                startTime,
                startTime, 0f
            )
        )
        return this
    }

    fun lastRoute(): Route {
        if (routes.isEmpty()) makeNewRoute(System.currentTimeMillis())
        return routes.last()
    }

    fun previousLastRoute(): Route? {
        if (routes.size < 2) return null
        return routes[routes.size - 2]
    }

    fun getDistanceMeter(): Float =
        routes.fold(0f, { total: Float, item: Route -> total + item.distanceMeter })

    fun getDurationMilis(): Long =
        routes.fold(0L, { total: Long, item: Route -> total + item.getDurationMilis() })

    fun getRunningDurationMilis(): Long {
        if (routes.isEmpty()) return 0
        var lastRunningDuration = System.currentTimeMillis() - lastRoute().startTimeMilis
        if (routes.size > 1)
            for (i in 0 until routes.size - 1) lastRunningDuration += routes[i].getDurationMilis()
        return lastRunningDuration
    }

    fun getRunningSpeedMS(): Float = lastRoute().lastStep()?.speedMS ?: 0f
    fun getAverageSpeedMS(): Float {
        val speeds = getSpeeds()
        if (speeds.isNullOrEmpty()) return 0f
        return speeds.average().toFloat()
    }

    fun getLocations(): List<LatLng> {
        val list: MutableList<LatLng> = ArrayList()
        for (i in 0 until routes.size) list.addAll(routes[i].getLocations())
        return list
    }

    private fun getSpeeds(): List<Float> {
        val list: MutableList<Float> = ArrayList()
        for (i in 0 until routes.size) list.addAll(routes[i].steps.map { it.speedMS })
        return list
    }

    private fun findCenterPoint(): RunningSession {
        val locations = getLocations()
        if (locations.isNotEmpty())
            centerPoint = getLatLngBounds(locations).center
        return this
    }

    fun isNotEmpty(): Boolean {
        return routes.isNotEmpty() && routes.first().isNotEmpty()
    }

    fun stop(): RunningSession {
        findCenterPoint()
        lastRoute().stop()
        for (i in routes.size - 1 downTo 0)
            if (routes[i].steps.size <= 1) routes.removeAt(i)
        return this
    }
}

data class RecordHistory(val records: MutableList<Long>) {
    fun addNewRecord(time: Long) {
        records.add(0, time)
    }
}
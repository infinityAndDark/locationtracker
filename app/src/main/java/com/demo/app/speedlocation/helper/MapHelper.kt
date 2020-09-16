package com.demo.app.speedlocation.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.demo.app.speedlocation.R
import com.demo.app.speedlocation.util.ViewUtils
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*


const val MIN_ZOOM_LEVEL = 14f
const val RUNNING_ZOOM_LEVEL = 18f
val DEFAULT_LOCATION = LatLng(10.777260, 106.694729)

fun GoogleMap?.configGoogleMap(justView: Boolean = false) {
    this?.uiSettings?.isCompassEnabled = !justView
    this?.uiSettings?.isMyLocationButtonEnabled = false
    this?.uiSettings?.isMapToolbarEnabled = false
    this?.uiSettings?.isIndoorLevelPickerEnabled = false
    this?.uiSettings?.isRotateGesturesEnabled = !justView
    this?.uiSettings?.isScrollGesturesEnabled = !justView
    this?.uiSettings?.isScrollGesturesEnabledDuringRotateOrZoom = !justView
    this?.uiSettings?.isZoomGesturesEnabled = !justView
    this?.uiSettings?.isZoomControlsEnabled = false
    this?.uiSettings?.isTiltGesturesEnabled = !justView
    this?.setMinZoomPreference(MIN_ZOOM_LEVEL)
}

fun GoogleMap?.moveToLocation(
    location: LatLng?,
    isAnimate: Boolean = false,
    duration: Int = 0,
    zoom: Float = MIN_ZOOM_LEVEL
) {
    this?.run {
        location?.let {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                it,
                if (zoom == 0f) this.cameraPosition.zoom else zoom
            )
            cameraUpdate?.let {
                if (isAnimate) {
                    if (duration != 0)
                        this.animateCamera(cameraUpdate, duration, null)
                    else this.animateCamera(cameraUpdate)
                } else this.moveCamera(cameraUpdate)
            }
        }
    }

}

fun GoogleMap?.addMarkerWithBitmapIcon(
    location: LatLng,
    icon: BitmapDescriptor,
    zIndex: Float = 0f,
    center: Boolean = false
): Marker? {
    try {
        var markerOption = MarkerOptions()
            .position(location)
            .zIndex(zIndex)
            .icon(icon)
        if (center)
            markerOption = markerOption.anchor(0.5f, 0.5f)
        return this?.addMarker(markerOption)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

private fun makeMarkerView(
    context: Context?,
    markerLayout: Int
): View {
    val inflater = LayoutInflater.from(context)
    return inflater.inflate(
        markerLayout,
        null,
        false
    )
}

fun Context.makeMarkerIcon(layout: Int): BitmapDescriptor {
    val viewMarker = makeMarkerView(this, layout)
    return BitmapDescriptorFactory.fromBitmap(getBitmapFromView(viewMarker))
}

fun getBitmapFromView(view: View): Bitmap {
    view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val bitmap = Bitmap.createBitmap(
        view.measuredWidth,
        view.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    view.layout(0, 0, view.width, view.height)
    view.draw(canvas)
    return bitmap
}


fun GoogleMap?.boundMarkers(models: List<LatLng>, animate: Boolean = true, padding: Int = 10) {
    try {
        val cameraUpdate = getBoundCamera(models, padding)
        if (animate)
            this?.animateCamera(cameraUpdate)
        else this?.moveCamera(cameraUpdate)
    } catch (e: Exception) {

    }
}

fun getBoundCamera(locations: List<LatLng>, padding: Int = 10): CameraUpdate {
    return CameraUpdateFactory.newLatLngBounds(getLatLngBounds(locations), padding)
}

fun getLatLngBounds(locations: List<LatLng>): LatLngBounds {
    val builder = LatLngBounds.Builder()
    locations.map { builder.include(it) }
    return builder.build()
}


fun createPolylineOption(context: Context, points: List<LatLng>): PolylineOptions {
    val ops = PolylineOptions()
    ops.geodesic(true).width(ViewUtils.dpToPx(6f).toFloat())
        .color(ContextCompat.getColor(context, R.color.colorAccent))
    for (point in points) {
        ops.add(point)
    }
    return ops
}
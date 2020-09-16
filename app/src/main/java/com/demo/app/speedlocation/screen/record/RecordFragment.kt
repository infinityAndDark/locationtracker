package com.demo.app.speedlocation.screen.record

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
import androidx.navigation.fragment.findNavController
import com.demo.app.speedlocation.MyFragment
import com.demo.app.speedlocation.R
import com.demo.app.speedlocation.data.*
import com.demo.app.speedlocation.databinding.FragmentRecordBinding
import com.demo.app.speedlocation.helper.*
import com.demo.app.speedlocation.service.TrackingLocationService
import com.demo.app.speedlocation.service.getLocationClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline

class RecordFragment : MyFragment(), OnMapReadyCallback {
    private var binding: FragmentRecordBinding? = null
    private var map: GoogleMap? = null
    private var currentLocationMarker: Marker? = null
    private var currentPolyline: Polyline? = null
    private var currentLastHead: Marker? = null

    private lateinit var iconMarkerHead: BitmapDescriptor
    private lateinit var iconMarkerStart: BitmapDescriptor
    private lateinit var iconMarkerEnd: BitmapDescriptor
    private lateinit var iconMarkerCurrent: BitmapDescriptor
    private var routeCount = 0
    private var isResumeFlow = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding == null) {
            binding = FragmentRecordBinding.inflate(inflater)
            binding?.root?.postDelayed(0) {
                bindView()
                if (TrackingLocationService.isRunning)
                    resumeFlow()
                else
                    startFlow()
            }
        }

        return binding?.root
    }

    private fun bindView() {
        binding?.run {
            setStatusBarSpace(viewStatusSpace)
            imageBackground.loadImage(R.drawable.background_prepare)
            handleUserActions()
            initValue()
            root.postDelayed(DELAY_LOADING_TIME) {
                addMapFragment()
            }
        }
    }

    private fun FragmentRecordBinding.handleUserActions() {
        buttonStart.setOnClickListener {
            if (!requireContext().isLocationServiceEnabled()) {
                notifyTurnOnLocationService()
                return@setOnClickListener
            }
            context?.vibrate()
            afterStartUI()
            postEvent(EventStartTrackingLocation())
        }
        buttonPause.setOnClickListener {
            if (isResumeFlow) return@setOnClickListener
            context?.vibrate()
            afterPauseUI()
            postEvent(EventPauseTrackingLocation())
        }
        buttonResume.setOnClickListener {
            if (isResumeFlow) return@setOnClickListener
            if (!requireContext().isLocationServiceEnabled()) {
                notifyTurnOnLocationService()
                return@setOnClickListener
            }
            context?.vibrate()
            afterResumeUI()
            postEvent(EventResumeTrackingLocation())
        }
        buttonStop.setOnClickListener {
            context?.vibrate()
            afterStopUI()
            postEvent(EventStopTrackingLocation())
            findNavController().navigate(RecordFragmentDirections.goToHistory())
        }
    }

    private fun FragmentRecordBinding.afterStopUI() {
        buttonStop.hide(true)
        buttonPause.hide(true)
    }

    private fun FragmentRecordBinding.afterResumeUI() {
        buttonResume.hide(true)
        buttonPause.show()
        imageBackground.loadImage(R.drawable.background_running)
    }

    private fun FragmentRecordBinding.afterPauseUI() {
        buttonPause.hide(true)
        buttonResume.show()
        imageBackground.loadImage(R.drawable.background_prepare)
    }

    private fun FragmentRecordBinding.afterStartUI() {
        buttonStart.hide(true)
        buttonPause.show()
        buttonStop.show()
        imageBackground.loadImage(R.drawable.background_running)
    }

    private fun FragmentRecordBinding.initValue() {
        textCurrentSpeed.text = String.format("%.1f", 0f)
        textAverageSpeed.text = 0f.getKmHText()
        textDistance.text = 0f.getKmText()
        textRoute.text = "0"
        textDuration.text = "00:00:00"
        binding?.textActivity?.text = getString(R.string.activity_still)
    }

    private fun resumeFlow() {
        isResumeFlow = true
        binding?.run {
            afterStartUI()
            if (TrackingLocationService.isPause)
                afterPauseUI()
        }
    }

    private fun startFlow() {

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        map = null
    }

    private fun getMapFragment(): SupportMapFragment? {
        return childFragmentManager.getFragment(SupportMapFragment::class.java)
                as SupportMapFragment?
    }

    private fun addMapFragment() {
        initMarkerIcons()
        var mapFragment = getMapFragment()
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
            childFragmentManager.addFragment(R.id.mapContainer, mapFragment)
        }
        mapFragment?.getMapAsync(this)
    }

    private fun initMarkerIcons() {
        iconMarkerCurrent = requireContext().makeMarkerIcon(R.layout.view_marker_current)
        iconMarkerHead = requireContext().makeMarkerIcon(R.layout.view_marker_head)
        iconMarkerStart = requireContext().makeMarkerIcon(R.layout.view_marker_start)
        iconMarkerEnd = requireContext().makeMarkerIcon(R.layout.view_marker_end)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.configGoogleMap()
        val location = TrackingLocationService.lastLocation
        map?.moveToLocation(location ?: DEFAULT_LOCATION, zoom = RUNNING_ZOOM_LEVEL)
        addCurrentLocation(location)
        getCurrentLocationInFirstTime()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationInFirstTime() {
        context?.run {
            if (isLocationServiceEnabled()) {
                if (isLocationPermissionGranted())
                    getLocationClient().lastLocation.addOnSuccessListener {
                        if (it != null)
                            addCurrentLocation(LatLng(it.latitude, it.longitude))
                    }
                else {

                }
            } else {
                notifyTurnOnLocationService()
            }
        }
    }

    private fun notifyTurnOnLocationService() =
        context?.showToast(getString(R.string.notification_turn_on_location_service))

    override fun onReceivedEvent(event: Any?) {
        if (event is EventTrackingUpdateLocation) {
            updateMap(event.session)
            updateInfo(event.session)
        }
        if (event is EventTrackingLocationRunning) {
            updateMap(event.session)
            updateInfo(event.session)
        }
        if (event is EventTrackingUpdateDuration) {
            binding?.textDuration?.text = event.durationMilis.getDurationMilisText()
        }
        if (event is EventActivityMovementChange) {
            binding?.textActivity?.text =
                if (event.isMoving) getString(R.string.activity_moving)
                else getString(R.string.activity_still)
        }
    }

    private fun updateInfo(session: RunningSession) {
        binding?.run {
            textCurrentSpeed.text =
                String.format("%.1f", session.getRunningSpeedMS().convertMSToKmH())
            textRoute.text = "${session.routes.size}"
            textAverageSpeed.text =
                session.getAverageSpeedMS().convertMSToKmH().getKmHText()
            textDistance.text =
                session.getDistanceMeter().convertMeterToKm().getKmText()
        }

    }

    private fun updateMap(session: RunningSession) {
        if (isResumeFlow) {
            isResumeFlow = false
            resumeRouteInMap(session)
        } else {
            addCurrentLocation(TrackingLocationService.lastLocation)
            if (session.routes[0].isNotEmpty()) {
                updateLastRoute(
                    session.lastRoute(),
                    session.previousLastRoute(),
                    routeCount < session.routes.size
                )
                routeCount = session.routes.size
            }
        }

    }

    private fun resumeRouteInMap(session: RunningSession) {
        routeCount = session.routes.size
        if (routeCount > 1)
            for (i in 0 until session.routes.size - 1) {
                if (session.routes[i].firstLocation() != null)
                    map?.addMarkerWithBitmapIcon(
                        session.routes[i].firstLocation()!!,
                        iconMarkerHead,
                        center = true
                    )
                if (session.routes[i].lastLocation() != null)
                    map?.addMarkerWithBitmapIcon(
                        session.routes[i].lastLocation()!!,
                        iconMarkerHead,
                        center = true
                    )
                if (session.routes[i].firstLocation() != null)
                    map?.addMarkerWithBitmapIcon(
                        session.routes[i].firstLocation()!!,
                        iconMarkerStart,
                        zIndex = 1f
                    )
                if (session.routes[i].lastLocation() != null)
                    map?.addMarkerWithBitmapIcon(
                        session.routes[i].lastLocation()!!,
                        iconMarkerEnd,
                        zIndex = 1f
                    )
                map?.addPolyline(
                    createPolylineOption(
                        requireContext(),
                        session.routes[i].getLocations()
                    )
                )
            }
        updateLastRoute(session.lastRoute(), null, true)
    }

    private fun addCurrentLocation(location: LatLng?) {
        if (location == null) return
        currentLocationMarker?.remove()
        currentLocationMarker =
            map?.addMarkerWithBitmapIcon(location, iconMarkerCurrent, zIndex = 3f)
        map?.moveToLocation(location, isAnimate = true, zoom = map!!.cameraPosition.zoom)
    }

    private fun updateLastRoute(route: Route, previousRoute: Route?, newRoute: Boolean) {
        if (newRoute && route.firstLocation() != null) {
            map?.addMarkerWithBitmapIcon(route.firstLocation()!!, iconMarkerHead, center = true)
            map?.addMarkerWithBitmapIcon(route.firstLocation()!!, iconMarkerStart, zIndex = 1f)
        }
        if (newRoute && previousRoute != null && route.lastLocation() != null) {
            currentPolyline = null
            currentLastHead = null
            map?.addMarkerWithBitmapIcon(previousRoute.lastLocation()!!, iconMarkerEnd, zIndex = 2f)
        }
        currentPolyline?.remove()
        currentLastHead?.remove()
        if (route.lastLocation() != null)
            currentLastHead =
                map?.addMarkerWithBitmapIcon(route.lastLocation()!!, iconMarkerHead, center = true)
        currentPolyline =
            map?.addPolyline(createPolylineOption(requireContext(), route.getLocations()))
    }
}
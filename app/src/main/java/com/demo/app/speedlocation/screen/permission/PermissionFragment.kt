package com.demo.app.speedlocation.screen.permission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.demo.app.speedlocation.MyFragment
import com.demo.app.speedlocation.R
import com.demo.app.speedlocation.data.SharedPrefsStore
import com.demo.app.speedlocation.databinding.FragmentPermissionBinding
import com.demo.app.speedlocation.helper.*
import com.demo.app.speedlocation.service.TrackingLocationService

class PermissionFragment : MyFragment() {
    private var binding: FragmentPermissionBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding == null) {
            binding = FragmentPermissionBinding.inflate(inflater)
            bindView()
            if (TrackingLocationService.isRunning)
                resumeFlow()
            else
                startFlow()
        }
        return binding?.root
    }

    private fun resumeFlow() {
        findNavController().navigate(PermissionFragmentDirections.goToRecord())
    }

    private fun bindView() {
        binding?.run {
            imageBackground.loadImage(R.drawable.background_prepare_2)
            buttonNext.setOnClickListener {
                context?.vibrate()
                postEvent(EventRequestLocationPermission())
            }
        }
    }

    private fun startFlow() {
        if (requireContext().isLocationServiceEnabled() && requireContext().isLocationPermissionGranted()) {
            if (SharedPrefsStore.getRecordHistory().records.size == 0)
                findNavController().navigate(PermissionFragmentDirections.goToRecord())
            else
                findNavController().navigate(PermissionFragmentDirections.goToHistory())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onReceivedEvent(event: Any?) {
        if (event is EventLocationPermissionGranted) {
            if (context?.isLocationServiceEnabled() == true)
                findNavController().navigate(PermissionFragmentDirections.goToRecord())
            else context?.showToast(getString(R.string.notification_turn_on_location_service))
        }
        if (event is EventLocationPermissionDeny) {
            context?.showToast(getString(R.string.notification_grant_permission))
        }
    }
}
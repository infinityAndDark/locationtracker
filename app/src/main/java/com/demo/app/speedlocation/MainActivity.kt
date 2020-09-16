package com.demo.app.speedlocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.demo.app.speedlocation.helper.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private val requestCodePermission: Int = 656

    override fun onCreate(savedInstanceState: Bundle?) {
        statusTransparent(contentWhite = true, hideNavigation = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerEventBus(this)
        setScreenBrightness(1f)
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterEventBus(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodePermission) {
            if (isGranted(grantResults)) {
                this.onPermissionGranted()
            } else {
                this.onPermissionDeny()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(message: Any?) {
        if (message is EventRequestLocationPermission)
            doRequestPermission(getLocationPermissions())
    }

    private fun doRequestPermission(
        permissions: Array<out String>
    ) {
        if (this.doRequestPermissionsInternal(permissions, requestCodePermission))
            onPermissionGranted()

    }

    private fun onPermissionGranted() {
        postEvent(EventLocationPermissionGranted())
    }

    private fun onPermissionDeny() {
        postEvent(EventLocationPermissionDeny())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) statusTransparent(contentWhite = true, hideNavigation = true)
        super.onWindowFocusChanged(hasFocus)
    }
}
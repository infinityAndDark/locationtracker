package com.demo.app.speedlocation.helper

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.app.speedlocation.util.ScreenUtils
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.transition.ViewPropertyTransition

fun View.show() {
    if (!isVisible)
        visibility = View.VISIBLE
}

fun View.hide(isGone: Boolean = false) {
    if (isVisible)
        visibility = if (isGone) View.GONE else View.INVISIBLE
}

fun RecyclerView.setVerticalLayout() {
    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
}

fun Activity.setScreenBrightness(value: Float) {
    val layout: WindowManager.LayoutParams = window.attributes
    layout.screenBrightness = value
    window.attributes = layout
}

fun Context.vibrate(duration: Long = 50) {
    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        v.vibrate(duration)
    }
}

@GlideModule
class MyAppGlideModule : AppGlideModule()

class AnimateObject {
    companion object {
        val FADE_IN = ViewPropertyTransition.Animator() {
            it.alpha = 0f
            val fadeAnim = ObjectAnimator.ofFloat(it, "alpha", 0f, 1f)
            fadeAnim.duration = 150
            fadeAnim.start()
        }
    }
}

fun ImageView.loadImage(source: Any) {
    var request = GlideApp.with(this)
        .load(source)
        .format(DecodeFormat.PREFER_RGB_565)
    request = request.diskCacheStrategy(DiskCacheStrategy.ALL)
    request = request.transition(GenericTransitionOptions.with(AnimateObject.FADE_IN))
    request = request.centerCrop()
    request.into(this)

}

fun Context.showToast(message: String?) {
    if (!message.isNullOrEmpty())
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.statusTransparent(contentWhite: Boolean = false, hideNavigation: Boolean = false) {
    ScreenUtils.setOverlayStatusBar(window, contentWhite, hideNavigation)
}

fun setStatusBarSpace(view: View?) {
    val layoutParams = view?.layoutParams
    layoutParams?.height = view?.context.getStatusHeight()
    view?.layoutParams = layoutParams
}

fun Context?.getStatusHeight(): Int {
    var height = 0
    this?.let {
        height = ScreenUtils.getStatusBarHeight(it)
    }
    return height
}

const val DELAY_LOADING_TIME = 120L
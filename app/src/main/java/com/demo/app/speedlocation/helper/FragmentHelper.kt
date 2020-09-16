package com.demo.app.speedlocation.helper

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun FragmentManager.getFragment(type: Class<*>): Fragment? {
    return getFragmentWithTag(type.canonicalName ?: type.name)
}

fun FragmentManager.getFragmentWithTag(tag: String): Fragment? {
    return this.findFragmentByTag(tag)
}

fun FragmentManager?.removeFragment(fragment: Fragment?, now: Boolean = false) {
    fragment?.let {
        val transaction = this?.beginTransaction()?.remove(fragment)
        if (now) transaction?.commitNowAllowingStateLoss()
        else transaction?.commitAllowingStateLoss()
    }
}

fun FragmentManager?.replaceFragment(containerId: Int, fragment: Fragment?, now: Boolean = false) {
    fragment?.let {
        val trans = this?.beginTransaction()?.replace(
            containerId,
            fragment,
            fragment::class.java.canonicalName
        )
        if (now) trans?.commitNowAllowingStateLoss()
        else trans?.commitAllowingStateLoss()
    }
}

fun FragmentManager.addFragment(containerId: Int, fragment: Fragment, now: Boolean = false) {
    addFragmentWithTag(
        fragment::class.java.canonicalName ?: fragment::class.java.name,
        containerId,
        fragment,
        now
    )
}

fun FragmentManager.addFragmentWithTag(
    tag: String,
    containerId: Int,
    fragment: Fragment,
    now: Boolean = false
) {
    val trans = beginTransaction().add(
        containerId,
        fragment,
        tag
    )
    if (now) trans.commitNowAllowingStateLoss()
    else
        trans.commitAllowingStateLoss()
}
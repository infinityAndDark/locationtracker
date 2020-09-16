package com.demo.app.speedlocation.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.demo.app.speedlocation.MainActivity
import com.demo.app.speedlocation.R

fun Context.makeForegroundNotification(channelId: String): Notification {
    val notificationIntent = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        this,
        0, notificationIntent, 0
    )
    return NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
        .setStyle(
            NotificationCompat.BigTextStyle()
                .setSummaryText(getString(R.string.tracking_notification_title))
                .bigText(getString(R.string.tracking_notification_description))
        )
        .setContentIntent(pendingIntent)
        .build()
}
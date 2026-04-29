package com.ovais.batterymonitorer.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

fun showNotification(context: Context, msg: String) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "battery_alerts"

    val channel = NotificationChannel(
        channelId,
        "Battery Alerts",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Notifications for battery alerts and insights"
    }
    manager.createNotificationChannel(channel)

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Battery Insight")
        .setContentText(msg)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    manager.notify(1, notification)
}

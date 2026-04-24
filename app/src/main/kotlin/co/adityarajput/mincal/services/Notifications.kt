package co.adityarajput.mincal.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import co.adityarajput.mincal.R

fun Context.sendNotification(id: Int, title: String, content: String) {
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
        if (getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
            createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "MinCal Events",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "Notifications for upcoming events" },
            )
        }

        notify(
            id,
            Notification.Builder(this@sendNotification, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title).setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true).build(),
        )
    }
}

const val NOTIFICATION_CHANNEL_ID = "mincal"

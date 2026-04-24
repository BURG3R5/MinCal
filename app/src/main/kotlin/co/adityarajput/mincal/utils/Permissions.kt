package co.adityarajput.mincal.utils

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.net.toUri
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.Permission.POST_NOTIFICATIONS
import co.adityarajput.mincal.utils.Permission.SCHEDULE_EXACT_ALARMS

enum class Permission(val text: Int) {
    POST_NOTIFICATIONS(R.string.post_notifications),
    SCHEDULE_EXACT_ALARMS(R.string.schedule_exact_alarms);
}

fun Context.has(permission: Permission) = when (permission) {
    POST_NOTIFICATIONS ->
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).areNotificationsEnabled()

    SCHEDULE_EXACT_ALARMS ->
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
}

fun Context.hasPermissions(permissions: Iterable<Permission> = Permission.entries) =
    permissions.associateWith { this.has(it) }.withDefault { false }

fun Context.request(permission: Permission) = try {
    when (permission) {
        POST_NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= TIRAMISU) {
            requestPermissions(
                this as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0,
            )
        } else {
            startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                },
            )
        }

        SCHEDULE_EXACT_ALARMS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${packageName}".toUri()
                },
            )
        } else {
        }
    }
} catch (e: Exception) {
    Log.e(TAG, "Failed to request $this", e)
}

const val TAG = "Permission"

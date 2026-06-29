package co.adityarajput.mincal.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.net.toUri
import androidx.work.*
import co.adityarajput.mincal.BuildConfig
import co.adityarajput.mincal.Receiver
import co.adityarajput.mincal.data.Constants
import co.adityarajput.mincal.data.EventInfo
import co.adityarajput.mincal.utils.asShortString
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class AlarmSetter(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Storage.init(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && !alarmManager.canScheduleExactAlarms())
            return Result.failure()

        val events = mutableListOf<EventInfo>()
        try {
            for (calendar in Storage.calenders) {
                val calendarEvents = Calendar.getEvents(calendar.id) ?: continue

                events += calendarEvents.filter { it.hasAccepted }.map {
                    it.copy(
                        calendar = calendar,
                        defaultReminders = calendar.defaultReminders,
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch events", e)
            return Result.failure()
        }

        try {
            for (event in events) {
                Log.d(TAG, "Processing $event")
                val timeToEvent = Duration.between(ZonedDateTime.now(), event.start.datetime)

                for (minutesBefore in event.reminders.sorted()) {
                    val delay = timeToEvent - Duration.ofMinutes(minutesBefore.toLong())
                    if (delay < Duration.ofSeconds(1) || delay > Duration.ofHours(5))
                        continue

                    Log.d(TAG, "Setting exact alarm in ${delay.toMillis()}ms")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + delay.toMillis(),
                        PendingIntent.getBroadcast(
                            context, (event to minutesBefore).hashCode(),
                            Intent(context, Receiver::class.java).apply {
                                data =
                                    "mincal://notify/${event.hashCode()}/${event.start.datetime}/$minutesBefore".toUri()
                                action = Constants.ACTION_NOTIFY
                                putExtra(Constants.EVENT_HASH_KEY, event.hashCode())
                                putExtra(Constants.EVENT_SUMMARY_KEY, event.summary)
                                putExtra(
                                    Constants.EVENT_START_KEY,
                                    event.start.datetime.asShortString,
                                )
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set alarms", e)
            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "AlarmSetter"
    }
}

class TokenRefresher(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Storage.init(context)

        Calendar.refreshTokens()

        return Result.success()
    }
}

fun Context.scheduleWork() {
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        ALARM_SETTER,
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<AlarmSetter>(
            // INFO: While debugging, use a shorter interval
            if (BuildConfig.DEBUG) 15 else 60,
            TimeUnit.MINUTES,
        ).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
        ).build(),
    )

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        TOKEN_REFRESHER,
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<TokenRefresher>(
            1,
            TimeUnit.DAYS,
        ).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
        ).build(),
    )
}

const val ALARM_SETTER = "mincal_worker"
const val TOKEN_REFRESHER = "mincal_token_refresher"

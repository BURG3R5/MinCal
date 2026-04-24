package co.adityarajput.mincal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import co.adityarajput.mincal.data.Constants
import co.adityarajput.mincal.services.sendNotification

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent with action: ${intent.action}")
        if (intent.action != Constants.ACTION_NOTIFY)
            return

        context.sendNotification(
            intent.getIntExtra(Constants.EVENT_HASH_KEY, 420),
            intent.getStringExtra(Constants.EVENT_SUMMARY_KEY) ?: "Event Reminder",
            intent.getStringExtra(Constants.EVENT_START_KEY) ?: "Starting soon",
        )
    }

    companion object {
        const val TAG = "Receiver"
    }
}

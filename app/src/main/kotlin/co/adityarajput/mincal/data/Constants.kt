package co.adityarajput.mincal.data

import co.adityarajput.mincal.BuildConfig

object Constants {
    const val CRASH_REPORT_EMAIL = "mail@adityarajput.co"

    const val ACTION_NOTIFY = "${BuildConfig.APPLICATION_ID}.NOTIFY"
    const val EVENT_HASH_KEY = "${BuildConfig.APPLICATION_ID}.eventHash"
    const val EVENT_SUMMARY_KEY = "${BuildConfig.APPLICATION_ID}.eventSummary"
    const val EVENT_START_KEY = "${BuildConfig.APPLICATION_ID}.eventStart"
}

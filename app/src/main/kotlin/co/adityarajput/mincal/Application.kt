package co.adityarajput.mincal

import android.app.Application
import co.adityarajput.mincal.data.Constants
import co.adityarajput.mincal.services.Storage
import co.adityarajput.mincal.services.scheduleWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.acra.ACRA
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

class MinCalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (ACRA.isACRASenderServiceProcess())
            return

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            mailSender {
                mailTo = Constants.CRASH_REPORT_EMAIL
                subject = "MinCal Crash Report"
            }

            dialog {
                title = "App Crashed"
                text =
                    "MinCal has encountered an unexpected error and crashed. Please report this incident to the developer using the following form."
                commentPrompt = "Your comments:"
                positiveButtonText = "Send email"
            }
        }

        Storage.init(this)

        CoroutineScope(Dispatchers.IO).launch {
            scheduleWork()
        }
    }
}

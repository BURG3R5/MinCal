package co.adityarajput.mincal.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.adityarajput.mincal.R
import co.adityarajput.mincal.data.CalendarInfo
import co.adityarajput.mincal.data.EventInfo
import co.adityarajput.mincal.services.Calendar
import co.adityarajput.mincal.services.Storage
import co.adityarajput.mincal.utils.hasPermissions
import kotlinx.coroutines.launch

class HomeViewModel(context: Context) :
    FormViewModel<HomeFormPage, HomeFormError>(HomeFormPage.entries) {
    suspend fun fetchAndSaveTokens(authCode: String) {
        isLoading = true

        val tokens = Calendar.getTokens(authCode)

        if (tokens == null) {
            error = HomeFormError.CANNOT_FETCH_TOKENS
            isLoading = false
            return
        }

        Storage.saveCredentials(tokens)

        isLoading = false
    }

    var calendars by mutableStateOf<List<CalendarInfo>?>(null)

    var events by mutableStateOf<List<EventInfo>?>(null)

    init {
        if (!Storage.areCredsStored()) {
            page = HomeFormPage.CREDENTIALS
        } else if (!Storage.areCalendarsStored()) {
            viewModelScope.launch {
                page = HomeFormPage.CALENDARS
                fetchCalendars()
            }
        } else if (context.hasPermissions().any { !it.value }) {
            page = HomeFormPage.PERMISSIONS
        } else {
            viewModelScope.launch {
                page = HomeFormPage.EVENTS
                fetchEvents()
            }
        }
    }

    suspend fun fetchCalendars() {
        isLoading = true

        calendars = Calendar.getCalendars()

        if (calendars == null) {
            error = HomeFormError.CANNOT_FETCH_CALENDARS
            isLoading = false
            return
        }

        isLoading = false
    }

    fun saveCalendars(calendars: Set<CalendarInfo>) {
        Storage.saveCalendars(calendars)
    }

    suspend fun fetchEvents() {
        isLoading = true

        val fetchedEvents = mutableListOf<EventInfo>()

        for (calendar in Storage.getCalenders()!!) {
            val calendarEvents = Calendar.getEvents(calendar.id)

            if (calendarEvents == null) {
                error = HomeFormError.CANNOT_FETCH_EVENTS
                continue
            }

            fetchedEvents += calendarEvents.map {
                it.copy(
                    calendar = calendar,
                    defaultReminders = calendar.defaultReminders,
                )
            }
        }

        events = fetchedEvents.sortedBy { it.start.datetime }

        isLoading = false
    }
}

enum class HomeFormPage {
    CREDENTIALS, CALENDARS, PERMISSIONS, EVENTS;
}

enum class HomeFormError(override val message: Int) : FormError {
    CANNOT_FETCH_TOKENS(R.string.cannot_fetch_tokens),
    CANNOT_FETCH_CALENDARS(R.string.cannot_fetch_calendars),
    CANNOT_FETCH_EVENTS(R.string.cannot_fetch_events);
}

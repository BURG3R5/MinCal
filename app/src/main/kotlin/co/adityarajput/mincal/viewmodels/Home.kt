package co.adityarajput.mincal.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import co.adityarajput.mincal.R
import co.adityarajput.mincal.data.Account
import co.adityarajput.mincal.data.CalendarInfo
import co.adityarajput.mincal.data.Credentials
import co.adityarajput.mincal.data.EventInfo
import co.adityarajput.mincal.services.Calendar
import co.adityarajput.mincal.services.Storage
import co.adityarajput.mincal.utils.hasPermissions
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HomeViewModel(context: Context) :
    FormViewModel<HomeFormPage, HomeFormError>(HomeFormPage.entries) {
    var accounts by mutableStateOf<List<Account>>(emptyList())

    var calendars by mutableStateOf<List<CalendarInfo>?>(null)

    var events by mutableStateOf<List<EventInfo>?>(null)

    var pendingAccountId by mutableStateOf<String?>(null)

    init {
        accounts = Storage.accounts

        if (!context.hasPermissions().all { it.value }) {
            page = HomeFormPage.PERMISSIONS
        } else if (!Storage.areCredsStored) {
            page = HomeFormPage.CREDENTIALS
        } else if (!Storage.areCalendarsStored) {
            pendingAccountId = accounts.firstOrNull()?.id
            page = HomeFormPage.CALENDARS
            viewModelScope.launch {
                fetchCalendars()
            }
        } else {
            page = HomeFormPage.EVENTS
            viewModelScope.launch {
                fetchEvents()
            }
        }
    }

    suspend fun readCredentialsJson(json: String) {
        try {
            val credentials = Json.decodeFromString<Credentials>(json)
            val account = Storage.addAccount(credentials)
            pendingAccountId = account.id
            accounts = Storage.accounts
            fetchCalendars()
            page = HomeFormPage.CALENDARS
            error = null
        } catch (_: Exception) {
            error = HomeFormError.CANNOT_READ_CREDENTIALS
        }
    }

    fun initiateAccountSetup() {
        calendars = null
        error = null
        page = HomeFormPage.CREDENTIALS
    }

    fun cancelAccountSetup() {
        if (accounts.isEmpty()) {
            previousPage()
            return
        }

        pendingAccountId = null
        page = HomeFormPage.EVENTS
    }

    fun saveSelectedCalendars(calendars: Set<CalendarInfo>) {
        pendingAccountId?.let { Storage.updateAccountCalendars(it, calendars) }
        accounts = Storage.accounts
    }

    suspend fun removeAccount(accountId: String) {
        Storage.removeAccount(accountId)
        accounts = Storage.accounts

        if (accounts.isEmpty()) {
            events = emptyList()
            page = HomeFormPage.CREDENTIALS
            return
        }

        fetchEvents()
    }

    suspend fun fetchCalendars() {
        isLoading = true

        val accountId = pendingAccountId ?: accounts.firstOrNull { it.calendars.isEmpty() }?.id
        val account = accountId?.let(Storage::getAccount)

        if (account == null) {
            calendars = emptyList()
            isLoading = false
            return
        }

        pendingAccountId = account.id

        calendars = Calendar.getCalendars(account)

        if (calendars == null) {
            error = HomeFormError.CANNOT_FETCH_CALENDARS
            isLoading = false
            return
        }

        isLoading = false
    }

    suspend fun fetchEvents() {
        isLoading = true

        val fetchedEvents = mutableListOf<EventInfo>()

        for (account in Storage.accounts) {
            for (calendar in account.calendars) {
                val calendarEvents = Calendar.getEvents(account, calendar.id)

                if (calendarEvents == null) {
                    error = HomeFormError.CANNOT_FETCH_EVENTS
                    continue
                }

                fetchedEvents += calendarEvents.map {
                    it.copy(
                        accountId = account.id,
                        calendar = calendar,
                        defaultReminders = calendar.defaultReminders,
                    )
                }
            }
        }

        events = fetchedEvents.sortedBy { it.start.datetime }

        isLoading = false
    }
}

enum class HomeFormPage {
    PERMISSIONS, CREDENTIALS, CALENDARS, EVENTS;
}

enum class HomeFormError(override val message: Int) : FormError {
    CANNOT_READ_CREDENTIALS(R.string.cannot_read_credentials),
    CANNOT_FETCH_CALENDARS(R.string.cannot_fetch_calendars),
    CANNOT_FETCH_EVENTS(R.string.cannot_fetch_events);
}

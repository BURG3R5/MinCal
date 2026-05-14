@file:OptIn(ExperimentalSerializationApi::class)

package co.adityarajput.mincal.data

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.time.ZonedDateTime

typealias Component = @Composable () -> Unit
typealias ChildOfColumn = @Composable (ColumnScope.() -> Unit)

@Serializable
data class Credentials(
    val clientId: String, val clientSecret: String,
    val accessToken: String, val refreshToken: String,
    val validTill: Long = 0,
)

@Serializable
@JsonIgnoreUnknownKeys
data class CalendarInfo(
    val id: String,
    val summary: String,
    val defaultReminders: List<Override>? = null,
    val primary: Boolean? = false,
)

@Serializable
@JsonIgnoreUnknownKeys
data class EventInfo(
    val id: String,
    @Transient val calendar: CalendarInfo? = null,
    val summary: String,
    val start: Time,
    @SerialName("reminders") private val eventReminders: Reminders? = null,
    @Transient private val defaultReminders: List<Override>? = null,
) {
    val reminders: List<Int>
        get() = eventReminders?.overrides?.map { it.minutes }
            ?: defaultReminders?.map { it.minutes }
            ?: emptyList()

    override fun toString() =
        "EventInfo(id=$id, calendar=${calendar?.summary}, summary=$summary, start=${start.datetime})"
}

@Serializable
@JsonIgnoreUnknownKeys
data class Reminders(
    val useDefault: Boolean,
    val overrides: List<Override>? = null,
)

@Serializable
@JsonIgnoreUnknownKeys
data class Override(val minutes: Int)

@Serializable
@JsonIgnoreUnknownKeys
data class Time(
    private val date: String? = null,
    private val dateTime: String? = null,
) {
    val datetime: ZonedDateTime
        get() {
            if (dateTime != null)
                return ZonedDateTime.parse(dateTime)

            if (date != null)
                return ZonedDateTime.parse(date + "T00:00:00Z")

            throw IllegalStateException("Invalid time object: both date and dateTime are null")
        }
}

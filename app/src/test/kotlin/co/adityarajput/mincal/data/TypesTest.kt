package co.adityarajput.mincal.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TypesTest {
    private val event = EventInfo(
        "event",
        CalendarInfo("cal-1", "Calendar 1", listOf(Override(20), Override(10)), true),
        "Event",
        Time("2026-06-30"),
        Reminders(false, listOf(Override(30), Override(15))),
        listOf(Override(20), Override(10)),
        listOf(),
    )

    @Test
    fun EventInfo_hasAccepted() {
        fun eventWithAttendees(
            selfResponse: String? = null,
            othersResponse: String? = null,
        ): EventInfo {
            val attendees = mutableListOf<Attendee>()
            selfResponse?.let { attendees.add(Attendee(true, it)) }
            othersResponse?.let { attendees.add(Attendee(false, it)) }
            return event.copy(attendees = attendees)
        }

        assertFalse(event.hasAccepted)

        assertFalse(eventWithAttendees("needsAction").hasAccepted)
        assertFalse(eventWithAttendees("declined").hasAccepted)
        assertTrue(eventWithAttendees("tentative").hasAccepted)
        assertTrue(eventWithAttendees("accepted").hasAccepted)

        assertFalse(eventWithAttendees(othersResponse = "needsAction").hasAccepted)
        assertFalse(eventWithAttendees(othersResponse = "declined").hasAccepted)
        assertFalse(eventWithAttendees(othersResponse = "tentative").hasAccepted)
        assertFalse(eventWithAttendees(othersResponse = "accepted").hasAccepted)

        assertFalse(eventWithAttendees("needsAction", "accepted").hasAccepted)
        assertFalse(eventWithAttendees("declined", "accepted").hasAccepted)
        assertTrue(eventWithAttendees("tentative", "accepted").hasAccepted)
        assertTrue(eventWithAttendees("accepted", "accepted").hasAccepted)
    }
}

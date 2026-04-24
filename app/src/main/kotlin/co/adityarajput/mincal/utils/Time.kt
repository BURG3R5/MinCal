package co.adityarajput.mincal.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val ZonedDateTime.asShortString: String
    get() = format(
        if (toLocalDate() == ZonedDateTime.now().toLocalDate())
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        else
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT),
    )

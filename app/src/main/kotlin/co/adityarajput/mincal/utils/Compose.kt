package co.adityarajput.mincal.utils

import androidx.compose.ui.text.AnnotatedString

fun AnnotatedString.Builder.appendBreakable(text: String) {
    text.forEach {
        append('\u200B')
        append(it)
    }
}

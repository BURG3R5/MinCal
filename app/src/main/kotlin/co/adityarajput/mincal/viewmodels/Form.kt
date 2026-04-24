package co.adityarajput.mincal.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

abstract class FormViewModel<P, E>(private val pages: List<P>) :
    ViewModel() where P : Enum<P>, E : Enum<E>, E : FormError {
    var isLoading by mutableStateOf(false)
        protected set

    var page by mutableStateOf(pages[0])
        protected set

    var error by mutableStateOf<E?>(null)
        protected set

    val isFirstPage get() = page.ordinal == 0
    val isLastPage get() = page.ordinal == pages.size - 1

    fun nextPage() {
        if (!isLastPage && error == null)
            page = pages[page.ordinal + 1]
    }

    fun previousPage() {
        if (!isFirstPage) {
            page = pages[page.ordinal - 1]
            error = null
        }
    }
}

interface FormError {
    val message: Int
}

package co.adityarajput.mincal.views.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.dim

@Composable
inline fun <T> SafeLazyColumn(
    items: List<T>?,
    noinline key: ((item: T) -> Any)?,
    emptyText: String,
    modifier: Modifier = Modifier,
    headerText: String? = null,
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit,
) {
    if (items == null) {
        Box(modifier, Alignment.Center) { CircularProgressIndicator() }
    } else if (items.isEmpty()) {
        Box(modifier, Alignment.Center) { Text(emptyText) }
    } else {
        if (headerText != null)
            Text(
                headerText,
                fontWeight = FontWeight.Medium,
            )
        LazyColumn(
            modifier,
            verticalArrangement = Arrangement.spacedBy(R.dimen.padding_small.dim),
        ) {
            items(items, key, itemContent = itemContent)
        }
    }
}

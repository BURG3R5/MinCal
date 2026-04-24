package co.adityarajput.mincal.views.components.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.adityarajput.mincal.R
import co.adityarajput.mincal.data.ChildOfColumn
import co.adityarajput.mincal.utils.dim

@Composable
fun FormPage(content: ChildOfColumn) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = R.dimen.padding_small.dim)
            .verticalScroll(rememberScrollState()),
        Arrangement.spacedBy(R.dimen.padding_medium.dim),
        content = content,
    )
}

package co.adityarajput.mincal.views.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.asIcon
import co.adityarajput.mincal.utils.dim
import co.adityarajput.mincal.utils.str

@Composable
fun ColumnScope.AppBar(
    title: Int = R.string.app_name,
    leading: Pair<Int, Int> = (R.drawable.calendar to R.string.app_logo),
    navigate: () -> Unit = {},
) {
    IconButton(
        navigate,
        Modifier.align(Alignment.CenterHorizontally),
        content = leading.asIcon,
    )
    Text(
        title.str,
        Modifier.align(Alignment.CenterHorizontally),
        style = MaterialTheme.typography.titleLarge,
    )
    Spacer(Modifier.height(R.dimen.padding_medium.dim))
}

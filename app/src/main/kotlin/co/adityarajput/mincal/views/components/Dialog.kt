package co.adityarajput.mincal.views.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog as ComposeDialog
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.dim

@Composable
fun Dialog(onDismissRequest: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    ComposeDialog(
        onDismissRequest,
        content = {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = R.dimen.padding_small.dim,
            ) { Column(Modifier.padding(R.dimen.padding_medium.dim)) { content() } }
        },
    )
}

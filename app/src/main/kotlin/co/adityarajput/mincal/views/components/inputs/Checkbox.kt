package co.adityarajput.mincal.views.components.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.dim

@Composable
fun Checkbox(
    value: Boolean, onValueChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier.toggleable(value, onValueChange = onValueChange),
        Arrangement.spacedBy(R.dimen.padding_small.dim),
        Alignment.CenterVertically,
    ) {
        Checkbox(value, null, enabled = enabled)
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
        )
    }
}

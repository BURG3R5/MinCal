package co.adityarajput.mincal.views.components.inputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Button(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick,
        modifier,
        enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.let {
                if (enabled) it else it.copy(alpha = 0.1f)
            },
        ),
    ) { Text(text) }
}

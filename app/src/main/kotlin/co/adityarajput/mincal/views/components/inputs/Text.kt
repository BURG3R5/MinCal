package co.adityarajput.mincal.views.components.inputs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction

@Composable
@SuppressLint("ModifierParameter")
fun TextField(
    label: String,
    state: TextFieldState,
    placeholder: String? = null,
    supporting: AnnotatedString? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isValid: Boolean = true,
    isValuePermitted: ((String) -> Boolean)? = null,
    keyboardAction: ImeAction = ImeAction.Next,
    maxLines: Int? = null,
) {
    OutlinedTextField(
        state,
        modifier.fillMaxWidth(),
        enabled,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(placeholder) } },
        supportingText = supporting?.let { { Text(it) } },
        isError = !isValid,
        inputTransformation = isValuePermitted?.let { { if (!it(asCharSequence() as String)) revertAllChanges() } },
        keyboardOptions = KeyboardOptions(imeAction = keyboardAction),
        lineLimits = when (maxLines) {
            null -> TextFieldLineLimits.Default
            1 -> TextFieldLineLimits.SingleLine
            else -> TextFieldLineLimits.MultiLine(1, maxLines)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    )
}

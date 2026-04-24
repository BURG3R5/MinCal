package co.adityarajput.mincal.views.components.forms

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.asText
import co.adityarajput.mincal.utils.str
import co.adityarajput.mincal.viewmodels.FormError
import co.adityarajput.mincal.viewmodels.FormViewModel
import co.adityarajput.mincal.views.Green

@Composable
fun <P, E> MultiPageForm(
    viewModel: FormViewModel<P, E>,
    onNext: (P) -> Unit = { viewModel.nextPage() },
    onPrevious: (P) -> Unit = { viewModel.previousPage() },
    canNext: Boolean = true,
    content: @Composable ColumnScope.(P) -> Unit,
) where P : Enum<P>, E : Enum<E>, E : FormError {
    AnimatedContent(
        viewModel.page,
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
        },
    ) {
        FormPage {
            content(it)

            if (viewModel.error != null)
                Text(viewModel.error!!.message.str, color = MaterialTheme.colorScheme.error)

            Row(
                Modifier.fillMaxWidth(),
                Arrangement.End,
            ) {
                if (!viewModel.isFirstPage)
                    TextButton(
                        { onPrevious(it) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    ) { R.string.back.asText() }

                TextButton(
                    { onNext(it) },
                    enabled = canNext,
                    colors = ButtonDefaults.textButtonColors(contentColor = Green),
                ) { (if (!viewModel.isLastPage) R.string.next else R.string.done).asText() }
            }
        }
    }
}

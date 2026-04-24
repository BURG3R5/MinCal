package co.adityarajput.mincal.views.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.*
import co.adityarajput.mincal.views.components.AppBar
import co.adityarajput.mincal.views.components.Dialog

@Composable
fun AboutScreen(goBack: () -> Unit = {}, goToLicensesScreen: () -> Unit = {}) {
    val context = LocalContext.current

    Dialog(goBack) {
        AppBar(R.string.about, R.drawable.info to R.string.about)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = R.dimen.padding_small.dim)
                .verticalScroll(rememberScrollState()),
            Arrangement.spacedBy(R.dimen.padding_medium.dim),
            Alignment.CenterHorizontally,
        ) {
            Text(
                AnnotatedString.fromHtml(
                    R.string.app_description.str,
                    TextLinkStyles(
                        SpanStyle(
                            MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        ),
                    ),
                ),
                Modifier.padding(R.dimen.padding_small.dim),
            )
            Row {
                listOf(
                    R.drawable.license to R.string.license to "https://github.com/BURG3R5/MinCal/blob/master/license",
                    R.drawable.code to R.string.source_code to "https://github.com/BURG3R5/MinCal",
                    R.drawable.help_doc to R.string.project_wiki to "https://github.com/BURG3R5/MinCal/wiki",
                ).forEach {
                    IconButton({ context.open(it.second) }) {
                        it.first.asIcon()
                    }
                }
                IconButton(goToLicensesScreen) { (R.drawable.graph to R.string.dependencies).asIcon() }
            }
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.End,
            ) {
                TextButton(
                    goBack,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) { R.string.done.asText() }
            }
        }
    }
}

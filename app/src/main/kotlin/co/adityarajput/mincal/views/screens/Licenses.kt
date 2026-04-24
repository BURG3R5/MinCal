package co.adityarajput.mincal.views.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.adityarajput.mincal.R
import co.adityarajput.mincal.utils.asText
import co.adityarajput.mincal.views.components.AppBar
import co.adityarajput.mincal.views.components.Dialog
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors

@Composable
fun LicensesScreen(goBack: () -> Unit = {}) {
    Dialog(goBack) {
        val libraries by produceLibraries(R.raw.aboutlibraries)

        AppBar(R.string.licenses, R.drawable.license to R.string.licenses)
        LibrariesContainer(
            libraries,
            Modifier.weight(1f),
            showDescription = true,
            colors = LibraryDefaults.libraryColors(
                Color.Transparent,
                dialogBackgroundColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        )
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

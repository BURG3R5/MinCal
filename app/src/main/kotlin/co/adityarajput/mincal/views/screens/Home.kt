package co.adityarajput.mincal.views.screens

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.mincal.Provider
import co.adityarajput.mincal.R
import co.adityarajput.mincal.data.CalendarInfo
import co.adityarajput.mincal.services.Storage
import co.adityarajput.mincal.services.scheduleWork
import co.adityarajput.mincal.utils.*
import co.adityarajput.mincal.viewmodels.HomeFormPage
import co.adityarajput.mincal.viewmodels.HomeViewModel
import co.adityarajput.mincal.views.LinkStyle
import co.adityarajput.mincal.views.components.AppBar
import co.adityarajput.mincal.views.components.Dialog
import co.adityarajput.mincal.views.components.SafeLazyColumn
import co.adityarajput.mincal.views.components.forms.MultiPageForm
import co.adityarajput.mincal.views.components.inputs.Button
import co.adityarajput.mincal.views.components.inputs.Checkbox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    goToAboutScreen: () -> Unit = {},
    viewmodel: HomeViewModel = viewModel(factory = Provider.Factory),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // region State
    // region Permissions
    var hasPermissions by remember { mutableStateOf(context.hasPermissions()) }

    val handler = remember { Handler(Looper.getMainLooper()) }
    val watcher = object : Runnable {
        override fun run() {
            hasPermissions = context.hasPermissions()
            if (!hasPermissions.all { it.value })
                handler.postDelayed(this, 500)
        }
    }
    DisposableEffect(Unit) {
        handler.post(watcher)
        onDispose { handler.removeCallbacksAndMessages(null) }
    }
    // endregion

    // region Credentials
    val fileSelector =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            coroutineScope.launch {
                uri?.let { context.contentResolver.openInputStream(it) }?.use {
                    viewmodel.readCredentialsJson(it.bufferedReader().readText())
                }
            }
        }
    // endregion

    var selectedCalendars by remember { mutableStateOf(emptySet<CalendarInfo>()) }
    // endregion

    Dialog {
        AppBar(navigate = goToAboutScreen)
        MultiPageForm(
            viewmodel,
            {
                coroutineScope.launch {
                    when (it) {
                        HomeFormPage.PERMISSIONS -> viewmodel.nextPage()

                        HomeFormPage.CREDENTIALS -> {
                            viewmodel.fetchCalendars()
                            viewmodel.nextPage()
                        }

                        HomeFormPage.CALENDARS -> {
                            Storage.calenders = selectedCalendars
                            viewmodel.fetchEvents()
                            viewmodel.nextPage()
                        }

                        HomeFormPage.EVENTS -> {
                            context.scheduleWork()
                            (context as? Activity)?.finish()
                        }
                    }
                }
            },
            {
                coroutineScope.launch {
                    when (it) {
                        HomeFormPage.PERMISSIONS -> {}

                        HomeFormPage.CREDENTIALS -> viewmodel.previousPage()

                        HomeFormPage.CALENDARS -> viewmodel.previousPage()

                        HomeFormPage.EVENTS -> {
                            viewmodel.fetchCalendars()
                            viewmodel.previousPage()
                        }
                    }
                }
            },
            when (viewmodel.page) {
                HomeFormPage.PERMISSIONS -> hasPermissions.all { it.value }

                HomeFormPage.CREDENTIALS -> !viewmodel.calendars.isNullOrEmpty()

                HomeFormPage.CALENDARS -> selectedCalendars.isNotEmpty()

                HomeFormPage.EVENTS -> true
            },
        ) { page ->
            when (page) {
                HomeFormPage.PERMISSIONS -> {
                    Text(
                        R.string.permissions_required.str,
                        fontWeight = FontWeight.Medium,
                    )
                    Permission.entries.forEach { permission ->
                        Checkbox(
                            hasPermissions.getValue(permission),
                            { context.request(permission) },
                            permission.text.str,
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = R.dimen.padding_small.dim),
                            !hasPermissions.getValue(permission),
                        )
                    }
                }

                HomeFormPage.CREDENTIALS -> {
                    Button(
                        R.string.select_credentials_json.str,
                        Modifier.align(Alignment.CenterHorizontally),
                    ) { fileSelector.launch(arrayOf("application/json")) }
                    Text(
                        AnnotatedString.fromHtml(
                            R.string.instructions_link.str,
                            LinkStyle,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                HomeFormPage.CALENDARS -> {
                    SafeLazyColumn(
                        viewmodel.calendars,
                        { it.id },
                        R.string.no_calendars.str,
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .heightIn(max = 300.dp),
                        R.string.select_calendars.str,
                    ) { calendar ->
                        Checkbox(
                            calendar in selectedCalendars,
                            {
                                if (it)
                                    selectedCalendars += calendar
                                else
                                    selectedCalendars -= calendar
                            },
                            if (calendar.primary != true) calendar.summary
                            else stringResource(R.string.primary, calendar.summary),
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = R.dimen.padding_small.dim),
                        )
                    }
                }

                HomeFormPage.EVENTS -> {
                    SafeLazyColumn(
                        viewmodel.events,
                        { it.id },
                        R.string.no_events.str,
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .heightIn(max = 300.dp),
                    ) { event ->
                        Card({}, Modifier.fillMaxWidth(), event.hasAccepted) {
                            Column(Modifier.padding(R.dimen.padding_small.dim)) {
                                Text(
                                    event.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    event.start.datetime.asShortString,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

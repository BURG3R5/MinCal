package co.adityarajput.mincal.views.screens

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import co.adityarajput.mincal.services.Calendar
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
import co.adityarajput.mincal.views.components.inputs.TextField
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
    val clientId = rememberTextFieldState(Storage.getClientId() ?: "")
    val isClientIdValid = remember(clientId.text) {
        clientId.text.matches(Regex("[a-z0-9\\-]+\\.apps\\.googleusercontent\\.com"))
    }
    val clientSecret = rememberTextFieldState(Storage.getClientSecret() ?: "")
    val isClientSecretValid = remember(clientSecret.text) {
        clientSecret.text.matches(Regex("[a-zA-Z0-9\\-_]+"))
    }
    var showAuthCodeInput by remember { mutableStateOf(false) }
    val authCode = rememberTextFieldState()
    // endregion

    var selectedCalendars by remember { mutableStateOf(emptySet<CalendarInfo>()) }
    // endregion

    Dialog {
        AppBar(navigate = goToAboutScreen)
        MultiPageForm(
            viewmodel,
            {
                when (it) {
                    HomeFormPage.CREDENTIALS -> {
                        coroutineScope.launch {
                            viewmodel.fetchAndSaveTokens(authCode.text as String)
                            viewmodel.fetchCalendars()
                            viewmodel.nextPage()
                        }
                    }

                    HomeFormPage.CALENDARS -> {
                        coroutineScope.launch {
                            viewmodel.saveCalendars(selectedCalendars)
                            if (hasPermissions.all { p -> p.value }) {
                                coroutineScope.launch {
                                    // INFO: Skip `HomeFormPage.PERMISSIONS`
                                    viewmodel.fetchEvents()
                                    viewmodel.nextPage()
                                }
                            }
                            viewmodel.nextPage()
                        }
                    }

                    HomeFormPage.PERMISSIONS -> {
                        coroutineScope.launch {
                            viewmodel.fetchEvents()
                            viewmodel.nextPage()
                        }
                    }

                    HomeFormPage.EVENTS -> {
                        coroutineScope.launch {
                            context.scheduleWork()
                            (context as? Activity)?.finish()
                        }
                    }
                }
            },
            {
                when (it) {
                    HomeFormPage.CREDENTIALS -> {}

                    HomeFormPage.CALENDARS ->
                        viewmodel.previousPage()

                    HomeFormPage.PERMISSIONS -> {
                        coroutineScope.launch {
                            viewmodel.fetchCalendars()
                            viewmodel.previousPage()
                        }
                    }

                    HomeFormPage.EVENTS -> {
                        if (hasPermissions.all { p -> p.value }) {
                            coroutineScope.launch {
                                // INFO: Skip `HomeFormPage.PERMISSIONS`
                                viewmodel.fetchCalendars()
                                viewmodel.previousPage()
                            }
                        }
                        viewmodel.previousPage()
                    }
                }
            },
            when (viewmodel.page) {
                HomeFormPage.CREDENTIALS -> authCode.text.isNotBlank()

                HomeFormPage.CALENDARS -> selectedCalendars.isNotEmpty()

                HomeFormPage.PERMISSIONS -> hasPermissions.all { it.value }

                HomeFormPage.EVENTS -> true
            },
        ) { page ->
            when (page) {
                HomeFormPage.CREDENTIALS -> {
                    TextField(
                        R.string.client_id.str,
                        clientId,
                        isValid = clientId.text.isBlank() || isClientIdValid,
                        maxLines = 1,
                    )
                    TextField(
                        R.string.client_secret.str,
                        clientSecret,
                        supporting = AnnotatedString.fromHtml(
                            R.string.client_creds_supporting.str,
                            LinkStyle,
                        ),
                        isValid = clientSecret.text.isBlank() || isClientSecretValid,
                        maxLines = 1,
                    )
                    Button(
                        R.string.authorize.str,
                        Modifier.align(Alignment.CenterHorizontally),
                        isClientIdValid,
                    ) {
                        Calendar.clientId = clientId.text as String
                        Calendar.clientSecret = clientSecret.text as String
                        context.open(Calendar.authUrl)
                        showAuthCodeInput = true
                    }
                    AnimatedVisibility(showAuthCodeInput) {
                        TextField(
                            R.string.auth_code.str,
                            authCode,
                            maxLines = 1,
                        )
                    }
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

                HomeFormPage.EVENTS -> {
                    SafeLazyColumn(
                        viewmodel.events,
                        { it.id },
                        R.string.no_events.str,
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .heightIn(max = 300.dp),
                    ) { event ->
                        Card(Modifier.fillMaxWidth()) {
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

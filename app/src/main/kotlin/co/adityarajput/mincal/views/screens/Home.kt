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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.adityarajput.mincal.Provider
import co.adityarajput.mincal.R
import co.adityarajput.mincal.data.Account
import co.adityarajput.mincal.data.CalendarInfo
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

    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    // endregion

    Dialog {
        AppBar(navigate = goToAboutScreen)
        MultiPageForm(
            viewmodel,
            {
                coroutineScope.launch {
                    when (it) {
                        HomeFormPage.PERMISSIONS -> viewmodel.nextPage()

                        HomeFormPage.CREDENTIALS -> viewmodel.nextPage()

                        HomeFormPage.CALENDARS -> {
                            viewmodel.saveSelectedCalendars(selectedCalendars)
                            viewmodel.fetchEvents()
                            selectedCalendars = emptySet()
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

                        HomeFormPage.CREDENTIALS -> viewmodel.cancelAccountSetup()

                        HomeFormPage.CALENDARS -> viewmodel.previousPage()

                        HomeFormPage.EVENTS -> {}
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
                    if (viewmodel.accounts.isNotEmpty()) {
                        Text(
                            R.string.explain_add_account.str,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                    Text(
                        AnnotatedString.fromHtml(
                            R.string.instructions_link.str,
                            LinkStyle,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                HomeFormPage.CALENDARS -> {
                    LaunchedEffect(viewmodel.pendingAccountId) {
                        selectedCalendars = emptySet()
                    }
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
                    Text(
                        buildAnnotatedString {
                            appendBreakable(R.string.accounts.str)
                            viewmodel.accounts.forEachIndexed { i, account ->
                                withLink(
                                    LinkAnnotation.Clickable(
                                        "account_$i",
                                        TextLinkStyles(
                                            SpanStyle(
                                                if (selectedAccount == account) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                textDecoration = TextDecoration.Underline,
                                            ),
                                        ),
                                    ) {
                                        if (selectedAccount != account) {
                                            selectedAccount = account
                                        } else {
                                            coroutineScope.launch {
                                                viewmodel.removeAccount(account.id)
                                                selectedAccount = null
                                            }
                                        }
                                    },
                                ) {
                                    appendBreakable(account.label ?: "Account $i")
                                }
                                appendBreakable(", ")
                            }
                            withLink(
                                LinkAnnotation.Clickable(
                                    "account_new",
                                    TextLinkStyles(
                                        SpanStyle(MaterialTheme.colorScheme.primary),
                                    ),
                                ) { viewmodel.initiateAccountSetup() },
                            ) {
                                appendBreakable("+")
                            }
                        },
                        style = LocalTextStyle.current.copy(
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            lineBreak = LineBreak.Unspecified,
                        ),
                    )
                    SafeLazyColumn(
                        viewmodel.events,
                        { "${it.accountId}-${it.id}-${it.start.datetime}" },
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
                                event.calendar?.summary?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
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

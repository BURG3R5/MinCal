@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("PropertyName")

package co.adityarajput.mincal.services

import android.net.Uri
import android.util.Log
import co.adityarajput.mincal.data.AuthTokens
import co.adityarajput.mincal.data.CalendarInfo
import co.adityarajput.mincal.data.EventInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.OffsetDateTime

object Calendar {
    lateinit var clientId: String
    lateinit var clientSecret: String

    val http = OkHttpClient()

    val authUrl: Uri
        get() = Uri.Builder().scheme("https").authority("accounts.google.com")
            .path("/o/oauth2/v2/auth")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
            .appendQueryParameter("prompt", "consent")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("access_type", "offline")
            .appendQueryParameter("scope", SCOPES.joinToString(" "))
            .build()

    suspend fun getTokens(authCode: String): AuthTokens? {
        return withContext(Dispatchers.IO) {
            try {
                val request = http.newCall(
                    Request.Builder().url("https://oauth2.googleapis.com/token")
                        .post(
                            FormBody.Builder().apply {
                                add("code", authCode)
                                add("client_id", clientId)
                                add("client_secret", clientSecret)
                                add("grant_type", "authorization_code")
                                add("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
                            }.build(),
                        ).build(),
                )

                request.execute().use {
                    if (!it.isSuccessful)
                        throw Exception("Request failed: ${it.body.string()}")

                    val response =
                        Json.decodeFromString<GetTokensResponse>(it.body.string())

                    return@withContext AuthTokens(
                        response.access_token,
                        response.refresh_token!!,
                        response.expires_in * 1000 + System.currentTimeMillis(),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get tokens", e)
            }

            return@withContext null
        }
    }

    suspend fun refreshTokens() {
        withContext(Dispatchers.IO) {
            try {
                val authTokens = Storage.getTokens()!!
                if (authTokens.validTill > System.currentTimeMillis() + 60_000)
                    return@withContext

                clientId = Storage.getClientId()!!
                clientSecret = Storage.getClientSecret()!!

                val request = http.newCall(
                    Request.Builder().url("https://oauth2.googleapis.com/token")
                        .post(
                            FormBody.Builder().apply {
                                add("client_id", clientId)
                                add("client_secret", clientSecret)
                                add("grant_type", "refresh_token")
                                add("refresh_token", authTokens.refreshToken)
                            }.build(),
                        ).build(),
                )

                request.execute().use {
                    if (!it.isSuccessful)
                        throw Exception("Request failed: ${it.body.string()}")

                    val response =
                        Json.decodeFromString<GetTokensResponse>(it.body.string())

                    Storage.saveCredentials(
                        AuthTokens(
                            response.access_token,
                            authTokens.refreshToken,
                            response.expires_in * 1000 + System.currentTimeMillis(),
                        ),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh tokens", e)
            }
        }
    }

    suspend fun getCalendars(): List<CalendarInfo>? {
        refreshTokens()

        return withContext(Dispatchers.IO) {
            try {
                val request = http.newCall(
                    Request.Builder()
                        .url(
                            HttpUrl.Builder()
                                .scheme("https")
                                .host("www.googleapis.com")
                                .addPathSegments("calendar/v3/users/me/calendarList")
                                .addQueryParameter("minAccessRole", "reader")
                                .build(),
                        )
                        .header("Authorization", "Bearer ${Storage.getTokens()!!.accessToken}")
                        .get().build(),
                )

                request.execute().use {
                    if (!it.isSuccessful)
                        throw Exception("Request failed: ${it.body.string()}")

                    val response =
                        Json.decodeFromString<GetCalendarsResponse>(it.body.string())

                    return@withContext response.items
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get calendars", e)
            }

            return@withContext null
        }
    }

    suspend fun getEvents(calendarId: String): List<EventInfo>? {
        refreshTokens()

        return withContext(Dispatchers.IO) {
            try {
                val request = http.newCall(
                    Request.Builder()
                        .url(
                            HttpUrl.Builder()
                                .scheme("https")
                                .host("www.googleapis.com")
                                .addEncodedPathSegments(
                                    "calendar/v3/calendars/${
                                        Uri.encode(
                                            calendarId,
                                            "@",
                                        )
                                    }/events",
                                )
                                .addQueryParameter("singleEvents", "true")
                                .addQueryParameter("orderBy", "startTime")
                                .addQueryParameter("timeMin", OffsetDateTime.now().toString())
                                .build(),
                        )
                        .header("Authorization", "Bearer ${Storage.getTokens()!!.accessToken}")
                        .get().build(),
                )

                request.execute().use {
                    if (!it.isSuccessful)
                        throw Exception("Request failed: ${it.body.string()}")

                    val response =
                        Json.decodeFromString<GetEventsResponse>(it.body.string())

                    return@withContext response.items
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get events for $calendarId", e)
            }

            return@withContext null
        }
    }

    private const val TAG = "Calendar"
    private val SCOPES = setOf(
        "https://www.googleapis.com/auth/calendar.calendarlist.readonly",
        "https://www.googleapis.com/auth/calendar.readonly",
        "https://www.googleapis.com/auth/calendar.events.readonly",
    )
}

@Serializable
@JsonIgnoreUnknownKeys
data class GetTokensResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String? = null,
    val scope: String,
    val token_type: String,
)

@Serializable
@JsonIgnoreUnknownKeys
data class GetCalendarsResponse(
    val items: List<CalendarInfo>,
)

@Serializable
@JsonIgnoreUnknownKeys
data class GetEventsResponse(
    val items: List<EventInfo>,
)

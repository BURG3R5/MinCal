package co.adityarajput.mincal.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import co.adityarajput.mincal.data.CalendarInfo
import co.adityarajput.mincal.data.Credentials
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

object Storage {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var crypto: Aead

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("mincal_prefs", Context.MODE_PRIVATE)
        TinkConfig.register()
        AeadConfig.register()

        crypto = AndroidKeysetManager.Builder()
            .withSharedPref(context, "mincal_keyset", "mincal_secure_prefs")
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri("android-keystore://mincal_master_key")
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    val areCredsStored
        get() = listOf(CLIENT_ID, CLIENT_SECRET, ACCESS_TOKEN, REFRESH_TOKEN, VALID_TILL)
            .all { sharedPreferences.contains(it) }

    var credentials: Credentials
        get() = Credentials(
            sharedPreferences.getString(CLIENT_ID, null)!!,
            decrypt(sharedPreferences.getString(CLIENT_SECRET, null)!!),
            decrypt(sharedPreferences.getString(ACCESS_TOKEN, null)!!),
            decrypt(sharedPreferences.getString(REFRESH_TOKEN, null)!!),
            sharedPreferences.getLong(VALID_TILL, 0L),
        )
        set(value) = sharedPreferences.edit {
            putString(CLIENT_ID, value.clientId)
            putString(CLIENT_SECRET, encrypt(value.clientSecret))
            putString(ACCESS_TOKEN, encrypt(value.accessToken))
            putString(REFRESH_TOKEN, encrypt(value.refreshToken))
            putLong(VALID_TILL, value.validTill)
        }

    val areCalendarsStored get() = sharedPreferences.contains(CALENDARS)

    var calenders: Set<CalendarInfo>
        get() = Json.decodeFromString(sharedPreferences.getString(CALENDARS, null)!!)
        set(value) = sharedPreferences.edit {
            putString(CALENDARS, Json.encodeToString(value))
        }

    private fun encrypt(text: String) =
        Base64.encodeToString(
            crypto.encrypt(
                text.toByteArray(StandardCharsets.UTF_8),
                null,
            ),
            Base64.DEFAULT,
        )

    private fun decrypt(text: String) =
        crypto.decrypt(
            Base64.decode(text, Base64.DEFAULT),
            null,
        ).toString(StandardCharsets.UTF_8)
}

const val CLIENT_ID = "client_id"
const val CLIENT_SECRET = "client_secret"
const val ACCESS_TOKEN = "access_token"
const val REFRESH_TOKEN = "refresh_token"
const val VALID_TILL = "valid_till"
const val CALENDARS = "calendars"

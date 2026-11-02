package co.adityarajput.mincal.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import co.adityarajput.mincal.data.Account
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
import java.util.UUID

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

    val areCredsStored get() = accounts.isNotEmpty()

    val areCalendarsStored get() = accounts.any { it.calendars.isNotEmpty() }

    var accounts: List<Account>
        get() = sharedPreferences.getString(ACCOUNTS, null)
            ?.let { decrypt(it) }
            ?.let { Json.decodeFromString(it) }
            ?: emptyList()
        set(value) = sharedPreferences.edit {
            putString(ACCOUNTS, encrypt(Json.encodeToString(value)))
        }

    fun addAccount(credentials: Credentials) =
        Account(UUID.randomUUID().toString(), credentials).also { upsertAccount(it) }

    fun getAccount(accountId: String) =
        accounts.find { it.id == accountId }

    fun upsertAccount(account: Account) {
        accounts = accounts.toMutableList().apply {
            val index = indexOfFirst { it.id == account.id }
            if (index < 0)
                add(account)
            else
                set(index, account)
        }
    }

    fun updateAccountCalendars(accountId: String, calendars: Set<CalendarInfo>) {
        val account = getAccount(accountId) ?: return
        upsertAccount(account.copy(calendars = calendars))
    }

    fun removeAccount(accountId: String) {
        accounts = accounts.filterNot { it.id == accountId }
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

const val ACCOUNTS = "accounts"

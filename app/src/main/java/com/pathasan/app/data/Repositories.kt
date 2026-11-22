package com.pathasan.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore by preferencesDataStore("pathasan_preferences")

data class AppPreferences(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val textSize: TextSize = TextSize.STANDARD,
    val notificationsEnabled: Boolean = true,
    val sessionUserId: Long? = null,
)

class PreferencesRepository(private val context: Context) {
    private val languageKey = stringPreferencesKey("language")
    private val textSizeKey = stringPreferencesKey("text_size")
    private val notificationsKey = booleanPreferencesKey("notifications")
    private val sessionKey = longPreferencesKey("session_user_id")

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { values ->
        AppPreferences(
            language = AppLanguage.entries.firstOrNull { it.name == values[languageKey] }
                ?: AppLanguage.ENGLISH,
            textSize = TextSize.entries.firstOrNull { it.name == values[textSizeKey] }
                ?: TextSize.STANDARD,
            notificationsEnabled = values[notificationsKey] ?: true,
            sessionUserId = values[sessionKey],
        )
    }

    suspend fun setLanguage(value: AppLanguage) = context.dataStore.edit { it[languageKey] = value.name }
    suspend fun setTextSize(value: TextSize) = context.dataStore.edit { it[textSizeKey] = value.name }
    suspend fun setNotifications(value: Boolean) = context.dataStore.edit { it[notificationsKey] = value }
    suspend fun setSession(userId: Long?) = context.dataStore.edit {
        if (userId == null) it.remove(sessionKey) else it[sessionKey] = userId
    }
}

class AuthRepository(
    private val accountDao: AccountDao,
    private val preferences: PreferencesRepository,
) {
    suspend fun signUp(name: String, email: String, password: String): Result<UserSession> = runCatching {
        require(accountDao.findByEmail(email) == null) { "An account with this email already exists." }
        val id = accountDao.insert(
            AccountEntity(fullName = name.trim(), email = email.trim().lowercase(), passwordHash = hash(password))
        )
        preferences.setSession(id)
        UserSession(id, name.trim(), email.trim().lowercase())
    }

    suspend fun login(email: String, password: String): Result<UserSession> = runCatching {
        val account = accountDao.findByEmail(email)
        require(account != null && account.passwordHash == hash(password)) { "Email or password is incorrect." }
        preferences.setSession(account.id)
        UserSession(account.id, account.fullName, account.email)
    }

    suspend fun logout() = preferences.setSession(null)

    suspend fun restoreSession(id: Long): UserSession? = accountDao.findById(id)?.let {
        UserSession(it.id, it.fullName, it.email)
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

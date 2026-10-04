package com.example.teman_belajar.utils.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")

        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val isFirstTimeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_TIME] ?: true
    }

    suspend fun setFirstTimeCompleted() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_TIME] = false
        }
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }

    val authTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }

    val userIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val userNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean, userId: String? = null, userName: String? = null, token: String? = null, refreshToken: String? = null) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn

            if (userId != null) {
                preferences[USER_ID] = userId
            } else {
                preferences.remove(USER_ID)
            }

            if (userName != null) {
                preferences[USER_NAME] = userName
            } else {
                preferences.remove(USER_NAME)
            }

            if (token != null) {
                preferences[AUTH_TOKEN] = token
            } else {
                preferences.remove(AUTH_TOKEN)
            }

            if (refreshToken != null) {
                preferences[REFRESH_TOKEN] = refreshToken
            } else {
                preferences.remove(REFRESH_TOKEN)
            }
        }
    }
}
package com.xiaobaotv.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private object Keys {
        val AUTO_NEXT_ENABLED = booleanPreferencesKey("auto_next_enabled")
    }

    val autoNextEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.AUTO_NEXT_ENABLED] ?: true
    }

    suspend fun setAutoNextEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTO_NEXT_ENABLED] = enabled
        }
    }
}

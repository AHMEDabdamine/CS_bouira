package com.example.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val SHOW_NAV_BUTTONS = booleanPreferencesKey("show_nav_buttons")
    val ENABLE_PINCH_ZOOM = booleanPreferencesKey("enable_pinch_zoom")
}

class SettingsManager(private val context: Context) {

    val showNavButtons: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[SettingsKeys.SHOW_NAV_BUTTONS] ?: true }

    val enablePinchZoom: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[SettingsKeys.ENABLE_PINCH_ZOOM] ?: true }

    suspend fun setShowNavButtons(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.SHOW_NAV_BUTTONS] = enabled
        }
    }

    suspend fun setEnablePinchZoom(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SettingsKeys.ENABLE_PINCH_ZOOM] = enabled
        }
    }
}

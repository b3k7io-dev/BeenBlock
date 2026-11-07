package com.beenblock.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.blockedAppsDataStore: DataStore<Preferences> by preferencesDataStore(name = "blocked_apps")

class BlockedAppsRepository(private val context: Context) {
    private val dataStore = context.applicationContext.blockedAppsDataStore
    private val blockedKey = stringSetPreferencesKey("blocked_packages")

    val blockedPackages: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[blockedKey] ?: emptySet()
    }

    suspend fun setBlocked(packageName: String, blocked: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[blockedKey]?.toMutableSet() ?: mutableSetOf()
            if (blocked) current.add(packageName) else current.remove(packageName)
            prefs[blockedKey] = current
        }
    }

    suspend fun toggle(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[blockedKey]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(packageName)) current.remove(packageName)
            prefs[blockedKey] = current
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs -> prefs[blockedKey] = emptySet() }
    }
}

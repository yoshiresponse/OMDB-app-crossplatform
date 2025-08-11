package com.example.omdb_kotlin.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "prefs")

class FavoritesStore(private val context: Context) {
    private val KEY_FAVORITES: Preferences.Key<Set<String>> = stringSetPreferencesKey("favorites")

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { it[KEY_FAVORITES] ?: emptySet() }

    suspend fun toggle(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FAVORITES] ?: emptySet()
            prefs[KEY_FAVORITES] = if (current.contains(id)) current - id else current + id
        }
    }

    suspend fun isFavorite(id: String): Boolean {
        return favoritesFlow.map { it.contains(id) }.first()
    }
}

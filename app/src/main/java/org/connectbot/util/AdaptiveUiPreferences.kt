/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2025 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension property to create/access the adaptive UI preferences DataStore.
 * The DataStore is created once and reused throughout the app lifecycle.
 */
val Context.adaptiveUiDataStore: DataStore<Preferences> by preferencesDataStore(name = "adaptive_ui_preferences")

/**
 * Manages preferences for adaptive UI behavior (tablet layouts, list pane visibility, etc.).
 * These preferences are stored using DataStore and automatically backed up via Android Auto Backup.
 */
@Singleton
class AdaptiveUiPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        /**
         * Whether to keep the list pane visible on tablets when viewing details.
         * Default: true (show list alongside details on tablets)
         */
        val KEEP_LIST_VISIBLE = booleanPreferencesKey("keep_list_visible")

        /**
         * Whether to automatically collapse the list pane after connecting to a host.
         * Applies to all connection types (new, reconnect, restored).
         * Default: false (keep list visible after connection)
         */
        val AUTO_COLLAPSE_AFTER_CONNECTION = booleanPreferencesKey("auto_collapse_after_connection")

        /**
         * Whether the navigation drawer is manually collapsed on large screens.
         * Default: false (drawer expanded on large screens)
         */
        val NAV_DRAWER_COLLAPSED = booleanPreferencesKey("nav_drawer_collapsed")
    }

    /**
     * Flow of the "keep list visible" preference value.
     */
    val keepListVisible: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEEP_LIST_VISIBLE] ?: true
        }

    /**
     * Flow of the "auto-collapse after connection" preference value.
     */
    val autoCollapseAfterConnection: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[AUTO_COLLAPSE_AFTER_CONNECTION] ?: false
        }

    /**
     * Flow of the "navigation drawer collapsed" preference value.
     */
    val navDrawerCollapsed: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[NAV_DRAWER_COLLAPSED] ?: false
        }

    /**
     * Update the "keep list visible" preference.
     */
    suspend fun setKeepListVisible(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEEP_LIST_VISIBLE] = value
        }
    }

    /**
     * Update the "auto-collapse after connection" preference.
     */
    suspend fun setAutoCollapse(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_COLLAPSE_AFTER_CONNECTION] = value
        }
    }

    /**
     * Update the "navigation drawer collapsed" preference.
     */
    suspend fun setNavDrawerCollapsed(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[NAV_DRAWER_COLLAPSED] = value
        }
    }
}

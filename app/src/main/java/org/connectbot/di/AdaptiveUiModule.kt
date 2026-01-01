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

package org.connectbot.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.connectbot.util.AdaptiveUiPreferences
import org.connectbot.util.adaptiveUiDataStore
import javax.inject.Singleton

/**
 * Hilt module providing dependencies for adaptive UI functionality.
 * Provides the DataStore for adaptive UI preferences.
 */
@Module
@InstallIn(SingletonComponent::class)
object AdaptiveUiModule {

    /**
     * Provides the DataStore for adaptive UI preferences.
     */
    @Provides
    @Singleton
    fun provideAdaptiveUiDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.adaptiveUiDataStore
    }

    /**
     * Provides the AdaptiveUiPreferences instance.
     */
    @Provides
    @Singleton
    fun provideAdaptiveUiPreferences(
        dataStore: DataStore<Preferences>
    ): AdaptiveUiPreferences {
        return AdaptiveUiPreferences(dataStore)
    }
}

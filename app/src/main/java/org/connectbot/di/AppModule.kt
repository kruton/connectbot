/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2018 Kenny Root, Jeffrey Sharkey
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

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import org.connectbot.App
import org.connectbot.AppExecutors
import org.connectbot.db.AppDatabase
import org.connectbot.db.PubkeyDatabase
import org.connectbot.db.dao.HostDao
import org.connectbot.db.dao.PubkeyDao
import org.connectbot.migrations.app.Migration25To26
import org.connectbot.migrations.pubkey.Migration2To3
import org.connectbot.repo.ConnectedHosts
import org.connectbot.repo.ConnectedHostsImpl
import org.connectbot.repo.EntropyPoolImpl
import org.connectbot.ui.BellSound
import org.connectbot.ui.BellSoundImpl
import org.connectbot.ui.common.ClipboardImpl
import org.connectbot.ui.common.IClipboard
import org.connectbot.ui.entropy.EntropyPool
import org.connectbot.util.ConnectionQueue
import org.connectbot.util.ConnectionQueueImpl
import org.connectbot.util.ITimingSource
import org.connectbot.util.TimingDelayHandler
import org.connectbot.util.TimingDelayHandlerImpl
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
	@Singleton
	@Provides
	fun provideHostDb(app: App, appExecutors: AppExecutors): AppDatabase {
		return Room.databaseBuilder(
			app.applicationContext,
			AppDatabase::class.java,
			"hosts"
		).addMigrations(Migration25To26())
			.setQueryExecutor(appExecutors.diskIO())
			.addCallback(AppDatabase.InitialData())
			.build()
	}

	@Singleton
	@Provides
	fun provideUserDao(db: AppDatabase): HostDao = db.hostDao()

	@Singleton
	@Provides
	fun providePubkeyDb(app: App, appExecutors: AppExecutors): PubkeyDatabase {
		return Room.databaseBuilder(
			app.applicationContext,
			PubkeyDatabase::class.java,
			"pubkeys"
		).addMigrations(Migration2To3())
			.setQueryExecutor(appExecutors.diskIO())
			.build()
	}

	@Singleton
	@Provides
	fun providePubkeyDao(db: PubkeyDatabase): PubkeyDao {
		return db.pubkeyDao()
	}

	@Singleton
	@Provides
	fun provideSharedPreferences(app: App): SharedPreferences =
		PreferenceManager.getDefaultSharedPreferences(app);

	@Singleton
	@Provides
	fun provideConnectedHosts(app: App, connectionQueue: ConnectionQueue): ConnectedHosts =
		ConnectedHostsImpl(app, connectionQueue)

	@Singleton
	@Provides
	fun providesEntropyPool(): EntropyPool = EntropyPoolImpl()

	@Singleton
	@Provides
	fun providesTimingSource(): ITimingSource = object : ITimingSource {
			override fun currentTimeMillis(): Long = System.currentTimeMillis()
		}

	@Singleton
	@Provides
	fun providesClipboard(app: App): IClipboard {
		return ClipboardImpl(app)
	}

	@Singleton
	@Provides
	fun providesConnectionQueue(): ConnectionQueue = ConnectionQueueImpl()

	@Provides
	fun providesTimingDelayHandler(): TimingDelayHandler = TimingDelayHandlerImpl()

	@Singleton
	@Provides
	fun providesBellSound(app: App, appExecutors: AppExecutors, sharedPreferences: SharedPreferences): BellSound =
		BellSoundImpl(app, appExecutors, sharedPreferences)
}

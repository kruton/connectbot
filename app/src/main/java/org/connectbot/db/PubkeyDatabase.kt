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

package org.connectbot.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.connectbot.db.dao.PubkeyDao
import org.connectbot.db.entity.Pubkey
import org.connectbot.migrations.pubkey.Migration2To3
import org.connectbot.util.SingletonHolder

@Database(
		version = 3,
		entities = [
			Pubkey::class
		]
)
@TypeConverters(Converters::class)
abstract class PubkeyDatabase : RoomDatabase() {
	abstract fun pubkeyDao(): PubkeyDao

	companion object : SingletonHolder<PubkeyDatabase, Context>({
		Room.databaseBuilder(
				it.applicationContext,
				PubkeyDatabase::class.java,
				"pubkeys"
		).addMigrations(Migration2To3())
				.allowMainThreadQueries()
				.build()
	})
}

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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import org.connectbot.db.dao.HostDao
import org.connectbot.db.entity.Color
import org.connectbot.db.entity.ColorScheme
import org.connectbot.db.entity.DefaultColor
import org.connectbot.db.entity.Host
import org.connectbot.db.entity.KnownHost
import org.connectbot.db.entity.PortForward

@Database(
		version = 26,
		entities = [
			Color::class,
			ColorScheme::class,
			DefaultColor::class,
			Host::class,
			KnownHost::class,
			PortForward::class
		]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun hostDao(): HostDao

	class InitialData : RoomDatabase.Callback() {
		override fun onCreate(db: SupportSQLiteDatabase) {
			db.execSQL("INSERT INTO ColorScheme (id, name) VALUES (${ColorScheme.DEFAULT_COLOR_SCHEME}, 'Default')")
			db.execSQL("INSERT INTO DefaultColor (schemeId, fg, bg) VALUES (1, ${ColorScheme.DEFAULT_FG_COLOR}, ${ColorScheme.DEFAULT_BG_COLOR})")
		}
	}
}

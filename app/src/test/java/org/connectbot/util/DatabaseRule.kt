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

package org.connectbot.util

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.ExternalResource

class DatabaseRule<T : Any, V : RoomDatabase>(private val databaseClass: Class<out V>, private val daoFetch: (V) -> T) : ExternalResource() {
	private lateinit var database: V
	lateinit var dao: T

	private var callbacks: Array<RoomDatabase.Callback> = arrayOf()

	fun addCallback(callback: RoomDatabase.Callback): DatabaseRule<T, V> {
		callbacks += callback
		return this
	}

	override fun before() {
		val context: Context = ApplicationProvider.getApplicationContext()
		try {
			val builder = Room.inMemoryDatabaseBuilder(context, databaseClass)
					.allowMainThreadQueries()
			callbacks.forEach { builder.addCallback(it) }
			database = builder.build()
		} catch (e: Exception) {
			Log.i("DatabaseRule", "Cannot build database for tests: ", e)
		}
		dao = daoFetch(database)
	}

	override fun after() {
		database.close()
	}
}

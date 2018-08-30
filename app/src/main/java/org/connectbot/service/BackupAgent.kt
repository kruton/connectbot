/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2010 Kenny Root, Jeffrey Sharkey
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

package org.connectbot.service

import android.app.backup.BackupAgentHelper
import android.app.backup.FileBackupHelper
import android.app.backup.SharedPreferencesBackupHelper
import android.preference.PreferenceManager
import org.connectbot.db.AppDatabase
import org.connectbot.db.PubkeyDatabase
import org.connectbot.util.PreferenceConstants
import timber.log.Timber
import javax.inject.Inject

/**
 * ConnectBot's backup agent. This is only loaded on API 8 and later by
 * reading the AndroidManifest.xml, so it shouldn't affect any minimum
 * SDK level.
 */
class BackupAgent: BackupAgentHelper() {
	@Inject
	lateinit var hostDb: AppDatabase

	@Inject
	lateinit var pubkeyDb: PubkeyDatabase

	override fun onCreate() {
		Timber.d("onCreate called")

		val prefs = PreferenceManager.getDefaultSharedPreferences(this)

		val prefsHelper = SharedPreferencesBackupHelper(this, packageName + "_preferences")
		addHelper(PreferenceConstants.BACKUP_PREF_KEY, prefsHelper)

		hostDb.query("PRAGMA wal_checkpoint(full);", null)
		val hosts = FileBackupHelper(this,
				"../databases/hosts",
				"../databases/hosts-wal",
				"../databases/hosts-shm")
		addHelper("hosts", hosts)

		if (prefs.getBoolean(PreferenceConstants.BACKUP_KEYS, PreferenceConstants.BACKUP_KEYS_DEFAULT)) {
			pubkeyDb.query("PRAGMA wal_checkpoint(full);", null)
			val pubkeys = FileBackupHelper(this,
					"../databases/pubkeys",
					"../databases/pubkeys-wal",
					"../databases/pubkeys-shm")
			addHelper("pubkeys", pubkeys)
		}
	}
}

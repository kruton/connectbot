/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
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

package org.connectbot.util;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * Public Key Encryption database. Contains private and public key pairs
 * for public key authentication.
 *
 * @author Kenny Root
 */
public class PubkeyDatabase {
	public final static String TAG = "CB.PubkeyDatabase";

	public final static String DB_NAME = "pubkeys";
	public final static int DB_VERSION = 2;

	// table pubkeys

	public void onRobustUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws SQLiteException {
			switch (oldVersion) {
			case 1:
				db.execSQL("ALTER TABLE pubkeys ADD COLUMN confirmuse INTEGER DEFAULT 0");
				db.execSQL("ALTER TABLE pubkeys ADD COLUMN lifetime INTEGER DEFAULT 0");
			}
	}
}

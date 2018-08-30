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

import java.nio.charset.Charset;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * Contains information about various SSH hosts, include public hostkey if known
 * from previous sessions.
 *
 * @author jsharkey
 */
public class HostDatabase {

	public final static String TAG = "CB.HostDatabase";

	//	Database name: hosts
	//	Final DB_VERSION = 25

	private static final String CREATE_TABLE_HOSTS = "CREATE TABLE hosts"
			+ "(_id INTEGER PRIMARY KEY, "
			+ "nickname TEXT, "
			+ "protocol TEXT DEFAULT 'ssh', "
			+ "username TEXT, "
			+ "hostname TEXT, "
			+ "port INTEGER, "
			+ "lastconnect INTEGER, "
			+ "color TEXT, "
			+ "usekeys TEXT, "
			+ "useauthagent TEXT, "
			+ "postlogin TEXT, "
			+ "pubkeyid INTEGER DEFAULT -1, "
			+ "delkey TEXT DEFAULT 'del', "
			+ "fontsize INTEGER, "
			+ "wantsession TEXT DEFAULT 'false', "
			+ "compression TEXT DEFAULT 'false', "
			+ "encoding TEXT DEFAULT 'utf-8', "
			+ "stayconnected TEXT DEFAULT 'false', "
			+ "quickdisconnect TEXT DEFAULT 'false')";

	private static final String CREATE_TABLE_COLOR_DEFAULTS =
			"CREATE TABLE colorDefaults"
			+ "(scheme INTEGER NOT NULL, "
			+ "fg INTEGER NOT NULL DEFAULT 7, "
			+ "bg INTEGER NOT NULL DEFAULT 0)";

	private static final String CREATE_TABLE_COLOR_DEFAULTS_INDEX =
			"CREATE INDEX colorDefaultsschemeindex ON colorDefaults (scheme)";

	// table hosts
	// table knownhosts
	// index knownhostshostidindex
	// table portforwards
	// index portforwardshostidindex
	// table colors
	// index colorsschemeindex
	// table colorDefaults
	// index colorDefaultsschemeindex

	private void createTables(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_HOSTS);

		db.execSQL("CREATE TABLE knownhosts"
				+ " (_id INTEGER PRIMARY KEY, "
				+ "hostid INTEGER, "
				+ "hostkeyalgo TEXT, "
				+ "hostkey BLOB)");

		db.execSQL("CREATE INDEX knownhostshostidindex ON "
				+ "knownhosts (hostid);");

		db.execSQL("CREATE TABLE portforwards"
				+ " (_id INTEGER PRIMARY KEY, hostid INTEGER, nickname TEXT, "
				+ "type TEXT NOT NULL DEFAULT 'local', sourceport INTEGER NOT NULL DEFAULT 8080, "
				+ "destaddr TEXT, destport TEXT)");

		db.execSQL("CREATE INDEX portforwardshostidindex ON portforwards (hostid);");

		db.execSQL("CREATE TABLE colors (_id INTEGER PRIMARY KEY, number INTEGER, "
				+ "value INTEGER, scheme INTEGER)");

		db.execSQL("CREATE INDEX colorsschemeindex ON colors (scheme);");

		db.execSQL(CREATE_TABLE_COLOR_DEFAULTS);
		db.execSQL(CREATE_TABLE_COLOR_DEFAULTS_INDEX);
	}

	public void onRobustUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) throws SQLiteException {
		// Versions of the database before the Android Market release will be
		// shot without warning.
		if (oldVersion <= 9) {
			db.execSQL("DROP TABLE IF EXISTS hosts");
			return;
		}

		switch (oldVersion) {
		case 10:
			db.execSQL("ALTER TABLE hosts ADD COLUMN pubkeyid INTEGER DEFAULT " + (long) -1);
			// fall through
		case 11:
			db.execSQL("CREATE TABLE portforwards"
					+ " (_id INTEGER PRIMARY KEY, hostid INTEGER, nickname TEXT, "
					+ "type TEXT NOT NULL DEFAULT 'local', sourceport INTEGER NOT NULL DEFAULT 8080, "
					+ "destaddr TEXT, destport INTEGER)");
			// fall through
		case 12:
			db.execSQL("ALTER TABLE hosts"
					+ " ADD COLUMN wantsession TEXT DEFAULT 'true'");
			// fall through
		case 13:
			db.execSQL("ALTER TABLE hosts"
					+ " ADD COLUMN compression TEXT DEFAULT 'false'");
			// fall through
		case 14:
			db.execSQL("ALTER TABLE hosts"
					+ " ADD COLUMN encoding TEXT DEFAULT '" + Charset.defaultCharset().name() + "'");
			// fall through
		case 15:
			db.execSQL("ALTER TABLE hosts"
					+ " ADD COLUMN protocol TEXT DEFAULT 'ssh'");
			// fall through
		case 16:
			db.execSQL("ALTER TABLE hosts"
					+ " ADD COLUMN delkey TEXT DEFAULT 'del'");
			// fall through
		case 17:
			db.execSQL("CREATE INDEX portforwardshostidindex ON "
					+ "portforwards (hostid);");

			// Add colors
			db.execSQL("CREATE TABLE colors"
					+ " (_id INTEGER PRIMARY KEY, "
					+ "number INTEGER, "
					+ "value INTEGER, "
					+ "scheme INTEGER)");
			db.execSQL("CREATE INDEX colorsschemeindex ON colors (scheme);");
			// fall through
		case 18:
			db.execSQL("ALTER TABLE hosts ADD COLUMN useauthagent TEXT DEFAULT 'no'");
			// fall through
		case 19:
			db.execSQL("ALTER TABLE hosts"
					+ " ADD COLUMN stayconnected TEXT");
			// fall through
		case 20:
			db.execSQL("ALTER TABLE hosts ADD COLUMN fontsize INTEGER");
			// fall through
		case 21:
			db.execSQL("DROP TABLE IF EXISTS colorDefaults");
			db.execSQL(CREATE_TABLE_COLOR_DEFAULTS);
			db.execSQL(CREATE_TABLE_COLOR_DEFAULTS_INDEX);
			// fall through
		case 22:
			db.execSQL("ALTER TABLE hosts ADD COLUMN quickdisconnect TEXT DEFAULT 'false'");
			// fall through
		case 24:
			// Move all the existing known hostkeys into their own table.
			db.execSQL("DROP TABLE IF EXISTS knownhosts");
			db.execSQL("CREATE TABLE knownhosts(_id INTEGER PRIMARY KEY, hostid INTEGER, hostkeyalgo TEXT, hostkey BLOB)");
			db.execSQL("INSERT INTO knownhosts (hostid, hostkeyalgo, hostkey) SELECT _id, hostkeyalgo, hostkey FROM hosts");
			// Work around SQLite not supporting dropping columns
			db.execSQL("DROP TABLE IF EXISTS hosts_upgrade");
			db.execSQL("CREATE TABLE hosts_upgrade (_id INTEGER PRIMARY KEY, "
					+ "nickname TEXT, protocol TEXT DEFAULT 'ssh', username TEXT, hostname TEXT, port INTEGER, "
					+ "lastconnect INTEGER, color TEXT, usekeys TEXT, useauthagent TEXT, postlogin TEXT, "
					+ "pubkeyid INTEGER DEFAULT -1, delkey TEXT DEFAULT 'del', fontsize INTEGER, "
					+ "wantsession TEXT DEFAULT 'true', compression TEXT DEFAULT 'false', encoding TEXT DEFAULT 'utf-8', "
					+ "stayconnected TEXT DEFAULT 'false', quickdisconnect TEXT DEFAULT 'false')");
			db.execSQL("INSERT INTO hosts_upgrade SELECT _id, nickname, protocol, username, hostname, "
					+ "port, lastconnect, color, usekeys, useauthagent, postlogin, pubkeyid, "
					+ "delkey, fontsize, wantsession, compression, encoding, stayconnected, "
					+ "quickdisconnect FROM hosts");
			db.execSQL("DROP TABLE hosts");
			db.execSQL("ALTER TABLE hosts_upgrade RENAME TO hosts");
		}
	}
}

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

package org.connectbot.migrations.app

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration25To26 : Migration(25, 26) {
	override fun migrate(db: SupportSQLiteDatabase) {
		db.execSQL("DROP TABLE IF EXISTS Host;")
		db.execSQL("CREATE TABLE Host (" +
				"id INTEGER NOT NULL PRIMARY KEY," +
				"nickname TEXT NOT NULL," +
				"protocol TEXT NOT NULL DEFAULT 'ssh'," +
				"username TEXT," +
				"hostname TEXT," +
				"port INTEGER NOT NULL DEFAULT 0," +
				"lastConnect INTEGER NOT NULL DEFAULT -1," +
				"color TEXT NOT NULL DEFAULT 'gray'," +
				"useKeys INTEGER NOT NULL DEFAULT 1," +
				"useAuthAgent TEXT NOT NULL DEFAULT 'no'," +
				"postLogin TEXT," +
				"pubkeyId INTEGER NOT NULL DEFAULT -2," +
				"delKey TEXT NOT NULL DEFAULT 'del'," +
				"fontSize INTEGER NOT NULL DEFAULT 10," +
				"wantSession INTEGER NOT NULL DEFAULT 1," +
				"compression INTEGER NOT NULL DEFAULT 0," +
				"encoding TEXT NOT NULL DEFAULT 'utf-8'," +
				"stayConnected INTEGER NOT NULL DEFAULT 0," +
				"quickDisconnect INTEGER NOT NULL DEFAULT 0" +
				");")
		db.execSQL("INSERT INTO Host(" +
				"id, nickname, protocol, username, hostname, port, lastConnect, color, useAuthAgent, postLogin, pubkeyId, delKey, encoding" +
				") SELECT " +
				"_id, nickname, protocol, username, hostname, port, lastconnect, CASE WHEN color IS NULL THEN 'gray' ELSE color END, useauthagent, postlogin, pubkeyid, delkey, encoding" +
				" FROM hosts WHERE hosts.color NOT NULL;")

		db.execSQL("UPDATE Host SET wantSession = (SELECT CASE WHEN wantsession = 'true' THEN 1 ELSE 0 END FROM hosts WHERE _id = Host.id)")
		db.execSQL("UPDATE Host SET stayConnected = (SELECT CASE WHEN stayconnected = 'true' THEN 1 ELSE 0 END FROM hosts WHERE _id = Host.id)")
		db.execSQL("UPDATE Host SET quickDisconnect = (SELECT CASE WHEN quickdisconnect = 'true' THEN 1 ELSE 0 END FROM hosts WHERE _id = Host.id)")
		db.execSQL("UPDATE Host SET useKeys = (SELECT CASE WHEN usekeys = 'true' THEN 1 ELSE 0 END FROM hosts WHERE _id = Host.id)")
		db.execSQL("UPDATE Host SET compression = (SELECT CASE WHEN compression = 'true' THEN 1 ELSE 0 END FROM hosts WHERE _id = Host.id)")
		db.execSQL("DROP TABLE hosts;")

		db.execSQL("DROP TABLE IF EXISTS KnownHost;")
		db.execSQL("CREATE TABLE KnownHost(id INTEGER NOT NULL PRIMARY KEY, hostId INTEGER NOT NULL, hostKey BLOB NOT NULL, hostKeyAlgorithm TEXT NOT NULL, FOREIGN KEY(hostId) REFERENCES Host(id));")
		db.execSQL("INSERT INTO KnownHost(id, hostId, hostKey, hostKeyAlgorithm) SELECT _id, hostid, hostkey, hostkeyalgo FROM knownhosts;")
		db.execSQL("CREATE INDEX index_KnownHost_hostId ON KnownHost(hostId);")
		db.execSQL("DROP TABLE knownhosts;")

		db.execSQL("DROP TABLE IF EXISTS PortForward;")
		db.execSQL("CREATE TABLE PortForward(id INTEGER NOT NULL PRIMARY KEY, destinationAddress TEXT, destinationPort INTEGER, hostId INTEGER NOT NULL, nickname TEXT NOT NULL, sourcePort INTEGER NOT NULL, type TEXT NOT NULL DEFAULT 'local', FOREIGN KEY(hostId) REFERENCES Host(id));")
		db.execSQL("INSERT INTO PortForward(id, destinationAddress, destinationPort, hostId, nickname, sourcePort) SELECT _id, destaddr, destport, hostid, nickname, sourceport FROM portforwards;")
		db.execSQL("CREATE INDEX index_PortForward_hostId ON PortForward(hostId);")
		db.execSQL("DROP TABLE portforwards;")

		db.execSQL("DROP TABLE IF EXISTS ColorScheme;")
		db.execSQL("CREATE TABLE ColorScheme(id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL);")
		db.execSQL("INSERT INTO ColorScheme(id, name) VALUES (0, 'Default');")

		db.execSQL("DROP TABLE IF EXISTS Color;")
		db.execSQL("CREATE TABLE Color(id INTEGER NOT NULL PRIMARY KEY, schemeId INTEGER NOT NULL, number INTEGER NOT NULL, value INTEGER NOT NULL, FOREIGN KEY(schemeId) REFERENCES ColorScheme(id));")
		db.execSQL("INSERT INTO Color(id, schemeId, number, value) SELECT _id, 0, number, value FROM colors;")
		db.execSQL("CREATE INDEX index_Color_schemeId ON Color(schemeId);")
		db.execSQL("DROP TABLE colors;")

		db.execSQL("DROP TABLE IF EXISTS DefaultColor;")
		db.execSQL("CREATE TABLE DefaultColor(id INTEGER NOT NULL PRIMARY KEY, schemeId INTEGER NOT NULL DEFAULT 0, fg INTEGER NOT NULL DEFAULT 7, bg INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(schemeId) REFERENCES ColorScheme(id));")
		db.execSQL("INSERT INTO DefaultColor DEFAULT VALUES;")
		db.execSQL("CREATE UNIQUE INDEX index_DefaultColor_schemeId ON DefaultColor(schemeId);")
	}
}

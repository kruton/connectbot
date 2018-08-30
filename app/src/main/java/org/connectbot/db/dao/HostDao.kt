/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2018 Kenny Root
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

package org.connectbot.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.connectbot.db.entity.Color
import org.connectbot.db.entity.ColorScheme
import org.connectbot.db.entity.DefaultColor
import org.connectbot.db.entity.Host
import org.connectbot.db.entity.KnownHost
import org.connectbot.db.entity.PortForward
import org.connectbot.db.entity.Pubkey

@Dao
interface HostDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun insertHost(host: Host): Long

	@Update(onConflict = OnConflictStrategy.IGNORE)
	fun updateHost(host: Host): Int

	@Transaction
	fun upsertHost(host: Host): Long {
		val hostId = insertHost(host)
		when (hostId == -1L) {
			true -> updateHost(host)
			false -> host.id = hostId
		}
		return host.id
	}

	@Delete
	fun deleteHost(host: Host): Int

	@Query("UPDATE Host SET lastConnect = :timeMs WHERE id = :hostId")
	fun updateLastConnectTime(hostId: Long, timeMs: Long)

	@Query("SELECT * FROM Host ORDER BY color")
	fun allHostsByColor(): LiveData<List<Host>>

	@Query("SELECT * FROM Host ORDER BY nickname")
	fun allHostsByNickname(): LiveData<List<Host>>

	@Query("SELECT * FROM Host WHERE id = :id LIMIT 1")
	fun getHostById(id: Long): LiveData<Host>

	@Query("SELECT * FROM Host WHERE nickname = :nickname")
	fun getHostByNickname(nickname: String): List<Host>

	@Query("SELECT * FROM Host WHERE protocol = 'ssh' AND nickname = :nickname AND hostname = :hostname AND port = :port AND username = :username LIMIT 1")
	fun getHostForSSH(nickname: String, hostname: String, port: Int, username: String): Host?

	@Query("SELECT * FROM Host WHERE protocol = 'local' AND nickname = :nickname LIMIT 1")
	fun getHostForLocal(nickname: String): Host?

	@Query("SELECT * FROM Host WHERE protocol = 'telnet' AND nickname = :nickname AND hostname = :hostname AND port = :port LIMIT 1")
	fun getHostForTelnet(nickname: String, hostname: String, port: Int): Host?

	@Query("SELECT * FROM KnownHost WHERE hostId = :id")
	fun getKnownHostsForHostId(id: Long): List<KnownHost>

	@Query("SELECT hostKeyAlgorithm FROM KnownHost WHERE hostId = :hostId")
	fun getHostKeyAlgorithmsForHost(hostId: Long): List<String>

	@Query("SELECT * FROM KnownHost WHERE hostId = :id AND hostKeyAlgorithm = :algo AND hostKey = :key")
	fun getKnownHost(id: Long, algo: String, key: ByteArray): KnownHost?

	@Insert
	fun addKnownHost(knownHost: KnownHost): Long?

	@Delete
	fun deleteKnownHost(knownHost: KnownHost): Int

	@Query("SELECT PortForward.* FROM PortForward LEFT JOIN Host ON Host.id = PortForward.hostId WHERE host.id = :id")
	fun getPortForwardsForHost(id: Long): LiveData<List<PortForward>>

	@Query("SELECT PortForward.* FROM PortForward WHERE id = :id")
	fun getPortForwardById(id: Long): LiveData<PortForward>

	@Transaction
	fun stopUsingPubkey(pubkey: Pubkey) = stopUsingPubkeyId(pubkey.id)

	@Query("UPDATE Host SET pubkeyId = -1 WHERE pubkeyId = :pubkeyId")
	fun stopUsingPubkeyId(pubkeyId: Long)

	@Query("SELECT * FROM ColorScheme WHERE id = :id")
	fun getColorScheme(id: Long): LiveData<ColorScheme>

	@Query("SELECT * FROM Color WHERE schemeId = :scheme ORDER BY number")
	fun getColorsForScheme(scheme: Long): LiveData<List<Color>>

	@Query("SELECT * FROM DefaultColor WHERE schemeId = :scheme LIMIT 1")
	fun getDefaultColorsForScheme(scheme: Long): LiveData<DefaultColor>

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun addColor(color: Color): Long

	@Update(onConflict = OnConflictStrategy.IGNORE)
	fun updateColor(color: Color): Int

	@Transaction
	fun upsertColor(color: Color): Long {
		val colorId = addColor(color)
		when (colorId == -1L) {
			true -> updateColor(color)
			false -> color.id = colorId
		}
		return color.id
	}

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun addDefaultColor(color: DefaultColor): Long

	@Update(onConflict = OnConflictStrategy.IGNORE)
	fun updateDefaultColor(color: DefaultColor): Int

	@Transaction
	fun insertOrUpdateDefaultColor(color: DefaultColor): Long {
		val colorId = addDefaultColor(color)
		when (colorId == -1L) {
			true -> updateDefaultColor(color)
			false -> color.id = colorId
		}
		return color.id
	}

	@Query("DELETE FROM Color WHERE schemeId = :schemeId")
	fun deleteColorsForScheme(schemeId: Long)

	@Query("UPDATE DefaultColor SET fg = " + ColorScheme.DEFAULT_FG_COLOR + ", bg = " + ColorScheme.DEFAULT_BG_COLOR + " WHERE schemeId = :schemeId")
	fun resetDefaultColorsForScheme(schemeId: Long)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun addPortForward(pf: PortForward): Long

	@Update(onConflict = OnConflictStrategy.IGNORE)
	fun updatePortForward(pf: PortForward): Int

	@Transaction
	fun upsertPortForward(pf: PortForward): Long {
		val pfId = addPortForward(pf)
		when (pfId == -1L) {
			true -> updatePortForward(pf)
			false -> pf.id = pfId
		}
		return pf.id
	}

	@Delete
	fun deletePortForward(portForward: PortForward): Int
}

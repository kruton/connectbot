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

package org.connectbot.repo

import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.connectbot.AppExecutors
import org.connectbot.db.dao.HostDao
import org.connectbot.db.dao.PubkeyDao
import org.connectbot.db.entity.Color
import org.connectbot.db.entity.DefaultColor
import org.connectbot.db.entity.Host
import org.connectbot.db.entity.KnownHost
import org.connectbot.db.entity.PortForward
import org.connectbot.db.entity.Pubkey
import org.connectbot.testing.OpenForTesting
import org.connectbot.util.Colors
import org.connectbot.util.PreferenceConstants
import org.connectbot.util.SharedPreferenceBooleanLiveData
import org.connectbot.util.SharedPreferenceStringLiveData
import org.connectbot.util.SharedPreferencesIntLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class DataRepository @Inject constructor(
	private val appExecutors: AppExecutors,
	private val hostDao: HostDao,
	private val pubkeyDao: PubkeyDao,
	private val connectedHosts: ConnectedHosts,
	sharedPreferences: SharedPreferences
) {
	private val _allHostsByColor = MediatorLiveData<List<Host>>().also { hosts ->
		hosts.addSource(hostDao.allHostsByColor()) {
			hosts.value = mergeHosts(connectedHosts.connectedHosts.value, it) }
		hosts.addSource(connectedHosts.connectedHosts) {
			hosts.value = mergeHosts(it, hostDao.allHostsByColor().value) }
	}

	val allHostsByColor: LiveData<List<Host>>
		get() = _allHostsByColor

	private val _allHostsByNickname = MediatorLiveData<List<Host>>().also { hosts ->
		hosts.addSource(hostDao.allHostsByNickname()) {
			hosts.value = mergeHosts(connectedHosts.connectedHosts.value, it) }
		hosts.addSource(connectedHosts.connectedHosts) {
			hosts.value = mergeHosts(it, hostDao.allHostsByNickname().value) }
	}

	val allHostsByNickname: LiveData<List<Host>>
		get() = _allHostsByNickname

	private fun mergeHosts(connected: List<Host>?, db: List<Host>?): List<Host> {
		val dbOrEmpty = db ?: emptyList()
		return connected?.union(dbOrEmpty)?.toList() ?: dbOrEmpty
	}

	@MainThread
	fun getHost(hostId: Long) = hostDao.getHostById(hostId)

	@MainThread
	fun upsertHost(host: Host, @MainThread callback: (Long?) -> Unit) {
		appExecutors.diskIO().execute {
			val id = hostDao.upsertHost(host)
			appExecutors.mainThreadExecutor().execute { callback(id) }
		}
	}

	@MainThread
	fun deleteHost(host: Host) {
		appExecutors.diskIO().execute {
			hostDao.deleteHost(host)
		}
	}

	@MainThread
	fun updateLastConnectTime(host: Host, timeMs: Long) {
		appExecutors.diskIO().execute {
			hostDao.updateLastConnectTime(host.id, timeMs)
		}
	}

	@MainThread
	fun addKnownHost(host: Host, knownHost: KnownHost, @MainThread callback: (Long?) -> Unit) {
		appExecutors.diskIO().execute {
			knownHost.hostId = host.id
			hostDao.addKnownHost(knownHost)?.let { callback(it) }
		}
	}

	/**
	 * @param uri URI for the target service.
	 * @param callback function to call when answer has been obtained.
	 *
	 */
	@MainThread
	fun findHost(uri: Uri, @MainThread callback: (Host?) -> Unit) {
		appExecutors.diskIO().execute {
			val answer = when (uri.scheme) {
				"ssh" -> hostDao.getHostForSSH(uri.fragment!!, uri.host!!, uri.port, uri.userInfo!!)
				"telnet" -> hostDao.getHostForTelnet(uri.fragment!!, uri.host!!, uri.port)
				"local" -> hostDao.getHostForLocal(uri.fragment!!)
				else -> null
			}
			appExecutors.mainThreadExecutor().execute { callback(answer) }
		}
	}

	@MainThread
	fun upsertPubkey(pubkey: Pubkey, callback: (Long?) -> Unit) {
		appExecutors.diskIO().execute {
			val pubkeyId = pubkeyDao.upsertPubkey(pubkey)
			appExecutors.mainThreadExecutor().execute { callback(pubkeyId) }
		}
	}

	@MainThread
	fun deletePubkey(pubkey: Pubkey) {
		appExecutors.diskIO().execute {
			pubkeyDao.deletePubkey(pubkey)
		}
	}

	@MainThread
	fun getColorScheme(id: Long) = hostDao.getColorScheme(id)

	@MainThread
	fun getColorsForScheme(schemeId: Long): LiveData<List<Color>> =
		Transformations.map(hostDao.getColorsForScheme(schemeId)) {
			leftJoinList(schemeId, Colors.mapToColors(), it)
		}

	private fun leftJoinList(schemeId: Long, leftList: List<Color>, rightList: List<Color>): List<Color> {
		var rightIndex = 0
		return leftList.map { color ->
			if (rightIndex < rightList.size && rightList[rightIndex].number == color.number) {
				rightList[rightIndex++]
			} else {
				color.schemeId = schemeId
				color
			}
		}
	}

	@MainThread
	fun getDefaultColorsForScheme(schemeId: Long) = hostDao.getDefaultColorsForScheme(schemeId)

	@MainThread
	fun updateDefaultColor(defaultColor: DefaultColor) {
		appExecutors.diskIO().execute {
			hostDao.updateDefaultColor(defaultColor)
		}
	}

	@MainThread
	fun upsertColor(color: Color) {
		appExecutors.diskIO().execute {
			hostDao.upsertColor(color)
		}
	}

	@MainThread
	fun resetColorsForScheme(schemeId: Long) {
		appExecutors.diskIO().execute {
			hostDao.deleteColorsForScheme(schemeId)
		}
	}

	@MainThread
	fun resetDefaultColorsForScheme(schemeId: Long) {
		appExecutors.diskIO().execute {
			hostDao.resetDefaultColorsForScheme(schemeId)
		}
	}

	val allPubkeys = pubkeyDao.allPubkeys()

	fun getPubkeyById(pubkeyId: Long) = pubkeyDao.getPubkeyById(pubkeyId)

	fun getPortForwardsByHost(hostId: Long) = hostDao.getPortForwardsForHost(hostId)

	fun getPortForwardById(id: Long) = hostDao.getPortForwardById(id)

	fun upsertPortForward(pf: PortForward, callback: (Long?) -> Unit) {
		appExecutors.diskIO().execute {
			val portForwardId = hostDao.upsertPortForward(pf)
			appExecutors.mainThreadExecutor().execute { callback(portForwardId) }
		}
	}

	fun deletePortForward(portForward: PortForward) {
		appExecutors.diskIO().execute {
			hostDao.deletePortForward(portForward)
		}
	}

	fun openConnection(host: Host) = connectedHosts.requestConnection(host)

	val emulation = SharedPreferenceStringLiveData(sharedPreferences, PreferenceConstants.EMULATION, "utf-8")

	val scrollBackSize = SharedPreferencesIntLiveData(sharedPreferences, PreferenceConstants.SCROLLBACK, 100)

	val deviceHasHardKeyboard = false //appres.getConfiguration().keyboard == Configuration.KEYBOARD_QWERTY);
	val hardKeyboardHidden = MutableLiveData<Boolean>().also { it.value = true }
	val keymode = SharedPreferenceStringLiveData(sharedPreferences, PreferenceConstants.KEYMODE, PreferenceConstants.KEYMODE_NONE)
	val shiftedNumbersAreFKeysOnHardKeyboard = SharedPreferenceBooleanLiveData(sharedPreferences, PreferenceConstants.SHIFT_FKEYS, false)
	val controlNumbersAreFKeysOnSoftKeyboard = SharedPreferenceBooleanLiveData(sharedPreferences, PreferenceConstants.CTRL_FKEYS, false)
	val volumeKeysChangeFontSize = SharedPreferenceBooleanLiveData(sharedPreferences, PreferenceConstants.VOLUME_FONT, true)
	val stickyModifiers = SharedPreferenceStringLiveData(sharedPreferences, PreferenceConstants.STICKY_MODIFIERS, PreferenceConstants.NO)
}

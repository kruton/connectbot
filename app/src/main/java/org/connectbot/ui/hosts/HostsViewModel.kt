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

package org.connectbot.ui.hosts

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.R
import org.connectbot.db.entity.Host
import org.connectbot.repo.DataRepository
import org.connectbot.ui.common.Event
import org.connectbot.util.ClickLiveData
import org.connectbot.util.ConnectionQueue
import org.connectbot.util.MenuItemClickLiveData
import org.connectbot.util.PreferenceConstants
import org.connectbot.util.ResourceProvider
import org.connectbot.util.SharedPreferenceBooleanLiveData
import org.connectbot.util.ViewClickLiveData
import timber.log.Timber
import javax.inject.Inject

class HostsViewModel @Inject constructor(
	sharedPreferences: SharedPreferences,
	private val dataRepository: DataRepository,
	private val connectionQueue: ConnectionQueue
) : ViewModel() {
	val resourceProvider = MutableLiveData<ResourceProvider>()

	val addHostClicked = ViewClickLiveData()

	val pubkeysButtonClicked = MenuItemClickLiveData()

	val colorsButtonClicked = MenuItemClickLiveData()

	val settingsButtonClicked = MenuItemClickLiveData()

	val helpButtonClicked = MenuItemClickLiveData()

	val sortByColor = SharedPreferenceBooleanLiveData(sharedPreferences, PreferenceConstants.SORT_BY_COLOR)

	val hosts: LiveData<List<Host>> = Transformations.switchMap(sortByColor) { byColor ->
		when (byColor) {
			true -> dataRepository.allHostsByColor
			false -> dataRepository.allHostsByNickname
		}
	}

	private val _hostDisconnectClick = ClickLiveData<Host>()
	val hostDisconnectClick: LiveData<Host>
		get() = _hostDisconnectClick

	private val _hostEditClick = ClickLiveData<Host>()
	val hostEditClick: LiveData<Host>
		get() = _hostEditClick

	private val _hostPortForwardsClick = ClickLiveData<Host>()
	val hostPortForwardClick: LiveData<Host>
		get() = _hostPortForwardsClick

	private val _hostDeleteClick = ClickLiveData<Host>()
	val hostDeleteClick: LiveData<Host>
		get() = _hostDeleteClick

	private val hostContextItemsResources = arrayOf(
		Pair(R.string.list_host_disconnect, _hostDisconnectClick),
		Pair(R.string.list_host_edit, _hostEditClick),
		Pair(R.string.list_host_portforwards, _hostPortForwardsClick),
		Pair(R.string.list_host_delete, _hostDeleteClick)
	)

	private val _hostContextMenuItems = MediatorLiveData<List<Pair<String, (Host) -> Unit>>>().also {
		it.addSource(resourceProvider) { rp ->
			it.value = hostContextItemsResources.map { pair ->
				Pair(rp.getString(pair.first), { host: Host -> pair.second.value = host })
			}
		}
	}

	val hostContextMenuItems: LiveData<List<Pair<String, (Host) -> Unit>>>
		get() = _hostContextMenuItems

	val emptyList: LiveData<Boolean> = Transformations.map(hosts) { Timber.d("Host list is %s", it); it.isNullOrEmpty() }

	val consoleNavigation = ClickLiveData<Event<Int>>()

	fun onSortByColorClicked() {
		sortByColor.value = true
	}

	fun onSortByNameClicked() {
		sortByColor.value = false
	}

	fun deleteHostClicked(host: Host) {
		dataRepository.deleteHost(host)
	}

	fun onHostClicked(host: Host) {
		val consoleRequestId = connectionQueue.addRequest(host)
		dataRepository.openConnection(host)
		consoleNavigation.value = Event(consoleRequestId)
	}
}

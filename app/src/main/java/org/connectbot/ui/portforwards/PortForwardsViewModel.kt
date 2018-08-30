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

package org.connectbot.ui.portforwards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.R
import org.connectbot.db.entity.PortForward
import org.connectbot.repo.DataRepository
import org.connectbot.ui.common.IResourceProvider
import org.connectbot.util.ClickLiveData
import org.connectbot.util.ViewClickLiveData
import javax.inject.Inject

class PortForwardsViewModel @Inject constructor(
	private val repository: DataRepository
): ViewModel() {
	val resourceProvider = MutableLiveData<IResourceProvider>()

	fun onPortForwardDeleteConfirmed(portForward: PortForward) {
		repository.deletePortForward(portForward)
	}

	val hostId = MutableLiveData<Long>()

	val addPortForwardClicked = ViewClickLiveData()

	val portForwards: LiveData<List<PortForward>> = Transformations.switchMap(hostId) {
		repository.getPortForwardsByHost(it)
	}

	val emptyList: LiveData<Boolean> = Transformations.map(portForwards) { it.isNullOrEmpty() }

	private val _portForwardDeleteClick = ClickLiveData<PortForward>()
	val portForwardDeleteClick: LiveData<PortForward>
		get() = _portForwardDeleteClick

	private val portForwardContextItemsResources = arrayOf(
		Pair(R.string.portforward_delete, _portForwardDeleteClick)
	)

	private val _portForwardContextMenuItems = MediatorLiveData<List<Pair<String, (PortForward) -> Unit>>>().also {
		it.addSource(resourceProvider) { rp ->
			it.value = portForwardContextItemsResources.map { pair ->
				Pair(rp.getString(pair.first), { portForward: PortForward -> pair.second.value = portForward })
			}
		}
	}

	val portForwardContextMenuItems: LiveData<List<Pair<String, (PortForward) -> Unit>>>
		get() = _portForwardContextMenuItems
}

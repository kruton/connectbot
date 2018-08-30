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

package org.connectbot.ui.portforwardedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.db.entity.PortForward
import org.connectbot.repo.DataRepository
import org.connectbot.ui.common.IResourceProvider
import org.connectbot.util.ClickLiveData
import org.connectbot.util.ViewClickLiveData
import javax.inject.Inject

class PortForwardEditViewModel @Inject constructor(
	private val executors: AppExecutors,
	private val dataRepository: DataRepository
) : ViewModel() {
	val resourceProvider = MutableLiveData<IResourceProvider>()

	private var _existingPortForward = false

	val hostId = MutableLiveData<Long>()

	val existingPortForward: Boolean
		get() = _existingPortForward

	private val _portForwardId = MutableLiveData<Long>()

	fun onPortForwardId(value: Long) {
		_existingPortForward = value != 0L
		if (value != 0L) _portForwardId.value = value
	}

	val portForward: LiveData<PortForward> = Transformations.switchMap(_portForwardId) {
		dataRepository.getPortForwardById(it)
	}

	val nickname = MediatorLiveData<String>().also {
		it.addSource(portForward) { portForward ->
			it.value = portForward.nickname
		}
	}

	val _type = MediatorLiveData<PortForward.PortForwardType>().also {
		it.value = PortForward.PortForwardType.LOCAL
		it.addSource(portForward) { portForward ->
			it.value = portForward.type
		}
	}

	private val _typeNamesValues = MediatorLiveData<List<Pair<String, PortForward.PortForwardType>>>().also {
		it.addSource(resourceProvider) { rp ->
			executors.diskIO().execute {
				rp?.useTypedArrayPair(R.array.list_portforward_types, R.array.list_portforward_types_values) { namesRes, valuesRes ->
					it.postValue(List(namesRes.length()) { i ->
						val name = namesRes.getText(i).toString()
						val value = valuesRes.getText(i).toString()
						Pair(name, PortForward.PortForwardType.values().find { it.toString() == value }!!)
					})
				}
			}
		}
	}

	val typeNamesValues: LiveData<List<Pair<String, PortForward.PortForwardType>>>
		get() = _typeNamesValues

	val type = MediatorLiveData<String>().also {
		fun updateType() {
			val target = _type.value
			it.value = _typeNamesValues.value?.find { pair -> pair.second == target }?.first
		}

		it.addSource(_type) { updateType() }
		it.addSource(_typeNamesValues) { updateType() }
	}

	val typeClicked = ViewClickLiveData()

	fun onTypeSelected(value: PortForward.PortForwardType) {
		_type.value = value
	}

	private val _sourcePort = MediatorLiveData<Int>().also {
		it.addSource(portForward) { portForward ->
			it.value = portForward.sourcePort
		}
	}

	val sourcePortText: LiveData<String> = Transformations.map(_sourcePort) { it.toString() }

	var userEditedSourcePort = false

	private val _sourcePortError = MediatorLiveData<String>().also {
		it.addSource(_sourcePort) { checkSourcePortForError() }
		it.addSource(resourceProvider) { checkSourcePortForError() }
	}

	private fun checkSourcePortForError() {
		_sourcePortError.value =  when {
			!userEditedSourcePort -> null
			_sourcePort.value == null -> resourceProvider.value?.getString(R.string.portforward_error_no_source_port)
			else -> null
		}
	}

	val sourcePortError: LiveData<String>
		get() = _sourcePortError

	fun onSourcePortChanged(value: String) {
		userEditedSourcePort = true
		value.toIntOrNull()?.let {
			_sourcePort.value = it.coerceIn(1 until 65536)
		}
	}

	private val _destinationRegex = "^(.*):([0-9]{1,5})$".toRegex()

	private fun splitDestinationInput(value: String): Pair<String, Int>? {
		val match = _destinationRegex.find(value)
		return when {
			match == null -> null
			match.groupValues.size != 3 -> null
			match.groupValues[2].toIntOrNull() == null -> null
			else -> Pair(match.groupValues[1], match.groupValues[2].toInt())
		}
	}

	private val _destination = MediatorLiveData<Pair<String, Int>>().also {
		it.addSource(portForward) { portForward ->
			val address = portForward.destinationAddress
			val port = portForward.destinationPort
			when {
				address != null && port != null -> it.value = Pair(address, port)
			}
		}
	}

	val destination: LiveData<String> = Transformations.map(_destination) {
		it.first + ":" + it.second
	}

	private val _destinationError = MediatorLiveData<String>().also {
		it.addSource(_destination) { checkForDestinationError() }
		it.addSource(resourceProvider) { checkForDestinationError() }
	}

	private fun checkForDestinationError() {
		_destinationError.value = when {
			destinationVisible.value == false -> null
			!userEditedDestination -> null
			_destination.value == null -> resourceProvider.value?.getString(R.string.portforward_error_no_destination)
			userEnteredInvalidDestination -> resourceProvider.value?.getString(R.string.portforward_error_invalid_destination)
			else -> null
		}
	}

	val destinationError: LiveData<String>
		get() = _destinationError

	var userEditedDestination = false

	var userEnteredInvalidDestination = false

	fun onDestinationChanged(value: String) {
		userEditedDestination = true

		val destPair = splitDestinationInput(value)
		when {
			destPair != null -> {
				userEnteredInvalidDestination = false
				_destination.value = Pair(destPair.first, destPair.second)
			}
			else -> userEnteredInvalidDestination = true
		}
		checkForDestinationError()
	}

	val destinationVisible: LiveData<Boolean> = Transformations.map(_type) {
		it != PortForward.PortForwardType.DYNAMIC5
	}

	fun onSaveButtonClicked() {
		userEditedDestination = true
		userEditedSourcePort = true

		checkForDestinationError()
		checkSourcePortForError()

		if (_sourcePortError.value != null || _destinationError.value != null)
			return

		val pf = PortForward()
		hostId.value?.let { pf.hostId = it }
		nickname.value?.let { pf.nickname = it }
		_type.value?.let { pf.type = it }
		_sourcePort.value?.let { pf.sourcePort = it }
		_destination.value?.let {
			pf.destinationAddress = it.first
			pf.destinationPort = it.second
		}

		dataRepository.upsertPortForward(pf, ::onPortForwardUpserted)
	}

	private val _fragmentFinished = ClickLiveData<Boolean>()

	val fragmentFinished: LiveData<Boolean> = _fragmentFinished

	private fun onPortForwardUpserted(id: Long?) {
		id?.let { _portForwardId.value = it }
		_fragmentFinished.value = true
	}
}

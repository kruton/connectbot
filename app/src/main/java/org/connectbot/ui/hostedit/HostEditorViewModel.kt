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

package org.connectbot.ui.hostedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.db.Converters
import org.connectbot.db.entity.Host
import org.connectbot.db.entity.Host.Companion.MAXIMUM_FONT_SIZE
import org.connectbot.db.entity.Host.Companion.MINIMUM_FONT_SIZE
import org.connectbot.db.entity.Pubkey
import org.connectbot.repo.DataRepository
import org.connectbot.transport.TransportFactory
import org.connectbot.ui.common.IResourceProvider
import org.connectbot.util.ClickLiveData
import org.connectbot.util.DescriptionUpdatingMediatorLiveData
import org.connectbot.util.ViewClickLiveData
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

class HostEditorViewModel @Inject constructor(
	executors: AppExecutors,
	private val dataRepository: DataRepository
) : ViewModel() {
	companion object {
		private const val URI_LISTENER_EXPANDED_BY_DEFAULT = false
		private const val MAXIMUM_SEEK_BAR_RANGE = MAXIMUM_FONT_SIZE - MINIMUM_FONT_SIZE
	}

	val protocols = MutableLiveData<List<Pair<String, String>>>().also {
		val transportList = TransportFactory.getTransportNames()
		it.value = List(transportList.size) { index ->
			Pair(transportList[index], transportList[index])
		}
	}

	val resourceProvider = MutableLiveData<IResourceProvider>()

	private val _hostId = MutableLiveData<Long>()

	fun onHostId(value: Long) {
		if (value != 0L) {
			_hostId.value = value
			nicknameUpdatedByUser = true
			_existingHost = true
		}
	}

	private var _existingHost = false

	val existingHost: Boolean
		get() = _existingHost

	private val _host: LiveData<Host> = Transformations.switchMap(_hostId) { dataRepository.getHost(it) }

	val protocolsClicked = ViewClickLiveData()

	private val _protocol = MediatorLiveData<String>().also {
		it.value = "ssh"
		it.addSource(_host) { host ->
			it.value = host.protocol }
	}

	val protocol: LiveData<String>
		get() = _protocol

	fun onProtocolSelected(value: String) {
		val transport = TransportFactory.getTransport(value) ?: return
		val oldTransport = _protocol.value?.let { TransportFactory.getTransport(it) }

		if (oldTransport == transport)
			return

		if (oldTransport?.defaultPort == _portNumber.value) {
			_portNumber.value = transport.defaultPort
		}

		_protocol.value = value
		onQuickConnectInputsChanged()
	}

	private val _quickConnectHint = MediatorLiveData<String>().also {
		fun updateHint() {
			resourceProvider.value?.let { rp ->
				it.value = TransportFactory.getFormatHint(_protocol.value, rp) }
		}

		it.addSource(_protocol) { updateHint() }
		it.addSource(resourceProvider) { updateHint() }
	}

	val quickConnectHint: LiveData<String>
		get() = _quickConnectHint

	private val _uriListenerExpanded = MutableLiveData<Boolean>().also {
		it.value = URI_LISTENER_EXPANDED_BY_DEFAULT
	}

	val uriListenerExpanded: LiveData<Boolean>
		get() = _uriListenerExpanded

	fun toggleListenerExpanded() {
		_uriListenerExpanded.value = !(_uriListenerExpanded.value ?: !URI_LISTENER_EXPANDED_BY_DEFAULT)
	}

	private val _username = MediatorLiveData<String>().also {
		it.addSource(_host) { host ->
			it.value = host.username }
	}

	val username: LiveData<String>
		get() = _username

	var usernameFocused = false

	private val _usernameVisible = MediatorLiveData<Boolean>().also {
		fun shouldBeVisible(uriExpanded: Boolean?, protocol: String?) =
			uriExpanded == true && protocol == SSH.getProtocolName()

		it.addSource(_protocol) { protocol ->
			it.value = shouldBeVisible(_uriListenerExpanded.value, protocol) }
		it.addSource(_uriListenerExpanded) { expanded ->
			it.value = shouldBeVisible(expanded, _protocol.value) }
	}

	val usernameVisible: LiveData<Boolean>
		get() = _usernameVisible

	fun onUsernameChanged(value: String) {
		if (usernameFocused) {
			_username.value = value
			onQuickConnectInputsChanged()
		}
	}

	private val _hostname = MediatorLiveData<String>().also {
		it.addSource(_host) { host ->
			it.value = host.hostname }
	}

	val hostname: LiveData<String>
		get() = _hostname

	var hostnameFocused = false

	private val _hostnameVisible = MediatorLiveData<Boolean>().also {
		fun shouldBeVisible(uriExpanded: Boolean?, protocol: String?) =
			uriExpanded == true && protocol != Local.getProtocolName()

		it.addSource(_protocol) { protocol ->
			it.value = shouldBeVisible(_uriListenerExpanded.value, protocol) }
		it.addSource(_uriListenerExpanded) { expanded ->
			it.value = shouldBeVisible(expanded, _protocol.value) }
	}

	val hostnameVisible: LiveData<Boolean>
		get() = _hostnameVisible

	fun onHostnameChanged(value: String) {
		if (hostnameFocused) {
			_hostname.value = value
			onQuickConnectInputsChanged()
		}
	}

	private val _portNumber = MediatorLiveData<Int>().also {
		it.value = 22
		it.addSource(_host) { host -> it.value = host.port }
	}

	val port: LiveData<String> = Transformations.map(_portNumber) { it.toString() }

	var portFocused = false

	private val _portVisible = MediatorLiveData<Boolean>().also {
		fun shouldBeVisible(uriExpanded: Boolean?, protocol: String?) =
			uriExpanded == true && protocol != Local.getProtocolName()

		it.addSource(_protocol) { protocol ->
			it.value = shouldBeVisible(_uriListenerExpanded.value, protocol) }
		it.addSource(_uriListenerExpanded) { expanded ->
			it.value = shouldBeVisible(expanded, _protocol.value) }
	}

	val portVisible: LiveData<Boolean>
		get() = _portVisible

	fun onPortChanged(value: String) {
		if (portFocused) {
			try {
				_portNumber.value = value.toInt().coerceIn(1, 65535)
				onQuickConnectInputsChanged()
			} catch (ignored: java.lang.NumberFormatException) {
			}
		}
	}

	private val _expanderVisible = MediatorLiveData<Boolean>().also {
		it.value = true
		it.addSource(_protocol) { newProtocol ->
			it.value = newProtocol != Local.getProtocolName() }
	}

	val expanderVisible: LiveData<Boolean>
		get() = _expanderVisible

	private val _nickname = MediatorLiveData<String>().also {
		it.addSource(_host) { host ->
			it.value = host.nickname }
	}

	val nickname: LiveData<String>
		get() = _nickname

	var nicknameFocused = false

	private var nicknameUpdatedByUser = false

	fun onNicknameUpdated(value: String) {
		if (nicknameFocused) {
			nicknameUpdatedByUser = true
			_nickname.value = value
		}
	}

	private val _quickConnectString = MediatorLiveData<String>().also {
		it.addSource(_host) { host ->
			it.value = TransportFactory.hostToString(host) }
	}

	val quickConnectString: LiveData<String>
		get() = _quickConnectString

	private val _quickConnectVisible: LiveData<Boolean> = Transformations.map(_protocol) {
		it != Local.getProtocolName()
	}

	val quickConnectVisible: LiveData<Boolean>
		get() = _quickConnectVisible

	var quickConnectFocused = false

	private fun onQuickConnectInputsChanged() {
		val host = Host()
		host.protocol = _protocol.value ?: "ssh"
		host.username = _username.value
		host.hostname = _hostname.value
		host.port = _portNumber.value ?: 22

		val connectionString = TransportFactory.hostToString(host)

		_quickConnectString.value = connectionString
		if (!nicknameUpdatedByUser && connectionString.isNotBlank()) {
			_nickname.value = connectionString
		}
	}

	fun onQuickConnectChanged(value: String) {
		if (quickConnectFocused) {
			val protocolCopy = _protocol.value
			val host = TransportFactory.createHost(protocolCopy, value) ?: return
			_username.value = host.username ?: ""
			_hostname.value = host.hostname ?: ""
			_portNumber.value = host.port

			_quickConnectString.value = value
			if (!nicknameUpdatedByUser) {
				_nickname.value = value
			}
		}
	}

	val colorsClicked = ViewClickLiveData()

	private val _colorNamesValues = MediatorLiveData<List<Pair<String, Host.HostColor>>>().also {
		it.addSource(resourceProvider) { rp ->
			executors.diskIO().execute {
				rp?.useTypedArrayPair(R.array.list_colors, R.array.list_color_values) { namesRes, valuesRes ->
					it.postValue(List(namesRes.length()) { i ->
						val name = namesRes.getText(i).toString()
						val value = valuesRes.getText(i).toString()
						Pair(name, Host.HostColor.values().find { color -> color.value == value }!!)
					})
				}
			}
		}
	}

	val colorNamesValues: LiveData<List<Pair<String, Host.HostColor>>>
		get() = _colorNamesValues

	private val _color = MediatorLiveData<Host.HostColor>().also {
		it.value = Host.HostColor.GRAY
		it.addSource(_colorNamesValues) { _ -> // nothing needed here, but we want _colorNamesValues observed.
		}
		it.addSource(_host) { host ->
			it.value = host.color }
	}

	val color: LiveData<Host.HostColor>
		get() = _color

	fun onColorSelected(selectedColor: Host.HostColor) {
		_color.value = selectedColor
	}

	private val _fontSize = MediatorLiveData<Int>().also {
		it.value = Host.DEFAULT_FONT_SIZE
		it.addSource(_host) { host -> it.value = host.fontSize }
	}

	val fontSize: LiveData<Int>
		get() = _fontSize

	private fun clampFontSize(size: Int) {
		_fontSize.value = size.coerceIn(MINIMUM_FONT_SIZE, MAXIMUM_FONT_SIZE)
	}

	private val _seekBarOffset = MediatorLiveData<Int>().also {
		it.addSource(_fontSize) { newFontSize ->
			it.value = _fontSize.value?.let { newFontSize - MINIMUM_FONT_SIZE }
		}
	}

	val seekBarOffset: LiveData<Int>
		get() = _seekBarOffset

	fun onFontSizeChanged(text: String) {
		try {
			clampFontSize(text.toInt())
		} catch (e: NumberFormatException) {
			_fontSize.value = _fontSize.value
		}
	}

	fun onFontSizeSeekChanged(progress: Int, fromUser: Boolean) {
		if (fromUser)
			clampFontSize(MINIMUM_FONT_SIZE + progress)
	}

	val maximumFontSeekValue get() = MAXIMUM_SEEK_BAR_RANGE

	private val _usePubkey = MediatorLiveData<Long>().also {
		it.value = Pubkey.PUBKEY_NEVER
		it.addSource(_host) { host -> it.value = host.pubkeyId }
	}

	val usePubkey: LiveData<Long>
		get() = _usePubkey

	val usePubkeysClicked = ViewClickLiveData()

	private val _pubkeys: LiveData<List<Pubkey>> = dataRepository.allPubkeys

	private val _pubkeyNamesValues = MediatorLiveData<List<Pair<String, Long>>>().also {
		fun mergePubkeys(rp: IResourceProvider?, pubkeys: List<Pubkey>?) {
			executors.diskIO().execute {
				val mergedList = mutableListOf<Pair<String, Long>>()
				rp?.useTypedArrayPair(R.array.list_pubkeyids, R.array.list_pubkeyids_value) { namesRes, valuesRes ->
					for (i in 0 until namesRes.length()) {
						mergedList.add(Pair(
							namesRes.getText(i).toString(),
							valuesRes.getText(i).toString().toLong()
						))
					}
				}
				pubkeys?.forEach { pubkey -> mergedList.add(Pair(pubkey.nickname, pubkey.id)) }
				it.postValue(mergedList)
			}
		}

		it.addSource(resourceProvider) { rp -> mergePubkeys(rp, _pubkeys.value) }
		it.addSource(_pubkeys) { keys -> mergePubkeys(resourceProvider.value, keys) }
	}

	val pubkeyNamesValues: LiveData<List<Pair<String, Long>>>
		get() = _pubkeyNamesValues

	val pubkeyDescription = DescriptionUpdatingMediatorLiveData(_pubkeyNamesValues, _usePubkey, Pubkey.PUBKEY_NEVER)

	fun onPubkeySelected(selectedPubkey: Long) {
		_usePubkey.value = selectedPubkey
	}

	private val _delKeyNamesValues = MediatorLiveData<List<Pair<String, Host.DelKey>>>().also {
		it.addSource(resourceProvider) { rp ->
			executors.diskIO().execute {
				rp?.useTypedArrayPair(R.array.list_delkey, R.array.list_delkey_values) { namesRes, valuesRes ->
					it.postValue(List(namesRes.length()) { i ->
						Pair(namesRes.getText(i).toString(), Converters.strToDelKey(valuesRes.getText(i).toString()))
					})
				}
			}
		}
	}

	val delKeyNamesValues: LiveData<List<Pair<String, Host.DelKey>>>
		get() = _delKeyNamesValues

	private val delKey = MediatorLiveData<Host.DelKey>().also {
		it.addSource(_host) { host -> it.value = host.delKey }
	}

	val delKeyDescription: LiveData<String> = DescriptionUpdatingMediatorLiveData(_delKeyNamesValues, delKey, Host.DelKey.DEL)

	val delKeysClicked = ViewClickLiveData()

	fun onDelKeySelected(value: Host.DelKey) {
		delKey.value = value
	}

	val possibleEncodings = MutableLiveData<List<Pair<String, String>>>().also {
		executors.diskIO().execute {
			val charsets = ArrayList<Pair<String, String>>()
			// Custom CP437 charset
			charsets.add(Pair("CP437", "CP437"))
			for ((key, c) in Charset.availableCharsets()) {
				if (c.canEncode() && c.isRegistered) {
					charsets.add(Pair(c.displayName(), key))
				}
			}
			charsets.sortWith(kotlin.Comparator { a, b -> a.first.compareTo(b.first, true) })
			it.postValue(charsets)
		}
	}

	private val encoding = MediatorLiveData<String>().also {
		it.value = "UTF-8"
		it.addSource(_host) { host -> it.value = host.encoding }
	}

	val encodingDescription: LiveData<String> = DescriptionUpdatingMediatorLiveData(possibleEncodings, encoding, Host.ENCODING_DEFAULT, String::toLowerCase)

	val encodingsClicked = ViewClickLiveData()

	fun onEncodingSelected(value: String) {
		encoding.value = value
	}

	private fun useAuthAgentValue(): Host.UseAuthAgent {
		if (useSshAuth.value == false)
			return Host.UseAuthAgent.AUTHAGENT_NO

		if (useAuthConfirmation.value == true)
			return Host.UseAuthAgent.AUTHAGENT_CONFIRM

		return Host.UseAuthAgent.AUTHAGENT_YES
	}

	val useSshAuth = MediatorLiveData<Boolean>().also {
		it.addSource(_host) { host -> it.value = host.useAuthAgent != Host.UseAuthAgent.AUTHAGENT_NO }
	}

	val useAuthConfirmation = MediatorLiveData<Boolean>().also {
		it.addSource(_host) { host -> it.value = host.useAuthAgent == Host.UseAuthAgent.AUTHAGENT_CONFIRM }
	}

	val useCompression = MediatorLiveData<Boolean>().also {
		it.addSource(_host) { host -> it.value = host.compression }
	}

	val startShell = MediatorLiveData<Boolean>().also {
		it.addSource(_host) { host -> it.value = host.wantSession }
	}

	val stayConnected = MediatorLiveData<Boolean>().also {
		it.addSource(_host) { host -> it.value = host.stayConnected }
	}

	val closeOnDisconnect = MediatorLiveData<Boolean>().also {
		it.addSource(_host) { host -> it.value = host.quickDisconnect }
	}

	val postLoginAutomation = MediatorLiveData<String>().also {
		it.addSource(_host) { host -> it.value = host.postLogin }
	}

	fun onSaveButtonClicked() {
		val host = _host.value ?: Host()

		_nickname.value?.let { host.nickname = it }
		_protocol.value?.let { host.protocol = it }

		_username.value?.let { host.username = it }
		_hostname.value?.let { host.hostname = it }
		_portNumber.value?.let { host.port = it }

		_color.value?.let { host.color = it }
		delKey.value?.let { host.delKey = it }
		postLoginAutomation.value?.let { host.postLogin = it }
		encoding.value?.let { host.encoding = it }
		_fontSize.value?.let { host.fontSize = it }
		host.useAuthAgent = useAuthAgentValue()
		closeOnDisconnect.value?.let { host.quickDisconnect = it }
		useCompression.value?.let { host.compression = it }
		startShell.value?.let { host.wantSession = it }
		stayConnected.value?.let { host.stayConnected = it }

		_usePubkey.value?.let { pubkeyId ->
			host.pubkeyId = pubkeyId
			when (pubkeyId) {
				Pubkey.PUBKEY_NEVER -> host.useKeys = false
				else -> host.useKeys = true
			}
		}

		dataRepository.upsertHost(host, ::onHostUpserted)
	}

	private val _fragmentFinished = ClickLiveData<Boolean>()

	val fragmentFinished: LiveData<Boolean> = _fragmentFinished

	private fun onHostUpserted(id: Long?) {
		id?.let { _hostId.value = it }
		_fragmentFinished.value = true
	}
}

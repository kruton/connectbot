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

package org.connectbot.ui.pubkeys

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.R
import org.connectbot.db.entity.Pubkey
import org.connectbot.repo.DataRepository
import org.connectbot.ui.common.IClipboard
import org.connectbot.util.ClickLiveData
import org.connectbot.util.MenuItemClickLiveData
import org.connectbot.util.PubkeyUtils
import org.connectbot.util.ResourceProvider
import org.connectbot.util.ViewClickLiveData
import javax.inject.Inject

class PubkeysViewModel @Inject constructor(
	private val repository: DataRepository,
	private val clipboard: IClipboard
) : ViewModel() {
	val resourceProvider = MutableLiveData<ResourceProvider>()

	fun onPubkeyDeleteConfirmed(pubkey: Pubkey) {
		repository.deletePubkey(pubkey)
	}

	fun onCopyPublicKeyRequested(pubkey: Pubkey) {
		val publicKey = pubkey.publicKey?.let {
			PubkeyUtils.decodePublic(it, pubkey.keyType)
		} ?: return
		clipboard.copyToClipboard(PubkeyUtils.convertToOpenSSHFormat(publicKey, pubkey.nickname))
	}

	fun onCopyPrivateKeyRequested(pubkey: Pubkey) {
		val privateKey = pubkey.privateKey?.let {
			PubkeyUtils.decodePrivate(it, pubkey.keyType)
		} ?: return
		TODO("fix encoding of private key")
//		clipboard.copyToClipboard(PubkeyUtils.exportPEM(privateKey, secret))
	}

	val addPubkeyClicked = ViewClickLiveData()

	val importPubkeyClicked = MenuItemClickLiveData()

	val pubkeys = repository.allPubkeys

	val emptyList: LiveData<Boolean> = Transformations.map(pubkeys) { it.isNullOrEmpty() }

	private val _pubkeyCopyPublicClick = ClickLiveData<Pubkey>()
	val pubkeyCopyPublicClick: LiveData<Pubkey>
		get() = _pubkeyCopyPublicClick

	private val _pubkeyCopyPrivateClick = ClickLiveData<Pubkey>()
	val pubkeyCopyPrivateClick: LiveData<Pubkey>
		get() = _pubkeyCopyPrivateClick

	private val _pubkeyDeleteClick = ClickLiveData<Pubkey>()
	val pubkeyDeleteClick: LiveData<Pubkey>
		get() = _pubkeyDeleteClick

	private val pubkeyContextItemsResources = arrayOf(
		Pair(R.string.pubkey_copy_public, _pubkeyCopyPublicClick),
		Pair(R.string.pubkey_copy_private, _pubkeyCopyPrivateClick),
		Pair(R.string.pubkey_delete, _pubkeyDeleteClick)
	)

	private val _pubkeyContextMenuItems = MediatorLiveData<List<Pair<String, (Pubkey) -> Unit>>>().also {
		it.addSource(resourceProvider) { rp ->
			it.value = pubkeyContextItemsResources.map { pair ->
				Pair(rp.getString(pair.first), { pubkey: Pubkey -> pair.second.value = pubkey })
			}
		}
	}

	val pubkeyContextMenuItems: LiveData<List<Pair<String, (Pubkey) -> Unit>>>
		get() = _pubkeyContextMenuItems
}

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

package org.connectbot.ui.generate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.db.entity.Pubkey
import org.connectbot.repo.DataRepository
import org.connectbot.ui.common.IResourceProvider
import org.connectbot.ui.common.ViewVisibility
import org.connectbot.util.ClickLiveData
import org.connectbot.util.ViewClickLiveData
import timber.log.Timber
import javax.inject.Inject

class GeneratePubkeyViewModel @Inject constructor(
	val dataRepository: DataRepository,
	val appExecutors: AppExecutors
) : ViewModel(), OnKeyGeneratedListener {
	val resourceProvider = MutableLiveData<IResourceProvider>()

	private val _pubkeyId = MutableLiveData<Long>()

	private var _existingPubkey = false

	val existingPubkey: Boolean
		get() = _existingPubkey

	private val _pubkey = Transformations.switchMap(_pubkeyId) {
		dataRepository.getPubkeyById(it)
	}

	private val _keyTypes = MutableLiveData<List<Pair<String, Pubkey.KeyType>>>().also {
		it.value = listOf(
			Pair(Pubkey.KeyType.EC.toString(), Pubkey.KeyType.EC),
			Pair(Pubkey.KeyType.ED25519.toString(), Pubkey.KeyType.ED25519),
			Pair(Pubkey.KeyType.RSA.toString(), Pubkey.KeyType.RSA)
		)
	}
	val keyTypes: LiveData<List<Pair<String, Pubkey.KeyType>>> = _keyTypes

	fun onKeyTypeSelected(value: Pubkey.KeyType) {
		_keyType.value = value
	}

	val nickname = MediatorLiveData<String>().also {
		it.addSource(_pubkey) { pubkey ->
			it.value = pubkey.nickname
		}
	}

	val keyTypeClicked = ViewClickLiveData()

	private val _keyType = MediatorLiveData<Pubkey.KeyType>().also {
		it.value = Pubkey.KeyType.ED25519
		it.addSource(_pubkey) { pubkey ->
			it.value = pubkey.keyType
		}
	}

	val keyTypeEnabled: Boolean
		get() = !_existingPubkey

	val keyType: LiveData<String> = Transformations.map(_keyType) { it.toString() }

	val keyCurveVisible: LiveData<Boolean> = Transformations.map(_keyType) {
		it == Pubkey.KeyType.EC
	}

	private val _keyCurves: List<Pair<String, Int>> = listOf(
		Pair("NIST P-256", 256),
		Pair("NIST P-384", 384),
		Pair("NIST P-521", 521)
	)
	val keyCurves: List<Pair<String, Int>>
		get() = _keyCurves

	private val _keyCurve = MutableLiveData<Int>().also {
		it.value = DEFAULT_EC_SIZE
	}

	val keyCurve: LiveData<String> = Transformations.map(_keyCurve) { keySize ->
		_keyCurves.find { it.second == keySize }?.first
	}

	val keyCurveClicked = ViewClickLiveData()

	fun onKeyCurveSelected(keyCurve: Int) {
		_keyCurve.value = keyCurve
	}

	val bitsVisible: LiveData<Boolean> = Transformations.map(_keyType) { it == Pubkey.KeyType.RSA }

	private val _bits = MutableLiveData<Int>().also { it.value = DEFAULT_RSA_SIZE }

	val bits: LiveData<Int>
		get() = _bits

	private fun updateBits(newBits: Int) {
		val clampedBits = newBits.coerceIn(MINIMUM_RSA_BITS, MAXIMUM_RSA_BITS)
		val quantizedBits = clampedBits - clampedBits % 8

		// We have to compare it to what the user entered or the EditText
		// won't get updated when our opinion of what it should be is different.
		// e.g., current=3192, user=3194, quantize to 3192 which == current but != user
		if (_bits.value != newBits) {
			_bits.value = quantizedBits
		}
	}

	val bitsText: LiveData<String> = Transformations.map(_bits) { it.toString() }

	fun onBitsTextUpdated(value: String) {
		val newBits = try {
			value.toInt()
		} catch (e: NumberFormatException) {
			DEFAULT_RSA_SIZE
		}

		updateBits(newBits)
	}

	fun onBitsSliderChanged(value: Int, fromUser: Boolean) {
		if (!fromUser)
			return

		updateBits(value + MINIMUM_RSA_BITS)
	}

	private val _performanceWarningShow = MediatorLiveData<ViewVisibility>().also {
		fun checkShowWarning() {
			val bits = _bits.value ?: DEFAULT_RSA_SIZE
			it.value = when {
				_keyType.value != Pubkey.KeyType.RSA -> ViewVisibility.GONE
				bits > RSA_BITS_PERFORMANCE -> ViewVisibility.VISIBLE
				else -> ViewVisibility.INVISIBLE
			}
		}

		it.value = ViewVisibility.GONE
		it.addSource(_keyType) { checkShowWarning() }
		it.addSource(_bits) { checkShowWarning() }
	}

	val performanceWarningShow: LiveData<ViewVisibility>
		get() = _performanceWarningShow

	private val _securityWarningShow = MediatorLiveData<ViewVisibility>().also {
		fun checkShowWarning() {
			val bits = _bits.value ?: DEFAULT_RSA_SIZE
			it.value = when {
				_keyType.value != Pubkey.KeyType.RSA -> ViewVisibility.GONE
				bits < RSA_BITS_SECURITY -> ViewVisibility.VISIBLE
				else -> ViewVisibility.INVISIBLE
			}
		}

		it.value = ViewVisibility.GONE
		it.addSource(_keyType) { checkShowWarning() }
		it.addSource(_bits) { checkShowWarning() }
	}

	val securityWarningShow: LiveData<ViewVisibility>
		get() = _securityWarningShow

	val sliderProgress: LiveData<Int> = Transformations.map(_bits) { it - MINIMUM_RSA_BITS }

	val sliderMaximumValue: Int
		get() = MAXIMUM_RSA_BITS - MINIMUM_RSA_BITS

	val password1 = MutableLiveData<String>()

	val password2 = MutableLiveData<String>()

	fun onPassword2Changed(value: String) {
		password2.value = value
	}

	private val _passwordError = MediatorLiveData<String>().also {
		fun checkPasswordsMatch() {
			val p1 = password1.value
			val p2 = password2.value
			appExecutors.diskIO().execute {
				it.postValue(when (p1 != null && p2 != null && p1 != p2) {
					true -> resourceProvider.value?.getString(R.string.alert_passwords_do_not_match_msg)
					false -> null
				})
			}
		}
		it.addSource(password1) { checkPasswordsMatch() }
		it.addSource(password2) { checkPasswordsMatch() }
	}

	val passwordError: LiveData<String>
		get() = _passwordError

	val loadKeyOnStart = MediatorLiveData<Boolean>().also {
		it.value = false
		it.addSource(_pubkey) { pubkey -> it.value = pubkey.onStartup }
	}

	val confirmBeforeUse = MediatorLiveData<Boolean>().also {
		it.value = true
		it.addSource(_pubkey) { pubkey -> it.value = pubkey.confirmUse }
	}

	private val _saveButtonEnabled = MediatorLiveData<Boolean>().also {
		it.value = existingPubkey

		fun checkFieldsFilledOut() {
			val p1 = password1.value
			val p2 = password2.value
			it.value = when {
				nickname.value.isNullOrEmpty() -> false
				p1.isNullOrEmpty() != p2.isNullOrEmpty() || p1 != p2 -> false
				else -> true
			}
		}

		it.addSource(password1) { checkFieldsFilledOut() }
		it.addSource(password2) { checkFieldsFilledOut() }
		it.addSource(nickname) { checkFieldsFilledOut() }
	}

	val saveButtonEnabled: LiveData<Boolean>
		get() = _saveButtonEnabled

	val _showGenerationDialog = MutableLiveData<Boolean>()
	val showGenerationDialog: LiveData<Boolean>
		get() = _showGenerationDialog

	fun onAddButtonClicked() {
		val keyType = _keyType.value ?: Pubkey.KeyType.ED25519
		val bits = when (keyType) {
			Pubkey.KeyType.EC -> _keyCurve.value!!
			Pubkey.KeyType.RSA -> _bits.value!!
			Pubkey.KeyType.ED25519 -> 256
			else -> 256
		}

		val generationTask = KeyGenerationRunnable(keyType, bits, this, password1.value)
		appExecutors.diskIO().execute(generationTask)
		_showGenerationDialog.value = true
	}

	fun onSaveButtonClicked() {
		val pubkey = _pubkey.value ?: Pubkey()

		nickname.value?.let { pubkey.nickname = it }
		loadKeyOnStart.value?.let { pubkey.onStartup = it }
		confirmBeforeUse.value?.let { pubkey.confirmUse = it }

		dataRepository.upsertPubkey(pubkey, ::onPubkeyUpserted)
	}

	private val _fragmentFinished = ClickLiveData<Boolean>()

	val fragmentFinished: LiveData<Boolean> = _fragmentFinished

	private fun onPubkeyUpserted(id: Long?) {
		id?.let { _pubkeyId.value = it }
		_showGenerationDialog.value = false
		_fragmentFinished.value = true
	}

	fun onPubkeyId(value: Long?) {
		value?.let {
			if (it != 0L) {
				_pubkeyId.value = value
				_existingPubkey = true
			}
		}
	}

	override fun onGenerationError(e: Exception) {
		Timber.d(e)
		TODO("add error message here")
	}

	override fun onGenerationSuccess(encodedPrivate: ByteArray, encodedPublic: ByteArray, encrypted: Boolean) {
		val pubkey = _pubkey.value ?: Pubkey()

		nickname.value?.let { pubkey.nickname = it }
		loadKeyOnStart.value?.let { pubkey.onStartup = it }
		confirmBeforeUse.value?.let { pubkey.confirmUse = it }
		_keyType.value?.let { pubkey.keyType = it }
		pubkey.privateKey = encodedPrivate
		pubkey.publicKey = encodedPublic
		pubkey.encrypted = encrypted

		dataRepository.upsertPubkey(pubkey, ::onPubkeyUpserted)
	}

	companion object {
		/** Minimum size of an RSA key in bits to allow. */
		const val MINIMUM_RSA_BITS = 1024

		/** Default size of an RSA key in bits. */
		const val DEFAULT_RSA_SIZE = 2048

		/** Maximum size of an RSA key in bits. */
		const val MAXIMUM_RSA_BITS = 16384

		/** RSA performance may be very slow above this value. */
		const val RSA_BITS_PERFORMANCE = 6144

		/** RSA security will be below recommended levels if it's below this size. */
		const val RSA_BITS_SECURITY = 2048

		/** Default group size for EC keys. */
		const val DEFAULT_EC_SIZE = 256
	}
}

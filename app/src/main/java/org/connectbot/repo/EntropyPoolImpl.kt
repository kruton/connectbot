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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.connectbot.ui.entropy.EntropyPool

class EntropyPoolImpl: EntropyPool {
	private val _entropyPool = MutableLiveData<ByteArray>()

	private val _entropyBytesNeeded = MutableLiveData<Int>()
	override val entropyBytesNeeded: LiveData<Int>
		get() = _entropyBytesNeeded

	override val entropyBytesAvailable: LiveData<Int> = Transformations.map(_entropyPool) {
		it.size
	}

	override fun requestEntropy(bytesNeeded: Int) {
		_entropyBytesNeeded.value = _entropyBytesNeeded.value?.let { it + bytesNeeded } ?: bytesNeeded
	}
	override fun addEntropy(entropy: Byte) {
		_entropyPool.value = _entropyPool.value?.let { it + entropy } ?: (ByteArray(0) + entropy)
		_entropyBytesNeeded.value = _entropyBytesNeeded.value?.let { it - 1 } ?: 0
	}
	override fun drainEntropy(sizeBytes: Int): ByteArray? {
		return _entropyPool.value?.let {
			val sizeCanReturn = minOf(sizeBytes, it.size)
			val output = it.sliceArray(0 until sizeCanReturn)
			_entropyPool.value = it.sliceArray(sizeCanReturn until it.size)
			output
		}
	}
}

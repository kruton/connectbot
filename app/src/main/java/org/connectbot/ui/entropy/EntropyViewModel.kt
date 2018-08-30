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

package org.connectbot.ui.entropy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.R
import org.connectbot.ui.common.IResourceProvider
import org.connectbot.util.ITimingSource
import javax.inject.Inject

class EntropyViewModel @Inject constructor(
	private val entropyPool: EntropyPool,
	private val timingSource: ITimingSource
): ViewModel() {
	val resourceProvider = MutableLiveData<IResourceProvider>()

	private val _percentGathered = MediatorLiveData<Int>().also {
		fun updateCalculation() {
			val available = entropyPool.entropyBytesAvailable.value ?: 0
			val needed = entropyPool.entropyBytesNeeded.value ?: 0

			it.value = when (needed) {
				0 -> 100
				else -> available * 100 / needed
			}
		}
		it.addSource(resourceProvider) { updateCalculation() }
		it.addSource(entropyPool.entropyBytesAvailable) { updateCalculation() }
		it.addSource(entropyPool.entropyBytesNeeded) { updateCalculation() }
	}

	val percentGathered: LiveData<Int>
		get() = _percentGathered

	private val _entropyText = MediatorLiveData<String>().also {
		it.addSource(percentGathered) { percent ->
			resourceProvider.value?.let { rp ->
				it.value = rp.getString(R.string.pubkey_touch_prompt, percent)
			}
		}
	}

	val entropyText: LiveData<String>
		get() = _entropyText

	private var flipFlop = false

	private var entropyBitIndex = 0

	private var entropyByte = 0

	private var lastInputTime = 0L

	val fragmentFinished: LiveData<Boolean> = Transformations.map(percentGathered) { it >= 100 }

	fun onUserTouch(x: Float, y: Float) {
		val currentTime = timingSource.currentTimeMillis()

		when (currentTime.minus(lastInputTime) < MILLIS_BETWEEN_INPUTS) {
			true -> return
			false -> lastInputTime = currentTime
		}

		// Get the lowest 4 bits of each X, Y input and concat to the entropy-gathering
		// string.
		var input: Int = when (flipFlop) {
			true -> (x.toInt() and 0x0F shl 4) or (y.toInt() and 0x0F)
			false -> (x.toInt() and 0x0F shl 4) or (x.toInt() and 0x0F)
		}

		flipFlop = !flipFlop

		// This tries to whiten the data:
		//     01 -> 1, 10 -> 0
		// Repeated bits get dropped.
		fun shiftAndAdd(added: Int) {
			entropyByte = entropyByte shl 1
			entropyByte = entropyByte or added
			entropyBitIndex++
			input = input shr 2
		}
		for (i in 0 until 4) {
			when (input.and(3)) {
				1 -> shiftAndAdd(1)
				2 -> shiftAndAdd(0)
			}

			if (entropyBitIndex >= 8) {
				entropyBitIndex = 0
				entropyPool.addEntropy(entropyByte.toByte())
			}
		}
	}

	companion object {
		const val MILLIS_BETWEEN_INPUTS = 50L
	}
}

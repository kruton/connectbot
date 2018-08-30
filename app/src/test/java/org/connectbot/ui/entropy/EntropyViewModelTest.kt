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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.connectbot.util.ITimingSource
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.random.Random

class EntropyViewModelTest {
	@Rule
	@JvmField
	val rule: TestRule = InstantTaskExecutorRule()

	@Test
	fun `calculates entropy needed correctly`() {
		val mockEntropyPool = mock<EntropyPool>()
		val bytesNeeded = MutableLiveData<Int>()
		val bytesAvailable = MutableLiveData<Int>()

		whenever(mockEntropyPool.entropyBytesNeeded).thenReturn(bytesNeeded)
		whenever(mockEntropyPool.entropyBytesAvailable).thenReturn(bytesAvailable)

		val viewModel = EntropyViewModel(mockEntropyPool, mock())

		val percentObserver = viewModel.percentGathered.test()
		percentObserver.assertNoValue()

		bytesNeeded.value = 10
		bytesAvailable.value = 2
		percentObserver.assertValue(20)

		bytesNeeded.value = 0
		percentObserver.assertValue(100)
	}

	@Test
	fun `adds entropy to pool`() {
		val mockEntropyPool = mock<EntropyPool>()
		val bytesNeeded = MutableLiveData<Int>()
		val bytesAvailable = MutableLiveData<Int>()
		whenever(mockEntropyPool.entropyBytesNeeded).thenReturn(bytesNeeded)
		whenever(mockEntropyPool.entropyBytesAvailable).thenReturn(bytesAvailable)

		val mockTimingSource = mock<ITimingSource>()
		val incrementGenerator = generateSequence(100L) { it + 50L }
		whenever(mockTimingSource.currentTimeMillis()).thenReturn(50L, *incrementGenerator.take(100).toList().toTypedArray())

		val viewModel = EntropyViewModel(mockEntropyPool, mockTimingSource)
		val random = Random(0)
		val touchGenerator = generateSequence { random.nextFloat() * 1024f }.iterator()

		for (i in 0..100) {
			viewModel.onUserTouch(touchGenerator.next(), touchGenerator.next())
		}

		verify(mockEntropyPool, atLeastOnce()).addEntropy(any())
	}
}

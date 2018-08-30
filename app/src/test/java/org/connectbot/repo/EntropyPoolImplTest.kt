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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.test
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EntropyPoolImplTest {
	@Rule
	@JvmField
	val rule: TestRule = InstantTaskExecutorRule()

	@Test
	fun `slices correctly`() {
		val pool = EntropyPoolImpl()
		pool.addEntropy(0xFF.toByte())
		pool.addEntropy(0xAA.toByte())
		pool.addEntropy(0x55.toByte())

		val entropyPoolObserver = pool.entropyBytesAvailable.test()
		entropyPoolObserver.assertValue(3)
		assertThat(pool.drainEntropy(2), equalTo(byteArrayOf(0xFF.toByte(), 0xAA.toByte())))
		assertThat(pool.drainEntropy(1), equalTo(byteArrayOf(0x55.toByte())))
	}

	@Test
	fun `returns null when empty`() {
		val pool = EntropyPoolImpl()
		assertThat(pool.drainEntropy(1), nullValue())
	}

	@Test
	fun `bytes needed decreases when entropy added`() {
		val pool = EntropyPoolImpl()
		val bytesNeededObserver = pool.entropyBytesNeeded.test()

		bytesNeededObserver.assertNoValue()

		pool.requestEntropy(1)
		bytesNeededObserver.assertValue(1)

		pool.addEntropy(0xFF.toByte())
		bytesNeededObserver.assertValue(0)
	}
}

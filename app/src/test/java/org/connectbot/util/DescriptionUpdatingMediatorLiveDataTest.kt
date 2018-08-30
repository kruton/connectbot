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

package org.connectbot.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.junit.MockitoJUnit

class DescriptionUpdatingMediatorLiveDataTest {
	@get:Rule
	val mockitoRule = MockitoJUnit.rule()

	@get:Rule
	val rule: TestRule = InstantTaskExecutorRule()

	@Test
	fun `takes a default value`() {
		val sourceList = MutableLiveData<List<Pair<String, String>>>().also {
			it.value = listOf(Pair("Pants", "GOOD"), Pair("Description", "VALUE"))
		}

		val dest = MutableLiveData<String>()
		val descObserver = DescriptionUpdatingMediatorLiveData(sourceList, dest, "VALUE").test()
		descObserver.assertValue("Description")
	}

	@Test
	fun `updates description when dest changes`() {
		val sourceList = MutableLiveData<List<Pair<String, String>>>()
		val dest = MutableLiveData<String>()

		sourceList.value = listOf(
			Pair("Pants", "GOOD"),
			Pair("Shirt", "BETTER"),
			Pair("Shoes", "SERVICE")
		)

		val descObserver = DescriptionUpdatingMediatorLiveData(sourceList, dest, "SERVICE").test()
		descObserver.assertValue("Shoes")

		dest.value = "BETTER"
		descObserver.assertValue("Shirt")
	}

	@Test
	fun `updates description when source list changes`() {
		val sourceList = MutableLiveData<List<Pair<String, String>>>()
		val dest = MutableLiveData<String>()

		dest.value = "BETTER"
		val descObserver = DescriptionUpdatingMediatorLiveData(sourceList, dest, "VALUE").test()
		descObserver.assertNoValue()
	}
}

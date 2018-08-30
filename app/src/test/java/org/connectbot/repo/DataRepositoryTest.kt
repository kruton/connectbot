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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.connectbot.db.dao.HostDao
import org.connectbot.db.entity.Color
import org.connectbot.db.entity.Host
import org.connectbot.util.Colors
import org.connectbot.util.InstantAppExecutors
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class DataRepositoryTest {
	@Rule
	@JvmField
	val rule: TestRule = InstantTaskExecutorRule()

	@Test
	fun `zips together colors list correctly`() {
		val hostDao = mock<HostDao>()
		whenever(hostDao.allHostsByColor()).thenReturn(mock())
		whenever(hostDao.allHostsByNickname()).thenReturn(mock())

		val fakeConnectedHosts = object : ConnectedHosts {
			override fun requestConnection(host: Host) {
				TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
			}

			override val connectedHosts: LiveData<List<Host>>
				get() = MutableLiveData<List<Host>>()
		}

		val dataRepository = DataRepository(
			appExecutors = InstantAppExecutors(),
			hostDao = hostDao,
			pubkeyDao = mock(),
			connectedHosts = fakeConnectedHosts)

		val color2 = Color()
		color2.schemeId = 9
		color2.number = 2
		color2.value = 0xA5A5A5

		val fakeLiveDataListColor = MutableLiveData<List<Color>>()
		fakeLiveDataListColor.value = listOf(color2)

		whenever(hostDao.getColorsForScheme(5))
			.thenReturn(fakeLiveDataListColor)

		val liveColorsObserver = dataRepository.getColorsForScheme(5).test()

		liveColorsObserver.assertHasValue()

		val defaultColors = Colors.mapToColors()
		liveColorsObserver.value().forEachIndexed { index, color ->
			when (index) {
				2 -> {
					assertThat(color.schemeId, equalTo(9L))
					assertThat(color.value, equalTo(color2.value))
					assertThat(color.number, equalTo(color2.number))
				}
				else -> {
					assertThat(color.schemeId, equalTo(5L))
					assertThat(color.value, equalTo(defaultColors[index].value))
					assertThat(color.number, equalTo(defaultColors[index].number))
				}
			}
		}
	}
}

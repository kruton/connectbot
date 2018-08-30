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

package org.connectbot.ui.colors

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.connectbot.db.entity.Color
import org.connectbot.db.entity.ColorScheme
import org.connectbot.db.entity.DefaultColor
import org.connectbot.repo.DataRepository
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class ColorsViewModelTest {
	@Rule
	@JvmField
	val rule: TestRule = InstantTaskExecutorRule()

	@Test
	fun `default values appear`() {
		val dataRepository = mock<DataRepository>()
		mockOutColorsCalls(dataRepository)

		val vm = ColorsViewModel(dataRepository)
		val colorsObserver = vm.colors.test()
		colorsObserver.assertHasValue()
		vm.fgColor.test().assertValue(1)
		vm.bgColor.test().assertValue(2)
	}

	@Test
	fun `scheme id is populated`() {
		val liveData = MutableLiveData<ColorScheme>()
		liveData.value = mock()

		val dataRepository = mock<DataRepository>()
		mockOutColorsCalls(dataRepository)

		val vm = ColorsViewModel(dataRepository)
		vm.colorScheme.test().assertHasValue()
	}

	@Test
	fun `changing FG and BG comes back to us`() {
		val dataRepository = mock<DataRepository>()
		mockOutColorsCalls(dataRepository)

		val vm = ColorsViewModel(dataRepository)
		val fgObserver = vm.fgColor.test()
		val bgObserver = vm.bgColor.test()

		// The values should be default and we shouldn't have tried to update the repo
		fgObserver.assertValue(1)
		bgObserver.assertValue(2)
		verify(dataRepository, never()).updateDefaultColor(any())

		// The updates shouldn't start until we have a list of colors.
		vm.onFgSelected(8)
		verify(dataRepository, never()).updateDefaultColor(any())

		vm.onBgSelected(4)
		verify(dataRepository, never()).updateDefaultColor(any())

		// Now that we have a list of colors, we can update the FG/BG
		val colorsObserver = vm.colors.test()
		colorsObserver.assertHasValue()

		vm.onBgSelected(15)
		verify(dataRepository, times(1)).updateDefaultColor(any())

		vm.onFgSelected(5)
		verify(dataRepository, times(2)).updateDefaultColor(any())
	}

	@Test
	fun `reset to defaults affects everything`() {

	}
	private fun mockOutColorsCalls(dataRepository: DataRepository) {
		val fakeLiveDefaultColor = MutableLiveData<DefaultColor>().also {
			val defaultColor = DefaultColor()
			defaultColor.fg = 1
			defaultColor.bg = 2
			it.value = defaultColor
		}
		whenever(dataRepository.getDefaultColorsForScheme(any())).thenReturn(fakeLiveDefaultColor)

		val fakeLiveColorScheme = MutableLiveData<ColorScheme>().also { it.value = mock() }
		whenever(dataRepository.getColorScheme(any())).thenReturn(fakeLiveColorScheme)

		val fakeLiveColors = MutableLiveData<List<Color>>().also {
			it.value = listOf(mock(), mock(), mock(), mock(), mock())
		}
		whenever(dataRepository.getColorsForScheme(any())).thenReturn(fakeLiveColors)
	}
}

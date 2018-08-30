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

import androidx.databinding.DataBindingComponent
import com.nhaarman.mockitokotlin2.mock
import org.connectbot.db.entity.Color
import org.connectbot.util.InstantAppExecutors
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(value = RobolectricTestRunner::class)
class ColorsAdapterTest {
	val dataBindingComponent = mock<DataBindingComponent>()
	val fakeClick: (Color) -> Unit = {}

	@Test
	fun `adapter can be instantiated`() {
		ColorsAdapter(dataBindingComponent, InstantAppExecutors(), fakeClick)
	}

	@Test
	fun `assert adapter starts empty`() {
		val adapter = ColorsAdapter(dataBindingComponent, InstantAppExecutors(), fakeClick)

		assertThat(adapter.itemCount, `is`(0))
	}

	@Test
	fun `assert submitting a list works`() {
		val adapter = ColorsAdapter(dataBindingComponent, InstantAppExecutors(), fakeClick)

		val mockItem = mock<Color>()
		val mockList: List<Color> = listOf(mockItem, mock(), mock())
		adapter.submitList(mockList)

		assertThat(adapter.itemCount, `is`(3))
	}
}

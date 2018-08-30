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

import android.content.res.TypedArray
import org.connectbot.ui.common.IResourceProvider
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class FakeListResourceProvider<T, V> constructor(val names: Array<T>, val values: Array<V>): IResourceProvider {
	init {
		assert(names.size == values.size) { "names and values must be same length" }
	}

	override fun getString(res: Int, vararg formatArgs: Any): String {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getString(res: Int): String {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun useTypedArrayPair(namesRes: Int, valesRes: Int, block: (TypedArray, TypedArray) -> Unit) {
		val fakeNames = mock(TypedArray::class.java)
		val fakeValues = mock(TypedArray::class.java)
		
		`when`(fakeNames.length()).thenAnswer { names.size }
		`when`(fakeNames.getString(anyInt())).thenAnswer { names[it.getArgument(0)] }
		`when`(fakeNames.getText(anyInt())).thenAnswer { names[it.getArgument(0)] }

		`when`(fakeValues.length()).thenAnswer { values.size }
		`when`(fakeValues.getString(anyInt())).thenAnswer { values[it.getArgument(0)] }
		`when`(fakeValues.getText(anyInt())).thenAnswer { values[it.getArgument(0)] }

		block(fakeNames, fakeValues)
	}
}

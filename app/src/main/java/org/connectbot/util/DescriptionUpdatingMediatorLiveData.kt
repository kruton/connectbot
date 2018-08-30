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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class DescriptionUpdatingMediatorLiveData<T> constructor(
	private val sourceList: LiveData<List<Pair<String, T>>>,
	private val dest: MutableLiveData<T>,
	private val default: T,
	private val mutateValue: (T) -> T = { it }
): MediatorLiveData<String>() {
	init {
		addSource(sourceList) { recalculate() }
		addSource(dest) { recalculate() }
	}

	private fun recalculate() {
		sourceList.value?.let { list ->
			val selectedValue = mutateValue(dest.value ?: default)
			for ((displayName, value) in list) {
				if (selectedValue == mutateValue(value)) {
					this.value = displayName
					break
				}
			}
		}
	}
}

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

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * This sends one-shot updates to a LiveData field. It doesn't ignore repeated sets of
 * non-{@code null} values. This makes it useful for handling View clicks for instance.
 */
open class ClickLiveData<T>: MutableLiveData<T>() {
	@MainThread
	override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
		/* Wrap with our own observer that resets to null after observing. */
		super.observe(owner, Observer { data ->
			data ?: return@Observer
			observer.onChanged(data)
			value = null
		})
	}
}

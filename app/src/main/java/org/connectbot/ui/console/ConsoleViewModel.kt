/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2019 Kenny Root, Jeffrey Sharkey
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

package org.connectbot.ui.console

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class ConsoleViewModel @Inject constructor(): ViewModel() {

	private val _isNoHostConnected = MutableLiveData<Boolean>().also {
		it.value = true
	}

	val isNoHostConnected: LiveData<Boolean>
		get() = _isNoHostConnected

	private val _passwordVisible = MutableLiveData<Boolean>().also {
		it.value = false
	}

	val passwordVisible: LiveData<Boolean>
		get() = _passwordVisible

	private val _passwordInstructionsVisible = MutableLiveData<Boolean>().also {
		it.value = false
	}

	val passwordInstructionsVisible: LiveData<Boolean>
		get() = _passwordInstructionsVisible

	private val _passwordInstructions = MutableLiveData<String>().also {
	}

	val passwordInstructions: LiveData<String>
		get() = _passwordInstructions

	private val _yesNoVisible = MutableLiveData<Boolean>().also {
		it.value = false
	}

	val yesNoVisible: LiveData<Boolean>
		get() = _yesNoVisible

	private val _yesNoPrompt = MutableLiveData<String>().also {
	}

	val yesNoPrompt: LiveData<String>
		get() = _yesNoPrompt
}

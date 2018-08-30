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

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

abstract class SharedPreferenceLiveData<T> constructor(
	val sharedPreferences: SharedPreferences,
	private val key: String,
	val defValue: T
) : MutableLiveData<T>() {
	private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
		if (this.key == key) {
			value = getValueFromSharedPreferences(key, defValue)
		}
	}

	override fun onInactive() {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
		super.onInactive()
	}

	override fun onActive() {
		super.onActive()
		value = getValueFromSharedPreferences(key, defValue)
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
	}

	abstract fun getValueFromSharedPreferences(key: String, defValue: T): T
}

class SharedPreferenceStringLiveData(sharedPreferences: SharedPreferences, key: String, defValue: String) :
	SharedPreferenceLiveData<String>(sharedPreferences, key, defValue) {
	override fun getValueFromSharedPreferences(key: String, defValue: String): String = sharedPreferences.getString(key, defValue)!!
}

class SharedPreferenceBooleanLiveData(sharedPreferences: SharedPreferences, key: String, defValue: Boolean = false) :
	SharedPreferenceLiveData<Boolean>(sharedPreferences, key, defValue) {
	override fun getValueFromSharedPreferences(key: String, defValue: Boolean): Boolean = sharedPreferences.getBoolean(key, defValue)
}

class SharedPreferencesIntLiveData(sharedPreferences: SharedPreferences, key: String, defValue: Int) :
	SharedPreferenceLiveData<Int>(sharedPreferences, key, defValue) {
	override fun getValueFromSharedPreferences(key: String, defValue: Int): Int = sharedPreferences.getInt(key, defValue)
}

class SharedPreferencesFloatLiveData(sharedPreferences: SharedPreferences, key: String, defValue: Float) :
	SharedPreferenceLiveData<Float>(sharedPreferences, key, defValue) {
	override fun getValueFromSharedPreferences(key: String, defValue: Float): Float = sharedPreferences.getFloat(key, defValue)
}

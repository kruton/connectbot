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

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.TypedArray
import androidx.annotation.ArrayRes


@SuppressLint("Recycle")
fun Resources.useTypedArrayPair(@ArrayRes names: Int, @ArrayRes values: Int, block: (TypedArray, TypedArray) -> Unit) {
	this.obtainTypedArray(names).useWith(this.obtainTypedArray(values)) { first, second ->
		block(first, second)
	}
}

fun TypedArray.useWith(other: TypedArray, block: (TypedArray, TypedArray) -> Unit) {
	block(this, other)
	other.recycle()
	this.recycle()
}

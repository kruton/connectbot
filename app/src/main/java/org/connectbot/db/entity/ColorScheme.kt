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

package org.connectbot.db.entity

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.connectbot.testing.OpenForTesting

@OpenForTesting
@Entity
class ColorScheme {
	@PrimaryKey(autoGenerate = true)
	var id: Long = 0

	@NonNull
	var name: String? = null

	companion object {
		const val DEFAULT_COLOR_SCHEME: Long = 1

		const val DEFAULT_FG_COLOR = 7
		const val DEFAULT_BG_COLOR = 0
	}
}

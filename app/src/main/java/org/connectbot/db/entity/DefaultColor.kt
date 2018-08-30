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

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
		foreignKeys = [ForeignKey(entity = ColorScheme::class, parentColumns = ["id"], childColumns = ["schemeId"])],
		indices = [Index(value = ["schemeId"], unique = true)]
)
class DefaultColor {
	@PrimaryKey(autoGenerate = true)
	var id: Long = 0

	var schemeId: Long = ColorScheme.DEFAULT_COLOR_SCHEME

	var fg: Int = ColorScheme.DEFAULT_FG_COLOR

	var bg: Int = ColorScheme.DEFAULT_BG_COLOR
}

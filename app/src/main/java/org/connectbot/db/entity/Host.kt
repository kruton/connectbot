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
data class Host(
	@PrimaryKey(autoGenerate = true)
	var id: Long = 0,

	var nickname: String = "",

	var username: String? = null,

	var hostname: String? = null,

	var port: Int = 0,

	var protocol: String = "ssh",

	var lastConnect: Long = -1,

	@NonNull
	var color: HostColor = HostColor.GRAY,

	@NonNull
	var useKeys: Boolean = true,

	@NonNull
	var useAuthAgent: UseAuthAgent = UseAuthAgent.AUTHAGENT_NO,

	var postLogin: String? = null,

	var pubkeyId: Long = Pubkey.PUBKEY_ANY,

	@NonNull
	var wantSession: Boolean = true,

	@NonNull
	var delKey: DelKey = DelKey.DEL,

	@NonNull
	var fontSize: Int = DEFAULT_FONT_SIZE,

	@NonNull
	var compression: Boolean = false,

	@NonNull
	var encoding: String = ENCODING_DEFAULT,

	@NonNull
	var stayConnected: Boolean = false,

	@NonNull
	var quickDisconnect: Boolean = false
) {
	companion object {
		const val DEFAULT_FONT_SIZE = 10
		const val MINIMUM_FONT_SIZE = 8
		const val MAXIMUM_FONT_SIZE = 40
		const val ENCODING_DEFAULT = "utf-8"
	}

	enum class HostColor(val value: String) {
		RED("red"),
		GREEN("green"),
		BLUE("blue"),
		GRAY("gray");

		override fun toString() = value
	}

	enum class UseAuthAgent(val value: String) {
		AUTHAGENT_NO("no"),
		AUTHAGENT_CONFIRM("confirm"),
		AUTHAGENT_YES("yes");

		override fun toString() = value
	}

	enum class DelKey(val value: String) {
		DEL("del"),
		BACKSPACE("backspace");

		override fun toString() = value
	}
}

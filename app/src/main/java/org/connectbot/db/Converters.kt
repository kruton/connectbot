/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2018 Kenny Root
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

package org.connectbot.db

import androidx.room.TypeConverter
import org.connectbot.db.entity.Host
import org.connectbot.db.entity.Host.DelKey
import org.connectbot.db.entity.Host.UseAuthAgent
import org.connectbot.db.entity.PortForward.PortForwardType
import org.connectbot.db.entity.Pubkey

object Converters {
	@TypeConverter
	@JvmStatic
	fun strToDelKey(s: String?) = when (s) {
		"del" -> DelKey.DEL
		"backspace" -> DelKey.BACKSPACE
		else -> throw IllegalArgumentException("unknown DelKey value")
	}

	@TypeConverter
	@JvmStatic
	fun delKeyToString(delKey: DelKey?) = delKey?.toString()

	@TypeConverter
	@JvmStatic
	fun strToUseAuthAgent(s: String?) = when (s) {
		"no" -> UseAuthAgent.AUTHAGENT_NO
		"confirm" -> UseAuthAgent.AUTHAGENT_CONFIRM
		"yes" -> UseAuthAgent.AUTHAGENT_YES
		else -> throw IllegalArgumentException("unknown UseAuthAgent value")
	}

	@TypeConverter
	@JvmStatic
	fun useAuthAgentToStr(useAuthAgent: UseAuthAgent?) = useAuthAgent?.toString()

	@TypeConverter
	@JvmStatic
	fun strToPortForwardType(s: String?) = when (s) {
		"local" -> PortForwardType.LOCAL
		"remote" -> PortForwardType.REMOTE
		"dynamic4" -> PortForwardType.DYNAMIC4
		"dynamic5" -> PortForwardType.DYNAMIC5
		else -> throw IllegalArgumentException("unknown PortForward value")
	}

	@TypeConverter
	@JvmStatic
	fun hostColorToStr(hostColor: Host.HostColor?) = hostColor?.toString()

	@TypeConverter
	@JvmStatic
	fun strToHostColor(s: String?) = when (s) {
		"blue" -> Host.HostColor.BLUE
		"green" -> Host.HostColor.GREEN
		"gray" -> Host.HostColor.GRAY
		"red" -> Host.HostColor.RED
		else -> throw IllegalArgumentException("unknown HostColor value")
	}

	@TypeConverter
	@JvmStatic
	fun pubkeyKeyTypeToStr(pubkeyKeyType: Pubkey.KeyType) = pubkeyKeyType.toString()

	@TypeConverter
	@JvmStatic
	fun strToPubkeyKeyType(s: String?) = when (s) {
		"DSA" -> Pubkey.KeyType.DSA
		"EC" -> Pubkey.KeyType.EC
		"ED25519" -> Pubkey.KeyType.ED25519
		"IMPORTED" -> Pubkey.KeyType.IMPORTED
		"RSA" -> Pubkey.KeyType.RSA
		else -> throw IllegalArgumentException("unknown PubkeyType value")
	}

	@TypeConverter
	@JvmStatic
	fun pftToString(pft: PortForwardType?) = pft?.toString()
}

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

import android.annotation.SuppressLint
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
		foreignKeys = [ForeignKey(entity = Host::class, parentColumns = ["id"], childColumns = ["hostId"])],
		indices = [Index("hostId")]
)
class PortForward {
	@PrimaryKey(autoGenerate = true)
	var id: Long = 0

	@NonNull
	var hostId: Long? = null

	@NonNull
	var nickname: String? = null

	var type: PortForwardType = PortForwardType.LOCAL

	enum class PortForwardType(val value: String) {
		LOCAL("local"),
		REMOTE("remote"),
		DYNAMIC4("dynamic4"),
		DYNAMIC5("dynamic5");

		override fun toString() = value
	}

	@NonNull
	var sourcePort: Int? = null

	var destinationAddress: String? = null

	var destinationPort: Int? = null

	@Ignore
	var identifier: Any? = null

	@Ignore
	var enabled: Boolean = false

	/**
	 * @param destination The destination in "host:port" format
	 */
	@Ignore
	fun setDestination(destination: String) {
		val destSplit = destination.split(":".toRegex()).toTypedArray()
		destinationAddress = destSplit[0]
		if (destSplit.size > 1) {
			destinationPort = Integer.parseInt(destSplit[destSplit.size - 1])
		}
	}

	/**
	 * @return human readable description of the port forward
	 */
	@Ignore
	@SuppressLint("DefaultLocale")
	fun getDescription(): CharSequence {
		return when (type) {
			PortForwardType.LOCAL ->
				String.format("Local port %d to %s:%d", sourcePort, destinationAddress, destinationPort)
			PortForwardType.REMOTE ->
				String.format("Remote port %d to %s:%d", sourcePort, destinationAddress, destinationPort)
			PortForwardType.DYNAMIC5 ->
				String.format("Dynamic port %d (SOCKS)", sourcePort)
			else -> "Unknown type"
		}
	}
}

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

package org.connectbot.util

import org.connectbot.db.entity.Host
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * This holds the hosts we intend to which we tant to connect with the service.
 *
 * In order to communicate with {@link TerminalService2} we use a connection queue
 * to indicate which {@link Host} we want to connect to in our {@link Context#startService}
 * call. We don't want to make {@code Host} a parcelable, so we just use this key-value
 * structure to store the pending connection and send the key via the {@link Intent} extras.
 */
interface ConnectionQueue {
	fun getRequest(id: Int): Host?
	fun addRequest(host: Host): Int
}

class ConnectionQueueImpl: ConnectionQueue {
	private val pendingConnections = ConcurrentHashMap<Int, Host>()

	private val nextPendingIndex = AtomicInteger()

	override fun addRequest(host: Host): Int =
		nextPendingIndex.getAndIncrement().let {
			pendingConnections[it] = host
			it
		}

	override fun getRequest(id: Int): Host? = pendingConnections.remove(id)
}

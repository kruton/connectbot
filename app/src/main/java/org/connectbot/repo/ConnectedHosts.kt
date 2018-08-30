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

package org.connectbot.repo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.connectbot.App
import org.connectbot.db.entity.Host
import org.connectbot.service.TerminalManager2
import org.connectbot.util.ConnectionQueue
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

interface ConnectedHosts {
	val connectedHosts: LiveData<List<Host>>

	fun requestConnection(host: Host)
}

class ConnectedHostsImpl @Inject constructor(
	private val context: App,
	private val connectionQueue: ConnectionQueue
): ConnectedHosts {
	private val bindingsCount = AtomicInteger(0)

	private val connection = ServiceConnection()

	private val _connectedHosts = object : MediatorLiveData<List<Host>>() {
		override fun onActive() {
			super.onActive()
			bindService()
		}

		override fun onInactive() {
			unbindService()
			super.onInactive()
		}
	}

	private fun bindService() {
		if (bindingsCount.incrementAndGet() == 1) {
			context.bindService(
				Intent(context, TerminalManager2::class.java),
				connection,
				Context.BIND_AUTO_CREATE)
		}
	}

	private fun unbindService() {
		if (bindingsCount.decrementAndGet() == 0) {
			context.unbindService(connection)
		}
	}

	override val connectedHosts: LiveData<List<Host>>
		get() = _connectedHosts

	override fun requestConnection(host: Host) {
		Intent(context, TerminalManager2::class.java).also {
			it.action = TerminalManager2.REQUEST_CONNECTION
			it.putExtra(TerminalManager2.KEY_PENDING_CONNECTION, connectionQueue.addRequest(host))
			TerminalManager2.startServiceForeground(context, it)
		}
	}

	inner class ServiceConnection : android.content.ServiceConnection {
		private var _upstreamHosts: LiveData<List<Host>>? = null

		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val binder = service as TerminalManager2.LocalBinder
			val upstreamHosts = binder.connectedHosts
			_connectedHosts.addSource(upstreamHosts) {
				_connectedHosts.value = it
			}
			_upstreamHosts = upstreamHosts
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			_upstreamHosts?.let { _connectedHosts.removeSource(it) }
		}
	}
}

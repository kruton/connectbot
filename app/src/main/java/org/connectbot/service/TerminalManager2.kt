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

package org.connectbot.service

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.android.AndroidInjection
import org.connectbot.AppExecutors
import org.connectbot.db.entity.Host
import org.connectbot.repo.DataRepository
import org.connectbot.transport.TransportFactory
import org.connectbot.util.ConnectionQueue
import org.connectbot.util.TimingDelayHandler
import timber.log.Timber
import javax.inject.Inject

/**
 * The terminal manager runs whenever a host is connected or when an activity needs to know
 * whether a host is connected is bound to it.
 *
 * When a connection request comes in, it will grab a Wake Lock until it can spin itself up
 * as a foreground service. It will stop running as a foreground service when the last host has
 * disconnected.
 */
class TerminalManager2: LifecycleService() {
	@Inject
	lateinit var connectionQueue: ConnectionQueue

	@Inject
	lateinit var appExecutors: AppExecutors

	@Inject
	lateinit var timingDelayHandler: TimingDelayHandler

	@Inject
	lateinit var repository: DataRepository

	private val connections = mapOf<Host, AbsTransport>()

	private val connectedHostsLiveData = MutableLiveData<List<Host>>()

	override fun onCreate() {
		AndroidInjection.inject(this)
		super.onCreate()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Timber.d("onStartCommand with startId %d and intent %s", startId, intent)
		intent?.let { handleIntent(it) }
		return super.onStartCommand(intent, flags, startId)
	}

	private fun handleIntent(intent: Intent) {
		when (intent.action) {
			REQUEST_CONNECTION -> {
				val requestId = intent.extras?.getInt(KEY_PENDING_CONNECTION, -1) ?: return
				val host = connectionQueue.getRequest(requestId) ?: return
				handleConnectionRequest(host)
			}
			else -> Timber.e("Unhandled action %s", intent.action)
		}
	}

	private fun handleConnectionRequest(host: Host) {
		Timber.d("Got request for host %s", host)
		ConnectionNotifier.getInstance().showRunningNotification(this)
		fireOffCheckShutdown()
		val transport = TransportFactory.getTransport(host.protocol)
		appExecutors.transportIO().execute {
		}
		// TODO actually open connection
	}

	private fun fireOffCheckShutdown() {
		timingDelayHandler.postDelayed(Runnable { checkShutdown() }, SHUTDOWN_CHECK_DELAY_MILLIS)
	}

	private fun checkShutdown() {
		Timber.d("Checking whether we should shut down service")
		when (connections.size) {
			0 -> shutdownService()
		}
	}

	private fun shutdownService() {
		Timber.d("Shutdown service")
		ConnectionNotifier.getInstance().hideRunningNotification(this)
	}

	override fun onDestroy() {
		Timber.d("onDestroy called")
		shutdownService()
		super.onDestroy()
	}

	override fun onBind(intent: Intent): IBinder? {
		super.onBind(intent)
		return LocalBinder()
	}

	inner class LocalBinder : Binder() {
		val connectedHosts: LiveData<List<Host>> = connectedHostsLiveData
	}

	interface StartService {
		fun startServiceForeground(context: Context, intent: Intent)
	}

	object LessThanAPI26 : StartService {
		override fun startServiceForeground(context: Context, intent: Intent) {
			context.startService(intent)
		}
	}

	@TargetApi(Build.VERSION_CODES.O)
	object API26 : StartService {
		override fun startServiceForeground(context: Context, intent: Intent) {
			context.startForegroundService(intent)
		}
	}

	companion object {
		const val REQUEST_CONNECTION = "request_connection"
		const val KEY_PENDING_CONNECTION = "pending_connection"

		const val SHUTDOWN_CHECK_DELAY_MILLIS = 5L * 1000L

		fun startServiceForeground(context: Context, intent: Intent) {
			when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				true -> API26.startServiceForeground(context, intent)
				false -> LessThanAPI26.startServiceForeground(context, intent)
			}
		}
	}
}

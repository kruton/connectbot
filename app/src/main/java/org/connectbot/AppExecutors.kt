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

package org.connectbot

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AppExecutors(
	private val diskIO: Executor,
	private val transportIO: Executor,
	private val mainThread: Executor
) {
	@Inject
	constructor(): this(
			Executors.newSingleThreadExecutor(),
			Executors.newCachedThreadPool(),
			MainThreadExecutor()
	)

	fun diskIO() = diskIO
	fun transportIO() = transportIO
	fun mainThreadExecutor() = mainThread

	private class MainThreadExecutor: Executor {
		private val mainThreadHandler = Handler(Looper.getMainLooper())

		override fun execute(command: Runnable?) {
			mainThreadHandler.post(command)
		}
	}
}

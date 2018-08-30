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

package org.connectbot.ui

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import org.connectbot.App
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.util.PreferenceConstants
import org.connectbot.util.SharedPreferenceBooleanLiveData
import org.connectbot.util.SharedPreferencesFloatLiveData
import javax.inject.Inject

interface BellSound {
	fun play()
}

class BellSoundImpl @Inject constructor(
	private val app: App,
	private val appExecutors: AppExecutors,
	sharedPreferences: SharedPreferences
): BellSound {
	var enabled = false

	val mediaPlayer = MediaPlayer().also {
		it.setOnPreparedListener { mp -> mp.start() }
		it.isLooping = false

		when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			true -> setupMediaPlayerSDK21(it)
			false -> setupMediaPlayerPreSDK22(it)
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private fun setupMediaPlayerSDK21(mp: MediaPlayer) {
		val attributes = AudioAttributes.Builder()
			.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
			.setUsage(AudioAttributes.USAGE_NOTIFICATION)
			.build()
		mp.setAudioAttributes(attributes)
	}

	private fun setupMediaPlayerPreSDK22(mp: MediaPlayer) {
		mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
	}


	private val bellOn = SharedPreferenceBooleanLiveData(sharedPreferences, PreferenceConstants.BELL)

	private val bellVolume = SharedPreferencesFloatLiveData(sharedPreferences, PreferenceConstants.BELL_VOLUME, PreferenceConstants.DEFAULT_BELL_VOLUME)

	override fun play() {
		appExecutors.diskIO().execute {
			mediaPlayer.reset()
			app.resources.openRawResourceFd(R.raw.bell).use {
				mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
			}
			mediaPlayer.prepare()
		}
	}
}

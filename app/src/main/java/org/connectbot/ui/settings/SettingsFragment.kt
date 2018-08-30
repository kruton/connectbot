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

package org.connectbot.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import com.takisoft.preferencex.PreferenceFragmentCompat
import org.connectbot.R
import org.connectbot.testing.OpenForTesting
import org.connectbot.util.VolumePreference
import org.connectbot.util.VolumePreferenceFragment

@OpenForTesting
class SettingsFragment : PreferenceFragmentCompat() {
	override fun onDisplayPreferenceDialog(preference: Preference?) {
		if (preference is VolumePreference) {
			val fragment = VolumePreferenceFragment.newInstance(preference)
			fragment.setTargetFragment(this, 0)
			fragment.show(fragmentManager!!,
				"android.support.v7.preference.PreferenceFragment.DIALOG")
		} else {
			super.onDisplayPreferenceDialog(preference)
		}
	}

	override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.preferences, rootKey)
	}
}

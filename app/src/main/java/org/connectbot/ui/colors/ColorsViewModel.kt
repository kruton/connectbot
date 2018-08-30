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

package org.connectbot.ui.colors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.connectbot.db.entity.Color
import org.connectbot.db.entity.ColorScheme
import org.connectbot.repo.DataRepository
import org.connectbot.testing.OpenForTesting
import javax.inject.Inject

@OpenForTesting
class ColorsViewModel @Inject constructor(val dataRepository: DataRepository) : ViewModel() {
	private val _colorSchemeId = MutableLiveData<Long>().also {
		it.value = ColorScheme.DEFAULT_COLOR_SCHEME
	}

	val colorScheme: LiveData<ColorScheme> = Transformations.switchMap(_colorSchemeId) {
		dataRepository.getColorScheme(it)
	}

	val colors: LiveData<List<Color>> = Transformations.switchMap(_colorSchemeId) {
		dataRepository.getColorsForScheme(it)
	}

	private val _defaultColors = Transformations.switchMap(_colorSchemeId) {
		dataRepository.getDefaultColorsForScheme(it)
	}

	val fgColor: LiveData<Int> = Transformations.map(_defaultColors) { it.fg }

	fun onFgSelected(fg: Int) {
		if (fg == fgColor.value || colors.value == null)
			return

		_defaultColors.value?.let {
			it.fg = fg
			dataRepository.updateDefaultColor(it)
		}
	}

	val bgColor: LiveData<Int> = Transformations.map(_defaultColors) { it.bg }

	fun onBgSelected(bg: Int) {
		if (bg == bgColor.value || colors.value == null)
			return

		_defaultColors.value?.let {
			it.bg = bg
			dataRepository.updateDefaultColor(it)
		}
	}

	fun onColorUpdated(color: Color) {
		dataRepository.upsertColor(color)
	}

	fun onResetColorsSelected(): Boolean {
		_colorSchemeId.value?.let {
			dataRepository.resetColorsForScheme(it)
			dataRepository.resetDefaultColorsForScheme(it)
		}
		return true
	}
}

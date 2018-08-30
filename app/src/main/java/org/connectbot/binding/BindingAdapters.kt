/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.binding

import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.google.android.material.textfield.TextInputLayout
import org.connectbot.R
import org.connectbot.db.Converters
import org.connectbot.db.entity.Host
import org.connectbot.ui.common.FocusChangeWatcher
import org.connectbot.ui.common.TextChangeWatcher
import org.connectbot.ui.common.TouchEventWatcher
import org.connectbot.ui.common.ViewVisibility


/**
 * Data Binding adapters specific to the app.
 */
object BindingAdapters {
	@JvmStatic
	@BindingAdapter("visibleGone")
	fun showHideGone(view: View, show: LiveData<Boolean>) {
		view.visibility = if (show.value == true) View.VISIBLE else View.GONE
	}

	@JvmStatic
	@BindingAdapter("viewVisibility")
	fun viewVisibility(view: View, show: LiveData<ViewVisibility>) {
		view.visibility = when (show.value) {
			ViewVisibility.GONE -> View.GONE
			ViewVisibility.INVISIBLE -> View.INVISIBLE
			ViewVisibility.VISIBLE -> View.VISIBLE
			null -> View.GONE
		}
	}

	@JvmStatic
	@BindingAdapter("hostColor")
	fun hostColor(view: TextView, show: LiveData<Host.HostColor>) {
		view.text = Converters.hostColorToStr(show.value)
	}

	@JvmStatic
	@BindingAdapter("updateOnFocusLost")
	fun updateOnFocusLost(view: EditText, listener: TextChangeWatcher) {
		class TextAndFocusWatcher: View.OnFocusChangeListener {
			var focused: Boolean = false

			override fun onFocusChange(v: View?, hasFocus: Boolean) {
				val textView = v as TextView
				if (hasFocus) {
					textView.setTag(R.id.previous_value, textView.text.toString())
				} else {
					val oldValue = textView.getTag(R.id.previous_value) as String
					val newValue = textView.text.toString()
					if (oldValue != newValue) {
						listener.onTextChanged(newValue)
					}
				}
				focused = hasFocus
			}
		}

		val watcher = TextAndFocusWatcher()
		view.onFocusChangeListener = watcher
	}

	@JvmStatic
	@BindingAdapter("onFocusChanged")
	fun onFocusChanged(view: View, listener: FocusChangeWatcher) {
		view.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
			listener.onFocusChanged(hasFocus)
		}
	}

	@JvmStatic
	@BindingAdapter("updateImmediately")
	fun updateImmediately(view: EditText, listener: TextChangeWatcher) {
		view.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {
				listener.onTextChanged(view.text.toString())
			}

			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
			}

			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
			}
		})
	}

	@JvmStatic
	@BindingAdapter("textAsInt")
	fun textAsInt(view: TextView, show: LiveData<Int>) {
		view.text = show.value.toString()
	}

	@JvmStatic
	@BindingAdapter("android:selected")
	fun setImageButtonSelected(imageButton: ImageButton, selected: Boolean) {
		imageButton.isSelected = selected
	}

	@JvmStatic
	@BindingAdapter("timeAgo")
	fun timeAgoFromLong(view: TextView, lastConnect: Long) {
		val text = if (lastConnect > 0) {
			DateUtils.getRelativeTimeSpanString(lastConnect * 1000)
		} else {
			view.context.getString(R.string.bind_never)
		}
		view.text = text
	}

	@JvmStatic
	@BindingAdapter("error")
	fun setError(view: TextInputLayout, errorMessage: CharSequence?) {
		view.error = errorMessage
	}

	@JvmStatic
	@BindingAdapter("onXYTouch")
	fun setTouchListener(view: View, listener: TouchEventWatcher) {
		view.setOnTouchListener { v, event ->
			listener.onTouch(event.x, event.y)
			true
		}
	}
}

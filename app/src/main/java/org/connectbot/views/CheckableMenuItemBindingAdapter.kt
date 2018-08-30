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

package org.connectbot.views

import android.widget.CompoundButton
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseBindingMethod
import androidx.databinding.InverseBindingMethods

@BindingMethods(value = [
	BindingMethod(type = CheckableMenuItem::class, attribute = "android:onCheckChanged", method = "setOnCheckChangedListener")
])
@InverseBindingMethods(value = [
	InverseBindingMethod(type = CheckableMenuItem::class, attribute = "android:checked")
])
class CheckableMenuItemBindingAdapter {
	@BindingAdapter("android:checked")
	fun setChecked(view: CheckableMenuItem, checked: Boolean) {
		if (view.checked != checked) {
			view.checked = checked
		}
	}

	@BindingAdapter(value = ["android:onCheckChanged", "android:checkedAttrChanged"], requireAll = false)
	fun setListeners(view: CheckableMenuItem, listener: CompoundButton.OnCheckedChangeListener?, attrChange: InverseBindingListener?) {
		view.setOnCheckedChangeListener { buttonView, isChecked ->
			listener?.onCheckedChanged(buttonView, isChecked)
			attrChange?.onChange()
		}
	}
}

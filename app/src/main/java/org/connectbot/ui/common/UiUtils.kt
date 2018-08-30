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

package org.connectbot.ui.common

import android.content.Context
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.connectbot.R

fun <T> createPopUp(v: View, arg: T, options: List<Pair<String, (T) -> Unit>>) {
	val popup = PopupMenu(v.context, v)
	popup.setOnMenuItemClickListener { item ->
		options[item.itemId].second(arg)
		true
	}
	options.forEachIndexed { index, pair ->
		popup.menu.add(Menu.NONE, index, Menu.NONE, pair.first)
	}
	popup.show()
}

fun <T> createPopUp(v: View, callback: (T) -> Unit, options: List<Pair<String, T>>) {
	val popup = PopupMenu(v.context, v)
	popup.setOnMenuItemClickListener { item ->
		callback(options[item.itemId].second)
		true
	}
	options.forEachIndexed { index, pair ->
		popup.menu.add(Menu.NONE, index, Menu.NONE, pair.first)
	}
	popup.show()
}

fun <T> createPopUp(viewLifecycleOwner: LifecycleOwner, v: View, callback: (T) -> Unit, options: LiveData<List<Pair<String, T>>>) {
	options.observe(viewLifecycleOwner, Observer {
		val popup = PopupMenu(v.context, v)
		popup.setOnMenuItemClickListener { item ->
			callback(it[item.itemId].second)
			true
		}
		it.forEachIndexed { index, pair ->
			popup.menu.add(Menu.NONE, index, Menu.NONE, pair.first)
		}
		popup.show()
		options.removeObservers(viewLifecycleOwner)
	})
}

fun <T> showDeleteDialog(context: Context, item: T, callback: (T) -> Unit, nickname: String?) {
	AlertDialog.Builder(context, R.style.AlertDialogTheme)
		.setMessage(context.getString(R.string.delete_message, nickname))
		.setPositiveButton(R.string.delete_pos) { _, _ -> callback(item) }
		.setNegativeButton(R.string.delete_neg, null).show()
}

enum class ViewVisibility {
	VISIBLE,
	INVISIBLE,
	GONE;
}

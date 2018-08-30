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

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Spinner
import androidx.databinding.ViewDataBinding

abstract class DataBoundSpinnerAdapter<T, V : ViewDataBinding>(val spinner: Spinner) : BaseAdapter() {
	private var _list: List<T>? = null

	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val view: View
		val holder: DataBoundViewHolder<V>
		if (convertView != null) {
			view = convertView
			holder = view.tag as DataBoundViewHolder<V>
		} else {
			val binding = createBinding(spinner)
			holder = DataBoundViewHolder(binding)
			view = holder.itemView;
			view.tag = holder
		}
		bind(holder.binding, getItem(position))
		holder.binding.executePendingBindings()
		return view
	}

	fun submitList(newList: List<T>) {
		this._list = newList
		notifyDataSetChanged()
	}

	override fun getItem(position: Int): T = _list?.get(position) ?: throw IllegalStateException("list was not initialized")

	override fun getItemId(position: Int): Long = position.toLong()

	override fun getCount(): Int = _list?.size ?: 0

	protected abstract fun createBinding(parent: ViewGroup): V

	protected abstract fun bind(binding: V, item: T)
}

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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Spinner
import androidx.annotation.StringRes
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.databinding.ItemColorBinding
import org.connectbot.databinding.ItemColorSpinnerBinding
import org.connectbot.db.entity.Color
import org.connectbot.ui.common.DataBoundListAdapter
import org.connectbot.ui.common.DataBoundSpinnerAdapter

class ColorsAdapter(
	private val dataBindingComponent: DataBindingComponent,
	appExecutors: AppExecutors,
	private val colorClickCallback: ((Color) -> Unit)?
) : DataBoundListAdapter<Color, ItemColorBinding>(
	appExecutors = appExecutors,
	diffCallback = object : DiffUtil.ItemCallback<Color>() {
		override fun areItemsTheSame(oldItem: Color, newItem: Color): Boolean {
			return oldItem.number == newItem.number
		}

		override fun areContentsTheSame(oldItem: Color, newItem: Color): Boolean {
			return oldItem.id == newItem.id &&
				oldItem.value == newItem.value &&
				oldItem.number == newItem.number
		}
	}
) {
	override fun createBinding(parent: ViewGroup): ItemColorBinding {
		val binding = DataBindingUtil.inflate<ItemColorBinding>(
			LayoutInflater.from(parent.context),
			R.layout.item_color,
			parent,
			false,
			dataBindingComponent
		)
		binding.root.setOnClickListener {
			binding.color?.let { color ->
				colorClickCallback?.invoke(color)
			}
		}
		return binding
	}

	override fun bind(binding: ItemColorBinding, item: Color) {
		binding.color = item
	}
}

class ColorsSpinnerAdapter(
	spinner: Spinner,
	@StringRes val descriptionRes: Int,
	private val dataBindingComponent: DataBindingComponent
) : DataBoundSpinnerAdapter<Color, ItemColorSpinnerBinding>(spinner) {
	override fun createBinding(parent: ViewGroup): ItemColorSpinnerBinding {
		return DataBindingUtil.inflate(
			LayoutInflater.from(parent.context),
			R.layout.item_color_spinner,
			parent,
			false,
			dataBindingComponent
		)
	}

	override fun bind(binding: ItemColorSpinnerBinding, item: Color) {
		binding.color = item
		binding.description = descriptionRes
	}
}

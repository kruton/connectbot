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

package org.connectbot.ui.hosts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.databinding.ItemHostBinding
import org.connectbot.db.entity.Host
import org.connectbot.ui.common.DataBoundListAdapter

class HostsAdapter(
	private val dataBindingComponent: DataBindingComponent,
	appExecutors: AppExecutors,
	private val clickCallback: ((Host) -> Unit)?,
	private val longClickCallback: ((View, Host) -> Unit)?
) : DataBoundListAdapter<Host, ItemHostBinding>(
	appExecutors = appExecutors,
	diffCallback = object : DiffUtil.ItemCallback<Host>() {
		override fun areItemsTheSame(oldItem: Host, newItem: Host): Boolean {
			return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: Host, newItem: Host): Boolean {
			return oldItem.equals(newItem)
		}
	}
) {
	override fun createBinding(parent: ViewGroup): ItemHostBinding {
		val binding = DataBindingUtil.inflate<ItemHostBinding>(
			LayoutInflater.from(parent.context),
			R.layout.item_host,
			parent,
			false,
			dataBindingComponent
		)
		binding.root.setOnClickListener {
			binding.host?.let { host ->
				clickCallback?.invoke(host)
			}
		}
		binding.root.setOnLongClickListener {
			binding.host?.let { host ->
				longClickCallback?.invoke(it, host)
				true
			} ?: false
		}
		return binding
	}

	override fun bind(binding: ItemHostBinding, item: Host) {
		binding.host = item
	}
}

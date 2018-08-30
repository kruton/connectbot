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

package org.connectbot.ui.portforwards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.databinding.ItemPortForwardBinding
import org.connectbot.db.entity.PortForward
import org.connectbot.ui.common.DataBoundListAdapter

class PortForwardsAdapter(
	private val dataBindingComponent: DataBindingComponent,
	appExecutors: AppExecutors,
	private val clickCallback: ((PortForward) -> Unit)?,
	private val longClickCallback: ((PortForward, View) -> Unit)?,
	private val settingsClickCallback: ((PortForward) -> Unit)?
) : DataBoundListAdapter<PortForward, ItemPortForwardBinding>(
	appExecutors = appExecutors,
	diffCallback = object : DiffUtil.ItemCallback<PortForward>() {
		override fun areItemsTheSame(oldItem: PortForward, newItem: PortForward): Boolean {
			return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: PortForward, newItem: PortForward): Boolean {
			return oldItem.equals(newItem)
		}
	}
) {
	override fun createBinding(parent: ViewGroup): ItemPortForwardBinding {
		val binding = DataBindingUtil.inflate<ItemPortForwardBinding>(
			LayoutInflater.from(parent.context),
			R.layout.item_port_forward,
			parent,
			false,
			dataBindingComponent
		)
		binding.root.setOnClickListener {
			binding.portForward?.let { portForward ->
				clickCallback?.invoke(portForward)
			}
		}
		binding.root.setOnLongClickListener {
			binding.portForward?.let { portForward ->
				longClickCallback?.invoke(portForward, it)
				true
			} ?: false
		}
		binding.settingsButton.setOnClickListener {
			binding.portForward?.let { portForward ->
				settingsClickCallback?.invoke(portForward)
			}
		}
		return binding
	}

	override fun bind(binding: ItemPortForwardBinding, item: PortForward) {
		binding.portForward = item
	}
}

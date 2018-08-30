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

package org.connectbot.ui.pubkeys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.databinding.ItemPubkeyBinding
import org.connectbot.db.entity.Pubkey
import org.connectbot.ui.common.DataBoundListAdapter

class PubkeysAdapter (
	private val dataBindingComponent: DataBindingComponent,
	appExecutors: AppExecutors,
	private val clickCallback: ((Pubkey) -> Unit)?,
	private val longClickCallback: ((Pubkey, View) -> Unit)?,
	private val settingsClickCallback: ((Pubkey) -> Unit)?
) : DataBoundListAdapter<Pubkey, ItemPubkeyBinding>(
	appExecutors = appExecutors,
	diffCallback = object : DiffUtil.ItemCallback<Pubkey>() {
		override fun areItemsTheSame(oldItem: Pubkey, newItem: Pubkey): Boolean {
			return oldItem.nickname == newItem.nickname
		}

		override fun areContentsTheSame(oldItem: Pubkey, newItem: Pubkey): Boolean {
			return oldItem.equals(newItem)
		}
	}
) {
	override fun createBinding(parent: ViewGroup): ItemPubkeyBinding {
		val binding = DataBindingUtil.inflate<ItemPubkeyBinding>(
			LayoutInflater.from(parent.context),
			R.layout.item_pubkey,
			parent,
			false,
			dataBindingComponent
		)

		binding.root.setOnClickListener {
			binding.pubkey?.let { pubkey ->
				clickCallback?.invoke(pubkey)
			}
		}
		binding.root.setOnLongClickListener {
			binding.pubkey?.let { pubkey ->
				longClickCallback?.invoke(pubkey, it)
				true
			} ?: false
		}
		binding.keySettingsButton.setOnClickListener {
			binding.pubkey?.let { pubkey ->
				settingsClickCallback?.invoke(pubkey)
			}
		}

		return binding
	}

	override fun bind(binding: ItemPubkeyBinding, item: Pubkey) {
		binding.pubkey = item
	}
}

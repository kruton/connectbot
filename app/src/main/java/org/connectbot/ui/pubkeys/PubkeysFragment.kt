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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.binding.FragmentDataBindingComponent
import org.connectbot.databinding.FragmentPubkeysBinding
import org.connectbot.ui.common.createPopUp
import org.connectbot.ui.common.showDeleteDialog
import org.connectbot.ui.pubkeys.PubkeysFragmentDirections.actionPubkeysToGeneratePubkeyFragment
import org.connectbot.ui.pubkeys.PubkeysFragmentDirections.actionPubkeysToImportPubkeyFragment
import org.connectbot.util.ResourceProvider
import org.connectbot.util.autoCleared
import javax.inject.Inject

class PubkeysFragment : DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: PubkeysViewModel

	@Inject
	lateinit var appExecutors: AppExecutors

	// mutable for testing
	var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentPubkeysBinding>()

	private var adapter by autoCleared<PubkeysAdapter>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		binding = DataBindingUtil.inflate(
			inflater,
			R.layout.fragment_pubkeys,
			container,
			false,
			dataBindingComponent
		)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel = ViewModelProviders.of(this, viewModelFactory)
			.get(PubkeysViewModel::class.java)

		with(binding) {
			vm = viewModel
			setLifecycleOwner(viewLifecycleOwner)
		}

		with(viewModel) {
			addPubkeyClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionPubkeysToGeneratePubkeyFragment()) })
			importPubkeyClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionPubkeysToImportPubkeyFragment()) })
			pubkeyDeleteClick.observe(viewLifecycleOwner, Observer {
				showDeleteDialog(context!!, it, viewModel::onPubkeyDeleteConfirmed, it.nickname) })
			pubkeyCopyPublicClick.observe(viewLifecycleOwner, Observer {
				viewModel.onCopyPublicKeyRequested(it) })
			pubkeyCopyPrivateClick.observe(viewLifecycleOwner, Observer {
				viewModel.onCopyPrivateKeyRequested(it) })
			pubkeyContextMenuItems.observe(viewLifecycleOwner, Observer { })
			resourceProvider.value = ResourceProvider(resources)
		}

		val pubkeysAdapter = PubkeysAdapter(
			dataBindingComponent = dataBindingComponent,
			appExecutors = appExecutors,
			clickCallback = {
				TODO("load key into manager")
			},
			longClickCallback = { pubkey, v ->
				viewModel.pubkeyContextMenuItems.value?.let { options ->
					createPopUp(v, pubkey, options)
				}
			},
			settingsClickCallback = {
				findNavController().navigate(
					actionPubkeysToGeneratePubkeyFragment().setPubkeyId(it.id))
			}
		)

		binding.pubkeyList.adapter = pubkeysAdapter
		adapter = pubkeysAdapter
		initPubkeyList(viewModel)
	}

	override fun onDetach() {
		super.onDetach()
		viewModel.resourceProvider.value = null
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		inflater?.inflate(R.menu.pubkeys, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.import_existing_key_icon -> viewModel.importPubkeyClicked.value = item
			else -> return false
		}
		return true
	}

	private fun initPubkeyList(viewModel: PubkeysViewModel) {
		viewModel.pubkeys.observe(viewLifecycleOwner, Observer { list ->
			adapter.submitList(list)
		})
	}
}

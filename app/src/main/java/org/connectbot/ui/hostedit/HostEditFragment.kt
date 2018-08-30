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

package org.connectbot.ui.hostedit

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
import org.connectbot.databinding.FragmentHostEditBinding
import org.connectbot.ui.common.createPopUp
import org.connectbot.util.ResourceProvider
import org.connectbot.util.autoCleared
import javax.inject.Inject

class HostEditFragment : DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: HostEditorViewModel

	@Inject
	lateinit var appExecutors: AppExecutors

	private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentHostEditBinding>()

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		viewModel = ViewModelProviders.of(this, viewModelFactory)[HostEditorViewModel::class.java]

		with(binding) {
			vm = viewModel
			setLifecycleOwner(viewLifecycleOwner)
		}

		with(viewModel) {
			protocolsClicked.observe(viewLifecycleOwner, Observer { v -> popupTransportList(v) })
			colorsClicked.observe(viewLifecycleOwner, Observer { v -> popupColorsList(v) })
			usePubkeysClicked.observe(viewLifecycleOwner, Observer { v -> popupUsePubkeysList(v) })
			delKeysClicked.observe(viewLifecycleOwner, Observer { v -> popupDelKeyList(v) })
			encodingsClicked.observe(viewLifecycleOwner, Observer { v -> popupEncodingsList(v) })
			fragmentFinished.observe(viewLifecycleOwner, Observer { findNavController().navigateUp() })
			resourceProvider.value = ResourceProvider(resources)
			onHostId(HostEditFragmentArgs.fromBundle(arguments).hostId)
		}
	}

	override fun onDestroy() {
		super.onDestroy()

		viewModel.resourceProvider.value = null
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val dataBinding = DataBindingUtil.inflate<FragmentHostEditBinding>(
			inflater,
			R.layout.fragment_host_edit,
			container,
			false,
			dataBindingComponent
		)
		binding = dataBinding
		return dataBinding.root
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		inflater?.inflate(R.menu.host_edit, menu)
	}

	override fun onPrepareOptionsMenu(menu: Menu?) {
		menu?.let {
			it.findItem(R.id.add).isVisible = !viewModel.existingHost
			it.findItem(R.id.save).isVisible = viewModel.existingHost
		}
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.save -> viewModel.onSaveButtonClicked()
			R.id.add -> viewModel.onSaveButtonClicked()
			else -> return false
		}
		return true
	}

	private fun popupTransportList(v: View) {
		createPopUp(viewLifecycleOwner, v, viewModel::onProtocolSelected, viewModel.protocols)
	}

	private fun popupColorsList(v: View) {
		createPopUp(viewLifecycleOwner, v, viewModel::onColorSelected, viewModel.colorNamesValues)
	}

	private fun popupUsePubkeysList(v: View) {
		createPopUp(viewLifecycleOwner, v, viewModel::onPubkeySelected, viewModel.pubkeyNamesValues)
	}

	private fun popupDelKeyList(v: View) {
		createPopUp(viewLifecycleOwner, v, viewModel::onDelKeySelected, viewModel.delKeyNamesValues)
	}

	private fun popupEncodingsList(v: View) {
		createPopUp(viewLifecycleOwner, v, viewModel::onEncodingSelected, viewModel.possibleEncodings)
	}
}

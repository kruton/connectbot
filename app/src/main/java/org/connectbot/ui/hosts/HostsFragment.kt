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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.binding.FragmentDataBindingComponent
import org.connectbot.databinding.FragmentHostsBinding
import org.connectbot.db.entity.Host
import org.connectbot.testing.OpenForTesting
import org.connectbot.ui.common.EventObserver
import org.connectbot.ui.common.createPopUp
import org.connectbot.ui.hosts.HostsFragmentDirections.actionGlobalSettings
import org.connectbot.ui.hosts.HostsFragmentDirections.actionHostsToColors
import org.connectbot.ui.hosts.HostsFragmentDirections.actionHostsToConsole
import org.connectbot.ui.hosts.HostsFragmentDirections.actionHostsToHelp
import org.connectbot.ui.hosts.HostsFragmentDirections.actionHostsToHostEditorFragment
import org.connectbot.ui.hosts.HostsFragmentDirections.actionHostsToPortForwards
import org.connectbot.ui.hosts.HostsFragmentDirections.actionHostsToPubkeys
import org.connectbot.util.ResourceProvider
import org.connectbot.util.autoCleared
import javax.inject.Inject

@OpenForTesting
class HostsFragment : DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: HostsViewModel

	@Inject
	lateinit var appExecutors: AppExecutors

	// mutable for testing
	@Suppress("LeakingThis")
	var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentHostsBinding>()

	private var adapter by autoCleared<HostsAdapter>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		binding = DataBindingUtil.inflate(
				inflater,
				R.layout.fragment_hosts,
				container,
				false,
				dataBindingComponent
		)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel = ViewModelProviders.of(this, viewModelFactory)[HostsViewModel::class.java]

		with (binding) {
			vm = viewModel
			setLifecycleOwner(viewLifecycleOwner)
		}

		with (viewModel) {
			addHostClicked.observe(viewLifecycleOwner, Observer { v ->
				findNavController(v).navigate(actionHostsToHostEditorFragment()) })
			pubkeysButtonClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionHostsToPubkeys()) })
			colorsButtonClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionHostsToColors()) })
			settingsButtonClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionGlobalSettings()) })
			helpButtonClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionHostsToHelp()) })
			hostDisconnectClick.observe(viewLifecycleOwner, Observer {
				})
			hostEditClick.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionHostsToHostEditorFragment().setHostId(it.id)) })
			hostPortForwardClick.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionHostsToPortForwards(it.id)) })
			hostDeleteClick.observe(viewLifecycleOwner, Observer {
				showDeleteDialog(it, viewModel::deleteHostClicked) })
			consoleNavigation.observe(viewLifecycleOwner, EventObserver {
				findNavController().navigate(actionHostsToConsole().setDisplayHost(it)) })
			sortByColor.observe(viewLifecycleOwner, Observer { })
			hostContextMenuItems.observe(viewLifecycleOwner, Observer { })
			resourceProvider.value = ResourceProvider(resources)
		}

		val hostsAdapter = HostsAdapter(
			dataBindingComponent = dataBindingComponent,
			appExecutors = appExecutors,
			clickCallback = { host ->
				viewModel.onHostClicked(host) },
			longClickCallback = { v, host ->
				viewModel.hostContextMenuItems.value?.let { options ->
					createPopUp(v, host, options)
				}
			}
		)

		binding.hostList.adapter = hostsAdapter
		adapter = hostsAdapter
		initHostsList(viewModel)
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
		inflater?.inflate(R.menu.hosts, menu)
	}

	override fun onPrepareOptionsMenu(menu: Menu?) {
		viewModel.sortByColor.value?.let { sortByColor ->
			// If we're sorting by color, we shouldn't show the sort-by-color selection.
			menu?.findItem(R.id.sort_by_color_menu_item)?.isVisible = !sortByColor
			menu?.findItem(R.id.sort_by_name_menu_item)?.isVisible = sortByColor
		}
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.sort_by_color_menu_item -> viewModel.onSortByColorClicked()
			R.id.sort_by_name_menu_item -> viewModel.onSortByNameClicked()
			R.id.pubkeys_menu_item -> viewModel.pubkeysButtonClicked.value = item
			R.id.colors_menu_item -> viewModel.colorsButtonClicked.value = item
			R.id.settings_menu_item -> viewModel.settingsButtonClicked.value = item
			R.id.help_menu_item -> viewModel.helpButtonClicked.value = item
			else -> return false
		}
		return true
	}

	private fun showDeleteDialog(host: Host, callback: (Host) -> Unit) {
		AlertDialog.Builder(context!!, R.style.AlertDialogTheme)
			.setMessage(getString(R.string.delete_message, host.nickname))
			.setPositiveButton(R.string.delete_pos) { _, _ -> callback(host) }
			.setNegativeButton(R.string.delete_neg, null).show()
	}

	private fun initHostsList(viewModel: HostsViewModel) {
		viewModel.hosts.observe(viewLifecycleOwner, Observer { list ->
			adapter.submitList(list)
		})
	}
}

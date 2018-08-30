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

import android.os.Bundle
import android.view.LayoutInflater
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
import org.connectbot.databinding.FragmentPortForwardsBinding
import org.connectbot.ui.common.createPopUp
import org.connectbot.ui.common.showDeleteDialog
import org.connectbot.ui.portforwards.PortForwardsFragmentDirections.actionPortForwardsToPortForwardEdit
import org.connectbot.util.ResourceProvider
import org.connectbot.util.autoCleared
import javax.inject.Inject

class PortForwardsFragment: DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: PortForwardsViewModel

	@Inject
	lateinit var appExecutors: AppExecutors

	// mutable for testing
	@Suppress("LeakingThis")
	var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentPortForwardsBinding>()

	private var adapter by autoCleared<PortForwardsAdapter>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		binding = DataBindingUtil.inflate(
			inflater,
			R.layout.fragment_port_forwards,
			container,
			false,
			dataBindingComponent
		)
		return binding.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		viewModel = ViewModelProviders.of(this, viewModelFactory)[PortForwardsViewModel::class.java]

		with (binding) {
			vm = viewModel
			setLifecycleOwner(viewLifecycleOwner)
		}

		val args = PortForwardsFragmentArgs.fromBundle(arguments)

		with (viewModel) {
			hostId.value = args.hostId
			addPortForwardClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionPortForwardsToPortForwardEdit(args.hostId)) })
			portForwardDeleteClick.observe(viewLifecycleOwner, Observer {
				showDeleteDialog(context!!, it, viewModel::onPortForwardDeleteConfirmed, it.nickname) })
			portForwardContextMenuItems.observe(viewLifecycleOwner, Observer { })
			resourceProvider.value = ResourceProvider(resources)
		}

		val portForwardsAdapter = PortForwardsAdapter(
			dataBindingComponent = dataBindingComponent,
			appExecutors = appExecutors,
			clickCallback = {
//				findNavController().navigate(PortFor().setHostId(it.id))
//				TODO("enable / disable port forward")
			},
			longClickCallback = { portForward, v ->
				viewModel.portForwardContextMenuItems.value?.let { options ->
					createPopUp(v, portForward, options)
				}
			},
			settingsClickCallback = {
				findNavController().navigate(actionPortForwardsToPortForwardEdit(args.hostId).setPortForwardId(it.id))
			})

		binding.portForwardList.adapter = portForwardsAdapter
		adapter = portForwardsAdapter
		initPortForwardsList(viewModel)
	}

	private fun initPortForwardsList(viewModel: PortForwardsViewModel) {
		viewModel.portForwards.observe(viewLifecycleOwner, Observer { list ->
			adapter.submitList(list)
		})
	}
}

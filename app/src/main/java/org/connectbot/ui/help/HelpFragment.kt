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

package org.connectbot.ui.help

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
import org.connectbot.R
import org.connectbot.binding.FragmentDataBindingComponent
import org.connectbot.databinding.FragmentHelpBinding
import org.connectbot.ui.help.HelpFragmentDirections.actionHelpToEula
import org.connectbot.ui.help.HelpFragmentDirections.actionHelpToHints
import org.connectbot.util.autoCleared
import javax.inject.Inject

class HelpFragment : DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: HelpViewModel

	private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentHelpBinding>()

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		viewModel = ViewModelProviders.of(this, viewModelFactory)[HelpViewModel::class.java]

		with(binding) {
			vm = viewModel
			version = Version.getInstance(activity!!.applicationContext)
			setLifecycleOwner(viewLifecycleOwner)
		}

		with (viewModel) {
			hintsButtonClicked.observe(viewLifecycleOwner, Observer {
				findNavController().navigate(actionHelpToHints())
			})
			shortcutsButtonClicked.observe(viewLifecycleOwner, Observer { view ->
				context?.let {
					androidx.appcompat.app.AlertDialog.Builder(
						it, R.style.AlertDialogTheme)
						.setView(R.layout.dia_keyboard_shortcuts)
						.setTitle(R.string.keyboard_shortcuts)
						.show()
				}
			})
			eulaButtonClicked.observe(viewLifecycleOwner, Observer { view ->
				findNavController().navigate(actionHelpToEula())
			})
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val dataBinding = DataBindingUtil.inflate<FragmentHelpBinding>(
			inflater,
			R.layout.fragment_help,
			container,
			false,
			dataBindingComponent
		)
		binding = dataBinding
		return dataBinding.root
	}
}

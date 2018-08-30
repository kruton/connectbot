/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2019 Kenny Root, Jeffrey Sharkey
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

package org.connectbot.ui.console

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import dagger.android.support.DaggerFragment
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.binding.FragmentDataBindingComponent
import org.connectbot.databinding.FragmentConsoleBinding
import org.connectbot.util.autoCleared
import javax.inject.Inject

class ConsoleFragment: DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: ConsoleViewModel

	@Inject
	lateinit var appExecutors: AppExecutors

	// mutable for testing
	@Suppress("LeakingThis")
	var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentConsoleBinding>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		binding = DataBindingUtil.inflate(
			inflater,
			R.layout.fragment_console,
			container,
			false,
			dataBindingComponent
		)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel = ViewModelProviders.of(this, viewModelFactory)[ConsoleViewModel::class.java]

		with(binding) {
			vm = viewModel
			setLifecycleOwner(viewLifecycleOwner)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		inflater?.inflate(R.menu.console, menu)
	}
}

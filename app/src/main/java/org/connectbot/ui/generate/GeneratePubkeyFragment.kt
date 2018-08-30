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

package org.connectbot.ui.generate

import android.app.ProgressDialog
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
import org.connectbot.databinding.FragmentGeneratePubkeyBinding
import org.connectbot.ui.common.createPopUp
import org.connectbot.util.ResourceProvider
import org.connectbot.util.autoCleared
import javax.inject.Inject

class GeneratePubkeyFragment : DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: GeneratePubkeyViewModel

	@Inject
	lateinit var appExecutors: AppExecutors

	private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentGeneratePubkeyBinding>()

	var progressDialog by autoCleared<ProgressDialog>()

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		viewModel = ViewModelProviders.of(this, viewModelFactory)[GeneratePubkeyViewModel::class.java]

		with(binding) {
			vm = viewModel
			setLifecycleOwner(viewLifecycleOwner)
		}

		with (viewModel) {
			keyTypeClicked.observe(viewLifecycleOwner, Observer { popupKeyTypeList(it) })
			keyCurveClicked.observe(viewLifecycleOwner, Observer { popupKeyCurveList(it) })
			fragmentFinished.observe(viewLifecycleOwner, Observer { findNavController().navigateUp() })
			saveButtonEnabled.observe(viewLifecycleOwner, Observer { activity?.invalidateOptionsMenu() })
			showGenerationDialog.observe(viewLifecycleOwner, Observer { showHideGenerationDialog(it) })
			resourceProvider.value = ResourceProvider(resources)
			onPubkeyId(GeneratePubkeyFragmentArgs.fromBundle(arguments).pubkeyId)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val dataBinding = DataBindingUtil.inflate<FragmentGeneratePubkeyBinding>(
			inflater,
			R.layout.fragment_generate_pubkey,
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

	override fun onDestroy() {
		super.onDestroy()

		viewModel.resourceProvider.value = null
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		inflater?.inflate(R.menu.generate, menu)
	}

	override fun onPrepareOptionsMenu(menu: Menu?) {
		menu?.also {
			it.findItem(R.id.add).also { item ->
				item.isVisible = !viewModel.existingPubkey
				viewModel.saveButtonEnabled.value?.let { item.isEnabled = it }
			}
			it.findItem(R.id.save).also { item ->
				item.isVisible = viewModel.existingPubkey
				viewModel.saveButtonEnabled.value?.let { item.isEnabled = it }
			}
		}
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.save -> viewModel.onSaveButtonClicked()
			R.id.add -> viewModel.onAddButtonClicked()
			else -> return false
		}
		return true
	}

	private fun popupKeyTypeList(v: View) {
		createPopUp(viewLifecycleOwner, v, viewModel::onKeyTypeSelected, viewModel.keyTypes)
	}

	private fun popupKeyCurveList(v: View) {
		createPopUp(v, viewModel::onKeyCurveSelected, viewModel.keyCurves)
	}

	// TODO move this to something less annoying to the user.
	private fun showHideGenerationDialog(show: Boolean) {
		if (show) {
			progressDialog = ProgressDialog(activity)
			progressDialog.setMessage(resources.getText(R.string.pubkey_generating))
			progressDialog.isIndeterminate = true
			progressDialog.setCancelable(false)
			progressDialog.show()
		} else {
			progressDialog.dismiss()
		}
	}
}

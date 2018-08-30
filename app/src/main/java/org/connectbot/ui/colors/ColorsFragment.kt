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

package org.connectbot.ui.colors

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import dagger.android.support.DaggerFragment
import org.connectbot.AppExecutors
import org.connectbot.R
import org.connectbot.binding.FragmentDataBindingComponent
import org.connectbot.databinding.FragmentColorsBinding
import org.connectbot.util.autoCleared
import javax.inject.Inject

class ColorsFragment: DaggerFragment() {
	@Inject
	lateinit var viewModelFactory: ViewModelProvider.Factory

	private lateinit var viewModel: ColorsViewModel

	@Inject
	lateinit var appExecutors: AppExecutors

	private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
	var binding by autoCleared<FragmentColorsBinding>()

	private var gridAdapter by autoCleared<ColorsAdapter>()
	private var fgAdapter by autoCleared<ColorsSpinnerAdapter>()
	private var bgAdapter by autoCleared<ColorsSpinnerAdapter>()

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		viewModel = ViewModelProviders.of(this, viewModelFactory)[ColorsViewModel::class.java]

		with(binding) {
			vm = viewModel
			setLifecycleOwner(viewLifecycleOwner)
		}

		with (viewModel) {
			colors.observe(viewLifecycleOwner, Observer {
				gridAdapter.submitList(it)
				fgAdapter.submitList(it)
				bgAdapter.submitList(it)
			})
		}

		val gridAdapter = ColorsAdapter(
			dataBindingComponent = dataBindingComponent,
			appExecutors = appExecutors
		) { color ->
			val dialog = ColorPickerDialog.newBuilder()
				.setDialogType(ColorPickerDialog.TYPE_CUSTOM)
				.setColor(color.value!!)
				.setAllowPresets(false)
				.setAllowCustom(true)
				.create()
			dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
				override fun onDialogDismissed(dialogId: Int) {
				}

				override fun onColorSelected(dialogId: Int, colorValue: Int) {
					color.value = colorValue
					viewModel.onColorUpdated(color)
				}
			})
			activity?.supportFragmentManager?.let {
				dialog.show(it, "color-pick-id")
			}
		}

		fgAdapter = ColorsSpinnerAdapter(
			binding.fg,
			R.string.colors_fg_label,
			dataBindingComponent)
		binding.fg.adapter = fgAdapter

		bgAdapter = ColorsSpinnerAdapter(
			binding.bg,
			R.string.color_bg_label,
			dataBindingComponent)
		binding.bg.adapter = bgAdapter

		this.gridAdapter = gridAdapter
		binding.colorGrid.adapter = this.gridAdapter
		val columnQuantity = ColumnQuantity(context!!, R.layout.item_color)
		val layoutManager = GridLayoutManager(context, columnQuantity.numberOfColumns)
		binding.colorGrid.layoutManager = layoutManager
		initAdapters(viewModel)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val dataBinding = DataBindingUtil.inflate<FragmentColorsBinding>(
			inflater,
			R.layout.fragment_colors,
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
		inflater?.inflate(R.menu.colors, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		return when (item?.itemId) {
			R.id.reset_colors -> viewModel.onResetColorsSelected()
			else -> false
		}
	}

	private fun initAdapters(viewModel: ColorsViewModel) {
		viewModel.colors.observe(viewLifecycleOwner, Observer { list ->
			gridAdapter.submitList(list)
			fgAdapter.submitList(list)
			bgAdapter.submitList(list)
		})
	}

	inner class ColumnQuantity constructor(val context: Context, @LayoutRes val layout: Int) {
		private val displayMetrics = context.resources.displayMetrics
		private val width: Int

		init {
			val view = layoutInflater.inflate(layout, null)
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
			width = view.measuredWidth
		}

		val numberOfColumns: Int
			get() {
				val estimatedColumns = displayMetrics.widthPixels / width
				return when {
					calculateRemainingWhen(estimatedColumns) < 15 -> estimatedColumns - 1
					else -> estimatedColumns
				}
			}

		private fun calculateRemainingWhen(columns: Int) = displayMetrics.widthPixels - (columns * width)
	}
}

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

package org.connectbot.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.connectbot.ui.colors.ColorsViewModel
import org.connectbot.ui.console.ConsoleViewModel
import org.connectbot.ui.entropy.EntropyViewModel
import org.connectbot.ui.generate.GeneratePubkeyViewModel
import org.connectbot.ui.help.HelpViewModel
import org.connectbot.ui.hostedit.HostEditorViewModel
import org.connectbot.ui.hosts.HostsViewModel
import org.connectbot.ui.portforwardedit.PortForwardEditViewModel
import org.connectbot.ui.portforwards.PortForwardsViewModel
import org.connectbot.ui.pubkeys.PubkeysViewModel
import org.connectbot.viewmodel.AppViewModelFactory

@Suppress("unused")
@Module
abstract class ViewModelModule {
	@Binds
	@IntoMap
	@ViewModelKey(ColorsViewModel::class)
	abstract fun bindColorsViewModel(viewModel: ColorsViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(ConsoleViewModel::class)
	abstract fun bindsConsoleViewModel(viewModel: ConsoleViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(EntropyViewModel::class)
	abstract fun bindEntropyViewModel(viewModel: EntropyViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(GeneratePubkeyViewModel::class)
	abstract fun bindGeneratePubkeyViewModel(viewModel: GeneratePubkeyViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(HelpViewModel::class)
	abstract fun bindHelpViewModel(viewModel: HelpViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(HostsViewModel::class)
	abstract fun bindHostsViewModel(viewModel: HostsViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(HostEditorViewModel::class)
	abstract fun bindHostEditViewModel(viewModel: HostEditorViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(PortForwardEditViewModel::class)
	abstract fun bindPortForwardEditViewModel(viewModel: PortForwardEditViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(PortForwardsViewModel::class)
	abstract fun bindPortForwardsViewModel(viewModel: PortForwardsViewModel): ViewModel

	@Binds
	@IntoMap
	@ViewModelKey(PubkeysViewModel::class)
	abstract fun bindPubkeysViewModel(viewModel: PubkeysViewModel): ViewModel

	@Binds
	abstract fun bindViewModelFactory(factory: AppViewModelFactory): ViewModelProvider.Factory
}

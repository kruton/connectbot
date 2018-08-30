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

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.connectbot.ui.colors.ColorsFragment
import org.connectbot.ui.console.ConsoleFragment
import org.connectbot.ui.entropy.EntropyFragment
import org.connectbot.ui.generate.GeneratePubkeyFragment
import org.connectbot.ui.help.HelpFragment
import org.connectbot.ui.hostedit.HostEditFragment
import org.connectbot.ui.hosts.HostsFragment
import org.connectbot.ui.import_pubkey.ImportPubkeyFragment
import org.connectbot.ui.portforwardedit.PortForwardEditFragment
import org.connectbot.ui.portforwards.PortForwardsFragment
import org.connectbot.ui.pubkeys.PubkeysFragment
import org.connectbot.ui.settings.SettingsFragment

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {
	@ContributesAndroidInjector
	abstract fun contributeColorsFragment(): ColorsFragment

	@ContributesAndroidInjector
	abstract fun contributeConsoleFragment(): ConsoleFragment

	@ContributesAndroidInjector
	abstract fun contributeEntropyFragment(): EntropyFragment

	@ContributesAndroidInjector
	abstract fun contributeGeneratePubkeyFragment(): GeneratePubkeyFragment

	@ContributesAndroidInjector
	abstract fun contributesHelpFragment(): HelpFragment

	@ContributesAndroidInjector
	abstract fun contributeHostEditorFragment(): HostEditFragment

	@ContributesAndroidInjector
	abstract fun contributeHostsFragment(): HostsFragment

	@ContributesAndroidInjector
	abstract fun contributeImportPubkeyFragment(): ImportPubkeyFragment

	@ContributesAndroidInjector
	abstract fun contributePortForwardEditFragment(): PortForwardEditFragment

	@ContributesAndroidInjector
	abstract fun contributePortForwardsFragment(): PortForwardsFragment

	@ContributesAndroidInjector
	abstract fun contributePubkeysFragment(): PubkeysFragment

	@ContributesAndroidInjector
	abstract fun contributePreferencesFragment(): SettingsFragment
}

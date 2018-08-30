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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.whenever
import org.connectbot.db.entity.Host
import org.connectbot.db.entity.Host.Companion.MAXIMUM_FONT_SIZE
import org.connectbot.db.entity.Host.Companion.MINIMUM_FONT_SIZE
import org.connectbot.db.entity.Pubkey
import org.connectbot.repo.DataRepository
import org.connectbot.util.FakeListResourceProvider
import org.connectbot.util.InstantAppExecutors
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock

class HostEditorViewModelTest {
	@Rule
	@JvmField
	val rule: TestRule = InstantTaskExecutorRule()

	private val dataRepository = mock(DataRepository::class.java)

	@Test
	fun `connection details not expanded by default`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.uriListenerExpanded.test().assertValue(false)
	}

	@Test
	fun `connection detail expander visible by default`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.expanderVisible.test().assertValue(true)
	}

	@Test
	fun `default protocol is SSH`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.protocol.test().assertValue(SSH.getProtocolName())
	}

	@Test
	fun `switching to local protocol changes connection detail visibility`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val protocolObserver = vm.protocol.test()
		val expanderVisibleObserver = vm.expanderVisible.test()

		expanderVisibleObserver.assertValue(true)
		vm.toggleListenerExpanded()
		vm.onProtocolSelected(Local.getProtocolName())
		vm.quickConnectVisible.test().assertValue(false)
		protocolObserver.assertValue(Local.getProtocolName())
		expanderVisibleObserver.assertValue(false)
		vm.uriListenerExpanded.test().assertValue(true)
		vm.usernameVisible.test().assertValue(false)
		vm.hostnameVisible.test().assertValue(false)
		vm.portVisible.test().assertValue(false)
	}

	@Test
	fun `setting quick connect string propagates to other fields`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val quickConnectObserver = vm.quickConnectString.test()
		val hostnameObserver = vm.hostname.test()
		val usernameObserver = vm.username.test()
		val portObserver = vm.port.test()

		vm.onQuickConnectChanged("testuser@the.test.example.com:2100")
		quickConnectObserver.assertNoValue()
		hostnameObserver.assertNoValue()
		usernameObserver.assertNoValue()
		portObserver.assertValue("22")

		vm.quickConnectFocused = true
		vm.onQuickConnectChanged("testuser@the.test.example.com:2100")
		quickConnectObserver.assertValue("testuser@the.test.example.com:2100")
		usernameObserver.assertValue("testuser")
		hostnameObserver.assertValue("the.test.example.com")
		portObserver.assertValue("2100")
	}

	@Test
	fun `switching to telnet protocol changes connection detail visibility`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val expandedObserver = vm.uriListenerExpanded.test()

		expandedObserver.assertValue(false)
		vm.toggleListenerExpanded()
		vm.onProtocolSelected(Telnet.getProtocolName())
		vm.quickConnectVisible.test().assertValue(true)
		vm.expanderVisible.test().assertValue(true)
		vm.protocol.test().assertValue(Telnet.getProtocolName())
		expandedObserver.assertValue(true)
		vm.uriListenerExpanded.test().assertValue(true)
		vm.usernameVisible.test().assertValue(false)
		vm.hostnameVisible.test().assertValue(true)
		vm.portVisible.test().assertValue(true)

		vm.toggleListenerExpanded()
		expandedObserver.assertValue(false)
	}

	@Test
	fun `can toggle connection details expansion`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val expandedObserver = vm.uriListenerExpanded.test()
		expandedObserver.assertValue(false)
		vm.toggleListenerExpanded()
		expandedObserver.assertValue(true)
		vm.toggleListenerExpanded()
		expandedObserver.assertValue(false)
	}

	@Test
	fun `font size has default value`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.fontSize.test().assertValue(Host.DEFAULT_FONT_SIZE)
	}

	@Test
	fun `font size seekbar changed fontsize`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val fontSizeObserver = vm.fontSize.test()
		val originalSize = fontSizeObserver.value()
		vm.onFontSizeSeekChanged(0, true)
		fontSizeObserver.assertValue { it < originalSize }.assertValue { it >= MINIMUM_FONT_SIZE }
		vm.onFontSizeSeekChanged(100, true)
		fontSizeObserver.assertValue { it > originalSize }.assertValue { it <= MAXIMUM_FONT_SIZE }
	}

	@Test
	fun `font size seekbar ignores non-user input`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val fontSizeObserver = vm.fontSize.test()
		val originalSize = fontSizeObserver.value()
		vm.onFontSizeSeekChanged(0, false)
		fontSizeObserver.assertValue(originalSize)
	}

	@Test
	fun `font size clamps to maximum value on manual input`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val fontSizeObserver = vm.fontSize.test()
		val fontSliderObserver = vm.seekBarOffset.test()
		val previousSliderOffset = fontSliderObserver.value()
		vm.onFontSizeChanged("9000")
		fontSizeObserver.assertValue(MAXIMUM_FONT_SIZE)
		fontSliderObserver.assertValue { it > previousSliderOffset }
	}

	@Test
	fun `font size clamps to minimum value on manual input`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val fontSizeObserver = vm.fontSize.test()
		val fontSliderObserver = vm.seekBarOffset.test()
		val previousSliderOffset = fontSliderObserver.value()
		vm.onFontSizeChanged("1")
		fontSizeObserver.assertValue(MINIMUM_FONT_SIZE)
		fontSliderObserver.assertValue { it < previousSliderOffset }
	}

	@Test
	fun `font size stays the same on non-number input`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val fontSizeObserver = vm.fontSize.test()
		val originalFontSize = fontSizeObserver.value()
		vm.onFontSizeChanged("foo")
		fontSizeObserver.assertValue(originalFontSize)
	}

	@Test
	fun `default del key value is delkey`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val resourceProvider = FakeListResourceProvider(
			arrayOf("Da Backa", "La Deleta"),
			arrayOf("backspace", "del")
		)

		val delKeyDescriptionObserver = vm.delKeyDescription.test()
		vm.resourceProvider.value = resourceProvider
		delKeyDescriptionObserver.assertValue("La Deleta")
	}

	@Test
	fun `change del key value to backspace`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val resourceProvider = FakeListResourceProvider(
			arrayOf("Da Dela", "Backa Racka"),
			arrayOf(Host.DelKey.DEL.toString(), Host.DelKey.BACKSPACE.toString())
		)

		val delKeyDescriptionObserver = vm.delKeyDescription.test()
		vm.resourceProvider.value = resourceProvider
		delKeyDescriptionObserver.assertValue("Da Dela")

		vm.onDelKeySelected(Host.DelKey.BACKSPACE)
		delKeyDescriptionObserver.assertValue("Backa Racka")
	}

	@Test
	fun `selecting a pubkey returns its value`() {
		whenever(dataRepository.allPubkeys).thenReturn(MutableLiveData<List<Pubkey>>())

		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val resourceProvider = FakeListResourceProvider(
			arrayOf("Foo", "Bar", "Baz"),
			arrayOf("1", "2", "3")
		)

		val usePubkeyObserver = vm.usePubkey.test()
		val pubkeyDescriptionObserver = vm.pubkeyDescription.test()
		vm.resourceProvider.value = resourceProvider


		vm.onPubkeySelected(2L)
		usePubkeyObserver.assertValue(2L)
		pubkeyDescriptionObserver.assertValue("Bar")
	}

	@Test
	fun `selecting a color changes its description`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val resourceProvider = FakeListResourceProvider(
			arrayOf(
				"Razzle",
				"Dazzle",
				"Root",
				"Beer"
			),
			arrayOf(
				Host.HostColor.RED.toString(),
				Host.HostColor.GREEN.toString(),
				Host.HostColor.BLUE.toString(),
				Host.HostColor.GRAY.toString()
			)
		)

		val colorObserver = vm.color.test()
		vm.resourceProvider.value = resourceProvider
		colorObserver.assertValue(Host.HostColor.GRAY)

		vm.onColorSelected(Host.HostColor.BLUE)
		colorObserver.assertValue(Host.HostColor.BLUE)
	}

	@Test
	fun `selecting an encoding changes the description`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)

		val encodingDescriptionObserver = vm.encodingDescription.test()
		encodingDescriptionObserver.assertValue("UTF-8")

		vm.onEncodingSelected("CP437")
		encodingDescriptionObserver.assertValue("CP437")
	}

	@Test
	fun `existing host is false when none set`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		assertThat(vm.existingHost, equalTo(false))
	}

	@Test
	fun `setting hostId changes values`() {
		val host = Host()
		host.nickname = "Test1"
		host.protocol = "ssh"
		host.username = "testacct"
		host.hostname = "example.com"
		host.port = 2222
		host.color = Host.HostColor.RED
		host.useAuthAgent = Host.UseAuthAgent.AUTHAGENT_CONFIRM
		host.useKeys = true
		host.pubkeyId = 3L
		host.stayConnected = true
		host.compression = true
		host.wantSession = false
		host.encoding = "CP437"
		host.fontSize = 15
		host.delKey = Host.DelKey.BACKSPACE
		host.postLogin = "ls"
		host.lastConnect = 100L

		val hostLiveData = MutableLiveData<Host>()
		hostLiveData.value = host

		whenever(dataRepository.getHost(any())).thenReturn(hostLiveData)

		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val nicknameObserver = vm.nickname.test()
		val protocolObserver = vm.protocol.test()
		val usernameObserver = vm.username.test()
		val hostnameObserver = vm.hostname.test()
		val portObserver = vm.port.test()
		val colorObserver = vm.color.test()
		val useSshAuthObserver = vm.useSshAuth.test()
		val useAuthConfirmationObserver = vm.useAuthConfirmation.test()

		vm.onHostId(1L)
		assertThat(vm.existingHost, equalTo(true))
		nicknameObserver.assertValue("Test1")
		protocolObserver.assertValue("ssh")
		usernameObserver.assertValue("testacct")
		hostnameObserver.assertValue("example.com")
		portObserver.assertValue("2222")
		colorObserver.assertValue(Host.HostColor.RED)
		useSshAuthObserver.assertValue(true)
		useAuthConfirmationObserver.assertValue(true)

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue, equalTo(host))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `use SSH auth value off is saved correctly`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.useSshAuth.value = false
		vm.useAuthConfirmation.value = true

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.useAuthAgent, equalTo(Host.UseAuthAgent.AUTHAGENT_NO))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `use SSH auth value on is saved correctly`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.useSshAuth.value = true
		vm.useAuthConfirmation.value = false

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.useAuthAgent, equalTo(Host.UseAuthAgent.AUTHAGENT_YES))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `use SSH auth value confirmation is saved correctly`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.useSshAuth.value = true
		vm.useAuthConfirmation.value = true

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.useAuthAgent, equalTo(Host.UseAuthAgent.AUTHAGENT_CONFIRM))
		}
		vm.onSaveButtonClicked()
	}


	@Test
	fun `Using PUBKEY_NEVER works and disables SSH keys`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.onPubkeySelected(Pubkey.PUBKEY_NEVER)

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.useKeys, equalTo(false))
			assertThat(argCapture.firstValue.pubkeyId, equalTo(Pubkey.PUBKEY_NEVER))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `Using PUBKEY_ANY works and enables SSH keys`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.onPubkeySelected(Pubkey.PUBKEY_ANY)

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.useKeys, equalTo(true))
			assertThat(argCapture.firstValue.pubkeyId, equalTo(Pubkey.PUBKEY_ANY))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `Using a normal pubkey works and enables SSH keys`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.onPubkeySelected(2L)

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.useKeys, equalTo(true))
			assertThat(argCapture.firstValue.pubkeyId, equalTo(2L))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `existing host with use SSH auth off works`() {
		val host = Host()
		host.useAuthAgent = Host.UseAuthAgent.AUTHAGENT_NO

		val hostLiveData = MutableLiveData<Host>()
		hostLiveData.value = host
		whenever(dataRepository.getHost(any())).thenReturn(hostLiveData)

		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val useAuthObserver = vm.useSshAuth.test()
		val useConfirmObserver = vm.useAuthConfirmation.test()

		vm.onHostId(1L)
		useAuthObserver.assertValue(false)
		useConfirmObserver.assertValue(false)
	}

	@Test
	fun `existing host with use SSH auth on works`() {
		val host = Host()
		host.useAuthAgent = Host.UseAuthAgent.AUTHAGENT_YES

		val hostLiveData = MutableLiveData<Host>()
		hostLiveData.value = host
		whenever(dataRepository.getHost(any())).thenReturn(hostLiveData)

		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val useAuthObserver = vm.useSshAuth.test()
		val useConfirmObserver = vm.useAuthConfirmation.test()

		vm.onHostId(1L)
		useAuthObserver.assertValue(true)
		useConfirmObserver.assertValue(false)
	}

	@Test
	fun `existing host with use SSH auth confirm works`() {
		val host = Host()
		host.useAuthAgent = Host.UseAuthAgent.AUTHAGENT_CONFIRM

		val hostLiveData = MutableLiveData<Host>()
		hostLiveData.value = host
		whenever(dataRepository.getHost(any())).thenReturn(hostLiveData)

		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val useAuthObserver = vm.useSshAuth.test()
		val useConfirmObserver = vm.useAuthConfirmation.test()

		vm.onHostId(1L)
		useAuthObserver.assertValue(true)
		useConfirmObserver.assertValue(true)
	}

	@Test
	fun `existing host sets quick-connect string`() {
		val host = Host()
		host.protocol = "ssh"
		host.username = "test1"
		host.hostname = "acme.example.com"
		host.port = 2222

		val hostLiveData = MutableLiveData<Host>()
		hostLiveData.value = host
		whenever(dataRepository.getHost(any())).thenReturn(hostLiveData)

		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val quickConnectStringObserver = vm.quickConnectString.test()

		vm.onHostId(1L)
		quickConnectStringObserver.assertValue("test1@acme.example.com:2222")
	}

	@Test
	fun `setting nickname works`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val nicknameObserver = vm.nickname.test()

		nicknameObserver.assertNoValue()
		vm.onNicknameUpdated("my old host")
		nicknameObserver.assertNoValue()

		vm.nicknameFocused = true
		vm.onNicknameUpdated("my old host")
		nicknameObserver.assertValue("my old host")

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.nickname, equalTo("my old host"))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `setting hostname works`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val hostnameObserver = vm.hostname.test()

		hostnameObserver.assertNoValue()
		vm.onHostnameChanged("example.com")
		hostnameObserver.assertNoValue()

		vm.hostnameFocused = true
		vm.onHostnameChanged("example.com")
		hostnameObserver.assertValue("example.com")

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.hostname, equalTo("example.com"))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `setting port number works`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val portObserver = vm.port.test()

		portObserver.assertValue("22")
		vm.onPortChanged("2222")
		portObserver.assertValue("22")

		vm.portFocused = true
		vm.onPortChanged("2222")
		portObserver.assertValue("2222")

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.port, equalTo(2222))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `invalid port number ignored`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val portObserver = vm.port.test()
		portObserver.assertValue("22")
		vm.portFocused = true
		vm.onPortChanged("PORT")
		portObserver.assertValue("22")

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.port, equalTo(22))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `setting color works`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val colorObserver = vm.color.test()

		colorObserver.assertValue(Host.HostColor.GRAY)
		vm.onColorSelected(Host.HostColor.RED)
		colorObserver.assertValue(Host.HostColor.RED)

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.color, equalTo(Host.HostColor.RED))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `setting delKey works`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		val delKeyObserver = vm.delKeyDescription.test()

		val resourceProvider = FakeListResourceProvider(
			arrayOf("Da Backa", "La Deleta"),
			arrayOf("backspace", "del")
		)
		vm.resourceProvider.value = resourceProvider

		delKeyObserver.assertValue("La Deleta")
		vm.onDelKeySelected(Host.DelKey.BACKSPACE)
		delKeyObserver.assertValue("Da Backa")

		val argCapture = argumentCaptor<Host>()
		whenever(dataRepository.upsertHost(argCapture.capture(), any())).then {
			assertThat(argCapture.firstValue.delKey, equalTo(Host.DelKey.BACKSPACE))
		}
		vm.onSaveButtonClicked()
	}

	@Test
	fun `on host insertion the fragment exits`() {
		val vm = HostEditorViewModel(InstantAppExecutors(), dataRepository)
		vm.onPubkeySelected(2L)

		val argCapture = argumentCaptor<(Long?) -> Unit>()
		whenever(dataRepository.upsertHost(any(), argCapture.capture())).then {
			argCapture.firstValue(2L)
			vm.fragmentFinished.test().assertValue(true)
		}
		vm.onSaveButtonClicked()
	}
}

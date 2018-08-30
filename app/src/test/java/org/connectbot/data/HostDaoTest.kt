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

package org.connectbot.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jraska.livedata.test
import org.connectbot.db.AppDatabase
import org.connectbot.db.dao.HostDao
import org.connectbot.db.entity.ColorScheme
import org.connectbot.db.entity.Host
import org.connectbot.db.entity.KnownHost
import org.connectbot.util.DatabaseRule
import org.connectbot.util.LiveDataTestUtil
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HostDaoTest {
	@Rule
	@JvmField
	val rule: TestRule = InstantTaskExecutorRule()

	@Rule
	@JvmField
	val db: DatabaseRule<HostDao, AppDatabase> = DatabaseRule(AppDatabase::class.java) { db ->
		db.hostDao()
	}.addCallback(AppDatabase.InitialData())

	@Test
	fun hasDefaultColorScheme() {
		val defaultSchemeObserver = db.dao.getDefaultColorsForScheme(ColorScheme.DEFAULT_COLOR_SCHEME).test()
		defaultSchemeObserver.value().let {
			assertThat(it.fg, equalTo(ColorScheme.DEFAULT_FG_COLOR))
			assertThat(it.bg, equalTo(ColorScheme.DEFAULT_BG_COLOR))
		}
	}

	@Test
	fun testAddAndRetrieveDataByNickname() {
		val preInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())

		val host1 = Host()
		host1.nickname = "AAA"
		host1.protocol = Local.getProtocolName()
		db.dao.insertHost(host1)

		val host2 = Host()
		host2.nickname = "ZZZ"
		host2.protocol = Local.getProtocolName()
		db.dao.insertHost(host2)

		val host3 = Host()
		host3.nickname = "MMM"
		host3.protocol = Local.getProtocolName()
		db.dao.insertHost(host3)

		val postInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())
		val sizeDifference = postInsertHosts!!.size - preInsertHosts!!.size
		Assert.assertEquals(3, sizeDifference)

		Assert.assertEquals("AAA", postInsertHosts[0].nickname)
		Assert.assertEquals("MMM", postInsertHosts[1].nickname)
		Assert.assertEquals("ZZZ", postInsertHosts[2].nickname)
	}

	@Test
	fun insertingSameHostTwiceIsIgnored() {
		val preInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())

		val host1 = Host()
		host1.nickname = "AAA"
		host1.protocol = Local.getProtocolName()
		host1.id = db.dao.insertHost(host1)
		db.dao.insertHost(host1)

		val postInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())
		val sizeDifference = postInsertHosts!!.size - preInsertHosts!!.size
		Assert.assertEquals(1, sizeDifference)

		Assert.assertEquals("AAA", postInsertHosts[0].nickname)
	}

	@Test
	fun updateNonExistentHostDoesNothing() {
		val preInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())

		val host1 = Host()
		host1.nickname = "AAA"
		host1.protocol = Local.getProtocolName()
		host1.id = 1

		db.dao.updateHost(host1)

		val postInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())
		val sizeDifference = postInsertHosts!!.size - preInsertHosts!!.size
		Assert.assertEquals(0, sizeDifference)
	}

	@Test
	fun canDeleteHost() {
		val preInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())

		val host1 = Host()
		host1.nickname = "AAA"
		host1.protocol = Local.getProtocolName()
		host1.id = db.dao.insertHost(host1)

		val postInsertHosts = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())
		val sizeDifference = postInsertHosts!!.size - preInsertHosts!!.size
		Assert.assertEquals(1, sizeDifference)

		Assert.assertEquals("AAA", postInsertHosts[0].nickname)

		db.dao.deleteHost(host1)
		val postDeleteHost = LiveDataTestUtil.getValue(db.dao.allHostsByNickname())
		val totalDifference = postDeleteHost!!.size - preInsertHosts.size
		Assert.assertEquals(0, totalDifference)
	}

	@Test
	fun canRetrieveHostById() {
		val host1 = Host()
		host1.nickname = "AAA"
		host1.protocol = Local.getProtocolName()
		host1.id = db.dao.insertHost(host1)

		val host2 = db.dao.getHostById(host1.id)
		host2.test().assertValue { host -> host.nickname == "AAA" }
	}

	@Test
	fun canRetrieveHostByNickname() {
		val host1 = Host()
		host1.nickname = "AAA"
		host1.protocol = Local.getProtocolName()
		host1.id = db.dao.insertHost(host1)

		val host2 = Host()
		host2.nickname = "AAA"
		host2.protocol = Local.getProtocolName()
		host2.id = db.dao.insertHost(host2)

		val hosts = db.dao.getHostByNickname("AAA")
		Assert.assertEquals(2, hosts.size)
		Assert.assertEquals("AAA", hosts[0].nickname)
		Assert.assertEquals("AAA", hosts[1].nickname)
	}

	@Test
	fun canRetrieveHostForSSH() {
		val host1 = Host()
		host1.nickname = "AAA"
		host1.username = "testuser"
		host1.hostname = "example.com"
		host1.port = 22
		host1.protocol = SSH.getProtocolName()
		host1.id = db.dao.insertHost(host1)

		val host2 = db.dao.getHostForSSH("AAA", "example.com", 22, "testuser")!!
		Assert.assertEquals("AAA", host2.nickname)
	}

	@Test
	fun addKnownHostAndGetById() {
		val host1 = Host()
		host1.nickname = "Test host"
		host1.username = "testuser"
		host1.hostname = "example.com"
		host1.port = 22
		host1.protocol = SSH.getProtocolName()
		host1.id = db.dao.upsertHost(host1)

		val knownHostsBeforeConnect = db.dao.getKnownHostsForHostId(host1.id)
		Assert.assertEquals(0, knownHostsBeforeConnect.size)

		val hostId1 = KnownHost()
		hostId1.hostId = host1.id
		hostId1.hostKey = byteArrayOf(0x01, 0x02)
		hostId1.hostKeyAlgorithm = "ssh-ed25519"

		db.dao.addKnownHost(hostId1)

		val knownHostsAfterConnect = db.dao.getKnownHostsForHostId(host1.id)
		Assert.assertEquals(1, knownHostsAfterConnect.size)
	}

	@Test
	fun getKnownKeyAlgorithmsForHost() {
		val host1 = Host()
		host1.nickname = "Test host"
		host1.username = "testuser"
		host1.hostname = "example.com"
		host1.port = 22
		host1.protocol = SSH.getProtocolName()
		host1.id = db.dao.upsertHost(host1)

		val hostId1 = KnownHost()
		hostId1.hostId = host1.id
		hostId1.hostKey = byteArrayOf(0x01, 0x02)
		hostId1.hostKeyAlgorithm = "ssh-ed25519"

		db.dao.addKnownHost(hostId1)

		val knownHostsAfterConnect = db.dao.getHostKeyAlgorithmsForHost(host1.id)
		Assert.assertEquals(1, knownHostsAfterConnect.size)
		Assert.assertEquals("ssh-ed25519", knownHostsAfterConnect[0])
	}
}

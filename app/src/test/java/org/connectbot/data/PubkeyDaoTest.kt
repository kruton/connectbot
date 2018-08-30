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
import org.connectbot.db.PubkeyDatabase
import org.connectbot.db.dao.PubkeyDao
import org.connectbot.db.entity.Pubkey
import org.connectbot.util.DatabaseRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PubkeyDaoTest {
	@Rule
	@JvmField
	val rule: TestRule = InstantTaskExecutorRule()

	@Rule
	@JvmField
	val db: DatabaseRule<PubkeyDao, PubkeyDatabase> = DatabaseRule(PubkeyDatabase::class.java) { db ->
		db.pubkeyDao()
	}

	@Test
	fun canStorePubkey() {
		val allPubkeys = db.dao.allPubkeys()
		allPubkeys.test().awaitValue().assertValue { it.size == 0 }

		val pubkey = Pubkey()
		pubkey.keyType = Pubkey.KeyType.ED25519
		pubkey.nickname = "test1"
		pubkey.id = db.dao.addPubkey(pubkey)

		allPubkeys.test().awaitValue().assertValue { it.size == 1 }
	}
}

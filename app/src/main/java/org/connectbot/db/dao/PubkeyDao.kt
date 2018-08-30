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

package org.connectbot.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.connectbot.db.entity.Pubkey

@Dao
interface PubkeyDao {
	@Query("SELECT * FROM Pubkey")
	fun allPubkeys(): LiveData<List<Pubkey>>

	@Query("SELECT * FROM Pubkey WHERE id = :id LIMIT 1")
	fun getPubkeyById(id: Long): LiveData<Pubkey>

	@Delete
	fun deletePubkey(pubkey: Pubkey): Int

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun addPubkey(pubkey: Pubkey): Long

	@Update(onConflict = OnConflictStrategy.IGNORE)
	fun updatePubkey(pubkey: Pubkey): Int

	@Transaction
	fun upsertPubkey(pubkey: Pubkey): Long {
		val pubkeyId = addPubkey(pubkey)
		when (pubkeyId == -1L) {
			true -> updatePubkey(pubkey)
			false -> pubkey.id = pubkeyId
		}
		return pubkey.id
	}
}

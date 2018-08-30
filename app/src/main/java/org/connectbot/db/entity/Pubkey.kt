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

package org.connectbot.db.entity

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.connectbot.R
import org.connectbot.util.PubkeyUtils
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException

@Entity
class Pubkey {
	companion object {
		const val PUBKEY_ANY: Long = -1
		const val PUBKEY_NEVER: Long = -2
	}

	@PrimaryKey(autoGenerate = true)
	var id: Long = 0

	@NonNull
	var nickname: String = ""

	var keyType: KeyType = KeyType.RSA

	enum class KeyType(val value: String) {
		DSA("DSA"),
		EC("EC"),
		ED25519("ED25519"),
		IMPORTED("IMPORTED"),
		RSA("RSA");

		override fun toString() = value
	}

	var privateKey: ByteArray? = null

	var publicKey: ByteArray? = null

	var encrypted: Boolean = false

	var onStartup: Boolean = false

	var confirmUse: Boolean = false

	var lifetime: Long? = null

	@Ignore
	var unlocked: Boolean = false

	@Ignore
	var unlockedPrivate: Any? = null

	@Ignore
	var bits: Int? = null

	fun getDescription(context: Context): String {
		if (bits == null) {
			try {
				bits = PubkeyUtils.getBitStrength(publicKey, keyType)
			} catch (ignored: NoSuchAlgorithmException) {
			} catch (ignored: InvalidKeySpecException) {
			}
		}

		val res = context.resources
		val sb = StringBuilder()
		sb.append(when (keyType) {
			KeyType.RSA -> res.getString(R.string.key_type_rsa_bits, bits)
			KeyType.DSA -> res.getString(R.string.key_type_dsa_bits, 1024)
			KeyType.EC -> res.getString(R.string.key_type_ec_bits, bits)
			KeyType.ED25519 -> res.getString(R.string.key_type_ed25519)
			else -> res.getString(R.string.key_type_unknown)
		})

		if (encrypted) {
			sb.append(' ')
			sb.append(res.getString(R.string.key_attribute_encrypted))
		}

		return sb.toString()
	}

	fun changePassword(oldPassword: String? = null, newPassword: String? = null): Boolean {
		val priv: PrivateKey

		try {
			priv = PubkeyUtils.decodePrivate(privateKey, keyType, oldPassword)
		} catch (e: Exception) {
			return false
		}

		privateKey = PubkeyUtils.getEncodedPrivate(priv, newPassword)
		encrypted = newPassword?.isNotEmpty() ?: false

		return true
	}
}

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

import org.connectbot.db.entity.Pubkey
import org.connectbot.util.PubkeyUtils
import java.security.KeyPairGenerator
import java.security.SecureRandom

class KeyGenerationRunnable(
	val keyType: Pubkey.KeyType,
	val numBits: Int,
//	val entropy: ByteArray,
	val listener: OnKeyGeneratedListener,
	val secret: String?
): Runnable {
	override fun run() {
		val random = SecureRandom()

// TODO: Add entropy back in
//		// Work around JVM bug
//		random.nextInt()
//		random.setSeed(entropy)

		try {
			val keyPairGen = KeyPairGenerator.getInstance(keyType.toString())
			keyPairGen.initialize(numBits, random)
			val pair = keyPairGen.generateKeyPair()

			listener.onGenerationSuccess(
				PubkeyUtils.getEncodedPrivate(pair.private, secret),
				pair.public.encoded,
				!secret.isNullOrBlank()
			)
		} catch (e: Exception) {
			listener.onGenerationError(e)
		}
	}

}

interface OnKeyGeneratedListener {
	fun onGenerationError(e: Exception)
	fun onGenerationSuccess(encodedPrivate: ByteArray, encodedPublic: ByteArray, encrypted: Boolean)
}

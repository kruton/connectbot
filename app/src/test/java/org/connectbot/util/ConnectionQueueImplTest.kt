/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2019 Kenny Root, Jeffrey Sharkey
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

package org.connectbot.util

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.connectbot.db.entity.Host
import org.junit.Test

class ConnectionQueueImplTest {
    @Test
    fun `adding to the queue works`() {
		val queue = ConnectionQueueImpl()
		assertThat(queue.addRequest(mock())).isNotNull()
    }

    @Test
    fun `adding to queue allows the same object to be retrieved`() {
		val queue = ConnectionQueueImpl()
		val original = mock<Host>()
		val id = queue.addRequest(original)
		assertThat(queue.getRequest(id)).isEqualTo(original)
	}

	@Test
	fun `retrieving from empty queue gets nothing`() {
		val queue = ConnectionQueueImpl()
		assertThat(queue.getRequest(0)).isNull()
	}

	@Test
	fun `cannot retrieve key value twice`() {
		val queue = ConnectionQueueImpl()
		val original = mock<Host>()
		val id = queue.addRequest(original)
		assertThat(queue.getRequest(id)).isEqualTo(original)
		assertThat(queue.getRequest(id)).isNull()
	}
}

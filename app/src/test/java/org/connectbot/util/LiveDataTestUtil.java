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

package org.connectbot.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class LiveDataTestUtil {
	public static <T> T getValue(LiveData<T> liveData) throws InterruptedException {
		final Object[] data = new Object[1];
		CountDownLatch latch = new CountDownLatch(1);
		Observer<T> observer = new Observer<T>() {
			@Override
			public void onChanged(@Nullable T o) {
				data[0] = o;
				latch.countDown();
				liveData.removeObserver(this);
			}
		};
		liveData.observeForever(observer);
		latch.await(2, TimeUnit.SECONDS);

		return (T) data[0];
	}
}

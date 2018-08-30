/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2015 Kenny Root, Jeffrey Sharkey
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

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Custom ViewPager [ViewPager] which is used to swipe between TerminalViews
 * [org.connectbot.TerminalView]. Also allows temporary disabling of paging
 * functionality to prevent event intercepts.
 *
 * @author rhansby
 */
class TerminalViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
	var pagingEnabled: Boolean = true

	override fun onTouchEvent(event: MotionEvent) = when (pagingEnabled) {
		true -> super.onTouchEvent(event)
		false -> false
	}

	override fun onInterceptTouchEvent(event: MotionEvent) = when (pagingEnabled) {
		true -> super.onInterceptTouchEvent(event)
		false -> false
	}
}

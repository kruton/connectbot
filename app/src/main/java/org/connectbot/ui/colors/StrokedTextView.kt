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

package org.connectbot.ui.colors

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import org.connectbot.R

/**
 * A regular TextView but with a stroke in a different color. Makes the grid of colors
 * legible no matter which color is used as the background.
 */
class StrokedTextView : AppCompatTextView {
	constructor(context: Context, attrs: AttributeSet, flags: Int) : super(context, attrs, flags) {
		val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.StrokedTextView)
		_strokeColor = styledAttrs.getColor(R.styleable.StrokedTextView_textStrokeColor, currentTextColor)
		_strokeWidth = styledAttrs.getDimension(R.styleable.StrokedTextView_textStrokeWidth, DEFAULT_STROKE_WIDTH)
		styledAttrs.recycle()
	}

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

	constructor(context: Context) : super(context) {
		_strokeColor = currentTextColor
		_strokeWidth = DEFAULT_STROKE_WIDTH
	}

	init {

	}
	@ColorInt
	private val _strokeColor: Int

	private val _strokeWidth: Float

	override fun onDraw(canvas: Canvas?) {
		if (_strokeWidth > 0) {
			val savedTextColor = currentTextColor
			val p = paint

			p.style = Paint.Style.STROKE
			p.strokeCap = Paint.Cap.ROUND
			p.strokeJoin = Paint.Join.ROUND
			p.strokeWidth = _strokeWidth
			setTextColor(_strokeColor)
			super.onDraw(canvas)

			setTextColor(savedTextColor)
			p.style = Paint.Style.FILL
			super.onDraw(canvas)
		} else {
			super.onDraw(canvas)
		}
	}

	companion object {
		const val DEFAULT_STROKE_WIDTH: Float = 0f
	}
}

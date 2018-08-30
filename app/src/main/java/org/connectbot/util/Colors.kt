/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
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

import org.connectbot.db.entity.Color

/**
 * @author Kenny Root
 */
object Colors {
	val defaults = intArrayOf(
			0xff000000.toInt(), // black
			0xffcc0000.toInt(), // red
			0xff00cc00.toInt(), // green
			0xffcccc00.toInt(), // brown
			0xff0000cc.toInt(), // blue
			0xffcc00cc.toInt(), // purple
			0xff00cccc.toInt(), // cyan
			0xffcccccc.toInt(), // light grey
			0xff444444.toInt(), // dark grey
			0xffff4444.toInt(), // light red
			0xff44ff44.toInt(), // light green
			0xffffff44.toInt(), // yellow
			0xff4444ff.toInt(), // light blue
			0xffff44ff.toInt(), // light purple
			0xff44ffff.toInt(), // light cyan
			0xffffffff.toInt(), // white
			0xff000000.toInt(), 0xff00005f.toInt(), 0xff000087.toInt(), 0xff0000af.toInt(), 0xff0000d7.toInt(),
			0xff0000ff.toInt(), 0xff005f00.toInt(), 0xff005f5f.toInt(), 0xff005f87.toInt(), 0xff005faf.toInt(),
			0xff005fd7.toInt(), 0xff005fff.toInt(), 0xff008700.toInt(), 0xff00875f.toInt(), 0xff008787.toInt(),
			0xff0087af.toInt(), 0xff0087d7.toInt(), 0xff0087ff.toInt(), 0xff00af00.toInt(), 0xff00af5f.toInt(),
			0xff00af87.toInt(), 0xff00afaf.toInt(), 0xff00afd7.toInt(), 0xff00afff.toInt(), 0xff00d700.toInt(),
			0xff00d75f.toInt(), 0xff00d787.toInt(), 0xff00d7af.toInt(), 0xff00d7d7.toInt(), 0xff00d7ff.toInt(),
			0xff00ff00.toInt(), 0xff00ff5f.toInt(), 0xff00ff87.toInt(), 0xff00ffaf.toInt(), 0xff00ffd7.toInt(),
			0xff00ffff.toInt(), 0xff5f0000.toInt(), 0xff5f005f.toInt(), 0xff5f0087.toInt(), 0xff5f00af.toInt(),
			0xff5f00d7.toInt(), 0xff5f00ff.toInt(), 0xff5f5f00.toInt(), 0xff5f5f5f.toInt(), 0xff5f5f87.toInt(),
			0xff5f5faf.toInt(), 0xff5f5fd7.toInt(), 0xff5f5fff.toInt(), 0xff5f8700.toInt(), 0xff5f875f.toInt(),
			0xff5f8787.toInt(), 0xff5f87af.toInt(), 0xff5f87d7.toInt(), 0xff5f87ff.toInt(), 0xff5faf00.toInt(),
			0xff5faf5f.toInt(), 0xff5faf87.toInt(), 0xff5fafaf.toInt(), 0xff5fafd7.toInt(), 0xff5fafff.toInt(),
			0xff5fd700.toInt(), 0xff5fd75f.toInt(), 0xff5fd787.toInt(), 0xff5fd7af.toInt(), 0xff5fd7d7.toInt(),
			0xff5fd7ff.toInt(), 0xff5fff00.toInt(), 0xff5fff5f.toInt(), 0xff5fff87.toInt(), 0xff5fffaf.toInt(),
			0xff5fffd7.toInt(), 0xff5fffff.toInt(), 0xff870000.toInt(), 0xff87005f.toInt(), 0xff870087.toInt(),
			0xff8700af.toInt(), 0xff8700d7.toInt(), 0xff8700ff.toInt(), 0xff875f00.toInt(), 0xff875f5f.toInt(),
			0xff875f87.toInt(), 0xff875faf.toInt(), 0xff875fd7.toInt(), 0xff875fff.toInt(), 0xff878700.toInt(),
			0xff87875f.toInt(), 0xff878787.toInt(), 0xff8787af.toInt(), 0xff8787d7.toInt(), 0xff8787ff.toInt(),
			0xff87af00.toInt(), 0xff87af5f.toInt(), 0xff87af87.toInt(), 0xff87afaf.toInt(), 0xff87afd7.toInt(),
			0xff87afff.toInt(), 0xff87d700.toInt(), 0xff87d75f.toInt(), 0xff87d787.toInt(), 0xff87d7af.toInt(),
			0xff87d7d7.toInt(), 0xff87d7ff.toInt(), 0xff87ff00.toInt(), 0xff87ff5f.toInt(), 0xff87ff87.toInt(),
			0xff87ffaf.toInt(), 0xff87ffd7.toInt(), 0xff87ffff.toInt(), 0xffaf0000.toInt(), 0xffaf005f.toInt(),
			0xffaf0087.toInt(), 0xffaf00af.toInt(), 0xffaf00d7.toInt(), 0xffaf00ff.toInt(), 0xffaf5f00.toInt(),
			0xffaf5f5f.toInt(), 0xffaf5f87.toInt(), 0xffaf5faf.toInt(), 0xffaf5fd7.toInt(), 0xffaf5fff.toInt(),
			0xffaf8700.toInt(), 0xffaf875f.toInt(), 0xffaf8787.toInt(), 0xffaf87af.toInt(), 0xffaf87d7.toInt(),
			0xffaf87ff.toInt(), 0xffafaf00.toInt(), 0xffafaf5f.toInt(), 0xffafaf87.toInt(), 0xffafafaf.toInt(),
			0xffafafd7.toInt(), 0xffafafff.toInt(), 0xffafd700.toInt(), 0xffafd75f.toInt(), 0xffafd787.toInt(),
			0xffafd7af.toInt(), 0xffafd7d7.toInt(), 0xffafd7ff.toInt(), 0xffafff00.toInt(), 0xffafff5f.toInt(),
			0xffafff87.toInt(), 0xffafffaf.toInt(), 0xffafffd7.toInt(), 0xffafffff.toInt(), 0xffd70000.toInt(),
			0xffd7005f.toInt(), 0xffd70087.toInt(), 0xffd700af.toInt(), 0xffd700d7.toInt(), 0xffd700ff.toInt(),
			0xffd75f00.toInt(), 0xffd75f5f.toInt(), 0xffd75f87.toInt(), 0xffd75faf.toInt(), 0xffd75fd7.toInt(),
			0xffd75fff.toInt(), 0xffd78700.toInt(), 0xffd7875f.toInt(), 0xffd78787.toInt(), 0xffd787af.toInt(),
			0xffd787d7.toInt(), 0xffd787ff.toInt(), 0xffd7af00.toInt(), 0xffd7af5f.toInt(), 0xffd7af87.toInt(),
			0xffd7afaf.toInt(), 0xffd7afd7.toInt(), 0xffd7afff.toInt(), 0xffd7d700.toInt(), 0xffd7d75f.toInt(),
			0xffd7d787.toInt(), 0xffd7d7af.toInt(), 0xffd7d7d7.toInt(), 0xffd7d7ff.toInt(), 0xffd7ff00.toInt(),
			0xffd7ff5f.toInt(), 0xffd7ff87.toInt(), 0xffd7ffaf.toInt(), 0xffd7ffd7.toInt(), 0xffd7ffff.toInt(),
			0xffff0000.toInt(), 0xffff005f.toInt(), 0xffff0087.toInt(), 0xffff00af.toInt(), 0xffff00d7.toInt(),
			0xffff00ff.toInt(), 0xffff5f00.toInt(), 0xffff5f5f.toInt(), 0xffff5f87.toInt(), 0xffff5faf.toInt(),
			0xffff5fd7.toInt(), 0xffff5fff.toInt(), 0xffff8700.toInt(), 0xffff875f.toInt(), 0xffff8787.toInt(),
			0xffff87af.toInt(), 0xffff87d7.toInt(), 0xffff87ff.toInt(), 0xffffaf00.toInt(), 0xffffaf5f.toInt(),
			0xffffaf87.toInt(), 0xffffafaf.toInt(), 0xffffafd7.toInt(), 0xffffafff.toInt(), 0xffffd700.toInt(),
			0xffffd75f.toInt(), 0xffffd787.toInt(), 0xffffd7af.toInt(), 0xffffd7d7.toInt(), 0xffffd7ff.toInt(),
			0xffffff00.toInt(), 0xffffff5f.toInt(), 0xffffff87.toInt(), 0xffffffaf.toInt(), 0xffffffd7.toInt(),
			0xffffffff.toInt(), 0xff080808.toInt(), 0xff121212.toInt(), 0xff1c1c1c.toInt(), 0xff262626.toInt(),
			0xff303030.toInt(), 0xff3a3a3a.toInt(), 0xff444444.toInt(), 0xff4e4e4e.toInt(), 0xff585858.toInt(),
			0xff626262.toInt(), 0xff6c6c6c.toInt(), 0xff767676.toInt(), 0xff808080.toInt(), 0xff8a8a8a.toInt(),
			0xff949494.toInt(), 0xff9e9e9e.toInt(), 0xffa8a8a8.toInt(), 0xffb2b2b2.toInt(), 0xffbcbcbc.toInt(),
			0xffc6c6c6.toInt(), 0xffd0d0d0.toInt(), 0xffdadada.toInt(), 0xffe4e4e4.toInt(), 0xffeeeeee.toInt()
	)

	fun mapToColors(): List<Color> =
		defaults.mapIndexed { index, value ->
			val color = Color()
			color.number = index
			color.value = value
			color
		}
}

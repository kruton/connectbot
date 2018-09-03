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

package org.connectbot;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.PreferenceMatchers.withTitle;
import static org.connectbot.ConnectbotMatchers.withHostNickname;
import static org.hamcrest.CoreMatchers.allOf;

import org.connectbot.TerminalView;
import org.connectbot.util.HostDatabase;
import org.connectbot.HostListActivity;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import com.google.android.mobly.snippet.Snippet;
import com.google.android.mobly.snippet.rpc.Rpc;
import org.junit.Before;
import org.junit.Rule;

public class E2eSnippet implements Snippet {
	@Rule
	public final ActivityTestRule<HostListActivity> mActivityRule = new ActivityTestRule<>(
			HostListActivity.class, false, false);

	@Rpc(description="Resets state and starts HostListActivity")
	public void makeDatabasePristine() {
		Context testContext = InstrumentationRegistry.getTargetContext();
		HostDatabase.resetInMemoryInstance(testContext);
		mActivityRule.launchActivity(new Intent());
	}

	@Rpc(description="Set rotation mode to automatic")
	public void setRotationToAuto() {
		openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());

		Intents.init();
		try {
			onView(withText(R.string.list_menu_settings)).perform(click());
			intended(hasComponent(SettingsActivity.class.getName()));
		} finally {
			Intents.release();
		}

		onView(withId(R.id.list))
				.perform(actionOnItem(
						hasDescendant(withText(R.string.pref_rotation_title)),
						click()));
		onView(withText(R.string.list_rotation_auto)).perform(click());

		onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
	}

	@Rpc(description="Start the normal SSH test")
	public void runSshTest(String host, int port) {
		onView(withId(R.id.add_host_button)).perform(click());
		onView(withId(R.id.protocol_text)).perform(click());
		onView(withText("ssh")).perform(click());
		onView(withId(R.id.expand_collapse_button)).perform(click());
		onView(withId(R.id.username_edit_text)).perform(scrollTo(), typeText("testuser"));
		onView(withId(R.id.hostname_edit_text)).perform(scrollTo(), typeText(host));
		onView(withId(R.id.port_edit_text)).perform(scrollTo(), clearText(), typeText(Integer.toString(port)));
		onView(withId(R.id.save)).perform(click());

		Intents.init();
		try {
			onView(withId(R.id.list)).perform(actionOnHolderItem(
					withHostNickname("testuser@" + host + ":" + Integer.toString(port)), click()));
			intended(hasComponent(ConsoleActivity.class.getName()));
		} finally {
			Intents.release();
		}

		onView(withId(R.id.console_flip)).check(matches(
				hasDescendant(allOf(isDisplayed(), withId(R.id.terminal_view)))));

		onView(withId(R.id.console_prompt_yes)).perform(click());

		// TODO[kroot]: get a IdlingResource in there.
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException ignored) {}

		onView(withId(R.id.console_password)).perform(typeText("testtest123\n"));
		onView(isAssignableFrom(TerminalView.class)).perform(click());
	}

	@Override
	public void shutdown() {
		mActivityRule.getActivity().finish();
	}
}

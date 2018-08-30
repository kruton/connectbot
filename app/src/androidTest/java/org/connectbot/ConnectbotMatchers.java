package org.connectbot;

import org.connectbot.databinding.ItemHostBinding;
import org.connectbot.databinding.ItemPubkeyBinding;
import org.connectbot.db.entity.Host;
import org.connectbot.db.entity.Pubkey;
import org.connectbot.ui.common.DataBoundViewHolder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.BoundedMatcher;

public class ConnectbotMatchers {
	public abstract static class BindingMatcher<T extends ViewDataBinding> extends BoundedMatcher<RecyclerView.ViewHolder, DataBoundViewHolder> {
		private Class<T> expectedBindingClass;

		BindingMatcher(Class<T> expectedBindingClass) {
			super(DataBoundViewHolder.class);
			this.expectedBindingClass = expectedBindingClass;
		}

		@Override
		public boolean matchesSafely(DataBoundViewHolder holder) {
			ViewDataBinding binding = holder.getBinding();
			if (binding.getClass().isAssignableFrom(expectedBindingClass)) {
				return matchBinding((T) binding);
			}
			return false;
		}

		public abstract boolean matchBinding(T binding);
	}

	/**
	 * Matches the nickname of a {@link Host}.
	 */
	@NonNull
	public static Matcher<RecyclerView.ViewHolder> withHostNickname(final String content) {
		return new BindingMatcher<ItemHostBinding>(ItemHostBinding.class) {
			@Override
			public boolean matchBinding(ItemHostBinding binding) {
				return binding.getHost().getNickname().matches(content);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with host nickname '" + content + "'");
			}
		};
	}

	/**
	 * Matches the nickname of a {@link Pubkey}.
	 */
	@NonNull
	public static Matcher<RecyclerView.ViewHolder> withPubkeyNickname(final String content) {
		return new BindingMatcher<ItemPubkeyBinding>(ItemPubkeyBinding.class) {
			@Override
			public boolean matchBinding(ItemPubkeyBinding binding) {
				return binding.getPubkey().getNickname().matches(content);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with host nickname '" + content + "'");
			}
		};
	}

	@NonNull
	public static Matcher<RecyclerView.ViewHolder> withConnectedHost() {
		return new BindingMatcher<ItemHostBinding>(ItemHostBinding.class) {
			@Override
			public boolean matchBinding(ItemHostBinding binding) {
				return false;
				// TODO[kenny]: reimplement this matcher
				// return hasDrawableState(binding.getHost().conn, android.R.attr.state_checked);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is connected status");
			}
		};
	}

	@NonNull
	public static Matcher<RecyclerView.ViewHolder> withDisconnectedHost() {
		return new BindingMatcher<ItemHostBinding>(ItemHostBinding.class) {
			@Override
			public boolean matchBinding(ItemHostBinding binding) {
				return false;
				// TODO[kenny]: reimplement this matcher
				// return hasDrawableState(holder.icon, android.R.attr.state_expanded);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is disconnected status");
			}
		};
	}

	@NonNull
	public static Matcher<RecyclerView.ViewHolder> withColoredText(@ColorInt final int expectedColor) {
		return new BindingMatcher<ItemHostBinding>(ItemHostBinding.class) {
			@Override
			public boolean matchBinding(ItemHostBinding binding) {
				return false;
				// TODO[kenny]: Reimplement this matcher
				// return hasTextColor(holder.nickname, expectedColor);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is text color " + expectedColor);
			}
		};
	}

	private static boolean hasDrawableState(View view, final int expectedState) {
		if (!(view instanceof ImageView)) {
			return false;
		}

		int[] states = view.getDrawableState();
		for (int state : states) {
			if (state == expectedState) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasTextColor(View view, @ColorInt final int expectedColor) {
		if (!(view instanceof TextView)) {
			return false;
		}

		TextView tv = (TextView) view;
		return tv.getCurrentTextColor() == expectedColor;
	}

	@NonNull
	public static ViewAssertion hasHolderItem(final Matcher<RecyclerView.ViewHolder> viewHolderMatcher) {
		return new ViewAssertion() {
			@Override public void check(View view, NoMatchingViewException e) {
				if (!(view instanceof RecyclerView)) {
					throw e;
				}

				boolean hasMatch = false;
				RecyclerView rv = (RecyclerView) view;
				for (int i = 0; i < rv.getChildCount(); i++) {
					RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(i);
					hasMatch |= viewHolderMatcher.matches(vh);
				}
				Assert.assertTrue(hasMatch);
			}
		};
	}
}

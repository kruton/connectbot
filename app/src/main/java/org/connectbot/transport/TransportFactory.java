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

package org.connectbot.transport;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.connectbot.R;
import org.connectbot.db.entity.Host;
import org.connectbot.ui.common.IResourceProvider;
import org.jetbrains.annotations.Contract;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

/**/


/**
 * @author Kenny Root
 */
public class TransportFactory {
	private static final Pattern SSH_HOSTMASK = Pattern.compile(
		"^(.+)@([0-9a-z.-]+|\\[[a-f:0-9]+])(:(\\d+))?$", Pattern.CASE_INSENSITIVE);
	private static final Pattern TELNET_HOSTMASK = Pattern.compile(

		"^([0-9a-z.-]+)(:(\\d+))?$", Pattern.CASE_INSENSITIVE);

	private static String[] transportNames = {
		"ssh",
		"telnet",
		"local",
	};

	public static Host createHost(String scheme, String input) {
		Timber.d("Attempting to discover URI for scheme=%s on input=%s", scheme, input);
		if ("ssh".equals(scheme)) {
			return createSshHost(input);
		} else if ("telnet".equals(scheme))
			return createTelnetHost(input);
		else if ("local".equals(scheme)) {
			return createLocalHost(input);
		} else {
			return null;
		}
	}

	private static Host createLocalHost(String input) {
		Uri uri = getLocalUri(input);

		Host host = new Host();

		host.setProtocol("local");

		String nickname = uri.getFragment();
		if (nickname == null || nickname.length() == 0) {
			host.setNickname("Local");
		} else {
			host.setNickname(uri.getFragment());
		}

		return host;
	}

	private static Uri getTelnetUri(String input) {
		Matcher matcher = TELNET_HOSTMASK.matcher(input);

		if (!matcher.matches())
			return null;

		try {
			return getTelnetUri(matcher.group(1), Integer.valueOf(matcher.group(3)));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Host createTelnetHost(String input) {
		Uri uri = getTelnetUri(input);

		Host host = new Host();

		host.setProtocol("telnet");

		host.setHostname(uri.getHost());

		int port = uri.getPort();
		if (port < 0 || port > 65535)
			port = 23;
		host.setPort(port);

		String nickname = uri.getFragment();
		if (nickname == null || nickname.length() == 0) {
			host.setNickname(getDefaultTelnetNickname(host.getHostname(), port));
		} else {
			host.setNickname(uri.getFragment());
		}

		return host;
	}

	private static String getDefaultTelnetNickname(String hostname, int port) {
		if (port == 23) {
			return String.format(Locale.US, "%s", hostname);
		} else {
			return String.format(Locale.US, "%s:%d", hostname, port);
		}
	}

	private static Host createSshHost(String input) {
		Uri uri = getSshUri(input);

		Host host = new Host();

		host.setProtocol("ssh");

		host.setHostname(uri.getHost());

		int port = uri.getPort();
		if (port < 0 || port > 65535)
			port = 22;
		host.setPort(port);

		host.setUsername(uri.getUserInfo());

		String nickname = uri.getFragment();
		if (nickname == null || nickname.length() == 0) {
			host.setNickname(getDefaultSshNickname(host.getUsername(),
				host.getHostname(), port));
		} else {
			host.setNickname(uri.getFragment());
		}

		return host;
	}

	private static Uri getSshUri(String input) {
		Matcher matcher = SSH_HOSTMASK.matcher(input);
		if (!matcher.matches())
			return null;

		int port;
		try {
			port = Integer.valueOf(matcher.group(4));
		} catch (NumberFormatException e) {
			port = 22;
		}

		return getSshUri(matcher.group(1), matcher.group(2), port);

	}

	private static String getDefaultSshNickname(String username, String hostname, int port) {
		if (port == 22) {
			return String.format(Locale.US, "%s@%s", username, hostname);
		} else {
			return String.format(Locale.US, "%s@%s:%d", username, hostname, port);
		}
	}

	@Contract(pure = true)
	public static String[] getTransportNames() {
		return transportNames;
	}

	public static boolean canForwardPorts(String protocol) {
		// TODO uh, make this have less knowledge about its children
		return "ssh".equals(protocol);
	}

	/**
	 * @param protocol text name of protocol
	 * @param context
	 * @return expanded format hint
	 */
	@NonNull
	public static String getFormatHint(String protocol, IResourceProvider context) {
		if ("ssh".equals(protocol)) {
			return getSshFormatHint(context);
		} else if ("telnet".equals(protocol)) {
			return getTelnetFormatHint(context);
		} else if ("local".equals(protocol)) {
			return getLocalFormatHint(context);
		} else {
			return "";
		}
	}

	private static String getLocalFormatHint(IResourceProvider context) {
		return context.getString(R.string.hostpref_nickname_title);
	}

	private static String getTelnetFormatHint(IResourceProvider context) {
		return String.format("%s:%s",
			context.getString(R.string.format_hostname),
			context.getString(R.string.format_port));
	}

	private static String getSshFormatHint(IResourceProvider context) {
		return String.format("%s@%s:%s",
			context.getString(R.string.format_username),
			context.getString(R.string.format_hostname),
			context.getString(R.string.format_port));
	}

	@Nullable
	public static Uri getUriForHost(Host host) {
		switch (host.getProtocol()) {
		case "ssh":
			return getSshUri(host.getUsername(), host.getHostname(), host.getPort());
		case "telnet":
			return getTelnetUri(host.getHostname(), host.getPort());
		case "local":
			return getLocalUri(host.getNickname());
		default:
			return null;
		}
	}

	private static Uri getLocalUri(String nickname) {
		Uri uri = Uri.parse("local:#Local");

		if (nickname != null && nickname.length() > 0) {
			uri = uri.buildUpon().fragment(nickname).build();
		}

		return uri;
	}

	private static Uri getTelnetUri(String hostname, int port) {
		StringBuilder sb = new StringBuilder();

		sb.append(hostname);

		if (port != 23) {
			sb.append(':');
			sb.append(port);
		}

		String hostPort = sb.toString();

		sb.setLength(0);
		sb.append("telnet")
			.append("://")
			.append(hostPort)
			.append("/#")
			.append(Uri.encode(hostPort));

		return Uri.parse(sb.toString());
	}

	private static Uri getSshUri(String username, String hostname, int port) {
		StringBuilder sb = new StringBuilder();

		sb.append(Uri.encode(username))
			.append('@')
			.append(Uri.encode(hostname));

		if (port != 22) {
			sb.append(':')
				.append(port);
		}

		String userHostPort = sb.toString();

		sb.setLength(0);
		sb.append("ssh")
			.append("://")
			.append(userHostPort)
			.append("/#")
			.append(Uri.encode(userHostPort));

		return Uri.parse(sb.toString());
	}

	@NonNull
	public static String hostToString(Host host) {
		if ("ssh".equals(host.getProtocol())) {
			if (host.getUsername() == null || host.getHostname() == null ||
				host.getUsername().equals("") || host.getHostname().equals(""))
				return "";

			if (host.getPort() == 22)
				return host.getUsername() + "@" + host.getHostname();
			else
				return host.getUsername() + "@" + host.getHostname() + ":" + host.getPort();
		} else if ("telnet".equals(host.getProtocol())) {
			if (host.getHostname() == null || host.getHostname().equals(""))
				return "";
			else if (host.getPort() == 23)
				return host.getHostname();
			else
				return host.getHostname() + ":" + host.getPort();
		} else if ("local".equals(host.getProtocol())) {
			return host.getNickname();
		}

		// Fail gracefully.
		return "";
	}
}

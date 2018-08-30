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

package org.connectbot.transport;

import java.util.List;

import org.connectbot.db.entity.PortForward;

public interface PortForwarding {
	/**
	 * Adds the {@link PortForward} to the list.
	 * @param portForward the port forward bean to add
	 * @return true on successful addition
	 */
	boolean addPortForward(PortForward portForward);

	/**
	 * Enables a port forward member. After calling this method, the port forward should
	 * be operational iff it could be enabled by the transport.
	 * @param portForward member of our current port forwards list to enable
	 * @return true on successful port forward setup
	 */
	boolean enablePortForward(PortForward portForward);

	/**
	 * Disables a port forward member. After calling this method, the port forward should
	 * be non-functioning iff it could be disabled by the transport.
	 * @param portForward member of our current port forwards list to enable
	 * @return true on successful port forward tear-down
	 */
	boolean disablePortForward(PortForward portForward);

	/**
	 * Removes the {@link PortForward} from the available port forwards.
	 * @param portForward the port forward bean to remove
	 * @return true on successful removal
	 */
	boolean removePortForward(PortForward portForward);

	/**
	 * Gets a list of the {@link PortForward} currently used by this transport.
	 * @return the list of port forwards
	 */
	List<PortForward> getPortForwards();
}

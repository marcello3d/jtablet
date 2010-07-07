/*!
 * Copyright (c) 2010 Jason Gerecke (killertofu@gmail.com)
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 * 
 *     2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 * 
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */

package cello.jtablet.impl;

import cello.jtablet.DriverStatus;

/**
 * TabletDrivers are responsible for using a library to obtain
 * access to any tablet devices which may be present on the system.
 * Most drivers will communicate with a native library to obtain tablet
 * events, though there is no strict requirement. For instance, the
 * {@MouseDriver} uses Java's own API to have the user's mouse emulate
 * a limited tablet.
 *
 * All TabletDrivers execute in their own thread, and should be
 * singletons. There should be no need for multiple instances of a
 * single driver running on the same system.
 * 
 * @author Jason Gerecke
 * @since 1.2.5
 */
public interface TabletDriver extends Runnable {
	
	/**
	 * Calling the {@code TabletDriver}'s {@link TabletDriver.load()}
	 * method causes the driver to attempt to load any native code
	 * that it relies on.
	 *
	 * @since 1.2.5
	 */
	public void load();
	
	/**
	 * While no code should care about any particular driver loading
	 * (and indeed, should be written to use the TabletManager!)
	 * it nevertheless can be helpful for debugging to know what is
	 * going on with the driver. This method provides the necessary
	 * insight.
	 *
	 * @return the status of the driver
	 * @since 1.2.5
	 */
	public DriverStatus getStatus();
}

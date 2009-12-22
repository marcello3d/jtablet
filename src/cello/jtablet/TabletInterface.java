/*!
 * Copyright (c) 2009 Marcello Bastéa-Forte (marcello@cellosoft.com)
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

package cello.jtablet;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import cello.jtablet.events.TabletListener;

/**
 * @author marcello
 */
public interface TabletInterface {
	/**
	 * Adds a TabletListener to the entire screen. This works very much like adding a {@link MouseListener} and 
	 * {@link MouseMotionListener} on the component, meaning:
	 * <ul>
	 * 	<li>Events will have coordinates relative to the screen</li>
	 *  <li>Enter and exit events will occur when the tablet stylus enters/exits proximity</li>
	 * </ul>
	 * <p><b>Implementation Note:</b> behavior for this method is undefined when working with mouse input (i.e. no 
	 * native library was loaded or the user is not using the tablet). Please use 
	 * {@link #addTabletListener(Component, TabletListener)} when working with on-screen components.</p>
	 * @see TabletListener
	 * @see #addTabletListener(Component, TabletListener)
	 * @param listener the listener to add
	 */
	public void addScreenTabletListener(TabletListener listener);
	
	/**
	 * Removes a TabletListener previously added with {@link #addScreenTabletListener(TabletListener)}. It is safe to 
	 * call this method if the specified listener has not been added (or already removed).
	 * @param listener the listener to remove
	 */
	public void removeScreenTabletListener(TabletListener listener);
	
	/**
	 * Adds a TabletListener to a specific Component. This works very much like adding a {@link MouseListener} and 
	 * {@link MouseMotionListener} on the component, meaning:
	 * <ul>
	 * 	<li>Events will have coordinates relative to the component</li>
	 *  <li>Events will only be received when: 
	 *   <ul>
	 *     <li>the mouse is over the component's bounds,</li>
	 *     <li>or a drag was initiated on the component</li>
	 *   </ul>
	 *  </li>
	 *  <li>Enter and exit events will occur both:
	 *   <ul>
	 *    <li>when the tablet stylus enters/exits proximity,
	 *    <li>and when the cursor enters/exits the component bounds</li>
	 *   </ul>
	 *  </li>
	 * </ul>
	 * @see TabletListener
	 * @param component the component to listen on
	 * @param listener the listener to send events to
	 */
	public void addTabletListener(Component component, TabletListener listener);
	
	/**
	 * Removes a TabletListener previously added to a specific component with 
	 * {@link #addTabletListener(Component,TabletListener)}. It is safe to call this method if the specified listener 
	 * has not been added to the given component (or already removed).
	 * 
	 * @param component the component to remove 
	 * @param listener the listener to remove
	 */
	public void removeTabletListener(Component component, TabletListener listener);
}

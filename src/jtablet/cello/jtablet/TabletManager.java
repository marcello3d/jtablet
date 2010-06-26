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

import cello.jtablet.event.TabletAdapter;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.TabletManagerImpl;

/**
 * {@code TabletManager} is responsible for detecting interesting
 * events, translating them into {@link cello.jtablet.event.TabletEvent}
 * objects, and finally broadcasting them to all registered
 * {@link cello.jtablet.event.TabletListener}s.
 * 
 * {@code TabletManager} relies on concrete private subclasses to
 * grab events from particular native libraries (e.g. Wacom's WinTab
 * driver, Cocoa's NSEvent system, or X11's XInput). Given the
 * difficulty in determining if a particular native library is
 * supported on the end-user's system, we provide the simple static
 * {@link TabletManager}.{@link TabletManager#getDefaultManager()}
 * method that is guarnteed to return a {@code TabletManager} which
 * is compatible.
 * 
 * <p>Example usage:
 * <pre>
 *  // Get tablet manager
 *  {@link TabletManager} manager = {@link TabletManager}.{@link TabletManager#getDefaultManager()};
 *  
 *  // Add tablet listener to component
 *  manager.{@link TabletManager#addTabletListener(Component, TabletListener) addTabletListener}(component, new {@link TabletAdapter}() {
 *      public void {@link TabletListener#cursorDragged(TabletEvent) cursorDragged}({@link TabletEvent} event) {
 *          // Print out tablet drag events as they occur to system output
 *          System.out.println("dragged " + event);
 *      }
 *  });
 * </pre>
 * 
 * @see TabletListener
 * @author marcello
 */
public abstract class TabletManager {
	
	/**
	 * To obtain a reference to a TabletManager, call the static
	 * {@link TabletManager}.{@link TabletManager#getDefaultManager()} method.
	 */
	protected TabletManager() {
	}
	
	/**
	 * Static singleton TabletManager to be used by the entire system.
	 */
	private static TabletManager tabletManager = new TabletManagerImpl();
	
	/**
	 * Returns a shared tablet manager with the default settings.
	 *
	 * @return the TabletManager for the whole system
	 */
	public static TabletManager getDefaultManager() {
		return tabletManager;
	}
	
	/**
	 * Adds a {@link TabletListener} to the entire screen. This works very much like adding a {@link MouseListener} and 
	 * {@link MouseMotionListener} on the component, meaning:
	 * <ul>
	 * 	<li>Events will have coordinates relative to the screen</li>
	 * 	<li>Enter and exit events will occur when the tablet stylus enters/exits proximity</li>
	 * </ul>
	 * <p><b>Implementation Note:</b> behavior for this method is undefined when working with mouse input (i.e. no 
	 * native library was loaded or the user is not using the tablet). Please use 
	 * {@link #addTabletListener(Component, TabletListener)} when working with on-screen components.</p>
	 *
	 * @see TabletListener
	 * @see #addTabletListener(Component, TabletListener)
	 * @param listener the listener to add
	 */
	public abstract void addScreenTabletListener(TabletListener listener);
	
	/**
	 * Removes a TabletListener previously added with {@link #addScreenTabletListener(TabletListener)}. It is safe to 
	 * call this method if the specified listener has not been added (or already removed).
	 *
	 * @param listener the listener to remove
	 */
	public abstract void removeScreenTabletListener(TabletListener listener);
	
	/**
	 * Adds a TabletListener to a specific Component. This works very much like adding a {@link MouseListener} and 
	 * {@link MouseMotionListener} on the component, meaning:
	 * <ul>
	 * 	<li>Events will have coordinates relative to the component</li>
	 * 	<li>Events will only be received when:
	 *	   <ul>
	 * 		<li>the mouse is over the component's bounds,</li>
	 * 		<li>or a drag was initiated on the component</li>
	 * 	   </ul>
	 * 	</li>
	 * 	<li>Enter and exit events will occur both:
	 * 	   <ul>
	 * 		<li>when the tablet stylus enters/exits proximity,
	 * 		<li>and when the cursor enters/exits the component bounds</li>
	 * 	   </ul>
	 * 	</li>
	 * </ul>
	 *
	 * @see TabletListener
	 * @param component component to add the listener to
	 * @param listener the listener to send events to
	 */
	public abstract void addTabletListener(Component component, TabletListener listener);
	
	/**
	 * Removes a TabletListener previously added to a specific component with 
	 * {@link #addTabletListener(Component,TabletListener)}. It is safe to call this method if the specified listener 
	 * has not been added to the given component (or already removed).
	 * 
	 * @param component component to remove the listener from
	 * @param listener the listener to remove
	 */
	public abstract void removeTabletListener(Component component, TabletListener listener);
	
	/**
	 * Returns the tablet driver status for this tablet manager. This contains exception information if there was a 
	 * problem instantiating the tablet driver and can be used for debugging driver/native issues.
	 *
	 * TODO: Why are we returning null?
	 * 
	 * @return the current driver status
	 */
	public DriverStatus getDriverStatus() {
		return null;
	}
}

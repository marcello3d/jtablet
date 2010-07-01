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
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import cello.jtablet.event.TabletAdapter;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.MouseDriver;


/**
 * <p>{@code TabletManager} is responsible for consuming events from any
 * attached tablet, filtering them as needed, and then forwarding the
 * remaining events to interested {@link cello.jtablet.event.TabletListener}s.</p>
 *
 * <p>{@code TabletManager} relies on {@link cello.jtablet.driver.TabletDriver}s
 * to obtain events from native libraries. Rather than creating instances
 * of the necessary drivers yourself, JTablet handles everything in the background.
 * All that is required to start listening for {@link cello.jtablet.event.TabletEvent}s
 * is to register a {@link java.awt.Component} to receive them.</p>
 * 
 * <p>Example usage:
 * <pre>
 *  // Add tablet listener to component
 *  {@link TabletManager}.{@link TabletManager#addTabletListener(Component, TabletListener) addTabletListener}(component, new {@link TabletAdapter}() {
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
public class TabletManager implements Runnable {
	
	protected static Queue<TabletEvent> events = new ConcurrentLinkedQueue<TabletEvent>();
	protected static Map<TabletListener, Component> listeners = Collections.synchronizedMap(new HashMap<TabletListener, Component>());
	protected static Map<TabletDevice, Set<Component>> targets = new HashMap<TabletDevice, Set<Component>>();
	protected static MouseDriver m = new MouseDriver();
	
	/**
	 * To obtain a reference to a TabletManager, call the static
	 * {@link TabletManager}.{@link TabletManager#getDefaultManager()} method.
	 */
	protected TabletManager() {
		TabletManager.m.load();
		new Thread(TabletManager.m).start();
	}
	
	/**
	 * Static singleton TabletManager to be used by the entire system.
	 */
	private static TabletManager tabletManager = new TabletManager();
	private static Thread managerThread = new Thread(tabletManager);
	
	/**
	 * Returns a shared tablet manager with the default settings.
	 *
	 * @return the TabletManager for the whole system
	 */
	public static TabletManager getDefaultManager() {
		if (managerThread.getState() == Thread.State.NEW)
			managerThread.start();
		
		return tabletManager;
	}
	
	@Deprecated
	public void addScreenTabletListener(TabletListener listener) { System.out.println("HITHERE"); addTabletListener(listener); }
	@Deprecated
	public void removeScreenTabletListener(TabletListener listener) { removeTabletListener(listener); }
	@Deprecated
	public void addTabletListener(Component component, TabletListener listener) { addTabletListener(listener, component); }
	@Deprecated
	public void removeTabletListener(Component component, TabletListener listener) { removeTabletListener(listener); }
	
	/**
	 * Get the status of all drivers.
	 */
	public DriverStatus getDriverStatus() { return TabletManager.m.getStatus(); }
	
	
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
	public static void addTabletListener(TabletListener listener) {
		synchronized (TabletManager.listeners) {
			TabletManager.listeners.put(listener, null);
		}
	}
	
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
	public static void addTabletListener(TabletListener listener, Component component) {
		if (component == null)
			throw new NullPointerException();
		
		synchronized (TabletManager.listeners) {
			TabletManager.listeners.put(listener, component);
		}
	}
	
	/**
	 * Removes a TabletListener previously added with {@link #addTabletListener(TabletListener)} or
	 * {@link #addTabletListener(TabletListener, Component)}. It is safe to  call this method
	 * if the specified listener has not been added (or already removed).
	 *
	 * @param listener the listener to remove
	 */
	public static void removeTabletListener(TabletListener listener) {
		synchronized (TabletManager.listeners) {
			TabletManager.listeners.remove(listener);
		}
	}
	
	/**
	 * Inserts the given event into the event queue. The event will be processed by the
	 * by TabletManager and possibly relayed to listeners.
	 */
	public static void postTabletEvent(TabletEvent event) {
		TabletManager.events.offer(event);
	}
	
	/**
	 * When the TabletManager thread is started, it will begin draining
	 * the event queue and notifying {@TabletListener}s of {@link TabletEvent}s
	 * of interest.
	 */
	public void run() {
		try {
			while (true) {
				drainEventQueue();
				Thread.sleep(10);
			}
		}
		catch (InterruptedException e) {
			System.err.println("TabletManager thread interrupted. Exiting thread.");
		}
	}
		
	/**
	 * Drains events from the event queue, and notifies {@TabletListener}s
	 * of events of interest.
	 */
	protected void drainEventQueue() {
	synchronized (TabletManager.listeners) {
	synchronized (TabletManager.events) {
		Set<Map.Entry<TabletListener,Component>> entries = TabletManager.listeners.entrySet();
		while (TabletManager.events.peek() != null) {
			TabletEvent event = TabletManager.events.remove();
			System.out.print("-");
			for (Map.Entry<TabletListener,Component> entry : entries) {
				
				if (shouldRecieve(event, entry.getKey(), entry.getValue())) {
					TabletEvent translatedEvent = event.translated(0,0); //Make a clone
					
					if (entry.getValue() != null) {
						System.out.print("T");
						Point point = entry.getValue().getLocationOnScreen();
						System.out.print(">   " + translatedEvent.getX() + ", " + translatedEvent.getY() + "\t" + point);
						translatedEvent = translatedEvent.translated(entry.getValue(), -point.x, -point.y);
						System.out.println("\t" + translatedEvent.getPoint());
					}
					System.out.print(":");
					updateTarget(entry.getValue(), translatedEvent);
					translatedEvent.fireEvent(entry.getKey());
				}
			}
		}
	}
	}
	}
	
	/**
	 * This method determines whether or not the given listener
	 * should recieve the given event.
	 */
	protected boolean shouldRecieve(TabletEvent event, TabletListener listener, Component component) {
		//1: Screen listeners recieve *everything*
		if (component == null) {
			System.out.print("s");
			return true;
		}
		
		//2: Is the component targeted by the event's device?
		if (isTargeted(component, event.getDevice())) {
			return true;
		}
		
		//3: Is the component on screen and in the focused window?
		if (!component.isShowing() || SwingUtilities.getWindowAncestor(component) != KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow())
			return false;
		
		//3: Is this a MOVED or PRESSED event over top of a viable component?
		if (event.getType() == TabletEvent.Type.MOVED || event.getType() == TabletEvent.Type.PRESSED) {
			Point point = component.getLocationOnScreen();
			TabletEvent newEvent = event.translated(component, -point.x, -point.y);
			System.out.println("~" + newEvent.getPoint() + "~");
			return component.contains(newEvent.getPoint());
		}
		
		return false;
	}
	
	protected boolean isTargeted(Component component, TabletDevice device) {
		Set<Component> components = targets.get(device);
		return components != null && components.contains(component);
	}
	
	
	
	protected void updateTarget(Component component, TabletEvent event) {
		Set<Component> components = targets.get(event.getDevice());
		if (components == null) {
			components = new HashSet<Component>();
			targets.put(event.getDevice(), components);
		}
		
		switch (event.getType()) {
			case PRESSED:
				components.add(component);
				break;
			case RELEASED:
				components.remove(component);
				break;
		}
	}
}


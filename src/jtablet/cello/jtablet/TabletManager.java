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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import cello.jtablet.event.TabletAdapter;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.TabletDriver;


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
	protected static Map<Component, Set<TabletListener>> listeners = Collections.synchronizedMap(new WeakHashMap<Component, Set<TabletListener>>());
	protected static Map<TabletDevice, Set<Component>> targets = new HashMap<TabletDevice, Set<Component>>();
	protected static Set<TabletDriver> drivers = new HashSet<TabletDriver>();
	
	/**
	 * To obtain a reference to a TabletManager, call the static
	 * {@link TabletManager}.{@link TabletManager#getDefaultManager()} method.
	 */
	protected TabletManager() {
		//Add all known drivers to our set of drivers
		//and have them load themselves up
		drivers.add(new cello.jtablet.impl.MouseDriver());
		for (TabletDriver d : drivers) {
			d.load();
			if (d.getStatus().getState() == DriverStatus.State.LOADED)
			    new Thread(d).start();
		}
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
	public void addScreenTabletListener(TabletListener listener) { addTabletListener(listener); }
	@Deprecated
	public void removeScreenTabletListener(TabletListener listener) { removeTabletListener(listener); }
	@Deprecated
	public void addTabletListener(Component component, TabletListener listener) { addTabletListener(listener, component); }
	@Deprecated
	public void removeTabletListener(Component component, TabletListener listener) { removeTabletListener(listener); }
	
	/**
	 * Get the status of all drivers.
	 */
	@Deprecated
	public DriverStatus getDriverStatus() {
		DriverStatus bestStatus = new DriverStatus(DriverStatus.State.UNSUPPORTED_OS);
		for (TabletDriver d : drivers) {
		    if (d.getStatus().getState().ordinal() < bestStatus.getState().ordinal())
			bestStatus = d.getStatus();
		}

		return bestStatus;
	}

	public Collection<TabletDriver> getDrivers() {
		return drivers;
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
	 * <p><b>Implementation Note:</b> behavior for this method is
	 * undefined when working with mouse input (i.e. no native library
	 * native library was loaded or the user is not using the tablet).
	 * Please use  {@link #addTabletListener(Component, TabletListener)}
	 * when working with on-screen components.</p>
	 *
	 * @see TabletListener
	 * @see #addTabletListener(Component, TabletListener)
	 * @param listener the listener to add
	 */
	public static void addTabletListener(TabletListener listener) {
		addTabletListener(listener, ScreenComponent.INSTANCE);
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
	 * @param component component to add the listener to
	 */
	public static void addTabletListener(TabletListener listener, Component component) {
		if (component == null)
			throw new IllegalArgumentException("Tried addding a TabletListener with 'null' component!");

		synchronized (TabletManager.listeners) {
			Set<TabletListener> l = TabletManager.listeners.get(component);
			if (l == null) {
				l = new HashSet<TabletListener>();
				TabletManager.listeners.put(component, l);
			}
			l.add(listener);
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
			for (Set<TabletListener> set : TabletManager.listeners.values())
				set.remove(listener);
		}
	}
	
	/**
	 * Inserts the given event into the event queue. The event will be processed by the
	 * by TabletManager and possibly relayed to listeners.
	 */
	public static void postTabletEvent(TabletEvent event) {
		if (event.getComponent() != ScreenComponent.INSTANCE)
			throw new IllegalArgumentException("All events posted to TabletManager must be in screen space. Use ScreenComponent.INSTANCE.");

		TabletManager.events.offer(event);
	}
	
	/**
	 * When the TabletManager thread is started, it will begin draining
	 * the event queue and notifying {@TabletListener}s of {@link TabletEvent}s
	 * of interest.
	 * 
	 * <p>Users of the JTablet API should not call this method. The static
	 * {@link TabletManager}.{@link TabletManager#getDefaultManager()}
	 * method will return an already-running TabletManager.</p>
	 *
	 * @since 1.2.5
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
		
		while (TabletManager.events.peek() != null) {

			TabletEvent event = TabletManager.events.remove();
			
			for (Component component : TabletManager.listeners.keySet())
			    if (shouldRecieve(event, component))
				fireEvents(event, component);
		}
	}
	}
	}

	/**
	 * Translates the X,Y coordinates of an event from screen space to
	 * to the target component's space.
	 *
	 * <p>Note that the event's coordinates <b>must</b> be in screen
	 * space for this method to work correctly. We assume that this
	 * won't be a problem since {@link TabletManager#postTabletEvent(cello.jtablet.event.TabletEvent)}
	 * throws an exception if you attempt to add an event to the queue
	 * which isn't relative to {@link ScreenComponent#INSTANCE}.</p>
	 *
	 * <p>This restriction could be relaxed in the future if there is a
	 * good reason, but for now we don't bother.</p>
	 *
	 * @see TabletManager#postTabletEvent(cello.jtablet.event.TabletEvent)
	 * @param event the event whose coordinates should be translated from screen space
	 * @param component the component whose coordinate space the returned event should be in
	 * @return an event translated from screen space to component space
	 * @since 1.2.5
	 */
	protected TabletEvent translate(TabletEvent event, Component component) {
		Point point = component.getLocationOnScreen();
		return event.translated(component, -point.x, -point.y);
	}
	
	/**
	 * This method determines whether or not the given listener
	 * should recieve the given event.
	 */
	protected boolean shouldRecieve(TabletEvent event, Component component) {
		//Screen listeners recieve *everything*
		if (component == ScreenComponent.INSTANCE) {
			return true;
		}
		
		//Is the component targeted by the event's device?
		Set<Component> components = targets.get(event.getDevice());
		if (components != null && components.contains(component)) {
			return true;
		}
		
		//Is the component off screen or not in the focused window?
		if (!component.isShowing() || SwingUtilities.getWindowAncestor(component) != KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow())
			return false;
		
		//Is this a MOVED or PRESSED event over top of a viable component?
		if (event.getType() == TabletEvent.Type.MOVED || event.getType() == TabletEvent.Type.PRESSED) {
			return component.contains(translate(event, component).getPoint());
		}
		
		return false;
	}

	/**
	 * Fires the given event -- as well as any needed supplementary
	 * events -- to listeners registered with the event's component.
	 *
	 * <p>Because {@link TabletDriver}s are decoupled from the location
	 * of components on screen, there is no way for them to create
	 * the necessary {@link TabletDevice.Type#ENTERED} and
	 * {@link TabletDevice.Type#EXITED} events. This method keeps
	 * track of "targeted" components which should receive events,
	 * and creates the necessary additional events as components
	 * are (de)targeted.</p>
	 *
	 * @param event the event to fire (and base any additional needed events on)
	 * @param component the component a listener must be registered with to receive these events
	 * @since 1.2.5
	 */
	protected void fireEvents(TabletEvent event, Component component) {
		if (component == ScreenComponent.INSTANCE) {
			fire(event);
			return;
		}

		Set<Component> components = targets.get(event.getDevice());
		if (components == null) {
			components = new HashSet<Component>();
			targets.put(event.getDevice(), components);
		}

		TabletEvent translated = translate(event, component);
		switch (event.getType()) {
			/**
			 * If the cursor has moved or been pressed, we
			 * need to check to see if the component is
			 * already targeted, and possibly fire an ENTER
			 * or EXIT event as necessary.
			 */
			case MOVED:
			case PRESSED:
				if (component.contains(translated.getPoint())) {
					if (!components.contains(component)) {
						components.add(component);
						fire(translated.withType(TabletEvent.Type.ENTERED));
					}

					fire(translated);
				}
				else if (components.contains(component)) {
					components.remove(component);
					fire(translated);
					fire(translated.withType(TabletEvent.Type.EXITED));
				}
				break;

			/**
			 * If the cursor has been released, we need to
			 * remove all targets that are no longer under
			 * the pointer and notify them of the exit.
			 */
			case RELEASED:
				if (component.contains(translated.getPoint())) {
					fire(translated);
				}
				else if (components.contains(component)) {
					components.remove(component);
					fire(translated);
					fire(translated.withType(TabletEvent.Type.EXITED));
				}
				break;

			/**
			 * All other cases (DRAG, LEVEL_CHANGE, SCROLL, etc)
			 * we assume do not change the components which are
			 * already listening, and should only be directed to
			 * them.
			 */
			default:
				if (components.contains(component))
					fire(translated);
				break;
		}
	}

	/**
	 * Fires an event to all listeners of the event's component.
	 * No checks of any kind are done by this method -- this is
	 * the last method used by TabletManager on each event in the
	 * queue.
	 *
	 * @param event the event to deliver to all listeners
	 *        which are registered to the event's component
	 * @since 1.2.5
	 */
	protected void fire(TabletEvent event) {
		for (TabletListener l : TabletManager.listeners.get(event.getComponent()))
			event.fireEvent(l);
	}
}


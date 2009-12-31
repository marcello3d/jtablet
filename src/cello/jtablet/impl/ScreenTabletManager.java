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

package cello.jtablet.impl;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.jpen.platform.NativeCocoaInterface;

/**
 * @author marcello
 *
 */
public abstract class ScreenTabletManager implements TabletManager {	
	
	private boolean enableEnterExitEventsOnDrag = true;
	private boolean sendNewDeviceEventOnEnter = true;
	
	private final List<TabletListener> screenListeners = new ArrayList<TabletListener>();
	
	// In order to avoid holding onto Component pointers, we only use weak references to the Component 
	private final Map<Component,ComponentManager> componentManagers 
			= Collections.synchronizedMap(new WeakHashMap<Component,ComponentManager>());	

    private final ReferenceQueue<Component> queue = new ReferenceQueue<Component>();
	
	private final List<ComponentManager> showingComponents = new CopyOnWriteArrayList<ComponentManager>();
	
//	public void setHints(TabletManagerFactory.Hints hints) {
//		if (hints.containsKey(HINT_ENABLE_ENTER_EXIT_EVENTS_ON_DRAG)) {
//			enableEnterExitEventsOnDrag = (Boolean)hints.get(HINT_ENABLE_ENTER_EXIT_EVENTS_ON_DRAG);
//		}
//		if (hints.containsKey(HINT_SEND_NEW_DEVICE_EVENT_ON_ENTER)) {
//			sendNewDeviceEventOnEnter = (Boolean)hints.get(HINT_SEND_NEW_DEVICE_EVENT_ON_ENTER);
//		}
//	}
	
	/**
	 * A fake component used because MouseEvent requires a real component.
	 */
	private static class ScreenComponent extends Component {
		
		public GraphicsConfiguration getMainScreen() {
			GraphicsDevice[] gs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			if (gs.length > 0) {
				return gs[0].getDefaultConfiguration();
			}
			return null;
		}
		@Override
		public Point getLocationOnScreen() {
			return new Point(0,0);
		}
		@Override
		public Rectangle bounds() {
			return getMainScreen().getBounds();
		}
		@Override
		public Rectangle getBounds(Rectangle rv) {
			if (rv == null) {
				return bounds();
			}
			rv.setBounds(bounds());
			return rv;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}
	protected final static ScreenComponent SCREEN_COMPONENT = new ScreenComponent();
	protected static final TabletDevice SYSTEM_MOUSE = TabletDevice.SYSTEM_MOUSE;
	
	private boolean started = false;
	
	protected abstract void start();
	protected abstract void stop();
	
	private void startIfNeeded() {
		if (!started) {
			started = true;
			start();
		}
	}
	private void stopIfNeeded() {
		if (started && screenListeners.isEmpty() && componentManagers.isEmpty()) {
			stop();
			started = false;
		}
	}

	
	protected void invokeOnEventThread(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			SwingUtilities.invokeLater(r);
		}
	}
	
	private boolean pressed = false;
	private final float PRESSED_THRESHOLD = 0;
	protected void fireScreenTabletEvent(final TabletEvent ev) {
		invokeOnEventThread(new Runnable() {
			public void run() {
				switch (ev.getType()) {
					case PRESSED:
						pressed = true;
						break;
					case RELEASED:
						pressed = false;
						break;
				}
				for (TabletListener l : screenListeners) {
					ev.fireEvent(l);
				}
				expungeFreedComponents();
				for (ComponentManager cm : showingComponents) {
					cm.fireScreenTabletEvent(ev);
				}
			}
		});
	}

	private void expungeFreedComponents() {
		Reference<? extends Component> c;
		while ((c = queue.poll()) != null) {
			System.out.println("expunging ... "+c);
		}
	}

	public void addScreenTabletListener(TabletListener l) {
		screenListeners.add(l);
		startIfNeeded();
	}
	public void removeScreenTabletListener(TabletListener l) {
		screenListeners.remove(l);
		stopIfNeeded();
	}

	public void addTabletListener(Component c, TabletListener l) {
		synchronized (c) {
			ComponentManager manager = componentManagers.get(c);
			if (manager == null) {
				manager = new ComponentManager(c);
				componentManagers.put(c, manager);
				showingComponents.add(manager);
			}
			manager.add(l);
			startIfNeeded();
		}
	}
	
	public void removeTabletListener(Component c, TabletListener l) {
		ComponentManager manager = componentManagers.get(c);
		if (manager == null) {
			return;
		}
		manager.remove(l);
	}


	private class ComponentManager {
		private boolean cursorOver = false;
		private boolean dragging = false;
		private List<TabletListener> listeners = new ArrayList<TabletListener>();
		private final WeakReference<Component> c;
		private TabletDevice lastDevice = null;
		
		public ComponentManager(Component c) {
			this.c = new WeakReference<Component>(c, queue);
		}
		
		private boolean isWindowFocused(Component c) {
			return SwingUtilities.getWindowAncestor(c) == 
					KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		}
		
		public void fireScreenTabletEvent(TabletEvent ev) {
			Component c = this.c.get();
			// Has this component been garbage collected?
			if (c == null) {
				removeSelf();
				return;
			}
			if (!isActive(c)) {
				return;
			}
			
			// Translate the event into the coordinate space of the component
			Point point = c.getLocationOnScreen();
			TabletEvent newEv = ev.translated(c, -point.x, -point.y);

			if (ev.getType() == TabletEvent.Type.PRESSED) {
				dragging = cursorOver;
			}
			// is this an enter/exit event?
			// 		getMousePosition() returns null if the mouse cursor is not directly over the component 
			boolean nowCursorOver = c.contains(newEv.getPoint()) && (isWindowFocused(c) || 
																		c.getMousePosition() != null);
			boolean activeComponent = (cursorOver && !pressed) || dragging;
			if (cursorOver != nowCursorOver) {
				TabletEvent enterExitEvent = newEv.withType(nowCursorOver ? 
																TabletEvent.Type.ENTERED : 
																TabletEvent.Type.EXITED);
				if (nowCursorOver) {
					// entering proximity with new device?
					if (sendNewDeviceEventOnEnter && (lastDevice == null || !lastDevice.equals(ev.getDevice()))) {
						fireEvent(newEv.withType(TabletEvent.Type.NEW_DEVICE));
						lastDevice = ev.getDevice();
					}
					// Send two events, one with the enter event, the second with the mouse move
					if (enableEnterExitEventsOnDrag || !pressed) {
						fireEvent(enterExitEvent);
					}

					if (activeComponent) {
						fireEvent(newEv);
					}
				} else {
					// Java only sends an exit event, no mouse motion event, in this case
					// My hypothesis is that move events only occur within the bounds of the component
					fireEvent(enterExitEvent);
				}

				cursorOver = nowCursorOver;
			} else if (activeComponent) {
				if (sendNewDeviceEventOnEnter && ev.getType() == TabletEvent.Type.NEW_DEVICE) {
					lastDevice = ev.getDevice();
				}
				fireEvent(newEv);
			}
			if (ev.getType() == TabletEvent.Type.RELEASED) {
				dragging = false;
			}
		}


		private boolean isActive(Component c) {
			return c.isShowing() && c.isDisplayable();
		}

		private void fireEvent(TabletEvent event) {
			for (TabletListener l : listeners) {
				event.fireEvent(l);
				if (event.isConsumed()) {
					break;
				}
			}
		}

		/**
		 * @param l listener to remove.
		 * @return false if the this is the last TabletListener to be removed
		 */
		public synchronized boolean remove(TabletListener l) {
			listeners.remove(l);
			
			if (listeners.isEmpty()) {
				componentManagers.remove(c.get());
				afterRemoved();
			}
			return true;
		}

		private void removeSelf() {
			listeners.clear();
			afterRemoved();
		}
		private void afterRemoved() {
			showingComponents.remove(this);
			stopIfNeeded();
			System.out.println("Removed ComponentManager...");
		}

		public synchronized void add(TabletListener l) {
			listeners.add(l);
		}

	}


	private TabletDevice lastDevice = SYSTEM_MOUSE;
	private boolean lastProximity = false;
	private boolean lastPressed = false;
	private float lastX = 0;
	private float lastY = 0;
	private float lastPressure = 0;
	private float lastTiltX = 0;
	private float lastTiltY = 0;
	private float lastTangentialPressure = 0;
	private float lastRotation = 0;
	private int lastButtonMask = 0;
	

	protected void generateDeviceEvents(TabletDevice device, long when, int keyModifiers, boolean enteringProximity) {
		generateDeviceEvents(device, when, keyModifiers, enteringProximity, lastX, lastY);
		
	}
	protected void generateDeviceEvents(TabletDevice device, long when, int keyModifiers, boolean enteringProximity, float x, float y) {
				
		int modifiers = lastButtonMask | keyModifiers; 
	
		if (enteringProximity && !lastDevice.equals(device)) {
			fireScreenTabletEvent(new TabletEvent(
				SCREEN_COMPONENT,
				TabletEvent.Type.NEW_DEVICE,
				when,
				modifiers,
				0,
				device, 
				x,y
			));
			lastDevice = device;
		}
		
		if (lastProximity != enteringProximity) {
			fireScreenTabletEvent(new TabletEvent(
				SCREEN_COMPONENT,
				enteringProximity ? TabletEvent.Type.ENTERED : TabletEvent.Type.EXITED,
				when,
				modifiers,
				0,
				device,
				x,y
			));
			lastProximity = enteringProximity;
		}
		if (!enteringProximity) {
			lastPressure = 0;
			lastTiltX = 0;
			lastTiltY = 0;
			lastTangentialPressure = 0;
			lastRotation = 0;
		}
		
		lastX = x;
		lastY = y;
	}
	protected void generatePointEvents(long when, int keyModifiers,
			float x, float y, float pressure, float tiltX, float tiltY,
			float sidePressure, float rotation, 
			int rawTabletButtonMask,
			int button, boolean buttonJustPressed, boolean buttonJustReleased) {
				
		int buttonMask = lastButtonMask;
		if (buttonJustPressed || buttonJustReleased) {
			int mask = 0;
			switch (button) {
				case MouseEvent.BUTTON1:
			    	mask = MouseEvent.BUTTON1_DOWN_MASK;
			    	break;
				case MouseEvent.BUTTON2:
			    	mask = MouseEvent.BUTTON2_DOWN_MASK;
			    	break;
				case MouseEvent.BUTTON3:
			    	mask = MouseEvent.BUTTON3_DOWN_MASK;
			    	break;
			}
			if (buttonJustPressed) {
				buttonMask |= mask;
			} else {
				buttonMask &= ~mask;
			}
		}
		
		int modifiers = buttonMask | keyModifiers; 
		
		if (!lastProximity && !lastDevice.equals(SYSTEM_MOUSE) && (x!=lastX || y!=lastY)) {			
			lastDevice = SYSTEM_MOUSE;
			fireScreenTabletEvent(new TabletEvent(
					SCREEN_COMPONENT,
					TabletEvent.Type.NEW_DEVICE,
					when,
					modifiers,
					rawTabletButtonMask,
					lastDevice, 
					x,y
				));
		}
		
		if (lastDevice.getType()==Type.MOUSE && (buttonMask & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
			pressure = 1;
		}
	
		boolean pressed = pressure > PRESSED_THRESHOLD;
		
		
		if (buttonJustReleased || buttonJustPressed) {
			fireScreenTabletEvent(new TabletEvent(
				SCREEN_COMPONENT,
				buttonJustPressed ? TabletEvent.Type.PRESSED : TabletEvent.Type.RELEASED,
				when,
				modifiers,
				rawTabletButtonMask,
				lastDevice,
				x,y,
				pressure,
				tiltX,tiltY,
				sidePressure,
				rotation,
				button
			));
		}
		if (lastX != x || lastY != y) {
			fireScreenTabletEvent(new TabletEvent(
				SCREEN_COMPONENT,
				buttonMask != 0 ? TabletEvent.Type.DRAGGED : TabletEvent.Type.MOVED,
				when,
				modifiers,
				rawTabletButtonMask,
				lastDevice,
				x,y,
				pressure,
				tiltX,tiltY,
				sidePressure,
				rotation,
				button
			));
	
		} else if (pressed == lastPressed && !buttonJustReleased && !buttonJustPressed && (
				pressure != lastPressure ||
				tiltX != lastTiltX ||
				tiltY != lastTiltY ||
				sidePressure!= lastTangentialPressure ||
				rotation != lastRotation
		)) {
			fireScreenTabletEvent(new TabletEvent(
				SCREEN_COMPONENT,
				TabletEvent.Type.LEVEL_CHANGED,
				when,
				modifiers,
				rawTabletButtonMask,
				lastDevice,
				x,y,
				pressure,
				tiltX,tiltY,
				sidePressure,
				rotation,
				button
			));
		}
		lastButtonMask = buttonMask;
		lastPressed = pressed;
		lastX = x;
		lastY = y;
		lastPressure = pressure;
		lastTiltX = tiltX;
		lastTiltY = tiltY;
		lastTangentialPressure = sidePressure;
		lastRotation = rotation;
	}

	protected void generateScrollEvent(long when, int keyModifiers,
			float screenX, float screenY, float deltaX, float deltaY) {
		int modifiers = lastButtonMask | keyModifiers;
		fireScreenTabletEvent(new TabletEvent(
			NativeCocoaInterface.SCREEN_COMPONENT,
			TabletEvent.Type.SCROLLED,
			when,
			modifiers,
			lastDevice,
			screenX,screenY,
			0,
			deltaX,deltaY,
			0
		));
	}

	protected void generateZoomGestureEvent(long when, int keyModifiers,
			float screenX, float screenY, float magnificationFactor) {
		int modifiers = lastButtonMask | keyModifiers;
		fireScreenTabletEvent(new TabletEvent(
			NativeCocoaInterface.SCREEN_COMPONENT,
			TabletEvent.Type.ZOOMED,
			when,
			modifiers,
			lastDevice,
			screenX,screenY,
			0,
			0,0,
			magnificationFactor
		));
	}

	protected void generateRotationGestureEvent(float screenX, float screenY,
			float rotationRadians, long when, int keyModifiers) {
		int modifiers = lastButtonMask | keyModifiers;
		fireScreenTabletEvent(new TabletEvent(
			NativeCocoaInterface.SCREEN_COMPONENT,
			TabletEvent.Type.ROTATED,
			when,
			modifiers,
			lastDevice,
			screenX,screenY,
			rotationRadians,
			0,0,
			0
		));
	}

	protected void generateSwipeGestureEvent(long when, int keyModifiers,
			float screenX, float screenY, float deltaX, float deltaY) {
		int modifiers = lastButtonMask | keyModifiers;
		fireScreenTabletEvent(new TabletEvent(
			NativeCocoaInterface.SCREEN_COMPONENT,
			TabletEvent.Type.SWIPED,
			when,
			modifiers,
			lastDevice,
			screenX,screenY,
			0,
			deltaX,deltaY,
			0
		));
	}
}

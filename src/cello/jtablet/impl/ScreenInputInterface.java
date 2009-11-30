package cello.jtablet.impl;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletListener;

/**
 * @author marcello
 *
 */
public abstract class ScreenInputInterface implements CursorDevice {	
	private final List<TabletListener> screenListeners = new ArrayList<TabletListener>();
	private final Map<Component,ComponentManager> componentManagers = new ConcurrentHashMap<Component,ComponentManager>();	
	private final List<ComponentManager> showingComponents = new CopyOnWriteArrayList<ComponentManager>();
	
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
			rv.setBounds(bounds());
			return rv;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
		
	}
	protected final static ScreenComponent SCREEN_COMPONENT = new ScreenComponent();
	private boolean started = false;
	
	protected abstract void start();
	protected abstract void stop();

	public boolean isDeviceAvailable() {
		return false;
	}
	
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
					if (ev.isConsumed()) {
						return;
					}
				}
				for (ComponentManager cm : showingComponents) {
					cm.fireScreenTabletEvent(ev);
					if (ev.isConsumed()) {
						break;
					}
				}
			}
		});
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
		if (!manager.remove(l)) {
			componentManagers.remove(c);
			showingComponents.remove(manager);
			stopIfNeeded();
		}
	}

	private class ComponentManager {
		private boolean cursorOver = false;
		private boolean dragging = false;
		private List<TabletListener> listeners = new ArrayList<TabletListener>();
		private final Component c;
		
		public ComponentManager(Component c) {
			this.c = c;
		}
		
		
		public void fireScreenTabletEvent(TabletEvent ev) {
			if (!c.isShowing()) {
				return;
			}
			
			// Translate the event into the coordinate space of the component
			Point point = c.getLocationOnScreen();
			TabletEvent newEv = ev.translated(c, -point.x, -point.y);


			switch (ev.getType()) {
				case PRESSED:
					dragging = cursorOver;
					break;
				case RELEASED:
					dragging = false;
					break;
			}
			// is this an enter/exit event?
			boolean nowCursorOver = c.contains(newEv.getPoint());
			if (cursorOver != nowCursorOver) {
				TabletEvent enterExitEvent = newEv.withType(nowCursorOver ? 
																TabletEvent.Type.ENTERED : 
																TabletEvent.Type.EXITED);
				if (nowCursorOver) {
					// Send two events, one with the enter event, the second with the mouse move
					fireEvent(enterExitEvent);
					// Only send event if the component is active
					if ((cursorOver && !pressed) || dragging) {
						fireEvent(newEv);
					}
				} else {
					// Java only sends an exit event, no mouse motion event, in this case
					// My hypothesis is that move events only occur within the bounds of the component
					fireEvent(enterExitEvent);
				}

				cursorOver = nowCursorOver;
			} else if ((!pressed && cursorOver) || dragging) {
				
				fireEvent(newEv);
			}
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
				return false;
			}
			return true;
		}

		public synchronized void add(TabletListener l) {
			listeners.add(l);
		}

	}

}

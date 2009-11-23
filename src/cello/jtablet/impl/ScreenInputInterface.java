package cello.jtablet.impl;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletListener;

/**
 * @author marcello
 *
 */
public abstract class ScreenInputInterface implements CursorDevice {	
	private final List<TabletListener> screenListeners = new ArrayList<TabletListener>();
	private final Map<Component,ComponentManager> componentListeners = new ConcurrentHashMap<Component,ComponentManager>();
	
	private final List<ComponentManager> activeComponents = Collections.synchronizedList(new ArrayList<ComponentManager>());
	
	private static class ScreenComponent extends Component {
		
		public GraphicsConfiguration getMainScreen() {
			GraphicsDevice[] gs = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			if (gs.length > 0) {
				
				return gs[0].getDefaultConfiguration();
			}
			return null;
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
		if (started && screenListeners.isEmpty() && componentListeners.isEmpty()) {
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
	protected void fireScreenTabletEvent(final TabletEvent ev) {
		invokeOnEventThread(new Runnable() {
			public void run() {
				for (TabletListener l : screenListeners) {
					ev.fireEvent(l);
					if (ev.isConsumed()) {
						return;
					}
				}
				for (ComponentManager cm : activeComponents) {
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
			ComponentManager list = componentListeners.get(c);
			if (list == null) {
				list = new ComponentManager(c);
				componentListeners.put(c, list);
			}
			list.add(l);
			startIfNeeded();
		}
	}
	
	public void removeTabletListener(Component c, TabletListener l) {
		ComponentManager list = componentListeners.get(c);
		if (list == null) {
			return;
		}
		if (!list.remove(l)) {
			componentListeners.remove(c);
			stopIfNeeded();
		}
	}

	private class ComponentManager implements MouseListener {
		private boolean mouseOver = false;
		private boolean cursorOver = false;
		private boolean active = false;
		private boolean dragging = false;
		private List<TabletListener> listeners = new ArrayList<TabletListener>();
		private final Component c;
		
		public ComponentManager(Component c) {
			this.c = c;
			c.addMouseListener(this);
		}
		
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {
			mouseOver = true;
			activate();
		}
		public void mouseExited(MouseEvent e) {
			mouseOver = false;
			deactivate();
		}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
		
		protected void activate() {
			if (!active) {
				activeComponents.add(ComponentManager.this);
				active = true;
			}
		}
		protected void deactivate() {
			// Only deactivate if we're not dragging
			if (active && !dragging && !mouseOver) {
				activeComponents.remove(ComponentManager.this);
				active = false;
			}
		}

		public void fireScreenTabletEvent(TabletEvent ev) {
			switch (ev.getType()) {
				case PRESSED:
				case DRAGGED:
					dragging = true;
					break;
				case RELEASED:
				case MOVED:
					dragging = false;
					break;
			}
			Point point = c.getLocationOnScreen();
			TabletEvent newEv = ev.translated(c, -point.x, -point.y);

			boolean nowCursorOver = c.contains(newEv.getPoint());
			if (cursorOver != nowCursorOver) {
				TabletEvent enterExitEvent = newEv.withType(nowCursorOver ? 
																TabletEvent.Type.ENTERED : 
																TabletEvent.Type.EXITED);
				if (nowCursorOver) {
					fireEvent(enterExitEvent);
					fireEvent(newEv);
				} else {
					fireEvent(newEv);
					fireEvent(enterExitEvent);
				}

				cursorOver = nowCursorOver;
			} else {
				fireEvent(newEv);
			}
		}

		private void fireEvent(TabletEvent event) {
			System.out.println("ev="+event);
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
				c.removeMouseListener(this);
				return false;
			}
			return true;
		}

		public synchronized void add(TabletListener l) {
			listeners.add(l);
		}
	}

}

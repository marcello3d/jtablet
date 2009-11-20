package cello.jtablet.impl;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.ConcurrentHashMap;

import cello.jtablet.TabletDevice;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletListener;
import cello.jtablet.events.TabletEvent.Type;

/**
 * @author marcello
 */
public class MouseListenerInterace implements CursorDevice {

	public boolean isDeviceAvailable() {
		return true;
	}

	public void addScreenTabletListener(TabletListener l) {
		throw new UnsupportedOperationException(getClass()+" does not support screen listeners");
	}
	public void removeScreenTabletListener(TabletListener l) {
		throw new UnsupportedOperationException(getClass()+" does not support screen listeners");
	}

	private final ConcurrentHashMap<TabletListener,MagicListener> listenerMap = new ConcurrentHashMap<TabletListener, MagicListener>();
	private final TabletDevice mousedevice = new TabletDevice.MouseDevice();
	
	public void addTabletListener(Component c, TabletListener l) {
		synchronized (l) {
			MagicListener magicListener = listenerMap.get(l);
			if (magicListener == null) {
				magicListener = new MagicListener(l);
				listenerMap.put(l,magicListener);
			}
			magicListener.increment();
			c.addMouseListener(magicListener);
			c.addMouseMotionListener(magicListener);
			c.addMouseWheelListener(magicListener);
		}
	}
	public void removeTabletListener(Component c, TabletListener listener) {
		synchronized (listener) {
			MagicListener magicListener = listenerMap.get(listener);
			if (magicListener != null) {
				c.removeMouseListener(magicListener);
				c.removeMouseMotionListener(magicListener);
				c.removeMouseWheelListener(magicListener);
				if (magicListener.decrement()) {
					listenerMap.remove(listener);
				}
			}
		}
		
	}

	private class MagicListener implements MouseListener, MouseMotionListener, MouseWheelListener {

		private final TabletListener listener;
		private int count = 0;

		public MagicListener(TabletListener listener) {
			this.listener = listener;
		}

		public boolean decrement() {
			count--;
			return count <= 0;
		}

		public void increment() {
			count++;
		}
		
		private TabletEvent toTabletEvent(MouseEvent e) {

			TabletEvent.Type type = null;
			switch (e.getID()) {
				case MouseEvent.MOUSE_PRESSED:
					type = Type.PRESSED;
					break;
				case MouseEvent.MOUSE_RELEASED:
					type = Type.RELEASED;
					break;
				case MouseEvent.MOUSE_MOVED:
					type = Type.MOVED;
					break;
				case MouseEvent.MOUSE_DRAGGED:
					type = Type.DRAGGED;
					break;
				case MouseEvent.MOUSE_ENTERED:
					type = Type.ENTERED;
					break;
				case MouseEvent.MOUSE_EXITED:
					type = Type.EXITED;
					break;
				case MouseEvent.MOUSE_CLICKED:
				case MouseEvent.MOUSE_WHEEL:
				default:
					type = null;
					break;
			}
			return new TabletEvent(e,type,mousedevice);
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			listener.cursorEntered(toTabletEvent(e));
		}

		public void mouseExited(MouseEvent e) {
			listener.cursorExited(toTabletEvent(e));
		}

		public void mousePressed(MouseEvent e) {
			listener.cursorPressed(toTabletEvent(e));
		}

		public void mouseReleased(MouseEvent e) {
			listener.cursorReleased(toTabletEvent(e));
		}

		public void mouseDragged(MouseEvent e) {
			listener.cursorDragged(toTabletEvent(e));
		}

		public void mouseMoved(MouseEvent e) {
			listener.cursorMoved(toTabletEvent(e));
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			
		}

	}
}

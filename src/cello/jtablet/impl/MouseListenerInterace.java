package cello.jtablet.impl;

import java.awt.Component;
import java.awt.event.InputEvent;
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
	private final TabletDevice mousedevice = new TabletDevice.SystemDevice();
	
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
		
		private void fireTabletEvent(MouseEvent e) {

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
				default:
					type = null;
					break;
			}
			fireEvent(new TabletEvent(e,type,mousedevice));
		}

		private void fireEvent(TabletEvent ev) {
			System.out.println("mouse input: "+ev);
			ev.fireEvent(listener);
		}
		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			fireTabletEvent(e);
		}

		public void mouseExited(MouseEvent e) {
			fireTabletEvent(e);
		}

		public void mousePressed(MouseEvent e) {
			fireTabletEvent(e);
		}		


		public void mouseReleased(MouseEvent e) {
			fireTabletEvent(e);
		}

		public void mouseDragged(MouseEvent e) {
			fireTabletEvent(e);
		}

		public void mouseMoved(MouseEvent e) {
			fireTabletEvent(e);
		}

		public void mouseWheelMoved(MouseWheelEvent e) {
			float deltaX=0,deltaY=0;
			if ((e.getModifiersEx()&InputEvent.SHIFT_DOWN_MASK)!=0) {
				deltaX = -e.getWheelRotation()*e.getScrollAmount();
			} else {
				deltaY = -e.getWheelRotation()*e.getScrollAmount();
			}
			listener.cursorScrolled(new TabletEvent(
				e.getComponent(),
				TabletEvent.Type.SCROLLED,
				e.getWhen(),
				e.getModifiersEx(),
				mousedevice,
				e.getX(),
				e.getY(),
				0,
				deltaX,
				deltaY,
				0
			));
		}

	}
}

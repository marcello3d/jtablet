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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.ConcurrentHashMap;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;
import cello.jtablet.event.TabletEvent.Type;

/**
 * @author marcello
 */
public class MouseTabletManager implements TabletManager {

	private boolean enabled = true;

	public void addScreenTabletListener(TabletListener l) {
		throw new UnsupportedOperationException(getClass()+" does not support screen listeners");
	}
	public void removeScreenTabletListener(TabletListener l) {
		throw new UnsupportedOperationException(getClass()+" does not support screen listeners");
	}

	private final ConcurrentHashMap<TabletListener,MagicListener> listenerMap = new ConcurrentHashMap<TabletListener, MagicListener>();
	
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

	/**
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return if this listener is enabled
	 */
	public boolean isEnabled() {
		return enabled;
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
			if (!enabled) {
				return;
			}
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
			fireEvent(new TabletEvent(e,type,TabletDevice.SYSTEM_MOUSE));
		}

		private void fireEvent(TabletEvent ev) {
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
			if (!enabled) {
				return;
			}
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
				TabletDevice.SYSTEM_MOUSE,
				e.getX(),
				e.getY(),
				0,
				deltaX,
				deltaY,
				0
			));
		}

	}

//	public void setHints(TabletManagerFactory.Hints hints) {
//		
//	}
}

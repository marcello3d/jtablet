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

import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletEvent.Type;

/**
 * The {@code MouseDriver} is a driver which acts as a wrapper for Java's
 * mouse event system. Though limited in ability, it allows a graceful
 * fallback for applications if no other {@link TabletDriver} is supported.
 * Though it does not support pressure data, applications will still recieve
 * X and Y positions with which to draw.
 *
 * If executed with sufficient permission (e.g. as part of a signed applet),
 * the {@code MouseDriver} will listen for mouse events on the entire screen
 * -- not just the application's GUI.
 *
 * @author marcello
 */
public class MouseDriver implements TabletDriver {
	
	private static class ScreenComponent extends java.awt.Component {
		private static final Point POINT = new Point(0,0);
		private java.awt.GraphicsConfiguration getMainScreen() {
			java.awt.GraphicsDevice[] gs = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			if (gs.length > 0) {
				return gs[0].getDefaultConfiguration();
			}
			return null;
		}
		@Override
		public Point getLocationOnScreen() {
			return POINT;
		}
		@Override
		public java.awt.Rectangle bounds() {
			return getMainScreen().getBounds();
		}
		@Override
		public java.awt.Rectangle getBounds(java.awt.Rectangle rv) {
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
	
	protected class MagicListener implements MouseListener, MouseMotionListener, MouseWheelListener, AWTEventListener {
		ScreenComponent SCREEN_COMPONENT = new ScreenComponent();
		
		private void fire(TabletEvent event) {
			if (event != null) {
				System.out.println(event.getPoint());
				TabletManager.postTabletEvent(event);
			}
			else {
				System.out.print("_");
			}
		}
		
		private TabletEvent translateEvent(MouseEvent e) {
			TabletEvent.Type type = null;
			switch (e.getID()) {
				case MouseEvent.MOUSE_PRESSED:  type = Type.PRESSED;  break;
				case MouseEvent.MOUSE_RELEASED: type = Type.RELEASED; break;
				case MouseEvent.MOUSE_MOVED:    type = Type.MOVED;    break;
				case MouseEvent.MOUSE_DRAGGED:  type = Type.DRAGGED;  break;
				case MouseEvent.MOUSE_ENTERED:  type = Type.ENTERED;  break;
				case MouseEvent.MOUSE_EXITED:   type = Type.EXITED;   break;
				default:                        type = null;          break;
			}
			
			if (type != null) {
				Point componentLocationOnScreen = e.getComponent().getLocationOnScreen();
				Point src = (Point)componentLocationOnScreen.clone();
				src.translate(e.getX(), e.getY());
				System.out.println(e.getX() + ", " + e.getY() + "\t" + componentLocationOnScreen + "\t" + src);
				return new TabletEvent(e, type, MouseDevice.INSTANCE, SCREEN_COMPONENT, componentLocationOnScreen.x + e.getX(), componentLocationOnScreen.y + e.getY());
			}
			else {
				return null;
			}
		}
		
		private TabletEvent translateEvent(MouseWheelEvent e) {
			float deltaX=0, deltaY=0;
			
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)
				deltaX = -e.getWheelRotation()*e.getScrollAmount();
			else
				deltaY = -e.getWheelRotation()*e.getScrollAmount();
			
			return new TabletEvent(
				e.getComponent(),
				TabletEvent.Type.SCROLLED,
				e.getWhen(),
				e.getModifiersEx(),
				MouseDevice.INSTANCE,
				e.getX(),
				e.getY(),
				0,
				deltaX,
				deltaY,
				0
			);
		}
		
		public void eventDispatched(AWTEvent event) {
			System.out.print("*");
			if (event instanceof MouseWheelEvent) {
				fire(translateEvent((MouseWheelEvent)event));
			} else if (event instanceof MouseEvent) {
				fire(translateEvent((MouseEvent)event));
			}
		}
		
		public void mouseClicked(MouseEvent e)         { return;                  }
		public void mouseEntered(MouseEvent e)         { fire(translateEvent(e)); }
		public void mouseExited(MouseEvent e)          { fire(translateEvent(e)); }
		public void mousePressed(MouseEvent e)         { fire(translateEvent(e)); }
		public void mouseReleased(MouseEvent e)        { fire(translateEvent(e)); }
		public void mouseDragged(MouseEvent e)         { fire(translateEvent(e)); }
		public void mouseMoved(MouseEvent e)           { fire(translateEvent(e)); }
		public void mouseWheelMoved(MouseWheelEvent e) { fire(translateEvent(e)); }
		
	}
	
	protected MagicListener listener = new MagicListener();
	protected DriverStatus status;
	
	public void load() {
		try {
			//Try to listen to the mouse anywhere on screen
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				public Void run() {
					Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
					return null;
				}
			});
			
			status = new DriverStatus(DriverStatus.State.LOADED);
		}
		catch (AccessControlException ex) {
			System.err.println("MouseDriver does not have \"listenToAllAWTEvents\" permission. Falling back to frame listening.");
			status = new DriverStatus(DriverStatus.State.NATIVE_EXCEPTION);
		}
	}
	
	public void run() {
		try {
			if (status.getState() != DriverStatus.State.LOADED) {
				while (true) {
					for (Frame f : Frame.getFrames()) {
						f.addMouseListener(listener);
						f.addMouseMotionListener(listener);
						f.addMouseWheelListener(listener);
					}
					Thread.sleep(1000);
				}
			}
		}
		catch (InterruptedException e) {
			System.err.println("MouseDriver thread interrupted. Exiting thread.");
		}
	}
	
	public DriverStatus getStatus() {
		return status;
	}
}


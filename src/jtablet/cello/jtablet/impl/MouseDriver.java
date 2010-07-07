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
import cello.jtablet.ScreenComponent;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletEvent.Type;

/**
 * The is a driver which acts as a wrapper for Java's mouse event system.
 * Though limited in ability, it allows a graceful fallback for applications
 * if no other {@link TabletDriver} is supported. Though it does not support
 * pressure data, applications will still receive X and Y positions with
 * which to draw.
 *
 * @author marcello
 * @since 1.2.5
 */
public class MouseDriver implements TabletDriver {

	/**
	 * This class is responsible for listening for all mouse-related
	 * events produced by Java, transforming them into
	 * {@link TabletEvent}s, and passing them on to the
	 * {@link TabletManager}.
	 *
	 * @since 1.2.5
	 */
	protected class MagicListener implements MouseListener, MouseMotionListener, MouseWheelListener, AWTEventListener {
		
		public void eventDispatched(AWTEvent event) {
			if (event instanceof MouseWheelEvent) {
				post(transform((MouseWheelEvent)event));
			} else if (event instanceof MouseEvent) {
				post(transform((MouseEvent)event));
			}
		}

		public void mouseClicked(MouseEvent e)         { return;             }
		public void mouseEntered(MouseEvent e)         { post(transform(e)); }
		public void mouseExited(MouseEvent e)          { post(transform(e)); }
		public void mousePressed(MouseEvent e)         { post(transform(e)); }
		public void mouseReleased(MouseEvent e)        { post(transform(e)); }
		public void mouseDragged(MouseEvent e)         { post(transform(e)); }
		public void mouseMoved(MouseEvent e)           { post(transform(e)); }
		public void mouseWheelMoved(MouseWheelEvent e) { post(transform(e)); }
		
		/**
		 * Transforms a MouseEvent into a TabletEvent for use by
		 * the rest of JTablet. This method maintains as much
		 * information about the original event as possible.
		 *
		 * @param e the event to transform
		 * @return a TabletEvent with much the same information as the source MouseEvent
		 */
		private TabletEvent transform(MouseEvent e) {
			System.out.print("~");
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
			
			Point componentLocationOnScreen = e.getComponent().getLocationOnScreen();
			return new TabletEvent(e,
				type,
				MouseDevice.INSTANCE,
				ScreenComponent.INSTANCE,
				componentLocationOnScreen.x + e.getX(),
				componentLocationOnScreen.y + e.getY()
			);
		}

		/**
		 * Transforms a MouseWheelEvent into a TabletEvent for use by
		 * the rest of JTablet. This method maintains as much
		 * information about the original event as possible.
		 *
		 * @param e the event to transform
		 * @return a TabletEvent with much the same information as the source MouseWheelEvent
		 */
		private TabletEvent transform(MouseWheelEvent e) {
			float deltaX=0, deltaY=0;
			
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0)
				deltaX = -e.getWheelRotation()*e.getScrollAmount();
			else
				deltaY = -e.getWheelRotation()*e.getScrollAmount();
			
			return new TabletEvent(
				ScreenComponent.INSTANCE,
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

		/**
		 * Posts the given event in the TabletManager's event queue
		 * for processing.
		 *
		 * @param event the event to send
		 */
		private void post(TabletEvent event) {
			if (event.getType() != null &&
				event.getType() != Type.ENTERED &&
				event.getType() != Type.EXITED)
				TabletManager.postTabletEvent(event);
			else
			    return; //I think proguard breaks this class unless this is here...
		}
	}
	
	protected MagicListener listener = new MagicListener();
	protected DriverStatus status;

	/**
	 * Loads the driver in preparation of its running. While no
	 * native code is called by this method, it does still attempt
	 * to grab as much pointer data as possible. If the necessary
	 * permissions exist, the driver will be able to listen to all
	 * AWT mouse events -- even those from other applications running
	 * in the same JVM.
	 */
	public void load() {
		try {
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
			status = new DriverStatus(DriverStatus.State.UNEXPECTED_EXCEPTION, ex);
		}
	}
	
	public void run() {
		/**  This code does not work for some reason...
		     It sure would be nice to just listen to all frames
		     as a fallback...
		     
		     PS: Be sure to switch the status state to LOADED
		     in the catch block of the load() method if you're
		     going to do any testing...
		     
		try {
			if (status.getThrowable() != null) {
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
		*/
	}
	
	public DriverStatus getStatus() {
		return status;
	}
}


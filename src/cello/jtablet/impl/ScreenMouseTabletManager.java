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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletEvent.Type;

/**
 * @author marcello
 */
class ScreenMouseTabletManager extends ScreenTabletManager {
	
	ScreenMouseTabletManager() {
		start();
		stop();
	}
	
	private final AWTEventListener listener = new AWTEventListener() {
		public void eventDispatched(AWTEvent event) {
			if (event instanceof MouseWheelEvent) {
				fireTabletEvent((MouseWheelEvent)event);
			} else if (event instanceof MouseEvent) {
				fireTabletEvent((MouseEvent)event);
			}
		}
	};

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
				System.out.println("id = "+e.getID());
				type = null;
				break;
		}
		Point screen = e.getLocationOnScreen();
		fireScreenTabletEvent(new TabletEvent(e, type, SystemDevice.INSTANCE, ScreenTabletManager.SCREEN_COMPONENT, screen.x, screen.y));
	}

	private void fireTabletEvent(MouseWheelEvent e) {
		float deltaX=0,deltaY=0;
		if ((e.getModifiersEx()&InputEvent.SHIFT_DOWN_MASK)!=0) {
			deltaX = -e.getWheelRotation()*e.getScrollAmount();
		} else {
			deltaY = -e.getWheelRotation()*e.getScrollAmount();
		}
		Point screen = e.getLocationOnScreen();
		fireScreenTabletEvent(new TabletEvent(
			ScreenTabletManager.SCREEN_COMPONENT,
			TabletEvent.Type.SCROLLED,
			e.getWhen(),
			e.getModifiersEx(),
			SystemDevice.INSTANCE,
			screen.x,
			screen.y,
			0,
			deltaX,
			deltaY,
			0
		));
	}


	@Override
	protected void start() {
		Toolkit.getDefaultToolkit().addAWTEventListener(listener, 
				AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
	}
	@Override
	protected void stop() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
	}
}

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

package cello.jtablet.impl.platform;

import java.awt.event.MouseEvent;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.impl.jpen.platform.NativeCocoaInterface;

public abstract class RawDataScreenInputInterface extends NativeScreenInputInterface {

	protected static final TabletDevice SYSTEM_MOUSE = TabletDevice.SYSTEM_MOUSE;

	private final float PRESSED_THRESHOLD = 0;
	
	private TabletDevice lastDevice = SYSTEM_MOUSE;
	private boolean lastProximity = false;
	

	private boolean lastPressed = false;
	private float lastX = 0, lastY = 0;
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
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.NEW_DEVICE,
				when,
				modifiers,
				device, 
				x,y
			));
			lastDevice = device;
		}
		
		if (lastProximity != enteringProximity) {
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				enteringProximity ? TabletEvent.Type.ENTERED : TabletEvent.Type.EXITED,
				when,
				modifiers,
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
	



	protected void generatePointEvents(long when, int keyModifiers, float x,
			float y, float pressure, float tiltX,
			float tiltY, float sidePressure, float rotation, int button,
			boolean buttonJustPressed, boolean buttonJustReleased) {
		
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
					NativeCocoaInterface.SCREEN_COMPONENT,
					TabletEvent.Type.NEW_DEVICE,
					when,
					modifiers,
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
				NativeCocoaInterface.SCREEN_COMPONENT,
				buttonJustPressed ? TabletEvent.Type.PRESSED : TabletEvent.Type.RELEASED,
				when,
				modifiers,
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
				NativeCocoaInterface.SCREEN_COMPONENT,
				buttonMask != 0 ? TabletEvent.Type.DRAGGED : TabletEvent.Type.MOVED,
				when,
				modifiers,
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
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.LEVEL_CHANGED,
				when,
				modifiers,
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

	protected int getLastButtonMask() {
		return lastButtonMask;
	}

	/**
	 * @return the lastDevice
	 */
	protected TabletDevice getLastDevice() {
		return lastDevice;
	}

}

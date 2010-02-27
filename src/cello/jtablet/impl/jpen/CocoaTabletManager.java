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

package cello.jtablet.impl.jpen;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.impl.AbstractTabletDevice;
import cello.jtablet.impl.NativeLoaderException;
import cello.jtablet.impl.NativeTabletManager;
import cello.jtablet.impl.ScreenTabletManager;
import cello.repackaged.jpen.provider.osx.CocoaAccess;

/**
 * @author marcello
 */
public class CocoaTabletManager extends ScreenTabletManager implements NativeTabletManager {
	
	private static final float DEVICE_DELTA_FACTOR = 0.25f;

	public void load() throws NativeLoaderException {
		ca.start();
	}

	private final Map<String,TabletDevice> devices = new HashMap<String,TabletDevice>();

	private final CocoaAccess ca = new CocoaAccess() {

		public void enable() {
			setTabletEventsEnabled(true);
			setGestureEventsEnabled(true);
			setScrollEventsEnabled(true);
			setProximityEventsEnabled(true);
		}

		@Override
		protected void postScrollEvent(
				  double eventTimeSeconds,
				  int cocoaModifierFlags,
				  float screenX, float screenY,
				  boolean isDeviceDelta,
				  float deviceDeltaX, float deviceDeltaY
				) {
			long when = System.currentTimeMillis();
			int keyModifiers = getMouseEventModifiers(cocoaModifierFlags);
			float factor = isDeviceDelta ? DEVICE_DELTA_FACTOR : 1;
			generateScrollEvent(when, keyModifiers, screenX, screenY, deviceDeltaX*factor, deviceDeltaY*factor);
		}
		@Override
		protected void postMagnifyEvent(
				  double eventTimeSeconds,
				  int cocoaModifierFlags,
				  float screenX, float screenY,
				  float magnificationFactor
				) {
			long when = System.currentTimeMillis();
			int keyModifiers = getMouseEventModifiers(cocoaModifierFlags);
			generateZoomGestureEvent(when, keyModifiers, screenX, screenY,
					magnificationFactor);
		}
		@Override
		protected void postSwipeEvent(
				  double eventTimeSeconds,
				  int cocoaModifierFlags,
				  float screenX, float screenY,
				  float deltaX, float deltaY
				) {
			long when = System.currentTimeMillis();
			int keyModifiers = getMouseEventModifiers(cocoaModifierFlags);
			generateSwipeGestureEvent(when, keyModifiers, screenX, screenY,
					deltaX,deltaY);
		}
		@Override
		protected void postRotateEvent(
				  double eventTimeSeconds,
				  int cocoaModifierFlags,
				  float screenX, float screenY,
				  float rotationDegrees
				) {
			long when = System.currentTimeMillis();
			int keyModifiers = getMouseEventModifiers(cocoaModifierFlags);
			generateRotationGestureEvent(screenX, screenY, rotationDegrees*RADIANS_PER_DEGREE,
					when, keyModifiers);
		}
		

		@Override
		protected void postProximityEvent(
				double eventTimeSeconds,
				int cocoaModifierFlags,
				int capabilityMask, 
				int deviceID,
				boolean enteringProximity, 
				int pointingDeviceID,
				int pointingDeviceSerialNumber, 
				int pointingDeviceType,
				int systemTabletID, 
				int tabletID, 
				final long uniqueID, 
				int vendorID,
				int vendorPointingDeviceType) {
			
			long when = System.currentTimeMillis();			
			int keyModifiers = getMouseEventModifiers(cocoaModifierFlags);

			TabletDevice device = getDevice(capabilityMask, uniqueID, pointingDeviceType);
			
			generateDeviceEvents(device, when, keyModifiers, enteringProximity);
			
		}

		private TabletDevice getDevice(int capabilityMask, final long uniqueId,
				int vendorPointingDeviceType) {
			String id = uniqueId+"/"+vendorPointingDeviceType+"/"+capabilityMask;
			TabletDevice td = devices.get(id);
			
			if (td == null) {
				td = makeDevice(uniqueId,capabilityMask,vendorPointingDeviceType);
				devices.put(id, td);
			}
			return td;
		}

		private Support getSupported(int capabilityMask, int capability) {
			if (capabilityMask == 0) {
				return Support.UNKNOWN;
			}
			return (capabilityMask & capability) != 0 ? Support.YES : Support.NO;
		}
		protected TabletDevice makeDevice(final long uniqueId, int capabilityMask, int pointingDeviceType) {
			Support floatSupport 		= Support.YES;
			Support buttonSupport 	 	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_BUTTONSMASK);
			Support uniqueIdSupport  	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_DEVICEIDMASK);
			Support pressureSupport  	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_PRESSUREMASK);
			Support rotationSupport 	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_ROTATIONMASK);
			Support sidePressureSupport = getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TANGENTIALPRESSUREMASK);
			Support tiltSupport 		= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TILTMASK);

			TabletDevice.Type type;
			switch (pointingDeviceType) {
				case NSPenPointingDevice:
					type = Type.STYLUS;
					break;
				case NSCursorPointingDevice:
					type = Type.MOUSE;
					break;
				case NSEraserPointingDevice:
					type = Type.ERASER;
					break;
				default:
					type = Type.UNKNOWN;
					break;
			}
			return new CocoaDevice(
				type,
				type.name(),
				Long.toHexString(uniqueId),
				floatSupport,
				buttonSupport,
				uniqueIdSupport,
				pressureSupport,
				rotationSupport,
				sidePressureSupport, 
				tiltSupport
			);
		}
		
		private boolean leftButton, rightButton, otherButton;
		
		@Override
		protected void postEvent(int type,
								 double eventTimeSeconds,
								 int cocoaModifierFlags,
								 float x, float y,
								 boolean tabletEvent,
								 int absoluteX, int absoluteY, int absoluteZ, 
								 int rawTabletButtonMask,
								 float pressure,
								 float rotation, 
								 float tiltX, float tiltY,
								 float tangentialPressure) {
			
			boolean buttonJustPressed = false, buttonJustReleased = false;
			int button = MouseEvent.NOBUTTON;

			long when = System.currentTimeMillis();
			int keyModifiers = getMouseEventModifiers(cocoaModifierFlags);
			
			// tilt is in range of -1 ~ 1, where 1 is 64 degrees
			tiltX *= TILT_TO_RADIANS;
			// Avoid negative zero...
			if (tiltY != 0) {
				tiltY = -tiltY * TILT_TO_RADIANS;
			}

			switch (type) {
			    case NS_EVENT_TYPE_LeftMouseDown:
			    	buttonJustPressed = true;
			    	button = MouseEvent.BUTTON1;
			    	leftButton = true;
			    	break;       
			    case NS_EVENT_TYPE_LeftMouseUp:
			    	buttonJustReleased = true;
			    	button = MouseEvent.BUTTON1;
			    	leftButton = false;
			    	break;
			    case NS_EVENT_TYPE_RightMouseDown:
			    	buttonJustPressed = true;
			    	button = MouseEvent.BUTTON3;
			    	rightButton = true;
			    	break;
			    case NS_EVENT_TYPE_RightMouseUp:
			    	buttonJustReleased = true;
			    	button = MouseEvent.BUTTON3;
			    	rightButton = false;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseDown:
			    	buttonJustPressed = true;
			    	button = MouseEvent.BUTTON2;
			    	otherButton = true;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseUp:
			    	buttonJustReleased = true;
			    	button = MouseEvent.BUTTON2;
			    	otherButton = false;
			    	break;
			    case NS_EVENT_TYPE_MouseMoved:
			    	// For some reason when you click maximize on a window, you get a MouseDown event, but no MouseUp...
			    	// To work around that issue, if I ever get a MouseMoved event but I think a button is pressed, we 
			    	// can simply generate a fake cursor released event.
			    	if (leftButton) {
			    		generatePointEvents(
							when, 
							keyModifiers, 
							x, y, 
							pressure, 
							tiltX, tiltY, 
							tangentialPressure, 
							rotation, 
							rawTabletButtonMask,
							MouseEvent.BUTTON1, 
							false,
							true
		    			);
			    		leftButton = false;
			    	}
			    	if (rightButton) {
			    		generatePointEvents(
							when, 
							keyModifiers, 
							x, y, 
							pressure, 
							tiltX, tiltY, 
							tangentialPressure, 
							rotation, 
							rawTabletButtonMask,
							MouseEvent.BUTTON1, 
							false,
							true
		    			);
			    		rightButton = false;
			    	}
			    	if (otherButton) {
			    		generatePointEvents(
							when, 
							keyModifiers, 
							x, y, 
							pressure, 
							tiltX, tiltY, 
							tangentialPressure, 
							rotation, 
							rawTabletButtonMask,
							MouseEvent.BUTTON1, 
							false,
							true
		    			);
			    		otherButton = false;
			    	}
			    	break;
			}

			generatePointEvents(
				when, 
				keyModifiers, 
				x, y, 
				pressure, 
				tiltX, tiltY, 
				tangentialPressure, 
				rotation, 
				rawTabletButtonMask,
				button, 
				buttonJustPressed,
				buttonJustReleased
			);
		}


		private int getMouseEventModifiers(int cocoaModifierFlags) {
			int modifiers = 0;
			if ((cocoaModifierFlags & NS_MODIFIER_ControlKeyMask)!=0) {
				modifiers |= MouseEvent.CTRL_DOWN_MASK;
			}
			if ((cocoaModifierFlags & NS_MODIFIER_ShiftKeyMask)!=0) {
				modifiers |= MouseEvent.SHIFT_DOWN_MASK;
			}
			if ((cocoaModifierFlags & NS_MODIFIER_AlternateKeyMask)!=0) {
				modifiers |= MouseEvent.ALT_DOWN_MASK;
			}
			if ((cocoaModifierFlags & NS_MODIFIER_CommandKeyMask)!=0) {
				modifiers |= MouseEvent.META_DOWN_MASK;
			}
			return modifiers;
		}
	};
	public boolean isSystemSupported(String os) {
		return os.contains("mac");
	}

	private static class CocoaDevice extends AbstractTabletDevice {
		protected CocoaDevice(Type type, String name, String uniqueId,
				Support floatSupport, Support buttonSupport,
				Support uniqueIdSupport, Support pressureSupport,
				Support rotationSupport, Support sidePressureSupport,
				Support tiltSupport) {
			super(type, name, uniqueId, floatSupport, buttonSupport, uniqueIdSupport,
					pressureSupport, rotationSupport, sidePressureSupport, tiltSupport);
		}
	}

	@Override
	protected void start() {
		ca.enable();
	}

	@Override
	protected void stop() {
		ca.disable();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		ca.stop();
	}

}

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

package cello.jtablet.impl.jpen.platform;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import jpen.provider.NativeLibraryLoader;
import jpen.provider.osx.CocoaAccess;
import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.impl.platform.RawDataScreenInputInterface;

/**
 * @author marcello
 */
public class NativeCocoaInterface extends RawDataScreenInputInterface {
	
	private static final NativeLibraryLoader LIB_LOADER=new NativeLibraryLoader(
			Integer.valueOf(jpen.Utils.getModuleProperties().getString("jpen.provider.osx.nativeVersion")));

	@Override
	protected NativeLibraryLoader getLoader() {
		return LIB_LOADER;
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
				  float deltaX, float deltaY
				) {
			long when = System.currentTimeMillis();
			int modifiers = getLastButtonMask() | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.SCROLLED,
				when,
				modifiers,
				getLastDevice(),
				screenX,screenY,
				0,
				deltaX,deltaY,
				0
			));
		}
		@Override
		protected void postMagnifyEvent(
				  double eventTimeSeconds,
				  int cocoaModifierFlags,
				  float screenX, float screenY,
				  float magnificationFactor
				) {
			long when = System.currentTimeMillis();
			int modifiers = getLastButtonMask() | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.ZOOMED,
				when,
				modifiers,
				getLastDevice(),
				screenX,screenY,
				0,
				0,0,
				magnificationFactor
			));
		}
		@Override
		protected void postSwipeEvent(
				  double eventTimeSeconds,
				  int cocoaModifierFlags,
				  float screenX, float screenY,
				  float deltaX, float deltaY
				) {
			long when = System.currentTimeMillis();
			int modifiers = getLastButtonMask() | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.SWIPED,
				when,
				modifiers,
				getLastDevice(),
				screenX,screenY,
				0,
				deltaX,deltaY,
				0
			));
		}
		@Override
		protected void postRotateEvent(
				  double eventTimeSeconds,
				  int cocoaModifierFlags,
				  float screenX, float screenY,
				  float rotationDegrees
				) {
			long when = System.currentTimeMillis();
			int modifiers = getLastButtonMask() | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.ROTATED,
				when,
				modifiers,
				getLastDevice(),
				screenX,screenY,
				rotationDegrees * RADIANS_PER_DEGREE,
				0,0,
				0
			));
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
			return (capabilityMask & capability) != 0 ? Support.SUPPORTED : Support.NONE;
		}
		protected TabletDevice makeDevice(final long uniqueId, int capabilityMask, int pointingDeviceType) {
			Support supportsButtons 	 = getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_BUTTONSMASK);
			Support supportsDeviceId  	 = getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_DEVICEIDMASK);
			Support supportsPressure  	 = getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_PRESSUREMASK);
			Support supportsRotation 	 = getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_ROTATIONMASK);
			Support supportsSidePressure = getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TANGENTIALPRESSUREMASK);
			Support supportsTiltXY 		 = getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TILTMASK);

			TabletDevice.Type type;
			switch (pointingDeviceType) {
				case NSPenPointingDevice:
					type = Type.STYLUS_TIP;
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
			return new CocoaTabletDevice(type,uniqueId,supportsButtons,supportsDeviceId,supportsPressure,supportsRotation,supportsSidePressure, supportsTiltXY);
		}
		
		
		
		
		@Override
		protected void postEvent(int type,
								 double eventTimeSeconds,
								 int cocoaModifierFlags,
								 float x, float y,
								 int absoluteX, int absoluteY, int absoluteZ, 
								 int rawTabletButtonMask,
								 float pressure,
								 float rotation, 
								 float tiltX, float tiltY,
								 float tangentialPressure) {
			

			boolean buttonJustPressed = false, buttonJustReleased = false;
			int button = MouseEvent.NOBUTTON;
			switch (type) {
			    case NS_EVENT_TYPE_LeftMouseDown:
			    	buttonJustPressed = true;
			    	button = MouseEvent.BUTTON1;
			    	break;       
			    case NS_EVENT_TYPE_LeftMouseUp:
			    	buttonJustReleased = true;
			    	button = MouseEvent.BUTTON1;
			    	break;
			    case NS_EVENT_TYPE_RightMouseDown:
			    	buttonJustPressed = true;
			    	button = MouseEvent.BUTTON3;
			    	break;
			    case NS_EVENT_TYPE_RightMouseUp:
			    	buttonJustReleased = true;
			    	button = MouseEvent.BUTTON3;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseDown:
			    	buttonJustPressed = true;
			    	button = MouseEvent.BUTTON2;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseUp:
			    	buttonJustReleased = true;
			    	button = MouseEvent.BUTTON2;
			    	break;
			}

			long when = System.currentTimeMillis();
			int modifiers = getMouseEventModifiers(cocoaModifierFlags);
			
			// tilt is in range of -1 ~ 1, where 1 is 64 degrees
			tiltX *= TILT_TO_RADIANS;
			// Avoid negative zero...
			if (tiltY != 0) {
				tiltY = -tiltY * TILT_TO_RADIANS;
			}
			generatePointEvents(
				when, 
				modifiers, 
				x, y, 
				pressure, 
				tiltX, tiltY, 
				tangentialPressure, 
				rotation, 
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


	private class CocoaTabletDevice extends TabletDevice {
		private final Type type;
		private final long uniqueId;
		private final Support supportsButtons;
		private final Support supportsDeviceId;
		private final Support supportsPressure;
		private final Support supportsRotation;
		private final Support supportsSidePressure;
		private final Support supportsTiltXY;
		
		

		public CocoaTabletDevice(Type type, long uniqueId,
				Support supportsButtons, Support supportsDeviceId,
				Support supportsPressure, Support supportsRotation,
				Support supportsSidePressure, Support supportsTiltXY) {
			super();
			this.type = type;
			this.uniqueId = uniqueId;
			this.supportsButtons = supportsButtons;
			this.supportsDeviceId = supportsDeviceId;
			this.supportsPressure = supportsPressure;
			this.supportsRotation = supportsRotation;
			this.supportsSidePressure = supportsSidePressure;
			this.supportsTiltXY = supportsTiltXY;
		}
		@Override
		public String toString() {
			return "CocoaDevice["+getType()+"-"+getPhysicalId()+"]";
		}
		@Override
		public Type getType() {
			return type;
		}
		@Override
		public String getPhysicalId() {
			return Long.toString(uniqueId);
		}

		@Override
		public Support supportsButtons() {
			return supportsButtons;
		}
		@Override
		public Support supportsDeviceID() {
			return supportsDeviceId;
		}
		@Override
		public Support supportsPressure() {
			return supportsPressure;
		}
		@Override
		public Support supportsRotation() {
			return supportsRotation;
		}

		@Override
		public Support supportsSidePressure() {
			return supportsSidePressure;
		}

		@Override
		public Support supportsTilt() {
			return supportsTiltXY;
		}
		
	}

	@Override
	protected void start() {
		ca.start();
		ca.enable();
	}

	@Override
	protected void stop() {
		ca.disable();
		ca.stop();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		ca.stop();
	}
}

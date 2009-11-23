package cello.jtablet.impl.jpen.platform;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import jpen.provider.osx.CocoaAccess;
import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.impl.platform.NativeScreenInputInterface;

/**
 * @author marcello
 */
public class NativeCocoaInterface extends NativeScreenInputInterface {
	


	private final float PRESSED_THRESHOLD = 0;
	
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
			int modifiers = lastButtonMask | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.SCROLLED,
				when,
				modifiers,
				lastDevice,
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
			int modifiers = lastButtonMask | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.ZOOMED,
				when,
				modifiers,
				lastDevice,
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
			int modifiers = lastButtonMask | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.SWIPED,
				when,
				modifiers,
				lastDevice,
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
			int modifiers = lastButtonMask | getMouseEventModifiers(cocoaModifierFlags);
			fireScreenTabletEvent(new TabletEvent(
				NativeCocoaInterface.SCREEN_COMPONENT,
				TabletEvent.Type.ROTATED,
				when,
				modifiers,
				lastDevice,
				screenX,screenY,
				rotationDegrees * RADIANS_PER_DEGREE,
				0,0,
				0
			));
		}
		
		private TabletDevice lastDevice = TabletDevice.BASIC_MOUSE;
		private boolean lastProximity = false;
		@Override
		protected void postProximityEvent(
				double eventTimeSeconds,
				int cocoaModifierFlags,
				float x, float y,
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
			int modifiers = lastButtonMask | getMouseEventModifiers(cocoaModifierFlags);

			TabletDevice td = getDevice(capabilityMask, uniqueID, pointingDeviceType);
			
			if (enteringProximity && !lastDevice.equals(td)) {
				fireScreenTabletEvent(new TabletEvent(
					NativeCocoaInterface.SCREEN_COMPONENT,
					TabletEvent.Type.NEW_DEVICE,
					when,
					modifiers,
					td, 
					x,y
				));
				lastDevice = td;
			}
			
			if (lastProximity != enteringProximity) {
				fireScreenTabletEvent(new TabletEvent(
					NativeCocoaInterface.SCREEN_COMPONENT,
					enteringProximity ? TabletEvent.Type.ENTERED : TabletEvent.Type.EXITED,
					when,
					modifiers,
					td,
					x,y
				));
				lastProximity = enteringProximity;
			}
			if (!enteringProximity) {
				lastDevice = TabletDevice.BASIC_MOUSE;
				lastPressure = 0;
				lastTiltX = 0;
				lastTiltY = 0;
				lastTangentialPressure = 0;
				lastRotation = 0;
			}
			
			lastX = x;
			lastY = y;
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
			return (capabilityMask&capability) != 0 ? Support.SUPPORTED : Support.NONE;
		}
		protected TabletDevice makeDevice(final long uniqueId, int capabilityMask, int pointingDeviceType) {
			final Support supportsButtons 		= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_BUTTONSMASK);
			final Support supportsDeviceId  	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_DEVICEIDMASK);
			final Support supportsPressure  	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_PRESSUREMASK);
			final Support supportsRotation 		= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_ROTATIONMASK);
//			final Support supportsOrientation	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_ORIENTINFOMASK);
			final Support supportsTanPressure	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TANGENTIALPRESSUREMASK);
			final Support supportsTiltXY 		= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TILTXMASK|
														CocoaAccess.WACOM_CAPABILITY_TILTYMASK);

			final TabletDevice.Type type;
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
			return new CocoaTabletDevice() {
				@Override
				public Type getType() {
					return type;
				}
				@Override
				public long getPhysicalId() {
					return uniqueId;
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
				public Support supportsTangentialPressure() {
					return supportsTanPressure;
				}

				@Override
				public Support supportsTilt() {
					return supportsTiltXY;
				}
				
			};
		}
		
		
		

		private boolean lastPressed = false;
		private float lastX = 0, lastY = 0;
		private float lastPressure = 0;
		private float lastTiltX = 0;
		private float lastTiltY = 0;
		private float lastTangentialPressure = 0;
		private float lastRotation = 0;
		private int lastButtonMask = 0;
		
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
			
			boolean down = false, up = false;
			int button = 0;
			int buttonMask = lastButtonMask;
			switch (type) {
			    case NS_EVENT_TYPE_LeftMouseDown:
			    	down = true;
			    	button = MouseEvent.BUTTON1;
			    	buttonMask |= MouseEvent.BUTTON1_DOWN_MASK;
			    	break;       
			    case NS_EVENT_TYPE_LeftMouseUp:
			    	up = true;
			    	button = MouseEvent.BUTTON1;
			    	buttonMask &= ~MouseEvent.BUTTON1_DOWN_MASK;
			    	break;
			    case NS_EVENT_TYPE_RightMouseDown:
			    	down = true;
			    	button = MouseEvent.BUTTON3;
			    	buttonMask |= MouseEvent.BUTTON3_DOWN_MASK;
			    	break;
			    case NS_EVENT_TYPE_RightMouseUp:
			    	up = true;
			    	button = MouseEvent.BUTTON3;
			    	buttonMask &= ~MouseEvent.BUTTON3_DOWN_MASK;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseDown:
			    	down = true;
			    	button = MouseEvent.BUTTON2;
			    	buttonMask |= MouseEvent.BUTTON2_DOWN_MASK;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseUp:
			    	up = true;
			    	button = MouseEvent.BUTTON2;
			    	buttonMask &= ~MouseEvent.BUTTON2_DOWN_MASK;
			    	break;
			}


			long when = System.currentTimeMillis();
			int modifiers = buttonMask | getMouseEventModifiers(cocoaModifierFlags);
			
			if (lastDevice.getType()==Type.MOUSE && (buttonMask & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
				pressure = 1;
			}

			boolean pressed = pressure > PRESSED_THRESHOLD;
			
			if (up || down) {
				fireScreenTabletEvent(new TabletEvent(
					NativeCocoaInterface.SCREEN_COMPONENT,
					down ? TabletEvent.Type.PRESSED : TabletEvent.Type.RELEASED,
					when,
					modifiers,
					lastDevice,
					x,y,
					pressure,
					tiltX,tiltY,
					tangentialPressure,
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
					tangentialPressure,
					rotation,
					button
				));

			} else if (pressed == lastPressed && !up && !down && (
					pressure != lastPressure ||
					tiltX != lastTiltX ||
					tiltY != lastTiltY ||
					tangentialPressure!= lastTangentialPressure ||
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
					tangentialPressure,
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
			lastTangentialPressure = tangentialPressure;
			lastRotation = rotation;
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


	private abstract class CocoaTabletDevice extends TabletDevice {
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


	@Override
	public boolean overridesMouseListener() {
		return true;
	}

}

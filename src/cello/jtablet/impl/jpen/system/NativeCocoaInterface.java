package cello.jtablet.impl.jpen.system;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import jpen.provider.osx.CocoaAccess;
import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.impl.NativeScreenInputInterface;

/**
 * @author marcello
 */
public class NativeCocoaInterface extends NativeScreenInputInterface {
	


	private final float PRESSED_THRESHOLD = 0;
	
	private final Map<String,TabletDevice> devices = new HashMap<String,TabletDevice>();

	private final CocoaAccess ca = new CocoaAccess() {

		private TabletDevice lastDevice = TabletDevice.BASIC_MOUSE;
		private boolean lastProximity = false;
		@Override
		protected void postProximityEvent(
				double eventTimeSeconds,
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
			long longTime = System.currentTimeMillis();
//			long longTime = (long)(eventTimeSeconds*1000+0.5);
			
			TabletDevice td = getDevice(capabilityMask, uniqueID, pointingDeviceType);
			
			if (enteringProximity && !lastDevice.equals(td)) {
				fireScreenTabletEvent(new TabletEvent(
					NativeCocoaInterface.SCREEN_COMPONENT,
					TabletEvent.Type.NEW_DEVICE,
					longTime,
					td,
					0, 
					x,y, 
					0
				));
				lastDevice = td;
			}
			
			if (lastProximity != enteringProximity) {
				fireScreenTabletEvent(new TabletEvent(
					NativeCocoaInterface.SCREEN_COMPONENT,
					enteringProximity ? TabletEvent.Type.ENTERED : TabletEvent.Type.EXITED,
					longTime,
					td,
					0,
					x,y,
					0
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

		private boolean lastPressed = false;
		private float lastX = 0, lastY = 0;
		private float lastPressure = 0;
		private float lastTiltX = 0;
		private float lastTiltY = 0;
		private float lastTangentialPressure = 0;
		private float lastRotation = 0;
		private int lastButtonMask = 0;
		
		@Override
		protected void postEvent(
									int type, 
									double eventTimeSeconds,
									int specialPointingDeviceType,
									float x, float y, 
									float deltaX, float deltaY, 
									int absoluteX, int absoluteY, int absoluteZ, 
									int cocoaButtonMask, 
									float pressure,
									float rotation, 
									float tiltX, float tiltY,
									float tangentialPressure, 
									short vendorDefined1,
									short vendorDefined2, 
									short vendorDefined3) {
			
			boolean down = false, up = false;
			int button = 0;
			int buttonMask = lastButtonMask;
			switch (type) {
			    case NS_EVENT_TYPE_LeftMouseDown:
			    	down = true;
			    	button = MouseEvent.BUTTON1;
			    	buttonMask |= MouseEvent.BUTTON1_MASK;
			    	break;       
			    case NS_EVENT_TYPE_LeftMouseUp:
			    	up = true;
			    	button = MouseEvent.BUTTON1;
			    	buttonMask &= ~MouseEvent.BUTTON1_MASK;
			    	break;
			    case NS_EVENT_TYPE_RightMouseDown:
			    	down = true;
			    	button = MouseEvent.BUTTON3;
			    	buttonMask |= MouseEvent.BUTTON3_MASK;
			    	break;
			    case NS_EVENT_TYPE_RightMouseUp:
			    	up = true;
			    	button = MouseEvent.BUTTON3;
			    	buttonMask &= ~MouseEvent.BUTTON3_MASK;
			    	break;
			    case NS_EVENT_TYPE_MouseMoved:
			    	break;
			    case NS_EVENT_TYPE_LeftMouseDragged:
			    	button = MouseEvent.BUTTON1;
			    	buttonMask |= MouseEvent.BUTTON1_MASK;
			    	break;
			    case NS_EVENT_TYPE_RightMouseDragged:
			    	button = MouseEvent.BUTTON3;
			    	buttonMask |= MouseEvent.BUTTON3_MASK;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseDown:
			    	button = MouseEvent.BUTTON2;
			    	buttonMask |= MouseEvent.BUTTON2_MASK;
			    	down = true;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseUp:
			    	button = MouseEvent.BUTTON2;
			    	buttonMask &= ~MouseEvent.BUTTON3_MASK;
			    	up = true;
			    	break;
			    case NS_EVENT_TYPE_OtherMouseDragged:
			    	button = MouseEvent.BUTTON2;
			    	buttonMask |= MouseEvent.BUTTON2_MASK;
			    	break;
			    case NS_EVENT_TYPE_MouseEntered:
			    	break;
			    case NS_EVENT_TYPE_MouseExited:
			    	break;
			    case NS_EVENT_TYPE_ScrollWheel:
			    	break;
			    case NS_EVENT_TYPE_TabletPoint:
			    	break;
			    case NS_EVENT_TYPE_TabletProximity:
			    	break;
			}
			if (lastDevice.getType()==Type.MOUSE && (buttonMask & MouseEvent.BUTTON1_MASK) != 0) {
				pressure = 1;
			}

			long longTime = System.currentTimeMillis();
//			long longTime = (long)(eventTimeSeconds*1000+0.5);
			boolean pressed = pressure > PRESSED_THRESHOLD;
			int modifiers = 0;
//			int button = 0;
			
			if (pressed != lastPressed) {

//				public TabletEvent(Component source, Type type, long when, int modifiers, 
//									TabletDevice device, float x, float y, float pressure,
//									float tiltX, float tiltY, float tangentialPressure,
//									float rotation,
//									int button) {
				fireScreenTabletEvent(new TabletEvent(
					NativeCocoaInterface.SCREEN_COMPONENT,
					pressed ? TabletEvent.Type.PRESSED : TabletEvent.Type.RELEASED,
					longTime,
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
					pressed ? TabletEvent.Type.DRAGGED : TabletEvent.Type.MOVED,
					longTime,
					modifiers,
					lastDevice,
					x,y,
					pressure,
					tiltX,tiltY,
					tangentialPressure,
					rotation,
					button
				));

			} else if (pressed == lastPressed && (
					pressure != lastPressure ||
					tiltX != lastTiltX ||
					tiltY != lastTiltY ||
					tangentialPressure!= lastTangentialPressure ||
					rotation != lastRotation
			)) {
				fireScreenTabletEvent(new TabletEvent(
					NativeCocoaInterface.SCREEN_COMPONENT,
					TabletEvent.Type.LEVEL_CHANGED,
					longTime,
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
			lastPressed = pressed;
			lastX = x;
			lastY = y;
			lastPressure = pressure;
			lastTiltX = tiltX;
			lastTiltY = tiltY;
			lastTangentialPressure = tangentialPressure;
			lastRotation = rotation;
		}
	};
	public boolean isSystemSupported(String os) {
		return os.contains("mac");
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

	private static Support getSupported(int capabilityMask, int capability) {
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
//		final Support supportsOrientation	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_ORIENTINFOMASK);
		final Support supportsTanPressure	= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TANGENTIALPRESSUREMASK);
		final Support supportsTiltXY 		= getSupported(capabilityMask, CocoaAccess.WACOM_CAPABILITY_TILTXMASK|
													CocoaAccess.WACOM_CAPABILITY_TILTYMASK);

		final TabletDevice.Type type;
		switch (pointingDeviceType) {
			case CocoaAccess.DEVICE_TYPE_STYLUS:
				type = Type.STYLUS_TIP;
				break;
			case CocoaAccess.DEVICE_TYPE_CURSOR:
				type = Type.MOUSE;
				break;
			case CocoaAccess.DEVICE_TYPE_ERASER:
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

/*!
 * Copyright (c) 2009 Marcello Bast�a-Forte (marcello@cellosoft.com)
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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.AbstractTabletDevice;
import cello.jtablet.impl.Architecture;
import cello.jtablet.impl.MouseTabletManager;
import cello.jtablet.impl.NativeLoaderException;
import cello.jtablet.impl.NativeTabletManager;
import cello.jtablet.impl.ScreenTabletManager;
import cello.repackaged.jpen.provider.wintab.WintabAccess;


/**
 * @author marcello
 */
public class WinTabTabletManager extends ScreenTabletManager implements NativeTabletManager {

	private MouseTabletManager mouseListener = new MouseTabletManager();
	private WintabAccess wa;
	private boolean running = false;
	private Thread thread = new Thread("JTablet-WinTab") {
		{
			setDaemon(true);
			setPriority(NORM_PRIORITY+1);
		}
		public void run() {
			while (running) {
				readPackets();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
	};
	
	public void load() throws NativeLoaderException {
		try {
			wa = new WintabAccess();
		} catch (Exception e) {
			throw new NativeLoaderException(e);
		}
	}
	private class WinTabCursor {
		private final String identifier;
		private final LevelRange xRange;
		private final LevelRange yRange;
		private final LevelRange pressureRange;
//		private final LevelRange altitudeRange;
//		private final LevelRange azimuthRange;
		private final LevelRange sidePressureRange;
		private final LevelRange rotationRange;
		private final TabletDevice device;
		private final int cursorType;

		private Support getSupported(int capabilityMask, int capability) {
			if (capabilityMask == 0) {
				return Support.UNKNOWN;
			}
			return (capabilityMask & capability) != 0 ? Support.YES : Support.NO;
		}
		
		public WinTabCursor(final int cursorId, final int physicalId, String identifier) {
			this.identifier = identifier;
			cursorType 			= WintabAccess.getRawCursorType(cursorId);
			xRange				= getLevelRangeObject(WintabAccess.LEVEL_TYPE_X);
			yRange				= getLevelRangeObject(WintabAccess.LEVEL_TYPE_Y);
			pressureRange		= getLevelRangeObject(WintabAccess.LEVEL_TYPE_PRESSURE);
//			altitudeRange		= getLevelRangeObject(WintabAccess.LEVEL_TYPE_TILT_ALTITUDE);
//			azimuthRange		= getLevelRangeObject(WintabAccess.LEVEL_TYPE_TILT_AZIMUTH);
			sidePressureRange	= getLevelRangeObject(WintabAccess.LEVEL_TYPE_SIDE_PRESSURE);
			rotationRange 		= getLevelRangeObject(WintabAccess.LEVEL_TYPE_ROTATION);
			
			int capabilityMask = WintabAccess.getCapabilityMask(cursorId);
			
			Support floatSupport 		= Support.YES;
			Support buttonSupport 	 	= getSupported(capabilityMask, WintabAccess.PK_BUTTONS);
			Support uniqueIdSupport     = getSupported(capabilityMask, WintabAccess.PK_SERIAL_NUMBER);
			Support pressureSupport     = getSupported(capabilityMask, WintabAccess.PK_NORMAL_PRESSURE);
			Support rotationSupport 	= (capabilityMask & WintabAccess.PK_ORIENTATION) != 0 ? 
													Support.UNKNOWN : Support.NO;
			Support sidePressureSupport	= getSupported(capabilityMask, WintabAccess.PK_TANGENT_PRESSURE);
			Support tiltSupport 	   	= getSupported(capabilityMask, WintabAccess.PK_ORIENTATION);

			TabletDevice.Type type;
			switch (WintabAccess.getCursorType(cursorId)) {
				case PENERASER:
					type = Type.ERASER;
					break;
				case PENTIP:
					type = Type.STYLUS;
					break;
				case PUCK:
					type = Type.MOUSE;
					break;
				default:
				case UNDEF:
					type = Type.UNKNOWN;
					break;
			}
					
			final String name = WintabAccess.getCursorName(cursorId);
			
			device = new WinTabDevice(
				type, 
				name, 
				Integer.toHexString(cursorType)+"-"+Long.toHexString(physicalId),
				floatSupport,
				buttonSupport,
				uniqueIdSupport,
				pressureSupport,
				rotationSupport,
				sidePressureSupport,
				tiltSupport	
			);
			
		}
		public TabletDevice getDevice() {
			return device;
		}


		// Assume we're in absolute mode by default...
		private boolean relativeMode = false;
		private int absoluteCount = 0;
		
		/**
		 * This method is used to determine if the cursor is mapped in relative mode or absolute mode. This can be 
		 * deduced by comparing how close the wintab coordinates are to the actual on-screen cursor coordinates. If they
		 * are wildly off, we can assume the device is in relative mode and we cannot take advantage of the fractional
		 * coordinates provided by wintab.
		 * 
		 * @param tabletScreenX
		 * @param tabletScreenY
		 * @param mouseEvent
		 * @return the mouse position if in relative mode, null if in absolute
		 */
		public Point updateRelativeMode(float tabletScreenX, float tabletScreenY, MouseEvent mouseEvent) {
			Point mouseScreen = mouseEvent.getLocationOnScreen();
			float dx = mouseScreen.x - tabletScreenX;
			float dy = mouseScreen.y - tabletScreenY;
			float distance = dx*dx+dy*dy;
			if (distance > 20*20) {
				relativeMode = true;
				absoluteCount = 0;
			} else if (distance < 5*5) {
				// It's possible (but rare) for the coordinates to match up when in relative mode, so we keep track of
				// how long they match before switching off of relative mode
				absoluteCount++;
				if (absoluteCount > 50) {
					relativeMode = false;
				}
			}
			return relativeMode ? mouseScreen : null;
		}
	}
	
	private class WinTabDevice extends AbstractTabletDevice {

		protected WinTabDevice(Type type, String name, String uniqueId,
				Support floatSupport, Support buttonSupport,
				Support uniqueIdSupport, Support pressureSupport,
				Support rotationSupport, Support sidePressureSupport,
				Support tiltSupport) {
			super(type, name, uniqueId, floatSupport, buttonSupport, uniqueIdSupport,
					pressureSupport, rotationSupport, sidePressureSupport, tiltSupport);
		}
		
	}
	
	private Map<String,WinTabCursor> cursors = new HashMap<String,WinTabCursor>();

	private int modifiers = 0;
	private static final double PI_over_2=Math.PI/2;
	private static final double PI_over_2_over_900=PI_over_2/900; // (/10) and (/90)
	private static final double PI_2 = Math.PI * 2;
	private static final int KEY_MODIFIERS =    InputEvent.ALT_DOWN_MASK |
											    InputEvent.ALT_GRAPH_DOWN_MASK | 
											    InputEvent.SHIFT_DOWN_MASK | 
											    InputEvent.CTRL_DOWN_MASK | 
											    InputEvent.META_DOWN_MASK;
	private static final int BUTTON_MODIFIERS = InputEvent.BUTTON1_DOWN_MASK |
												InputEvent.BUTTON2_DOWN_MASK | 
												InputEvent.BUTTON3_DOWN_MASK;
	
	private WinTabCursor cursor = null;
	private long lastTime = 0;
	protected synchronized void readPackets() {
		long when = System.currentTimeMillis();
		Rectangle r = new Rectangle();
		for (GraphicsDevice gd : environment.getScreenDevices()){
			GraphicsConfiguration graphicsConfiguration = gd.getDefaultConfiguration();
			r.add(graphicsConfiguration.getBounds());
		}
		while (wa.nextPacket()) {
			mouseListener.setFiringEvents(false);
			boolean newCursor = checkCursor();
			
			readValues();
			
			float x = toFloat(this.x, cursor.xRange);
			float y = 1 - toFloat(this.y, cursor.yRange);

			x *= r.width;
			y *= r.height;
			
			x += r.x;
			y += r.y;
			
			MouseEvent mouseEvent = mouseListener.getLastEvent();
			if (mouseEvent != null) {
				Point mouse = cursor.updateRelativeMode(x, y, mouseEvent);
				if (mouse != null) {
					x = mouse.x;
					y = mouse.y;
				}
			}

			float pressure = toFloat(this.pressure, cursor.pressureRange);
			float sidePressure = toFloat(this.sidePressure, cursor.sidePressureRange);

			float rotation = (float)(toFloat(this.rotation, cursor.rotationRange) * PI_2);
			
			float tiltX = 0, tiltY = 0;
			
			// when using the eraser the altitude is negative
			if (altitude < 0) {
				altitude = -altitude; 
			}
			// altitude values are given (in deg) multiplied by 10. 
			// Always 900 when tilt is not supported by the tablet (or is no tilt).
			if (altitude != 900) {
				double betha = altitude * PI_over_2_over_900;
				double theta = azimuth * PI_over_2_over_900 - PI_over_2;
				double tan = Math.tan(betha);
				tiltX = (float)Math.atan(Math.cos(theta)/tan);
				tiltY = (float)Math.atan(Math.sin(theta)/tan);
			}
			

			int lastModifiers = mouseListener.getLastModifiersEx();
			int buttonModifiers = lastModifiers & BUTTON_MODIFIERS;
			int keyModifiers = lastModifiers & KEY_MODIFIERS;
			
			if (newCursor) {
				generateDeviceEvents(cursor.getDevice(), when, modifiers, true, x, y);
			}
			int button = 0;
			boolean buttonJustReleased = false;
			boolean buttonJustPressed = false;
			
			int difference = buttonModifiers ^ lastButtonMask;
			if (difference != 0) {
				if ((difference & InputEvent.BUTTON1_DOWN_MASK)!=0) {
					button = MouseEvent.BUTTON1;
					buttonJustPressed = (lastModifiers & InputEvent.BUTTON1_DOWN_MASK) != 0;
				} else if ((difference & InputEvent.BUTTON2_DOWN_MASK)!=0) {
					button = MouseEvent.BUTTON2;
					buttonJustPressed = (lastModifiers & InputEvent.BUTTON2_DOWN_MASK) != 0;
				} else if ((difference & InputEvent.BUTTON3_DOWN_MASK)!=0) {
					button = MouseEvent.BUTTON3;
					buttonJustPressed = (lastModifiers & InputEvent.BUTTON3_DOWN_MASK) != 0;
				}
				buttonJustReleased = !buttonJustPressed;
			}
			
			lastButtonMask = buttonModifiers;
			
			generatePointEvents(
				when, 
				keyModifiers, 
				x, y, 
				pressure, tiltX, 
				tiltY, 
				sidePressure, rotation, 
				rawTabletButtonMask,
				button, 
				buttonJustPressed, 
				buttonJustReleased
			);
			lastTime = when;
		}
		if (cursor != null && when - lastTime > 100) {
			generateDeviceEvents(cursor != null ? cursor.getDevice() : null, when, modifiers, false);
			cursor = null;
			mouseListener.setFiringEvents(true);
		}
	}


	private LevelRange getLevelRangeObject(int type) {
		return new LevelRange(wa.getLevelRange(type));
	}
	private float toFloat(int value, LevelRange range) {
		int min = range.min;
		int max = range.max;
		return (float)(value - min) / (max - min);
	}

	private int x,y,pressure,altitude,azimuth,sidePressure,rotation;
	private int rawTabletButtonMask,lastButtonMask=0;


	private GraphicsEnvironment environment;

		
	
	private void readValues() {
		x				= wa.getValue(WintabAccess.LEVEL_TYPE_X);
		y				= wa.getValue(WintabAccess.LEVEL_TYPE_Y);
		pressure		= wa.getValue(WintabAccess.LEVEL_TYPE_PRESSURE);
		altitude		= wa.getValue(WintabAccess.LEVEL_TYPE_TILT_ALTITUDE);
		azimuth			= wa.getValue(WintabAccess.LEVEL_TYPE_TILT_AZIMUTH);
		sidePressure	= wa.getValue(WintabAccess.LEVEL_TYPE_SIDE_PRESSURE);
		rotation		= wa.getValue(WintabAccess.LEVEL_TYPE_ROTATION);
		rawTabletButtonMask	= wa.getButtons();
	}

	private boolean checkCursor() {
		int cursorId = wa.getCursor();
		int physicalId = WintabAccess.getPhysicalId(cursorId);
		String identifier = physicalId+"/"+cursorId+"/"+WintabAccess.getCursorName(cursorId);
		if (cursor == null || !identifier.equals(cursor.identifier)) {
			WinTabCursor newCursor = cursors.get(identifier);
			if (newCursor == null) {
				newCursor = new WinTabCursor(cursorId, physicalId, identifier);
				cursors.put(identifier, newCursor);
			}
			cursor = newCursor;
			return true;
		}
		return false;
	}

	public boolean isSystemSupported(String os) {
		return os.contains("win");
	}
	
	private final AWTEventListener mouseEnterListener = new AWTEventListener() {
		public void eventDispatched(AWTEvent event) {
			// If we haven't received any events recently and the mouse entered a component
			if (cursor == null && event.getID() == MouseEvent.MOUSE_ENTERED) {
				// Cycle the Wintab driver to wake it up
				wa.setEnabled(false);
				wa.setEnabled(true);
			}
			// Make sure we get the latest packets (for legacy JTablet 0.9.x applications)
			readPackets();
		}
	};
	
	@Override
	protected void start() {
		wa.setEnabled(true);
		running = true;
		environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if (!thread.isAlive()) {
			thread.start();
		}
		
		// We want to track all mouse enter events so that we can access the tablet when it is over an element but we 
		// don't have focus
		
		// We also want to track mouse motion events so we know when to read packets from Wacom
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
				Toolkit.getDefaultToolkit().addAWTEventListener(mouseEnterListener, 
						AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);
				return null;
            }
		});
	}
	@Override
	protected void stop() {
		// Remove the awt event listener again
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
            	Toolkit.getDefaultToolkit().removeAWTEventListener(mouseEnterListener);
            	return null;
            }
		});
		wa.setEnabled(false);
		running = false;
	}

	@Override
	public void addTabletListener(Component c, TabletListener l) {
		mouseListener.addTabletListener(c, l);
		super.addTabletListener(c, l);
	}
	@Override
	public void removeTabletListener(Component c, TabletListener l) {
		mouseListener.removeTabletListener(c, l);
		super.removeTabletListener(c, l);
	}

	/* unit specifiers (from wintab.h) */
//	public static final int TU_NONE			= 0;
//	public static final int TU_INCHES		= 1;
//	public static final int TU_CENTIMETERS	= 2;
//	public static final int TU_CIRCLE		= 3;
//
//	private static enum Unit {
//		NONE,
//		INCHES,
//		CENTIMETERS,
//		CIRCLE
//	};
//	
//	private float fix32ToFloat(int fix32) {
//		return (float)fix32 / (1<<16);
//	}
//	private double fix32ToDouble(int fix32) {
//		return (double)fix32 / (1<<16);
//	}

	private class LevelRange {
		public final int min;
		public final int max;
//		public final Unit unit;
//		public final float resolution;
		public LevelRange(int ranges[]) {
			min = ranges[0];
			max = ranges[1];
//			switch (ranges[2]) {
//			case TU_CENTIMETERS:
//				unit = Unit.CENTIMETERS;
//				break;
//			case TU_INCHES:
//				unit = Unit.INCHES;
//				break;
//			case TU_CIRCLE:
//				unit = Unit.CIRCLE;
//				break;
//			case TU_NONE:
//			default:
//				unit = Unit.NONE;
//				break;
//			}
//			resolution = fix32ToFloat(ranges[3]);
		}
	}

	public Architecture getArchitecture() {
		String architecture = System.getProperty("os.arch");
		if (architecture.contains("64")) {
			return Architecture.X64;
		}
		return Architecture.DEFAULT;
	}
}

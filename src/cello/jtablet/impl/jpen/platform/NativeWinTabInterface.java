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

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import jpen.provider.NativeLibraryLoader;
import jpen.provider.wintab.WintabAccess;
import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.TabletDevice.Type;
import cello.jtablet.event.TabletListener;
import cello.jtablet.impl.MouseTabletManager;
import cello.jtablet.impl.platform.NativeException;
import cello.jtablet.impl.platform.NativeScreenTabletManager;
import cello.jtablet.impl.platform.NativeTabletManager;


/**
 * @author marcello
 */
public class NativeWinTabInterface extends NativeScreenTabletManager implements NativeTabletManager {

	private MouseTabletManager mouseListener = new MouseTabletManager();
	private WintabAccess wa;
	private boolean running = true;
	private Thread thread = new Thread("JTablet-WinTab") {
		{
			setDaemon(true);
			setPriority(NORM_PRIORITY+1);
		}
		public void run() {
			while (running) {
				readPackets();
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
				}
			}
		}
	};
	
	public void load() throws NativeException {
		try {
			wa = new WintabAccess();
		} catch (Exception e) {
			throw new NativeException(e);
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
			return (capabilityMask & capability) != 0 ? Support.SUPPORTED : Support.NONE;
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
			
			final int capabilityMask = WintabAccess.getCapabilityMask(cursorId);
			
			final Support supportsButtons 	 = getSupported(capabilityMask, WintabAccess.PK_BUTTONS);
//			final Support supportsDeviceId  	 = getSupported(capabilityMask, WintabAccess.PK_SERIAL_NUMBER);
			final Support supportsPressure  	 = getSupported(capabilityMask, WintabAccess.PK_NORMAL_PRESSURE);
			final Support supportsRotation 	 = (capabilityMask & WintabAccess.PK_ORIENTATION) != 0 ? Support.UNKNOWN : Support.NONE;
			final Support supportsSidePressure = getSupported(capabilityMask, WintabAccess.PK_TANGENT_PRESSURE);
			final Support supportsTiltXY 		 = getSupported(capabilityMask, WintabAccess.PK_ORIENTATION);

//			System.out.println("cursor type="+ wa.getRawCursorType(cursorId));
//			System.out.println("device name="+ wa.getDeviceName());
//			System.out.println("device caps="+ wa.getDeviceHardwareCapabilities());
//			System.out.println("context rate="+ wa.getPacketRate());
//			System.out.println("buttonCount="+WintabAccess.getButtonCount(cursorId));
//			System.out.println("buttons="+Arrays.toString(WintabAccess.getButtonNames(cursorId)));
			final TabletDevice.Type type;
			switch (WintabAccess.getCursorType(cursorId)) {
				case PENERASER:
					type = Type.ERASER;
					break;
				case PENTIP:
					type = Type.STYLUS_TIP;
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
			
			device = new TabletDevice() {
				@Override
				public Type getType() {
					return type;
				}
				@Override
				public String getName() {
					return name;
				}
				@Override
				public String getPhysicalId() {
					return Integer.toHexString(cursorType)+"-"+Long.toHexString(physicalId);
				}

				@Override
				public Support supportsButtons() {
					return supportsButtons;
				}

				@Override
				public Support supportsDeviceID() {
					return null;
				}

				@Override
				public Support supportsPressure() {
					return supportsPressure;
				}

				@Override
				public Support supportsRotation() {
					// TODO Auto-generated method stub
					return supportsRotation;
				}

				@Override
				public Support supportsSidePressure() {
					// TODO Auto-generated method stub
					return supportsSidePressure;
				}

				@Override
				public Support supportsTilt() {
					// TODO Auto-generated method stub
					return supportsTiltXY;
				}
			};
		}
		public TabletDevice getDevice() {
			return device;
		}
	}
	
	private Map<String,WinTabCursor> cursors = new HashMap<String,WinTabCursor>();

	private int modifiers = 0;
	private static final double PI_over_2=Math.PI/2;
	private static final double PI_over_2_over_900=PI_over_2/900; // (/10) and (/90)
	private static final double PI_2 = Math.PI * 2;
	
	private WinTabCursor cursor = null;
	private long lastTime = 0;
	protected void readPackets() {
		long when = System.currentTimeMillis();
		Rectangle r = new Rectangle();
		for (GraphicsDevice gd : environment.getScreenDevices()){
			GraphicsConfiguration graphicsConfiguration = gd.getDefaultConfiguration();
			r.add(graphicsConfiguration.getBounds());
		}
		while (wa.nextPacket()) {
			mouseListener.setEnabled(false);
			boolean newCursor = checkCursor();
			
			readValues();
			
			float x = toFloat(this.x, cursor.xRange);
			float y = 1 - toFloat(this.y, cursor.yRange);

			x *= r.width;
			y *= r.height;
			
			x += r.x;
			y += r.y;

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
				double tan = tan(betha);
				tiltX = (float)atan(cos(theta)/tan);
				tiltY = (float)atan(sin(theta)/tan);
			}
			

			int keyModifiers = 0;
			
			if (newCursor) {
				generateDeviceEvents(cursor.getDevice(), when, modifiers, true, x, y);
			}
			int button = 0;
			boolean buttonJustReleased = false;
			boolean buttonJustPressed = false;
			
			int difference = buttonMask ^ lastButtonMask;
			if (difference != 0) {
				if ((difference & WintabAccess.BUTTON1_MASK)!=0) {
					button = MouseEvent.BUTTON1;
					buttonJustPressed = (buttonMask & WintabAccess.BUTTON1_MASK) != 0;
				} else if ((difference & WintabAccess.BUTTON2_MASK)!=0) {
					button = MouseEvent.BUTTON2;
					buttonJustPressed = (buttonMask & WintabAccess.BUTTON2_MASK) != 0;
				} else if ((difference & WintabAccess.BUTTON3_MASK)!=0) {
					button = MouseEvent.BUTTON3;
					buttonJustPressed = (buttonMask & WintabAccess.BUTTON3_MASK) != 0;
				}
				buttonJustReleased = !buttonJustPressed;
			}
			
			lastButtonMask = buttonMask;
			
			generatePointEvents(
				when, 
				keyModifiers, 
				x, y, 
				pressure, tiltX, 
				tiltY, 
				sidePressure, rotation, 
				button, 
				buttonJustPressed, 
				buttonJustReleased
			);
			lastTime = when;
		}
		if (cursor != null && when - lastTime > 100) {
			generateDeviceEvents(cursor != null ? cursor.getDevice() : null, when, modifiers, false);
			cursor = null;
			mouseListener.setEnabled(true);
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
	private int buttonMask,lastButtonMask=0;


	private GraphicsEnvironment environment;
//	private long deviceTime;
	
	
	private void readValues() {
//		deviceTime		= wa.getTime();
		x				= wa.getValue(WintabAccess.LEVEL_TYPE_X);
		y				= wa.getValue(WintabAccess.LEVEL_TYPE_Y);
		pressure		= wa.getValue(WintabAccess.LEVEL_TYPE_PRESSURE);
		altitude		= wa.getValue(WintabAccess.LEVEL_TYPE_TILT_ALTITUDE);
		azimuth			= wa.getValue(WintabAccess.LEVEL_TYPE_TILT_AZIMUTH);
		sidePressure	= wa.getValue(WintabAccess.LEVEL_TYPE_SIDE_PRESSURE);
		rotation		= wa.getValue(WintabAccess.LEVEL_TYPE_ROTATION);
		buttonMask		= wa.getButtons();
//		System.out.printf("currentTime=%10d time=%10d x=%6d y=%6d alt=%6d azi=%6d pressure=%6d\n", System.nanoTime()/1000000, deviceTime, x, y, altitude, azimuth, pressure);	
	}

	private boolean checkCursor() {
		int cursorId = wa.getCursor();
		int physicalId = WintabAccess.getPhysicalId(cursorId);
		String identifier = physicalId+"/"+cursorId+"/"+WintabAccess.getCursorName(cursorId);
		if (cursor == null || !identifier.equals(cursor.identifier)) {
			WinTabCursor newCursor = cursors.get(identifier);
			System.out.println("cursor -> "+identifier);
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
	@Override
	protected void start() {
		wa.setEnabled(true);
		environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		if (!thread.isAlive()) {
			thread.start();
		}
	}

	@Override
	protected void stop() {
		wa.setEnabled(false);
	}
	@Override
	protected NativeLibraryLoader getLoader() {
		return null;
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

}

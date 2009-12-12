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
import cello.jtablet.events.TabletListener;
import cello.jtablet.impl.MouseListenerInterface;
import cello.jtablet.impl.platform.NativeCursorDevice;
import cello.jtablet.impl.platform.NativeDeviceException;
import cello.jtablet.impl.platform.RawDataScreenInputInterface;


public class NativeWinTabInterface extends RawDataScreenInputInterface implements NativeCursorDevice {

	private MouseListenerInterface mouseListener = new MouseListenerInterface();
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
	
	public void load() throws NativeDeviceException {
		try {
			wa = new WintabAccess();
		} catch (Exception e) {
			throw new NativeDeviceException(e);
		}
	}
	private class WinTabCursor {
		private final int cursorId;
		private final int[] xRange;
		private final int[] yRange;
		private final int[] pressureRange;
		private final int[] altitudeRange;
		private final int[] azimuthRange;
		private final int[] sidePressureRange;
		private final int[] rotationRange;
		private final TabletDevice device;
		public WinTabCursor(int cursorId) {
			this.cursorId = cursorId;
			xRange				= wa.getLevelRange(WintabAccess.LEVEL_TYPE_X);
			yRange				= wa.getLevelRange(WintabAccess.LEVEL_TYPE_Y);
			pressureRange		= wa.getLevelRange(WintabAccess.LEVEL_TYPE_PRESSURE);
			altitudeRange		= wa.getLevelRange(WintabAccess.LEVEL_TYPE_TILT_ALTITUDE);
			azimuthRange		= wa.getLevelRange(WintabAccess.LEVEL_TYPE_TILT_AZIMUTH);
			sidePressureRange	= wa.getLevelRange(WintabAccess.LEVEL_TYPE_SIDE_PRESSURE);
			rotationRange 		= wa.getLevelRange(WintabAccess.LEVEL_TYPE_ROTATION);
			
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
						
			device = new TabletDevice() {
				@Override
				public Type getType() {
					return type;
				}

				@Override
				public Support supportsButtons() {
					return null;
				}

				@Override
				public Support supportsDeviceID() {
					return null;
				}

				@Override
				public Support supportsPressure() {
					return null;
				}

				@Override
				public Support supportsRotation() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Support supportsSidePressure() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Support supportsTilt() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
		private TabletDevice.Support convertToSupport(int range[]) {
			if (range == null || range.length != 2) {
				throw new IllegalArgumentException("Unexpected range value");
			}
			if (range[0] == range[1]) {
				return Support.NONE;
			}
			if (range[0] > range[1]) {
				return Support.UNKNOWN;
			}
			return Support.SUPPORTED;
		}
		public TabletDevice getDevice() {
			return device;
		}
	}
	
	private Map<Integer,WinTabCursor> cursors = new HashMap<Integer,WinTabCursor>();

	private int modifiers = 0;
	private static final double PI_over_2=Math.PI/2;
	private static final double PI_over_2_over_900=PI_over_2/900; // (/10) and (/90)
	private static final double PI_2 = Math.PI * 2;
	
	private WinTabCursor cursor = null;
	private long lastTime = 0;
	protected void readPackets() {
		long when = System.currentTimeMillis();
		while (wa.nextPacket()) {
			mouseListener.setEnabled(false);
			boolean newCursor = checkCursor();
			
			readValues();
			
			float x = toFloat(this.x, cursor.xRange);
			float y = 1 - toFloat(this.y, cursor.yRange);

			Rectangle r = new Rectangle();
			for (GraphicsDevice gd : environment.getScreenDevices()){
				GraphicsConfiguration graphicsConfiguration = gd.getDefaultConfiguration();
				r.add(graphicsConfiguration.getBounds());
			}
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
		if (getLastDevice() != SYSTEM_MOUSE && when - lastTime > 100) {
			generateDeviceEvents(cursor != null ? cursor.getDevice() : null, when, modifiers, false, x, y);
			cursor = null;
			mouseListener.setEnabled(true);
		}
	}
	private float toFloat(int value, int minMax[]) {
		int min = minMax[0];
		int max = minMax[1];
		return (float)(value - min) / (max - min);
	}

	private int x,y,pressure,altitude,azimuth,sidePressure,rotation;
	private int buttonMask,lastButtonMask=0;


	private GraphicsEnvironment environment;
	
	
	private void readValues() {
		x				= wa.getValue(WintabAccess.LEVEL_TYPE_X);
		y				= wa.getValue(WintabAccess.LEVEL_TYPE_Y);
		pressure		= wa.getValue(WintabAccess.LEVEL_TYPE_PRESSURE);
		altitude		= wa.getValue(WintabAccess.LEVEL_TYPE_TILT_ALTITUDE);
		azimuth			= wa.getValue(WintabAccess.LEVEL_TYPE_TILT_AZIMUTH);
		sidePressure	= wa.getValue(WintabAccess.LEVEL_TYPE_SIDE_PRESSURE);
		rotation		= wa.getValue(WintabAccess.LEVEL_TYPE_ROTATION);
		buttonMask		= wa.getButtons();
	}

	private boolean checkCursor() {
		int cursorId = wa.getCursor();
		if (cursor == null || cursorId != cursor.cursorId) {
			WinTabCursor newCursor = cursors.get(cursorId);
			if (newCursor == null) {
				newCursor = new WinTabCursor(cursorId);
				cursors.put(cursorId, newCursor);
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
}

/**
 * 
 */
package cello.jtablet;

import java.io.Serializable;

/**
 * @author marcello
 */
public abstract class TabletDevice implements Serializable {


	private TabletDevice() {}
	
	/**
	 * Default Stylus-tip type
	 */
	public static final TabletDevice STYLUS_TIP = new Stylus();
	/**
	 * Default Stylus-eraser type
	 */
	public static final TabletDevice STYLUS_ERASER = new StylusEraser();
	/**
	 * Default Mouse type
	 */
	public static final TabletDevice MOUSE = new Mouse();

	/**
	 * A stylus tablet device
	 */
	public static class Stylus extends TabletDevice {
		@Override
		public Support supportsPressure() {
			return Support.UNKNOWN;
		}
		@Override
		public Support supportsTilt() {
			return Support.UNKNOWN;
		}
		@Override
		public Support supportsTangentialPressure() {
			return Support.UNKNOWN;
		}
		@Override
		public Support supportsRotation() {
			return Support.UNKNOWN;
		}
		@Override
		public Support supportsButtons() {
			return Support.UNKNOWN;
		}
		@Override
		public Support supportsDeviceID() {
			return Support.UNKNOWN;
		}
	}
	/**
	 * A stylus eraser tablet device
	 * @author marcello
	 *
	 */
	public static class StylusEraser extends Stylus {
	}
	/**
	 * A mouse-like tablet device
	 */
	public static class Mouse extends TabletDevice {
		@Override 
		public Support supportsPressure() {
			return Support.NONE;
		}
		@Override
		public Support supportsTilt() {
			return Support.NONE;
		}
		@Override
		public Support supportsTangentialPressure() {
			return Support.NONE;
		}
		@Override
		public Support supportsRotation() {
			return Support.NONE;
		}
		@Override
		public Support supportsButtons() {
			return Support.SUPPORTED;
		}
		@Override
		public Support supportsDeviceID() {
			return Support.NONE;
		}
	}
	
	/**
	 * Support result returned by various supports*() methods
	 */
	public static enum Support {
		/**
		 * This feature is definitely not supported.
		 */
		NONE,
		/**
		 * It is unknown whether or not this feature is supported.
		 */
		UNKNOWN,
		/**
		 * This feature is definitely supported.
		 */
		SUPPORTED
	};

	/**
	 * @return whether this device supports buttons
	 */
	public abstract Support supportsButtons();
	/**
	 * @return whether this device supports rotat
	 */
	public abstract Support supportsDeviceID();
	/**
	 * @return whether this device supports pressure sensitivity
	 */
	public abstract Support supportsPressure();
	/**
	 * @return whether this device supports tilt (away from the vertical axis)
	 */
	public abstract Support supportsTilt();
	/**
	 * @return whether this device supports tangential pressure (e.g. control wheel on Wacom airbrush)
	 */
	public abstract Support supportsTangentialPressure();
	/**
	 * @return whether this device supports rotation (around the axis of the stylus)
	 */
	public abstract Support supportsRotation();
	
	/**
	 * @return the name of this device
	 */
	public String getName() {
		return null;
	}
	/**
	 * @return the device id
	 */
	public int getPhysicalId() {
		return 0;
	}
}

/**
 * 
 */
package cello.jtablet;

import java.io.Serializable;

/**
 * @author marcello
 */
public abstract class TabletDevice implements Serializable {


	/**
	 * The default mouse tablet device
	 */
	public static final TabletDevice BASIC_MOUSE = new MouseDevice();

	protected TabletDevice() {}
	
	/**
	 * Types of tablet devices
	 */
	public enum Type {
		/** a mouse-style device */
		MOUSE,
		/** the tip of a stylus */
		STYLUS_TIP,
		/** the eraser of a stylus */
		ERASER,
		/** unknown input device */
		UNKNOWN
	};
	
	/**
	 * A stylus tablet device
	 */
	public static class Stylus extends TabletDevice {
		@Override
		public Type getType() {
			return Type.STYLUS_TIP;
		}
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
	 * A mouse-like tablet device
	 */
	public static class MouseDevice extends TabletDevice {
		@Override
		public Type getType() {
			return Type.MOUSE;
		}
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
	 * @return the type of device
	 */
	public abstract Type getType();
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
	public long getPhysicalId() {
		return 0;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+getType()+"]";
	}
}

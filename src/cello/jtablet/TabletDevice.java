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
	
	public static final TabletDevice STYLUS = new Stylus();
	public static final TabletDevice STYLUS_ERASER = new StylusEraser();
	public static final TabletDevice MOUSE = new Mouse();

	public static class Stylus extends TabletDevice {
		public Support supportsPressure() {
			return Support.UNKNOWN;
		}
		public Support supportsTilt() {
			return Support.UNKNOWN;
		}
		public Support supportsTangentialPressure() {
			return Support.UNKNOWN;
		}
	}
	public static class StylusEraser extends Stylus {
	}
	public static class Mouse extends TabletDevice { 
		public Support supportsPressure() {
			return Support.NONE;
		}
		public Support supportsTilt() {
			return Support.NONE;
		}
		public Support supportsTangentialPressure() {
			return Support.NONE;
		}
	}
	
	enum Support {
		UNKNOWN,
		NONE,
		SUPPORTED
	};
	
	public abstract Support supportsPressure();
	public abstract Support supportsTilt();
	public abstract Support supportsTangentialPressure();
}

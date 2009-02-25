/**
 * 
 */
package cello.tablet.events;

import java.io.Serializable;

/**
 * @author marcello
 */
public class TabletDevice implements Serializable {

	private final Device device;

	public TabletDevice(Device device) {
		this.device = device;
	}

	/**
	 * Represents a cursor
	 */
	public static enum Device {
		/** stylus cursor */
		STYLUS,
		/** stylus eraser cursor */
		STYLUS_ERASER,
		/** mouse cursor */
		MOUSE
	}

}

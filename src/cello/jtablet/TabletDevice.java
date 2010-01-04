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

package cello.jtablet;

import java.io.Serializable;

import cello.jtablet.event.TabletEvent;

/**
 * Represents a physical input device referenced in {@link TabletEvent}s.
 * 
 * @author marcello
 */
public abstract class TabletDevice implements Serializable {

	protected TabletDevice() {}
	
	/**
	 * Tablet devices
	 */
	public enum Type {
		/** a mouse-style device */
		MOUSE,
		/** the tip of a stylus */
		STYLUS,
		/** the eraser end of a stylus */
		ERASER,
		/** unknown input device */
		UNKNOWN
	};
	

	/**
	 * Support result returned by various getSupport() methods
	 */
	public static enum Support {
		/**
		 * This feature is definitely not supported.
		 */
		NO,
		/**
		 * It is unknown whether or not this feature is supported.
		 */
		UNKNOWN,
		/**
		 * This feature is definitely supported.
		 */
		YES
	};

	/**
	 * Returns this device's type (such as stylus pen tip, stylus eraser, mouse cursor, or other).
	 * @return the device's type
	 */
	public abstract Type getType();

	/**
	 * Returns whether this device supports floating point coordinates.
	 * 
	 * @see TabletEvent#getFloatX()
	 * @see TabletEvent#getFloatY()
	 * @see TabletEvent#getPoint2D()
	 * @return floating point support
	 */
	public Support getFloatSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device supports buttons.
	 * 
	 * @see TabletEvent#getRawTabletButtonMask()
	 * @see TabletEvent#getModifiersEx()
	 * @return button support
	 */
	public Support getButtonSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device supports unique IDs.
	 * @see TabletDevice#getUniqueIdSupport()
	 * @return unique ID support
	 */
	public Support getUniqueIdSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device supports pressure sensitivity.
	 * @see TabletEvent#getPressure()
	 * @return pressure sensitivity support
	 */
	public Support getPressureSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device supports tilt orientation.
	 * @see TabletEvent#getTiltX()
	 * @see TabletEvent#getTiltY()
	 * @return tilt (from the vertical axis) support
	 */
	public Support getTiltSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device supports side pressure. (E.g. the side wheel on a Wacom airbrush tool.)
	 * @see TabletEvent#getSidePressure()
	 * @return side pressure support
	 */
	public Support getSidePressureSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device supports axis rotation.
	 * 
	 * @see TabletEvent#getRotation()
	 * 
	 * @return rotation (around the axis of the stylus) support 
	 */
	public Support getRotationSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns the device's name
	 * @return the name of this device
	 */
	public String getName() {
		return getType().name();
	}
	
	/**
	 * Returns the unique physical device ID. This can be used to uniquely identify a physical stylus on tablets that 
	 * support it (see {@link TabletDevice#getUniqueIdSupport()}, otherwise a non-unique id may be returned.
	 * 
	 * <p>When supported, this id for a pen will be consistent across sessions, and even tablets/computers. 
	 * 
	 * @see TabletDevice#getUniqueIdSupport()
	 * @return the unique id
	 */
	public String getUniqueId() {
		return getName();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+getType()+"]";
	}
}

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
 * Represents a cursor from a physical input device. All {@link TabletEvent}s will reference a 
 * {@linkplain TabletDevice}.
 * 
 * @author marcello
 */
public abstract class TabletDevice implements Serializable {


	/**
	 * Default system mouse input device
	 */
	public static final TabletDevice SYSTEM_MOUSE = new SystemDevice();

	protected TabletDevice() {}
	
	/**
	 * Tablet devices
	 */
	public enum Type {
		/** a mouse-style device */
		MOUSE,
		/** the tip of a stylus */
		STYLUS_TIP,
		/** the eraser end of a stylus */
		ERASER,
		/** unknown input device */
		UNKNOWN
	};
	

	/**
	 * A mouse-like tablet device
	 */
	private static class SystemDevice extends TabletDevice {
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
		public Support supportsSidePressure() {
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
	 * Returns this device's type (such as stylus pen tip, stylus eraser, mouse cursor, or other).
	 * @return the device's type
	 */
	public abstract Type getType();
	
	/**
	 * Returns button support of this device.
	 * @return button support
	 */
	public abstract Support supportsButtons();
	
	/**
	 * Returns device ID support of this device.
	 * @return device ID support
	 */
	public abstract Support supportsDeviceID();
	
	/**
	 * Returns pressure sensitivity support of this device.
	 * @return pressure sensitivity support
	 */
	public abstract Support supportsPressure();
	
	/**
	 * Returns tilt orientation support of this device.
	 * @return tilt (from the vertical axis) support
	 */
	public abstract Support supportsTilt();
	
	/**
	 * Returns side pressure support of this device. (E.g. the side wheel on a Wacom airbrush tool.)
	 * @return side pressure support
	 */
	public abstract Support supportsSidePressure();
	
	/**
	 * Returns axis rotation support of this device.
	 * @return rotation (around the axis of the stylus) support 
	 */
	public abstract Support supportsRotation();
	
	/**
	 * Returns the device's name
	 * @return the name of this device
	 */
	public String getName() {
		return getType().name();
	}
	/**
	 * Returns the physical device ID. This can be used to uniquely identify a physical stylus on tablets that support 
	 * it, otherwise a generic id will be returned.
	 * 
	 * @return the device ID
	 */
	public String getPhysicalId() {
		return "["+getName()+"]";
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+getType()+"]";
	}
}

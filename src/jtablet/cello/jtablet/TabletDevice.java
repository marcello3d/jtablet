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
 * Represents a physical input device. Objects of this class describe
 * the capabilities of a tablet. When a tablet does something interesting,
 * a {@link TabletEvent} is posted. Each TabletEvent references the
 * instance of this class which produced the event.
 *
 * @author marcello
 * @since 1.2.5
 */
public abstract class TabletDevice implements Serializable {
	
	/**
	 * Describes the type of tablet that this device is.
	 *
	 * @author marcello
	 * @since 1.2.5
	 */
	public enum Type {
		/**
		 * A mouse-style device. Examples include the system mouse
		 * device, "puck" tablet devices, etc.
		 *
		 * @since 1.2.5
		 */
		MOUSE,

		/**
		 * The tip-end of a tablet pen.
		 *
		 * @since 1.2.5
		 */
		STYLUS,

		/**
		 * The eraser-end of a tablet pen.
		 *
		 * @since 1.2.5
		 */
		ERASER,

		/**
		 * An unknown input device.
		 *
		 * @since 1.2.5
		 */
		UNKNOWN
	};
	
	/**
	 * This enum is used by various {@code getSupport} methods
	 * to describe if a given feature is supported by an individual
	 * device.
	 *
	 * @since 1.2.5
	 */
	public static enum Support {
		/**
		 * This feature is not supported
		 *
		 * @since 1.2.5
		 */
		NO,

		/**
		 * It is unknown whether or not this feature is supported
		 *
		 * @since 1.2.5
		 */
		UNKNOWN,

		/**
		 * This feature is supported
		 *
		 * @since 1.2.5
		 */
		YES
	};

	/**
	 * Returns this device's type (such as stylus pen tip, stylus
	 * eraser, mouse cursor, etc).
	 *
	 * @return the device's type
	 * @since 1.2.5
	 */
	public abstract Type getType();

	/**
	 * Returns whether this device can report its on-screen
	 * coordinates with sub-pixel precision.
	 *
	 * @see TabletEvent#getFloatX()
	 * @see TabletEvent#getFloatY()
	 * @see TabletEvent#getPoint2D()
	 * @return floating point support
	 * @since 1.2.5
	 */
	public Support getFloatSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device has buttons and can report
	 * when they change state.
	 * 
	 * @see TabletEvent#getRawTabletButtonMask()
	 * @see TabletEvent#getModifiersEx()
	 * @return button support
	 * @since 1.2.5
	 */
	public Support getButtonSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device reports a unique ID.
	 *
	 * @see TabletDevice#getUniqueIdSupport()
	 * @return unique ID support
	 * @since 1.2.5
	 */
	public Support getUniqueIdSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device can sense and report the
	 * amount of pressure being used. Note that devices without
	 * explicit pressure support <i>may</i> return 100% pressure
	 * when the primary button is pressed (for convenience),
	 * regardless of what this method returns.
	 *
	 * @see TabletEvent#getPressure()
	 * @return pressure sensitivity support
	 * @since 1.2.5
	 */
	public Support getPressureSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device can sense and report the
	 * amount of tilt from the vertical axis.
	 *
	 * @see TabletEvent#getTiltX()
	 * @see TabletEvent#getTiltY()
	 * @return tilt (from the vertical axis) support
	 * @since 1.2.5
	 */
	public Support getTiltSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device can sense and can report the
	 * amount of "side pressure" (e.g. the side wheel on a Wacom
	 * airbrush tool).
	 *
	 * @see TabletEvent#getSidePressure()
	 * @return side pressure support
	 * @since 1.2.5
	 */
	public Support getSidePressureSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns whether this device can sense and report the amount
	 * it is rotated about its own axis.
	 * 
	 * @see TabletEvent#getRotation()
	 * @return rotation (around the axis of the stylus) support
	 * @since 1.2.5
	 */
	public Support getRotationSupport() {
		return Support.UNKNOWN;
	}
	
	/**
	 * Returns the name of the device. This name may be provided by
	 * the system, or any identifier created by the {@link TabletDriver}.
	 *
	 * @return the name of this device
	 * @since 1.2.5
	 */
	public String getName() {
		return getType().name();
	}
	
	/**
	 * Returns the unique physical device ID. This can be used to
	 * uniquely identify a physical stylus on tablets that support
	 * it.
	 * 
	 * <p>When supported, this id for a pen will be consistent across
	 * sessions, and even tablets/computers. If not supported, a non-
	 * unique ID may be returned.</p>
	 * 
	 * @see TabletDevice#getUniqueIdSupport()
	 * @return the unique id
	 * @since 1.2.5
	 */
	public String getUniqueId() {
		return getName();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+getType()+"]";
	}
}

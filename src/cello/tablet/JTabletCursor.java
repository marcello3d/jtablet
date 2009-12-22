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

package cello.tablet;

import java.awt.Toolkit;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.event.TabletEvent;

/**
 * Object representation of a physical cursor on the tablet.
 * <p>
 * For each input of a stylus, mouse, airbrush, etc. on the tablet, a separate
 * JTabletCursor object is created. The same stylus could have multiple
 * JTabletCursor objects associated. They should have the same physicalId,
 * assuming the tablet supports this feature.
 * <p>
 * For example, the pen tip, and eraser will be separate JTabletCursor objects,
 * but getPhysicalId() on both will return the same value.
 * <p>
 * 
 * <pre>
 * if(jtablet.hasCursor()) {
 *     JTabletCursor cursor = jtablet.getCursor();
 *     setPosition(cursor.getData(JTabletCursor.DATA_X), cursor
 *             .getData(JTabletCursor.DATA_Y));
 * }
 * </pre>
 * 
 * @version 0.9.1 9/7/2003
 * @since 0.9.1
 * @author Marcello Bastea-Forte
 * @see cello.tablet.JTablet
 * @see cello.tablet.JTabletException
 */

@Deprecated
public class JTabletCursor {
    /**
     * Data type ID for getting the cursor id of this cursor.
     */
    public static final int DATA_CURSOR = 0;

    /**
     * Data type ID for getting the bitmask of buttons for this cursor.
     */
    public static final int DATA_BUTTONS = 1;

    /**
     * Data type ID for getting x position for this cursor.
     */
    public static final int DATA_X = 2;

    /**
     * Data type ID for getting y position for this cursor.
     */
    public static final int DATA_Y = 3;

    /**
     * Data type ID for getting z position for this cursor.
     */
    public static final int DATA_Z = 4;

    /**
     * Data type ID for getting pressure value for this cursor.
     */
    public static final int DATA_PRESSURE = 5;

    /**
     * Data type ID for getting the tangent pressure for this cursor (airbrush
     * fingerwheel).
     */
    public static final int DATA_TANGENT_PRESSURE = 6;

    /**
     * Data type ID for getting orientation azimuth for this cursor (full 360
     * degrees clockwise rotation about the z axis).
     */
    public static final int DATA_ORIENTATION_AZIMUTH = 7;

    /**
     * Data type ID for getting orientation altitude angle for this cursor
     * (between 90 and 0 degrees, although physical range may be less). Also,
     * this number will be negative if the eraser is being used.
     */
    public static final int DATA_ORIENTATION_ALTITUDE = 8;

    /**
     * Data type ID for getting orientation twist angle for this cursor
     * (clockwise rotation about the cursor's major access).
     */
    public static final int DATA_ORIENTATION_TWIST = 9;

    /**
     * Data type ID for getting orientation rotation pitch (currently
     * unsupported).
     */
    public static final int DATA_ROTATION_PITCH = 10;

    /**
     * Data type ID for getting orientation rotation roll (currently
     * unsupported).
     */
    public static final int DATA_ROTATION_ROLL = 11;

    /**
     * Data type ID for getting orientation rotation yaw (currently
     * unsupported).
     */
    public static final int DATA_ROTATION_YAW = 12;

    /**
     * Data type ID for getting the maximum number of data types.
     */
    public static final int DATA_ARRAY_SIZE = 13;

    /**
     * Unknown Cursor Type.
     * 
     * @see #getCursorType
     * @see #getCursorTypeSpecific
     */
    public static final int TYPE_UNKNOWN = 0;

    /**
     * Pen tip cursor type. (A cursor's tip.)
     * 
     * @see #getCursorType
     */
    public static final int TYPE_PEN_TIP = 1;

    /**
     * Pen eraser cursor type. (A cursor's eraser.)
     * 
     * @see #getCursorType
     */
    public static final int TYPE_PEN_ERASER = 2;

    /**
     * Puck cursor type. (A mouse, lens cursor, or similar.)
     * 
     * @see #getCursorType
     */
    public static final int TYPE_PUCK = 3;

    /**
     * Stylus specific cursor type. (A regular stylus pen.)
     * 
     * @see #getCursorTypeSpecific
     */
    public static final int TYPE_STYLUS = 1;

    /**
     * Airbrush specific cursor type. (A airbrush stylus, supporting tangent
     * pressure.)
     * 
     * @see #getCursorTypeSpecific
     */
    public static final int TYPE_AIRBRUSH = 2;

    /**
     * 4-D Mouse specific cursor type. (A mouse with extended buttons and the
     * ability to retrieve rotation.)
     * 
     * @see #getCursorTypeSpecific
     */
    public static final int TYPE_4DMOUSE = 3;

    /**
     * 2D Mouse specific cursor type. (A regular mouse.)
     * 
     * @see #getCursorTypeSpecific
     */
    public static final int TYPE_LENS_CURSOR = 4;

	private static final int PRESSURE_MAX = 1024*5;

	private final TabletEvent event;

	/**
	 * Constructs a new JTabletCursor
	 * 
	 * @param event
	 */
    /*package*/ JTabletCursor(TabletEvent event) {
		this.event = event;
	}

	/**
     * Retrieves the name generated by the tablet driver. This may be null.
     * 
     * @return the name
     */
    public String getName() {
        return event.getDevice().getName();
    }

    /**
     * Retrieves the 32bit physicalId of this cursor from tablet driver. This
     * may be 0.
     * 
     * @return the id int
     */
    public int getPhysicalId() {
        return 0;
    }

    /**
     * Retrieves the general type of this cursor. Possible values are:
     * TYPE_UNKNOWN, TYPE_PEN_TIP, TYPE_PEN_ERASER, TYPE_PUCK.
     * 
     * @return the type
     */
    public int getCursorType() {
    	TabletDevice device = event.getDevice();
    	switch (device.getType()) {
	    	case MOUSE:
	    		return TYPE_PUCK;
	    	case ERASER:
	    		return TYPE_PEN_ERASER;
	    	case STYLUS_TIP:
	    		return TYPE_PEN_TIP;
	    	default:
	    		return TYPE_UNKNOWN;
    	}
    }

    /**
     * Retrieves the specific type of this cursor. Possible values are:
     * TYPE_UNKNOWN, TYPE_STYLUS, TYPE_AIRBRUSH, TYPE_4DMOUSE, TYPE_LENS_CURSOR.
     * 
     * @return the type
     */
    public int getCursorTypeSpecific() {
        return TYPE_UNKNOWN;
    }

    /**
     * Retrieves the time of the last poll. This number isn't necessarily
     * related to <code>System.currentTimeMillis()</code>.
     * 
     * @since 0.9.2
     * @return the time in milliseconds
     */
    public long getCursorTime() {
        return event.getWhen();
    }

    /**
     * Retrieves the <i>raw integer</i> data for the specified DATA_<i>XXX</i>
     * type. You will most likely want to use getDataMaximum and getDataMinimum
     * to find out the range for these raw values.
     * 
     * @see #getDataMaximum(int)
     * @see #getDataMinimum(int)
     * @param type A JTabletCursor.DATA_<i>XXX...</i> constant.
     * @return the raw data
     */
    public int getData(int type) {
    	switch (type) {
	    	case DATA_BUTTONS:
	    		return event.getButton();
	    	case DATA_PRESSURE:
	    		return getPressure();
	    	case DATA_TANGENT_PRESSURE:
	    		return (int)Math.round(event.getSidePressure()*PRESSURE_MAX);
	    	case DATA_X:
	    		return event.getX();
	    	case DATA_Y:
	    		return event.getY();

	    		// NOT SUPPORTED
//	    	case DATA_CURSOR:
//	    	case DATA_Z:
	    	case DATA_ORIENTATION_ALTITUDE:
	            if (getCursorType() == JTabletCursor.TYPE_PEN_ERASER) {
	            	return -1;
	            }
	            return 0;
//	    	case DATA_ORIENTATION_AZIMUTH:
//	    	case DATA_ORIENTATION_TWIST:
//	    	case DATA_ROTATION_PITCH:
//	    	case DATA_ROTATION_ROLL:
//	    	case DATA_ROTATION_YAW:
	    	default:
	    		return 0;
    	}
    }

    /**
     * Retrieves the tablet's maximum value for the specified data type.
     * 
     * @param type A JTabletCursor.DATA_<i>XXX...</i> constant.
     * @return the maximum value
     */
    public int getDataMaximum(int type) {
    	switch (type) {
	    	case DATA_BUTTONS:
	    		// doesn't make sense
	    		return 0;
	    	case DATA_PRESSURE:
	    		return PRESSURE_MAX;
	    	case DATA_TANGENT_PRESSURE:
	    		return PRESSURE_MAX;
	    	case DATA_X:
	    		return Toolkit.getDefaultToolkit().getScreenSize().width;
	    	case DATA_Y:
	    		return Toolkit.getDefaultToolkit().getScreenSize().height;
	
	    		// NOT SUPPORTED
	//    	case DATA_CURSOR:
	//    	case DATA_Z:
	//    	case DATA_ORIENTATION_ALTITUDE:
	//    	case DATA_ORIENTATION_AZIMUTH:
	//    	case DATA_ORIENTATION_TWIST:
	//    	case DATA_ROTATION_PITCH:
	//    	case DATA_ROTATION_ROLL:
	//    	case DATA_ROTATION_YAW:
	    	default:
	    		return 0;
		}
    }

    /**
     * Retrieves the tablet's minimum value for the specified data type.
     * 
     * @param type A JTabletCursor.DATA_<i>XXX...</i> constant.
     * @return the minimum value
     */
    public int getDataMinimum(int type) {
    	switch (type) {
    	case DATA_BUTTONS:
    		return 0;
    	case DATA_PRESSURE:
    		return 0;
    	case DATA_TANGENT_PRESSURE:
    		return 0;
    	case DATA_X:
    		return 0;
    	case DATA_Y:
    		return 0;

    		// NOT SUPPORTED
//    	case DATA_CURSOR:
//    	case DATA_Z:
//    	case DATA_ORIENTATION_ALTITUDE:
//    	case DATA_ORIENTATION_AZIMUTH:
//    	case DATA_ORIENTATION_TWIST:
//    	case DATA_ROTATION_PITCH:
//    	case DATA_ROTATION_ROLL:
//    	case DATA_ROTATION_YAW:
    	default:
    		return 0;
	}
    }

    /**
     * Returns if a specified data type is supported by the cursor. As of
     * version 0.9.1, this information is not specific to the actual cursor, and
     * a return value of true, does <i>not</i> guarantee that the cursor or
     * tablet actually supports this value.
     * 
     * @param type A JTabletCursor.DATA_<i>XXX...</i> constant.
     * @return if specified data type is supported
     */
    public boolean isDataSupported(int type) {
    	switch (type) {
	    	case DATA_PRESSURE:
	    		return event.getDevice().supportsPressure() != Support.NONE;
	    	case DATA_TANGENT_PRESSURE:
	    		return event.getDevice().supportsSidePressure() != Support.NONE;
	    	case DATA_BUTTONS:
	    	case DATA_X:
	    	case DATA_Y:
	    		return true;
	
	    		// NOT SUPPORTED
	//    	case DATA_CURSOR:
	//    	case DATA_Z:
	//    	case DATA_ORIENTATION_ALTITUDE:
	//    	case DATA_ORIENTATION_AZIMUTH:
	//    	case DATA_ORIENTATION_TWIST:
	//    	case DATA_ROTATION_PITCH:
	//    	case DATA_ROTATION_ROLL:
	//    	case DATA_ROTATION_YAW:
	    	default:
	    		return false;
		}
    }
    
    /**
     * A helper function that returns the translated raw pressure. This uses the
     * following code:
     * 
     * <pre>
     * getData(DATA_PRESSURE) - getDataMinimum(DATA_PRESSURE)
     * </pre>
     * 
     * @see #getPressureFloat
     * @see #getPressureExtent
     * @return the pressure range starting at 0 and going to
     *         JTabletCursor.getPressureExtent()-1
     */
    public final int getPressure() {
        return (int)Math.round(event.getPressure()*PRESSURE_MAX);
    }

    /**
     * A helper function that returns the maximum pressure. This uses the
     * following code:
     * 
     * <pre>
     * getDataMaximum(DATA_PRESSURE) - getDataMinimum(DATA_PRESSURE)
     * </pre>
     * 
     * @see #getPressure
     * @return the maximum pressure value
     */
    public final int getPressureExtent() {
        return PRESSURE_MAX;
    }

    /**
     * A helper function that returns a percentage based pressure range. This
     * uses the following code:
     * 
     * <pre>
     *      ((float)getPressure())/getPressureExtent();
     * </pre>
     * 
     * @see #getPressure
     * @see #getPressureExtent
     * @return the pressure range as a float [0,1]
     */
    public final float getPressureFloat() {
        return event.getPressure();
    }

    /**
     * A helper function that returns a percentage based on minimum, maximum,
     * and current value.
     * 
     * @param type A JTabletCursor.DATA_<i>XXX...</i> constant.
     * @return the value range as a float [0,1]
     */
    public final float getDataFloat(int type) {
        return ((float)(getData(type) - getDataMinimum(type)))
                / (getDataMaximum(type) - getDataMinimum(type));
    }
}

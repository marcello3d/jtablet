/**
 * 
 */
package cello.jtablet.impl;

import cello.jtablet.TabletDevice;

/**
 * Specifies a tablet device using constructor arguments.
 * 
 * @author marcello
 */
public abstract class AbstractTabletDevice extends TabletDevice {
	private final Type type;
	private final String uniqueId;
	private final Support supportsButtons;
	private final Support supportsDeviceId;
	private final Support supportsPressure;
	private final Support supportsRotation;
	private final Support supportsSidePressure;
	private final Support supportsTiltXY;
	
	

	protected AbstractTabletDevice(Type type, String uniqueId,
			Support supportsButtons, Support supportsDeviceId,
			Support supportsPressure, Support supportsRotation,
			Support supportsSidePressure, Support supportsTiltXY) {
		super();
		this.type = type;
		this.uniqueId = uniqueId;
		this.supportsButtons = supportsButtons;
		this.supportsDeviceId = supportsDeviceId;
		this.supportsPressure = supportsPressure;
		this.supportsRotation = supportsRotation;
		this.supportsSidePressure = supportsSidePressure;
		this.supportsTiltXY = supportsTiltXY;
	}
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+getType()+"-"+getPhysicalId()+"]";
	}
	@Override
	public Type getType() {
		return type;
	}
	@Override
	public String getPhysicalId() {
		return uniqueId;
	}

	@Override
	public Support supportsButtons() {
		return supportsButtons;
	}
	@Override
	public Support supportsDeviceID() {
		return supportsDeviceId;
	}
	@Override
	public Support supportsPressure() {
		return supportsPressure;
	}
	@Override
	public Support supportsRotation() {
		return supportsRotation;
	}

	@Override
	public Support supportsSidePressure() {
		return supportsSidePressure;
	}

	@Override
	public Support supportsTilt() {
		return supportsTiltXY;
	}
	
}
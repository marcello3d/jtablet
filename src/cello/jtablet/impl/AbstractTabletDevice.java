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
	private final String name;
	private final String uniqueId;
	private final Support buttonSupport;
	private final Support uniqueIdSupport;
	private final Support pressureSupport;
	private final Support rotationSupport;
	private final Support sidePressureSupport;
	private final Support tiltSupport;
	private final Support floatSupport;
	
	

	protected AbstractTabletDevice(Type type, String name,
			String uniqueId, Support floatSupport,
			Support buttonSupport, Support uniqueIdSupport,
			Support pressureSupport, Support rotationSupport,
			Support sidePressureSupport, Support tiltSupport) {
		super();
		this.type = type;
		this.name = name;
		this.uniqueId = uniqueId;
		this.floatSupport = floatSupport;
		this.buttonSupport = buttonSupport;
		this.uniqueIdSupport = uniqueIdSupport;
		this.pressureSupport = pressureSupport;
		this.rotationSupport = rotationSupport;
		this.sidePressureSupport = sidePressureSupport;
		this.tiltSupport = tiltSupport;
	}
	@Override
	public String toString() {
		return getClass().getSimpleName()+"["+getType()+"-"+getUniqueId()+"]";
	}
	@Override
	public String getName() {
		return name != null ? name : super.getName();
	}
	@Override
	public Type getType() {
		return type;
	}
	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public Support getFloatSupport() {
		return floatSupport;
	}
	@Override
	public Support getButtonSupport() {
		return buttonSupport;
	}
	@Override
	public Support getUniqueIdSupport() {
		return uniqueIdSupport;
	}
	@Override
	public Support getPressureSupport() {
		return pressureSupport;
	}
	@Override
	public Support getRotationSupport() {
		return rotationSupport;
	}
	@Override
	public Support getSidePressureSupport() {
		return sidePressureSupport;
	}
	@Override
	public Support getTiltSupport() {
		return tiltSupport;
	}
	
}
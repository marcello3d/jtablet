package cello.jtablet.impl;

import cello.jtablet.TabletDevice;


/**
 * A mouse-like tablet device
 */
public class SystemDevice extends TabletDevice {
	

	/**
	 * Default system mouse input device
	 */
	public static final TabletDevice INSTANCE = new SystemDevice();

	
	@Override
	public Type getType() {
		return Type.UNKNOWN;
	}
	@Override 
	public Support getPressureSupport() {
		return Support.NO;
	}
	@Override
	public Support getTiltSupport() {
		return Support.NO;
	}
	@Override
	public Support getSidePressureSupport() {
		return Support.NO;
	}
	@Override
	public Support getRotationSupport() {
		return Support.NO;
	}
	@Override
	public Support getButtonSupport() {
		return Support.YES;
	}
	@Override
	public Support getUniqueIdSupport() {
		return Support.NO;
	}
	@Override
	public Support getFloatSupport() {
		return Support.NO;
	}
}
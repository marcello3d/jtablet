package cello.jtablet.impl.platform;

import cello.jtablet.impl.CursorDevice;

/**
 * This interface specifies cursor devices that load native code
 * 
 * @author marcello
 */
public interface NativeCursorDevice extends CursorDevice {

	/**
	 * Loads the actual device
	 * 
	 * @throws NativeDeviceException
	 */
	public void load() throws NativeDeviceException;
	/**
	 * @param os the os string
	 * @return true if the given os is supported by this native device
	 */
	public boolean isSystemSupported(String os);
}

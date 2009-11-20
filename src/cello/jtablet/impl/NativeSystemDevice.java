package cello.jtablet.impl;

/**
 * This interface specifies cursor devices that load native code
 * 
 * @author marcello
 */
public interface NativeSystemDevice extends CursorDevice {

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
	/**
	 * @return true if this device handles all mouse input
	 */
	public boolean overridesMouseListener();
}

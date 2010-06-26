package cello.jtablet.impl;

/**
 * This class is currently only used by the Windows driver, because you can't make a universal binary, and there's no
 * architecture-specific naming convention for DLLs.
 * 
 * @author marcello
 */
public enum Architecture {
	/**
	 * Used by everything exception 64bit Windows.
	 */
	DEFAULT,
	/**
	 * Used by 64-bit Windows
	 */
	X64;
}

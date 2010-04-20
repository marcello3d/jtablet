package cello.jtablet.impl;

/**
 * {@code NativeLoaderException} provides a class of objects that can
 * be thrown/caught and indicate a problem with loading native code.
 * This may be thrown, for instance, if the required native library is
 * not found.
 *
 * @author marcello
 */
public class NativeLoaderException extends Exception {
	/**
	 * 
	 */
	public NativeLoaderException() {
	}
	
	/**
	 * @param message
	 */
	public NativeLoaderException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public NativeLoaderException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public NativeLoaderException(String message, Throwable cause) {
		super(message, cause);
	}
}

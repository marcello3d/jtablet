package cello.jtablet.impl;

/**
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
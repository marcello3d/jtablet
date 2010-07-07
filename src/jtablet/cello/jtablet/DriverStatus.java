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

/**
 * This class represents the status of a {@link TabletDriver}. It can
 * be used to diagnose why a given driver is not working on a system.
 * 
 * @author marcello
 */
public class DriverStatus {

	/**
	 * Describes in general the state of the tablet driver.
	 * 
	 * @author marcello
	 * @since 1.2.5
	 */
	public static enum State {
		/**
		 * Driver code was successfully loaded.
		 *
		 * @since 1.2.5
		 */
		LOADED,
		
		/**
		 * A exception occurred loading the native library.
		 * This can happen if the application does not have
		 * permission to load native libraries, if the native
		 * libraries cannot be found, or if some linked library
		 * required by the native code cannot be found
		 * (e.g. wintab32.dll not available).
		 *
		 * @since 1.2.5
		 */
		NATIVE_EXCEPTION,
		
		/**
		 * Some other exception occurred loading the driver.
		 *
		 * @since 1.2.5
		 */
		UNEXPECTED_EXCEPTION,
		
		/**
		 * The driver does not support running under the
		 * current OS. For instance, the WinTabDriver
		 * only supports the Windows operating system.
		 *
		 * @since 1.2.5
		 */
		UNSUPPORTED_OS
	}
	
	private final Throwable throwable;
	private final State state;
	
	/**
	 * Builds a {@link DriverStatus} with a given state without
	 * an exception.
	 *
	 * @param state the state of the driver
	 * @since 1.2.5
	 */
	public DriverStatus(State state) {
		this(state,null);
	}
	
	/**
	 * Builds a {@link DriverStatus} with a given state and exception.
	 *
	 * @param state the state of the driver
	 * @param throwable exception thrown in the driver load process, or
	 *        {@code null} if not applicable
	 * @since 1.2.5
	 */
	public DriverStatus(State state, Throwable throwable) {
		this.state = state;
		this.throwable = throwable;
	}
	
	/**
	 * Returns the generalized state of the driver. This can be used
	 * for a high-level understanding of if the driver
	 * failed to load and why.
	 *
	 * @return the state of the driver.
	 * @since 1.2.5
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Returns the {@link Throwable} associated with any driver load
	 * failure, or null if not applicable.
	 *
	 * @return an exception thrown in the driver load process, or
	 *         {@code null} if not applicable
	 * @since 1.2.5
	 */
	public Throwable getThrowable() {
		return throwable;
	}
	
	@Override
	public String toString() {
		return getClass().getName()+"[state="+state+",throwable="+throwable+"]";
	}

}

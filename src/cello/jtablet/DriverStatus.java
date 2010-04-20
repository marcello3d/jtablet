/**
 * FIXME: Need license header
 */
package cello.jtablet;

/**
 * This class represents the status of the tablet driver. It can be used to determine partial or incomplete 
 * installations of JTablet, insufficient security permissions, errors
 * 
 * @author marcello
 */
public class DriverStatus {

	/**
	 * A generalized state of the native tablet driver. 
	 * 
	 * @author marcello
	 */
	public enum State {
		/**
		 * Successfully loaded a native driver. 
		 */
		LOADED,
		
		/**
		 * A exception occurred loading the native library. This can happen if the native libraries cannot be found, 
		 * or if some linked library required by the native code cannot be found (e.g. wintab32.dll not available).
		 */
		NATIVE_EXCEPTION, 
		
		/**
		 * Some other exception occurred loading the native driver.
		 */
		UNEXPECTED_EXCEPTION,
		
		/**
		 * JTablet isn't able to handle the given OS.
		 */
		UNSUPPORTED_OS,
		
		/**
		 * JTablet isn't installed on this system.
		 */
		NOT_INSTALLED
	}
	
	private final Throwable throwable;
	private final State state;
	
	/**
	 * Builds a {@linkplain DriverStatus} with a given state without an exception.
	 * @param state the driver state
	 */
	public DriverStatus(State state) {
		this(state,null);
	}
	
	/**
	 * Builds a {@linkplain DriverStatus} with a given state and exception
	 * @param state
	 * @param throwable
	 */
	public DriverStatus(State state, Throwable throwable) {
		this.state = state;
		this.throwable = throwable;
	}
	
	/**
	 * Returns the generalized state of the driver. This can be used for a high-level understanding of if the driver 
	 * failed to load and why. 
	 * @return the current state of the driver.
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Returns the {@link Throwable} associated with any driver load failure, or null if not applicable.
	 * @return an exception thrown in the driver load process
	 */
	public Throwable getThrowable() {
		return throwable;
	}
	@Override
	public String toString() {
		return getClass().getName()+"[state="+state+",throwable="+throwable+"]";
	}

}
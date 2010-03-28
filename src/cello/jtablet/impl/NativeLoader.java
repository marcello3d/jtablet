package cello.jtablet.impl;

/**
 * @author marcello
 */
public class NativeLoader {
	
	private static final String LIBNAME = "jtablet2";
	
	/**
	 */
	/*package*/ NativeLoader() {
	}

	/*package*/ void load() throws NativeLoaderException {
		try {
			System.loadLibrary(LIBNAME);
		} catch (Throwable t) {
			// For security reasons, we don't want to leak an exception with the home folder name, so swap it out with ~
			String exceptionString = t.toString();
			String homeFolder = System.getProperty("user.home");
			if (exceptionString.contains(homeFolder)) {
				throw new NativeLoaderException(exceptionString.replace(homeFolder, "~"));
			}
			throw new NativeLoaderException(t);
		}
	}

}

package cello.jtablet.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author marcello
 */
public class NativeLoader {
	
	private static final String LIBNAME = "jtablet2";
	private static final String LIBNAME64 = "jtablet2-64";
	private final Map<String,String> architectureMap = new HashMap<String,String>();
	
	/**
	 */
	/*package*/ NativeLoader() {
	}

	/*package*/ void load(Architecture arch) throws NativeLoaderException {
		try {
			String libraryName = LIBNAME;
			if (arch == Architecture.X64) { 
				libraryName = LIBNAME64;
			}
			System.loadLibrary(libraryName);
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

	
	/*package*/ void addArchitectures(Map<String, String> architectureMap) {
		this.architectureMap.putAll(architectureMap);
	}
}

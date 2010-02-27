package cello.jtablet.impl;

import java.io.File;

/**
 * @author marcello
 */
public class NativeLoader {
	
	private final static String JTABLET_FOLDER_NAME = ".jtablet/";
	private static final String LIBNAME = "jtablet2";
	
	/**
	 */
	/*package*/ NativeLoader() {
	}

	private String getLibraryPath(String homeFolder) throws NativeLoaderException {
		File libraryDirectory = new File(homeFolder + "/" + JTABLET_FOLDER_NAME);
		if (!libraryDirectory.exists()) {
			throw new NativeLoaderException("Cannot find ~/" + JTABLET_FOLDER_NAME);
		}
		String libraryName = System.mapLibraryName(LIBNAME);
		File nativeFile = new File(libraryDirectory, libraryName);
		System.out.println("Attempting to load: "+nativeFile);
		if (!nativeFile.exists()) {
			throw new NativeLoaderException("Cannot find native library: ~/" + JTABLET_FOLDER_NAME + libraryName);
		}
		return nativeFile.getPath();
	}

	/*package*/ void load() throws NativeLoaderException {
		String homeFolder = System.getProperty("user.home");
		try {
			System.load(getLibraryPath(homeFolder));
		} catch (Throwable t) {
			// For security reasons, we don't want to leak an exception with the home folder name, so swap it out with ~
			String exceptionString = t.toString();
			if (exceptionString.contains(homeFolder)) {
				throw new NativeLoaderException(exceptionString.replace(homeFolder, "~"));
			}
			throw new NativeLoaderException(t);
		}
	}

}

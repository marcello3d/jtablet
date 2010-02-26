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

	/**
	 * @return the path to the library
	 * @throws NativeLoaderException
	 */
	public String getLibraryPath() throws NativeLoaderException {
		File libraryDirectory = new File(System.getProperty("user.home") + "/" + JTABLET_FOLDER_NAME);
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

	public void load() throws NativeLoaderException {
		System.load(getLibraryPath());
	}

}

package cello.jtablet.impl;

import java.io.File;

/**
 * @author marcello
 */
public class NativeLoader {
	
	private final static String JTABLET_FOLDER_NAME = ".jtablet/";
	private File libraryDirectory; 
	
	/**
	 * @throws Exception
	 */
	NativeLoader() throws Exception {
		libraryDirectory = new File(System.getProperty("user.home") + "/" + JTABLET_FOLDER_NAME);
		if (!libraryDirectory.exists()) {
			throw new Exception("Cannot find ~/" + JTABLET_FOLDER_NAME);
		}
	}

	/**
	 * @throws Exception
	 */
	public void load() throws Exception {
		String libraryName = System.mapLibraryName("jpen");
		File nativeFile = new File(libraryDirectory, libraryName);
		if (!nativeFile.exists()) {
			throw new Exception("Cannot find native library: ~/" + JTABLET_FOLDER_NAME + libraryName);
		}
		System.load(nativeFile.getPath());
	}
	
	/**
	 * @author marcello
	 */
	public static class Exception extends java.lang.Exception {
		/**
		 * 
		 */
		public Exception() {
		}
		
		/**
		 * @param message
		 */
		public Exception(String message) {
			super(message);
		}
		
		/**
		 * @param cause
		 */
		public Exception(Throwable cause) {
			super(cause);
		}
		
		/**
		 * @param message
		 * @param cause
		 */
		public Exception(String message, Throwable cause) {
			super(message, cause);
		}
	}

}

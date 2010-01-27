package cello.jtablet.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * @author marcello
 */
public class NativeLoader {
	
	private final static String JTABLET_FOLDER_NAME = ".jtablet/";
	private File libraryDirectory; 
	private ClassLoader classLoader;
	
	/**
	 * @throws Exception
	 */
	/*package*/ NativeLoader() throws Exception {
		libraryDirectory = new File(System.getProperty("user.home") + "/" + JTABLET_FOLDER_NAME);
		if (!libraryDirectory.exists()) {
			throw new Exception("Cannot find ~/" + JTABLET_FOLDER_NAME);
		}
		try {
			
			ClassLoader loader = getClass().getClassLoader();
			URL[] urls = new URL[]{
					new File(libraryDirectory,"jtablet-jpen.jar").toURI().toURL(),
					new File(libraryDirectory,"jpen.jar").toURI().toURL()
			};
			classLoader = new URLClassLoader(urls,loader);
		} catch (MalformedURLException e) {
			throw new Exception("Could not load jpen.jar: MalformedURLException");
		}
	}

	/**
	 * @throws Exception
	 */
	public String getLibraryPath() throws Exception {
		String libraryName = System.mapLibraryName("jpen");
		File nativeFile = new File(libraryDirectory, libraryName);
		System.out.println("Attempting to load: "+nativeFile);
		if (!nativeFile.exists()) {
			throw new Exception("Cannot find native library: ~/" + JTABLET_FOLDER_NAME + libraryName);
		}
		return nativeFile.getPath();
	}
	
	/**
	 * @param name
	 * @return the class
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	/*package*/ Class<NativeTabletManager> loadClass(String name) throws ClassNotFoundException {
		Class<NativeTabletManager> nativeTabletManagerClass = NativeTabletManager.class;
		String className = "cello.jtablet.impl.jpen."+name;
		Class<?> clazz = classLoader.loadClass(className);
		System.out.println("classloader for "+clazz+" is: "+clazz.getClassLoader());
		if (nativeTabletManagerClass.isAssignableFrom(clazz)) {
			return (Class<NativeTabletManager>) clazz;
		}
		throw new ClassCastException(className+" is not a "+nativeTabletManagerClass.getName());
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

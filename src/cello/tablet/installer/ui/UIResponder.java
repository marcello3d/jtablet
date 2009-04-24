package cello.tablet.installer.ui;

import java.io.File;
import java.util.List;

public interface UIResponder {

	/**
	 * @param jarDirectory 
	 * @param libraryDirectory
	 * @throws IllegalArgumentException 
	 */
	public void useDirectories(File jarDirectory, File libraryDirectory) throws IllegalArgumentException;
}

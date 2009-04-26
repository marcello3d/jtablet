package cello.jtablet.installer.platforms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cello.jtablet.installer.JTabletInstaller;

/**
 * @author marcello
 *
 */
public abstract class OSInstaller {

	private final JTabletInstaller installer;

	protected OSInstaller(JTabletInstaller installer) {
		this.installer = installer;
	}

	protected void addLogMessage(String s) {
		installer.addLogMessage(s);
	}
	
	/**
	 * @return true if the given installer works on the current system
	 */
	public abstract boolean isCompatible();

	/**
	 */
	public abstract void install();
	
	/**
	 * @return a prioritized list of directories to install library files to
	 */
	public List<File> getLibraryInstallDirectories() {

		List<File> directories = new ArrayList<File>();

		installer.addLogMessage("Looking for Java Extension folders...");

		String separator = System.getProperty("path.separator");
		String libs[] = System.getProperty("java.library.path").split("\\Q"+separator);
		
		for (String s : libs) {
			// Don't try to install to "."
			if (s.equals(".")) {
				continue;
			}
			File f = new File(s);
			try {
				installer.addLogMessage("\tLibrary dir = "+f.getCanonicalPath()+" ("+f.canWrite()+")");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (f.isDirectory() && f.canWrite()) {
				directories.add(f);
			}
		}
		
		return directories;
	}
	/**
	 * @return a prioritized list of directories to install jars to
	 */
	public List<File> getJarInstallDirectories() {

		List<File> directories = new ArrayList<File>();

		installer.addLogMessage("Looking for Java Extension folders...");

		String separator = System.getProperty("path.separator");
		String jarDirs[] = System.getProperty("java.ext.dirs").split("\\Q"+separator);
		
		for (String s : jarDirs) {;
			File f = new File(s);
			installer.addLogMessage("\tExtension dir = "+s+" ("+f.canWrite()+")");
			if (f.isDirectory() && f.exists() && f.canWrite()) {
				directories.add(f);
			}
		}
		return directories;
	}
	
	/**
	 * @return a list of library files to download/install
	 */
	public abstract String[] getLibraryFiles();
	
	/**
	 * @return a list of jar files to download/install
	 */
	public String[] getJarFiles() {
		return new String[] { "jtablet2.jar", "jpen-2.jar" };
	}

//	protected boolean installTo(File destination) {
//		installer.addLogMessage("Installing to "+destination.getAbsolutePath());
//		for (String file : getLibraryFiles()) {
//			File f = new File(".",file);
//			if (f.renameTo(new File(destination,file))) {
//				
//			} else {
//				installer.addLogMessage("Could not move file");
//			}
//		}
//		return false;
//	}

}

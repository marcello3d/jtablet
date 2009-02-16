package cello.tablet.installer;

import java.io.File;

/**
 * @author marcello
 *
 */
public abstract class OSInstaller {

	/**
	 * @return true if the given installer works on the current system
	 */
	public abstract boolean isCompatible();

	/**
	 * @param installer
	 */
	public abstract void install(JTabletInstaller installer);
	
	public abstract String[] getFiles();

	protected boolean installTo(JTabletInstaller installer, File destination) {
		installer.addLine("Installing to "+destination.getAbsolutePath());
		for (String file : getFiles()) {
			File f = new File(".",file);
			if (f.renameTo(new File(destination,file))) {
				
			} else {
				installer.addLine("Could not move file");
			}
		}
		return false;
	}
}

package cello.jtablet.installer.platforms;

import cello.jtablet.installer.JTabletInstaller;


/**
 * @author marcello
 *
 */
public class WindowsInstaller extends OSInstaller {

	public WindowsInstaller(JTabletInstaller installer) {
		super(installer);
	}

	@Override
	public boolean isCompatible() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	@Override
	public void install() {
		
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		addLogMessage("Detected Java "+javaVersion+" ("+javaVendor+")");

	}

	@Override
	public String[] getLibraryFiles() {
		return new String[] {
			"jpen-2.dll"
		};
	}

}

package cello.jtablet.installer;


/**
 * @author marcello
 *
 */
public class MacOSXInstaller extends OSInstaller {

	protected MacOSXInstaller(JTabletInstaller installer) {
		super(installer);
	}

	@Override
	public boolean isCompatible() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
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
			"libjpen-2.jnilib"
		};
	}

}

package cello.tablet.installer;

import java.io.File;

/**
 * @author marcello
 *
 */
public class MacOSXInstaller extends OSInstaller {

	@Override
	public boolean isCompatible() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
	}

	@Override
	public void install(JTabletInstaller installer) {
		
		String javaVersion = System.getProperty("java.version");
		String javaVendor = System.getProperty("java.vendor");
		installer.addLine("Detected Java "+javaVersion+" ("+javaVendor+")");
		
		

		String separator = System.getProperty("path.separator");
		String dirs[] = System.getProperty("java.ext.dirs").split("\\Q"+separator);
		String libs[]= System.getProperty("java.library.path").split("\\Q"+separator);
		
		String jarDir = null;
		String libDir = null;
		for (String s : libs) {
			File f = new File(s);
			installer.addLine("Library dir = "+s+" ("+f.canWrite()+")");
			if (!s.equals(".") && f.canWrite()) {
				libDir = s;
				break;
			}
		}
		
		for (String s : dirs) {;
			File f = new File(s);
			installer.addLine("Extension dir = "+s+" ("+f.canWrite()+")");
			if (f.exists() && f.canWrite()) {
				jarDir = s;
			}
		}
		if (jarDir != null) {
			installer.addLine("Selected jar dir: "+jarDir);
		}
		if (libDir != null) {
			installer.addLine("Selected dll dir: "+libDir);
		}
	}

	@Override
	public String[] getFiles() {
		return new String[] {
			"jpen-2.jar",
			"libjpen-2.jnilib"
		};
	}

}

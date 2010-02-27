package cello.jtablet.installer;

import java.applet.Applet;
import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import cello.jtablet.impl.PluginConstant;

/**
 * This class provides a standard way to guarantee JTablet version compatibility. It performs checks for installed 
 * version of JTablet, allowing you to avoid problems with incompatible JTablet installations.
 * 
 * <p>You should choose the oldest version of JTablet that makes sense for your application, and will likely want to 
 * stick to major version releases (e.g. "2.0.0").  
 * @author marcello
 */
public class ExtensionLoader {

	private static final String MESSAGE_TITLE = "JTablet Version Check";
	private static final String INSTALL_URL = "https://secure.cellosoft.com/jtablet/install";
//	private static final Pattern VERSION_NUMBER = Pattern.compile("([0-9]+)\\.([0-9]+)(?:\\.([0-9]+))?");

	private static Package getInstalledPackage() {
		if (!PluginConstant.IS_PLUGIN) {
			return null;
		}
		Package p = ExtensionLoader.class.getPackage();
		if (p == null) {
			return null;
		}
		String version = p.getImplementationVersion();
		if (version == null) {
			return null;
		}
		return p;
	}
	/**
	 * Returns the currently installed JTablet extension version.
	 * 
	 * @return the current JTablet version
	 */
	public static String getInstalledVersion() {
		Package p = getInstalledPackage();
		if (p == null) {
			return null;
		}
		return p.getImplementationVersion();
	}
	
	/**
	 * @param desiredMinimumVersion
	 * @return the install status
	 */
	public static InstallStatus getInstallStatus(String desiredMinimumVersion) {
		Package installedVersion = getInstalledPackage();
		if (installedVersion == null) {
			return InstallStatus.NOT_INSTALLED;
		}
		//compareVersions(installedVersion, desiredMinimumVersion) < 0) {
		if (!installedVersion.isCompatibleWith(desiredMinimumVersion)) { 
			return InstallStatus.UPDATE_REQUIRED;
		}
		return InstallStatus.INSTALLED;
	}
	
//	private static int compareVersions(String version, String version2) {
//		Matcher m1 = VERSION_NUMBER.matcher(version);
//		Matcher m2 = VERSION_NUMBER.matcher(version2);
//		
//		if (m1.matches() && m2.matches()) {
//		
//			int compare = Integer.valueOf(m1.group(1)) - Integer.valueOf(m2.group(1));
//			if (compare != 0) {
//				return compare;
//			}
//			compare = Integer.valueOf(m1.group(2)) - Integer.valueOf(m2.group(2));
//			if (compare != 0) {
//				return compare;
//			}
//			if (m1.groupCount() >= 3 && m2.groupCount() >= 3) {
//				compare = Integer.valueOf(m1.group(3)) - Integer.valueOf(m2.group(3));
//				if (compare != 0) {
//					return compare;
//				}
//			}
//			
//			return tokenizedCompare(version,version2,'.');
//		} else if (m1.matches()) {
//			return -1;
//		} else if (m2.matches()) {
//			return 1;
//		}
//		return 0;
//	}

//	private static int tokenizedCompare(String a, String b, char delimiter) {
//		int aStart = 0, bStart = 0;
//		// Walk down the string looking for the delimiter
//		do {
//			int aEnd = a.indexOf(delimiter, aStart);
//			int bEnd = b.indexOf(delimiter, bStart);
//			
//			boolean aHasDelimiter = aEnd >= 0;
//			boolean bHasDelimiter = bEnd >= 0;
//			
//			if (!aHasDelimiter && !bHasDelimiter) {
//				return 0;
//			} else if (!aHasDelimiter) {
//				return 1;
//			} else if (!bHasDelimiter) {
//				return -1;
//			}
//			
//			String aPiece = a.substring(aStart, aEnd);
//			String bPiece = b.substring(bStart, bEnd);
//			try {
//				int aNumber = Integer.valueOf(aPiece);
//				int bNumber = Integer.valueOf(bPiece);
//				int compare = aNumber - bNumber;
//				if (compare != 0) {
//					return compare;
//				}
//			} catch (NumberFormatException ex) {
//			} 
//			aStart = aEnd;
//			bStart = bEnd;
//		} while (true);
//	}

	
	/**
	 * 
	 * 
	 * @param parent
	 * @param version
	 * @throws MalformedURLException 
	 * @throws URISyntaxException 
	 */
	public static void doUpgrade(Component parent, String version) throws MalformedURLException, URISyntaxException {
		Component root = SwingUtilities.getRoot(parent);
		URL installUrl = getInstallUrl(version);
		if (root instanceof Applet) {
			Applet applet = (Applet)root;
			applet.getAppletContext().showDocument(installUrl);
		} else {
			BrowserLauncher.browse(installUrl.toURI());
		}
	}

	private static URL getInstallUrl(String version) throws MalformedURLException {
		return new URL(INSTALL_URL + "?version="+version);
	}
	
//	private static void showInstallDialog(Component parent) {
//		JOptionPane.showInputDialog(parent, "Would you like to install JTablet? ", MESSAGE_TITLE);
//	}
	
	private static void showUpgradeDialog(Component parent, String version) {
		if (JOptionPane.showConfirmDialog(
				parent, 
				"This program requires JTablet "+version+".\n\n"+
					"You have JTablet "+getInstalledVersion()+" installed.\n\n"+
					"Would you like to open the JTablet update website?", 
					MESSAGE_TITLE,
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			try {
				doUpgrade(parent, version);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(
						parent, 
						"Unable to open JTablet website", 
						MESSAGE_TITLE, 
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * @param version
	 * @param parent
	 * @return true if no JTablet conflicts were found (JTablet may not be installed)
	 */
	public static boolean checkCompatibilityUI(String version, Component parent) {
		InstallStatus result = getInstallStatus(version);
		
		switch (result) {
			case UPDATE_REQUIRED:
				showUpgradeDialog(parent, version);
				return false;
			case RESTART_REQUIRED:
				showRestartRequiredDialog(parent, version);
				return false;
		}
		return true;
	}
	

	private static void showRestartRequiredDialog(Component parent, String version) {
		JOptionPane.showMessageDialog(
				parent, 
				"JTablet was successfully installed, but you will need to restart your browser (possibly computer) to enable it.", 
				MESSAGE_TITLE, 
				JOptionPane.ERROR_MESSAGE);
	}


	/**
	 * Describes the current install state as returned by {@link ExtensionLoader#getInstallStatus(String)}.
	 * 
	 * @author marcello
	 */
	public enum InstallStatus {
		/**
		 * Signifies that no version of JTablet is installed. The standalone embedded version will be used.
		 */
		NOT_INSTALLED,
		
		/**
		 * Signifies that an older version of JTablet is installed and may cause problems/exceptions.  
		 */
		UPDATE_REQUIRED,
		
		/**
		 * Signifies that a new version of JTablet was detected as installed or updated, but it cannot be loaded without 
		 * restarting the Java VM and may cause problems/exceptions otherwise.
		 */
		RESTART_REQUIRED,
		
		/**
		 * Signifies that JTablet is installed and 
		 */
		INSTALLED
	}
}

package cello.jtablet.installer;

import java.applet.Applet;
import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * This class provides a mechanism for checking the installation status of 
 * @author marcello
 */
public class ExtensionChecker {

	private static final String INSTALL_URL = "https://secure.cellosoft.com/jtablet/install";
	private static final Pattern VERSION_NUMBER = Pattern.compile("([0-9]+)\\.([0-9]+)(?:\\.([0-9]+))?");

	/**
	 * Returns the currently installed JTablet extension version.
	 * 
	 * @return the current JTablet version
	 */
	public static String getInstalledVersion() {
//		ClassLoader loader = ExtensionChecker.class.getClassLoader();
//		if (loader instanceof URLClassLoader) {
//			System.out.println("urls = "+Arrays.toString(((URLClassLoader)loader).getURLs()));
//		}
		Package p = ExtensionChecker.class.getPackage();
		if (p == null) {
			return null;
		}
		String version = p.getImplementationVersion();
		if (version == null || version.endsWith("-standalone")) {
			return null;
		}
		return version;
	}
	
	private static InstallStatus getInstallStatus(String desiredMinimumVersion) {
		String installedVersion = getInstalledVersion();
		if (installedVersion == null) {
			return InstallStatus.NOT_INSTALLED;
		}
		if (compareVersions(installedVersion, desiredMinimumVersion) < 0) {
			return InstallStatus.UPDATE_REQUIRED;
		}
		return InstallStatus.INSTALLED;
	}
	
	private static int compareVersions(String version, String version2) {
		Matcher m1 = VERSION_NUMBER.matcher(version);
		Matcher m2 = VERSION_NUMBER.matcher(version2);
		
		int compare = Integer.valueOf(m1.group(1)) - Integer.valueOf(m2.group(1));
		if (compare != 0) {
			return compare;
		}
		compare = Integer.valueOf(m1.group(2)) - Integer.valueOf(m2.group(2));
		if (compare != 0) {
			return compare;
		}
		if (m1.groupCount() >= 3 && m2.groupCount() >= 3) {
			compare = Integer.valueOf(m1.group(3)) - Integer.valueOf(m2.group(3));
			if (compare != 0) {
				return compare;
			}
		}
		
		return tokenizedCompare(version,version2,'.');
	}

	private static int tokenizedCompare(String a, String b, char delimiter) {
		int aStart = 0, bStart = 0;
		// Walk down the string looking for the delimiter
		do {
			int aEnd = a.indexOf(delimiter, aStart);
			int bEnd = b.indexOf(delimiter, bStart);
			
			boolean aHasDelimiter = aEnd >= 0;
			boolean bHasDelimiter = bEnd >= 0;
			
			if (!aHasDelimiter && !bHasDelimiter) {
				return 0;
			} else if (!aHasDelimiter) {
				return 1;
			} else if (!bHasDelimiter) {
				return -1;
			}
			
			String aPiece = a.substring(aStart, aEnd);
			String bPiece = b.substring(bStart, bEnd);
			try {
				int aNumber = Integer.valueOf(aPiece);
				int bNumber = Integer.valueOf(bPiece);
				int compare = aNumber - bNumber;
				if (compare != 0) {
					return compare;
				}
			} catch (NumberFormatException ex) {
			} 
			aStart = aEnd;
			bStart = bEnd;
		} while (true);
	}

	
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
	
	private static void showInstallDialog(Component parent) {
		JOptionPane.showInputDialog(parent, "Would you like to install JTablet? ", "JTablet Version Check");
	}
	
	private static void showUpgradeDialog(Component parent, String version) {
		JOptionPane.showInputDialog(parent, "", "JTablet Version Check");
	}

	public static InstallStatus checkCompatibilityUI(String version, Component parent) {
		InstallStatus result = getInstallStatus(version);
		
		switch (result) {
			case NOT_INSTALLED:
				break;
			case UPDATE_REQUIRED:
				break;
			case RESTART_REQUIRED:
				break;
		}
		return result;
	}
	

	/**
	 * Describes the current install state as returned by {@link ExtensionChecker#getInstallStatus(String)}.
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

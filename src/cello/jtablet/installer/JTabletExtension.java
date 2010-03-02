package cello.jtablet.installer;

import java.applet.Applet;
import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import cello.jtablet.impl.PluginConstant;

/**
 * This class provides a system for JTablet version compatibility for Applets and Applications. Even if you are bundling 
 * JTablet in your product, an older version of JTablet installed as an extension will override and potentially conflict
 * with your program. This class provides three mechanisms for dynamically understanding the situation: 
 * 
 * <ol>
 * <li>Use {@link #checkCompatibility(Component, String)} to ensure any installed JTablet is compatible. 
 * This will display a simple (English) GUI asking the user to upgrade if they have an older version installed.</li>
 * 
 * <li>{@link #getInstallStatus(String)} to determine the installation status (such as if an upgrade is required) and 
 * call {@link #install(Component, String)} to perform the install itself.</li>
 * 
 * <li>{@link #getInstalledVersion()} to determine the exact versio of JTablet installed.</li>
 * 
 * </ol>
 * 
 * <p>For the best user experience, choose the earliest version of JTablet that makes sense for your application.
 * 
 * @author marcello
 */
public class JTabletExtension {


	/**
	 * Describes the current install state as returned by {@link JTabletExtension#getInstallStatus(String)}.
	 * 
	 * @author marcello
	 */
	public enum InstallStatus {
		/**
		 * JTablet is not installed as an extension.
		 */
		NOT_INSTALLED,
		
		/**
		 * An older version of JTablet is installed and may cause problems/errors.  
		 */
		UPDATE_REQUIRED,
		
		/**
		 * An equal or newer version of JTablet is installed.
		 */
		INSTALLED
	}
	
	private static final String MESSAGE_TITLE = "JTablet Version Check";
	private static final String INSTALL_URL = "https://secure.cellosoft.com/jtablet/install";

	/**
	 * Returns the currently installed version of JTablet.
	 * 
	 * @return the version, or null, if it is not installed
	 */
	public static String getInstalledVersion() {
		Package p = getInstalledPackage();
		if (p == null) {
			return getLegacyJTabletVersion();
		}
		return p.getImplementationVersion();
	}
	
	/**
	 * Checks the installed version of JTablet and determines if an upgrade is required.
	 * 
	 * @param desiredMinimumVersion the version of JTablet to compare to 
	 * @return the installed version 
	 */
	public static InstallStatus getInstallStatus(String desiredMinimumVersion) {
		Package installedVersion = getInstalledPackage();
		if (installedVersion == null) {
			return InstallStatus.NOT_INSTALLED;
		}
		if (!installedVersion.isCompatibleWith(desiredMinimumVersion)) { 
			return InstallStatus.UPDATE_REQUIRED;
		}
		return InstallStatus.INSTALLED;
	}
	
	/**
	 * Opens up a web-page to install (or upgrade) JTablet (if necessary). 
	 * 
	 * <p>In the future, this method may do a silent/in-place install if called prior to loading any JTablet classes.
	 * 
	 * @param parentComponent the parent component for showing UI messages
	 * @param desiredMinimumVersion the minimum version of JTablet to install, or null to install regardless
	 * @return true if the upgrade was successful.
	 * 		(This only happens if the desiredMinimumVersion is already installed. In the future it may be true after 
	 * 		an in-place install/upgrade.)
	 * @throws IOException if there was a problem attempting to do an upgrade 
	 */
	public static boolean install(Component parentComponent, String desiredMinimumVersion) throws IOException {
		if (desiredMinimumVersion != null && 
				getInstallStatus(desiredMinimumVersion) == InstallStatus.INSTALLED) {
			return true;
		}
		Component root = SwingUtilities.getRoot(parentComponent);
		URL installUrl = getInstallUrl(desiredMinimumVersion);
		if (root instanceof Applet) {
			Applet applet = (Applet)root;
			applet.getAppletContext().showDocument(installUrl);
		} else {
			try {
				BrowserLauncher.browse(installUrl.toURI());
			} catch (URISyntaxException e) {
				throw new IOException("Error navigating to URL: "+installUrl);
			}
		}
		return false;
	}

	/**
	 * Checks the installed version of JTablet, and displays a message to the user if an incompatible version is found.
	 * The method returns true if not conflicts are found.
	 * 
	 * <p><b>Note:</b> This method will still return true if JTablet is <b>not</b> installed. If you want to require
	 * JTablet be installed, you should use JTablet as an extension.
	 * 
	 * @param parentComponent the component to launch the UI from (this is used to detect Applets)
	 * @param desiredMinimumVersion the minimum version of JTablet required for the upgrade 
	 * 			(you may get a later version)
	 * @return true if no JTablet conflicts were found
	 */
	public static boolean checkCompatibility(Component parentComponent, String desiredMinimumVersion) {
		switch (getInstallStatus(desiredMinimumVersion)) {
			case UPDATE_REQUIRED:
				return showUpgradeDialog(parentComponent, desiredMinimumVersion);
		}
		return true;
	}
	
	private static String getLegacyJTabletVersion() {
		try {
			// Get a class object through Reflection API
			Class<?> jtablet = Class.forName("cello.tablet.JTablet");
			// Create an instance of the object
			Object tablet = jtablet.newInstance();
			try {
				// getVersion() was added in 0.2 BETA 2
				// You can safely assume this function will exist, since the 0.1 BETA
				// was distributed to a select group of users.
				// If the user is using 0.1 BETA, you aren't required to support them
				// so you can simply recommend an upgrade.
				Method tablet_getVersion = jtablet.getMethod("getVersion");
	
				// Invoke function
				return (String)tablet_getVersion.invoke(tablet);
			} catch (Exception e) {
				// If the class exists but the getVersion method doesn't, 
				// they are using the old 0.1 beta version (highly unlikely)
				return "0.1.0-beta";
			}
		} catch (Exception e) {
			// No JTablet found
			return null;
		}
	}

	private static Package getInstalledPackage() {
		if (!PluginConstant.IS_PLUGIN) {
			return null;
		}
		Package p = JTabletExtension.class.getPackage();
		if (p == null) {
			return null;
		}
		String version = p.getImplementationVersion();
		if (version == null) {
			return null;
		}
		return p;
	}
	private static URL getInstallUrl(String version) throws MalformedURLException {
		return new URL(INSTALL_URL + "?version="+version);
	}
	
	private static boolean showUpgradeDialog(Component parent, String version) {
		if (JOptionPane.showConfirmDialog(
				parent, 
				"This program requires JTablet "+version+".\n\n"+
					"You have JTablet "+getInstalledVersion()+" installed which may conflict.\n\n"+
					"Would you like to open the JTablet update website?", 
					MESSAGE_TITLE,
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			try {
				return install(parent, version);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(
						parent, 
						"Unable to open JTablet website", 
						MESSAGE_TITLE, 
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return false;
	}

}

package cello.jtablet.installer;

import java.lang.reflect.Method;
import java.net.URI;

import javax.swing.JOptionPane;
import java.util.Arrays;

/**
 * <b>Bare Bones Browser Launch for Java</b><br>
 * Utility class to open a web page from a Swing application in the user's
 * default browser.<br>
 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista<br>
 * Example Usage:<code><br> &nbsp; &nbsp;
 *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
 *    BareBonesBrowserLaunch.openURL(url);<br></code> Latest Version: <a
 * href="http://www.centerkey.com/java/browser/"
 * >www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br>
 * Public Domain Software -- Free to Use as You Like
 * 
 * @version 2.0, May 26, 2009
 */
public class BrowserLauncher {

	private static final String[] UNIX_BROWSERS = {
		"xdg-open",
		"firefox", 
		"opera", 
		"konqueror",
		"epiphany", 
		"seamonkey", 
		"galeon", 
		"kazehakase", 
		"mozilla",
		"netscape" 
	};

	/**
	 * Opens the specified web page in a web browser
	 * 
	 * @param uri
	 *            A web address (URL) of a web page (ex: "http://www.google.com/")
	 */
	public static void browse(URI uri) {
		String osName = System.getProperty("os.name");
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Method getDesktop = desktopClass.getDeclaredMethod("getDesktop");
			Method browse = desktopClass.getDeclaredMethod("browse", URI.class);
			Object desktopInstance = getDesktop.invoke(null);
			browse.invoke(desktopInstance, uri);
		} catch (Exception ex) {
			try {
				String uriString = uri.toString();
				if (osName.startsWith("Mac OS")) {
					Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
					Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
					openURL.invoke(null, uriString);
				} else {
					Runtime runtime = Runtime.getRuntime();
					if (osName.startsWith("Windows")) {
						runtime.exec("rundll32 url.dll,FileProtocolHandler " + uriString);
					} else { 
						// assume Unix or Linux
						for (String browser : UNIX_BROWSERS) {
							if (runtime.exec( new String[] { "which", browser }).waitFor() == 0) {
								runtime.exec( new String[] { browser, uriString });
								return;
							}
						}
						throw new Exception(Arrays.toString(UNIX_BROWSERS));
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "I'm sorry, I tried to open "+uri
						+" in your web browser, but could not.\n\nJava Error: " + e.toString());
			}
		}
	}

}

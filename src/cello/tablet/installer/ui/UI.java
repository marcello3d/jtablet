package cello.tablet.installer.ui;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @author marcello
 *
 */
public interface UI {

	/**
	 * Add a log message.
	 * 
	 * @param message
	 */
	public void logMessage(String message);
	
	/**
	 * Tells the UI that there are install directories to choose from.
	 * 
	 * @param jarDirectories
	 * @param libDirectories
	 * @param responder 
	 */
	public void requestInstallDirectories(List<File> jarDirectories, List<File> libDirectories, UIResponder responder);
	
	
	public void renderDownloads(List<Download> downloads);
	/**
	 * @param hasToRestart  set to true if the browser must be restarted for the plugin to be used.
	 */
	public void finishedInstall(boolean hasToRestart);
}

package cello.tablet.installer.ui;

import java.io.File;
import java.net.URL;
import java.util.List;

import netscape.javascript.JSObject;

public class BrowserUI implements UI {

	private final JSObject window;

	/**
	 * @param window
	 */
	public BrowserUI(JSObject window) {
		this.window = window;
	}

	public void finishedInstall(boolean hasToRestart) {

	}

	public void logMessage(String message) {
		window.call("addLogMessage", new Object[]{message});
	}

	public void requestInstallDirectories(List<File> jarDirectories, List<File> libDirectories, UIResponder responder) {
		window.call("requestInstallDirectories", new Object[]{
			jarDirectories.toArray(),
			libDirectories.toArray(),
			responder
		});
	}
	public void renderDownloads(List<Download> downloads) {
		// TODO Auto-generated method stub
		
	}

}

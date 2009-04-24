package cello.tablet.installer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import cello.tablet.installer.ui.AppletUI;
import cello.tablet.installer.ui.BrowserUI;
import cello.tablet.installer.ui.UI;
import cello.tablet.installer.ui.UIResponder;

/**
 * @author marcello
 *
 */
public class JTabletInstaller extends JApplet {
	private JSObject window;
	private UI ui;
	
	
	@Override
	public void init() {
		try {
			window = JSObject.getWindow(this);
		} catch (JSException e) {
			e.printStackTrace();
		}
		
		if (window != null) {
			ui = new BrowserUI(window);
		} else {
			ui = new AppletUI(this);
		}
		
		Thread t = new Thread() {
			public void run() {
				
				URL documentBase = getDocumentBase(),
					codeBase = getCodeBase();
				
				String codeBaseProtocol = codeBase.getProtocol().toLowerCase();
				String documentBaseProtocol = documentBase.getProtocol().toLowerCase();
				if ((!documentBaseProtocol.equals("file") && !documentBaseProtocol.equals("https"))||
					(!    codeBaseProtocol.equals("file") && !    codeBaseProtocol.equals("https"))) {
					addLogMessage("JTablet has detected a security breach.");
					return;
				}

				OSInstaller installers[] = {
					new MacOSXInstaller(JTabletInstaller.this)
				};
				
				String os = System.getProperty("os.name");
				String osVersion = System.getProperty("os.version");
				addLogMessage("You appear to be using "+os+" ("+osVersion+").");
				
				try {
					Class<?> c = Class.forName("jpen.provider.Utils");
					Method m = c.getMethod("getFullVersion");
					String version = "unknown";
					try {
						version = (String)m.invoke(null);
					} catch (Exception e) {}
					addLogMessage("Detected JPen ("+version+")");
				} catch (Exception e) {
					addLogMessage("JPen not detected.");
				}
				
				for (int i=0; i<installers.length; i++) {
					if (installers[i].isCompatible()) {
						doInstall(installers[i]);
						return;
					}
				}
				addLogMessage("Sorry, "+os+" is currently unsupported.");
			}
		};
		t.start();
	}
	protected void doInstall(final OSInstaller installer) {
		final List<File> jarInstallDirectories = installer.getJarInstallDirectories();
		final List<File> libraryInstallDirectories = installer.getLibraryInstallDirectories();

		
		
		ui.requestInstallDirectories(jarInstallDirectories, libraryInstallDirectories, new UIResponder() {
			private Thread thread = null;
			public void useDirectories(final File jarDirectory, final File libraryDirectory) throws IllegalArgumentException {
				if (thread != null && thread.isAlive()) {
					throw new IllegalArgumentException("Already installing...");
				}
				if (!jarInstallDirectories.contains(jarDirectory)) {
					throw new IllegalArgumentException(jarDirectory + " is an invalid jar directory.");
				}
				if (!libraryInstallDirectories.contains(libraryDirectory)) {
					throw new IllegalArgumentException(libraryDirectory + " is an invalid library directory.");
				}
				if (!jarDirectory.isDirectory()) {
					throw new IllegalArgumentException(jarDirectory + " is not a directory.");
				}
				if (!libraryDirectory.isDirectory()) {
					throw new IllegalArgumentException(libraryDirectory + " is not a directory.");
				}
				if (!jarDirectory.canWrite()) {
					throw new IllegalArgumentException(jarDirectory + " is not writeable.");
				}
				if (!libraryDirectory.canWrite()) {
					throw new IllegalArgumentException(libraryDirectory + " is not writeable.");					
				}
				
				Thread t = new Thread(new Runnable() {
					public void run() {
						String[] jarFiles = installer.getJarFiles();
						String[] libraryFiles = installer.getLibraryFiles();
						for (String jar : jarFiles) {
							try {
								downloadFile(getCodeBase(), jar, jarDirectory);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						for (String library : libraryFiles) {
							try {
								downloadFile(getCodeBase(), library, libraryDirectory);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					private void downloadFile(URL source, String filename, File destination) throws IOException {
						URL sourceURL = new URL(source,filename);
						ui.logMessage("Downloading "+sourceURL+" to "+destination);
						URLConnection connection = sourceURL.openConnection();
						connection.setUseCaches(false);
						connection.connect();
						
						long length = connection.getContentLength();
						ui.logMessage("Filesize: "+length+" bytes");
						
						InputStream inputStream = connection.getInputStream();
						File file = new File(destination,filename);
						FileOutputStream fos = new FileOutputStream(file);
						
						byte data[] = new byte[4096];
						
						long totalWriteCount = 0;
						int readByteCount; 
						while (-1 != (readByteCount = inputStream.read(data))) {
							fos.write(data, 0, readByteCount);
							totalWriteCount += readByteCount;
							ui.renderDownload(sourceURL, file, totalWriteCount, length);
						}

						ui.logMessage("Finished downloading.");
					}
				});
				t.start();
			}
		});
	}
	/**
	 * @param s
	 */
	public void addLogMessage(String s) {
		ui.logMessage(s);
	}

}

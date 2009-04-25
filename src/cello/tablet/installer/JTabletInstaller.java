package cello.tablet.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JApplet;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import cello.tablet.installer.ui.AppletUI;
import cello.tablet.installer.ui.BrowserUI;
import cello.tablet.installer.ui.Download;
import cello.tablet.installer.ui.UI;
import cello.tablet.installer.ui.UIResponder;

/**
 * @author marcello
 *
 */
public class JTabletInstaller extends JApplet {
	private static final int BUFFER_SIZE = 4096;
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
						class FileDownload extends Download {

							boolean started = false;
							private final File destinationDirectory;
							private long bytesDownloaded;
							private long bytesTotal;
							FileDownload(URL sourceBaseURL, String filename, File destinationDirectory) throws MalformedURLException {
								super(new URL(sourceBaseURL,filename),new File(destinationDirectory,filename));
								this.destinationDirectory = destinationDirectory;
							}
							protected void download() {
								try {
									started = true;
									if (listener != null) {
										listener.downloadStarted(this);
									}
	
									URL sourceURL = getSource();
									ui.logMessage("Downloading "+sourceURL+" to "+destinationDirectory);
									URLConnection connection = sourceURL.openConnection();
									connection.setUseCaches(false);
									connection.connect();
									
									bytesTotal = connection.getContentLength();
									ui.logMessage("Filesize: "+bytesTotal+" bytes");
									
									InputStream inputStream = connection.getInputStream();
									File file = getDestination();
									FileOutputStream fos = new FileOutputStream(file);
									
									byte data[] = new byte[BUFFER_SIZE];
									
									bytesDownloaded = 0;
									int readByteCount; 
									while (-1 != (readByteCount = inputStream.read(data))) {
										fos.write(data, 0, readByteCount);
										bytesDownloaded += readByteCount;
										if (listener != null) {
											listener.downloadProgress(this);
										}
									}
	
									ui.logMessage("Finished downloading.");
								} catch (IOException ex) {
									ex.printStackTrace();
									if (listener != null) {
										listener.downloadFailed(this, ex);
									}
								}
							}
							@Override
							public long getBytesDownloaded() {
								return bytesDownloaded;
							}

							@Override
							public long getBytesTotal() {
								return bytesTotal;
							}

							@Override
							public boolean hasStarted() {
								return started;
							}
							
						}
						List<Download> downloads = new ArrayList<Download>(jarFiles.length+libraryFiles.length);
						for (String jar : jarFiles) {
							try {
								downloads.add(new FileDownload(getCodeBase(), jar, jarDirectory));
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
						for (String library : libraryFiles) {
							try {
								downloads.add(new FileDownload(getCodeBase(), library, libraryDirectory));
							} catch (MalformedURLException e) {
								e.printStackTrace();
							}
						}
						ui.renderDownloads(downloads);
						for (Download d : downloads) {
							((FileDownload)d).download();
						}
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

package cello.jtablet.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/*package*/ class JTabletUpdateChecker {
	

	private static final int HTTP_TIMEOUT_MS = 5000;

	/*package*/ interface UpdateListener {
		public void newGoodStatus(String message);
		public void newBadStatus(String message);
	}
	
	private final UpdateListener listener;


	private final String installedVersion;	
	/*package*/ JTabletUpdateChecker(String installedVersion, UpdateListener listener) {
		this.installedVersion = installedVersion;
		this.listener = listener;
		updateThread.setDaemon(true);
		updateThread.setPriority(Thread.MIN_PRIORITY);
	}
	
	private HttpURLConnection connection;

	private final Thread updateThread = new Thread("JTablet2-UpdateCheck") {

		public void run() {
			try {
				final URL url = new URL("https://secure.cellosoft.com/jtablet/versioncheck?version=" + 
						URLEncoder.encode(installedVersion == null ? "dev" : installedVersion,"UTF-8") +
						"&java="+URLEncoder.encode(System.getProperty("java.version"),"UTF-8")+
						"&os="+URLEncoder.encode(System.getProperty("os.name")+"/"+System.getProperty("os.version"),"UTF-8")+
						"&arch="+URLEncoder.encode(System.getProperty("os.arch"),"UTF-8"));

				synchronized (JTabletUpdateChecker.this) {
					connection = (HttpURLConnection)url.openConnection();
				}
				connection.setUseCaches(false);
				connection.setReadTimeout(HTTP_TIMEOUT_MS);
				InputStream stream = null;
				
				// Get http body content stream
				try {
					// We need security clearance to connect to a SSL server on an applet 
					stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
			            public InputStream run() throws IOException {
							return connection.getInputStream();
			            }
					});
				} catch (PrivilegedActionException ex) {
					throw ex.getException();
				}
				
				
				// Read data
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String line = reader.readLine();
				if (line != null) {
					listener.newGoodStatus(line);
				} else {
					listener.newBadStatus("The update site appears to be broken.");
				}
				stream.close();
			} catch (SocketTimeoutException e) {
				listener.newBadStatus("It took too long to check for updates.");
			} catch (FileNotFoundException e) {
				listener.newBadStatus("The update site appears to be broken.");
			} catch (IOException e) {
				// For some reason I can't compress the jar with ProGuard if I reference SSLException directly...
				if (e.getClass().getSimpleName().contains("SSL")) {
					listener.newBadStatus("Security error trying to check for updates!");
				} else {
					listener.newBadStatus("There was a network problem checking for updates.");
				}
				e.printStackTrace();
			} catch (Exception e) {
				listener.newBadStatus("Error checking for updates: "+e.getClass().getSimpleName());
				e.printStackTrace();
			}
			synchronized (JTabletUpdateChecker.this) {
				connection = null;
			}
		}
	};
	public void start() {
		updateThread.start();
	}
	
	public synchronized void stop() {
		if (connection != null) {
			// doesn't seem to actually work
			connection.disconnect();
		}
	}
}

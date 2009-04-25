package cello.tablet.installer.ui;

import java.io.File;
import java.net.URL;

public abstract class Download {
	
	private final URL source;
	private final File destination;

	public Download(URL source, File destination) {
		this.source = source;
		this.destination = destination;
	}
	
	public abstract boolean hasStarted();
	public abstract long getBytesDownloaded();
	public abstract long getBytesTotal();

	protected DownloadListener listener;

	public void setListener(DownloadListener listener) {
		this.listener = listener;
	}

	/**
	 * @return the source
	 */
	public URL getSource() {
		return source;
	}

	/**
	 * @return the destination
	 */
	public File getDestination() {
		return destination;
	}
}

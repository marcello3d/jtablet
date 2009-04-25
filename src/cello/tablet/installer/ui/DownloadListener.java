package cello.tablet.installer.ui;

import java.io.IOException;

public interface DownloadListener {
	public void downloadProgress(Download d);
	public void downloadFinished(Download d);
	public void downloadFailed(Download d, IOException ex);
	public void downloadStarted(Download d);
}

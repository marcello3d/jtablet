package cello.jtablet.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import cello.jtablet.installer.platforms.MacOSXInstaller;
import cello.jtablet.installer.platforms.OSInstaller;
import cello.jtablet.installer.platforms.WindowsInstaller;
import cello.jtablet.installer.ui.Download;

public class JTabletNativeInstallerMain {

	private static final int BUFFER_SIZE = 4096;


	public JTabletNativeInstallerMain() {
		if (JOptionPane.showConfirmDialog(null, "Download and install JTablet plugin?") == JOptionPane.YES_OPTION) {

//			URL sourceURL = makeNativeUrl();
//			URLConnection connection = sourceURL.openConnection();
//			connection.setUseCaches(false);
//			connection.connect();
//			
//			int bytesTotal = connection.getContentLength();
//			
//			InputStream inputStream = connection.getInputStream();
//			File file = getDestination();
//			FileOutputStream fos = new FileOutputStream(file);
//			
//			byte data[] = new byte[BUFFER_SIZE];
//			
//			int bytesDownloaded = 0;
//			int readByteCount; 
//			while (-1 != (readByteCount = inputStream.read(data))) {
//				fos.write(data, 0, readByteCount);
//				bytesDownloaded += readByteCount;
//			}
		}
	}
	
//
//	private URL makeNativeUrl() {
//		// TODO Auto-generated method stub
//		return null;
//	}


	public static void main(String[] args) {
		new JTabletNativeInstallerMain();
	}

}

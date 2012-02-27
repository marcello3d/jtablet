package cello.research;

import java.applet.Applet;
import java.awt.BorderLayout;

import javax.swing.JApplet;
import javax.swing.JTextArea;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class WacomWebPluginApplet extends JApplet {

	private JTextArea textArea;
	private boolean running = true;
	private RunThread thread;
	private JSObject wacom;
	@Override
	public void init() {
		textArea = new JTextArea();
		getContentPane().add(textArea, BorderLayout.CENTER);
		try {
			JSObject window = JSObject.getWindow(this);
			System.out.println("window = "+window);
			JSObject document = (JSObject)window.getMember("document");
			JSObject embeds = (JSObject)document.getMember("embeds");
			wacom = (JSObject)embeds.getMember("wacom-plugin");
			System.out.println("wacom = "+wacom);
		} catch (JSException ex) {
			ex.printStackTrace();
		}
	}
	private final String[] properties = {
		"isWacom",
		"isEraser",
		"pressure",
		"posX",
		"posX",
		"sysX",
		"sysY",
		"rotationDeg",
		"rotationRad",
		"tiltX",
		"tiltY",
		"tangentialPressure",
		"version",
		"pointerType"
	};
	
	private class RunThread extends Thread {
		private boolean running = false;
		
		@Override
		public void run() {
			while (running && wacom != null) {
				for (String property : properties) {
					System.out.println(property+" => "+wacom.getMember(property));
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void startRunning() {
			running = true;
			start();
		}
		public void stopRunning() {
			running = false;
		}
	}
	
	@Override
	public synchronized void start() {
		thread = new RunThread();
		thread.setDaemon(true);
		thread.startRunning();
	}
	@Override
	public synchronized void stop() {
		thread.stopRunning();
	}
}

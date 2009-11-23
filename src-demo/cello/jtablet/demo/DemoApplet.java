package cello.jtablet.demo;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * A simple sketching surface applet
 * 
 * @author marcello
 */
public class DemoApplet extends JApplet {
	
	
	public void init() {
		System.out.println("System Properties:");
		for (Object property : System.getProperties().keySet()) {
			String propertyName = property.toString();
			System.out.println(propertyName+": "+System.getProperty(propertyName));
		}
		getContentPane().add(new DemoSurface(),BorderLayout.CENTER);
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String ...args) throws InterruptedException {
		final JFrame frame = new JFrame("JTablet Demo");
		try {
			final DemoApplet demo = new DemoApplet();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(500,550);
			frame.setLocationByPlatform(true);
			frame.setContentPane(demo);
			demo.init();
			demo.start();	
			frame.setVisible(true);
		} catch (Throwable t) {
			t.printStackTrace();
	        StringBuilder buf = new StringBuilder();
	        buf.append(t.toString()).append('\n');
	        for (StackTraceElement s : t.getStackTrace()) {
	        	buf.append('\n').append(s.toString());
	        }
			JOptionPane.showMessageDialog(frame, buf.toString());
			System.exit(-1);
		}
	}
}

package cello.jtablet.demo;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import cello.jtablet.installer.JTabletInstaller;

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
	 */
	public static void main(String ...args) {
		final JFrame frame = new JFrame("JTablet Plugin Installer");
		try {
			final JTabletInstaller installer = new JTabletInstaller();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(500,550);
			frame.setLocationByPlatform(true);
			frame.setContentPane(installer);
			installer.init();
			installer.start();	
			frame.setVisible(true);
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(frame, Arrays.toString(t.getStackTrace()));
		}
	}
}

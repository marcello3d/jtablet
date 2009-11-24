package cello.jtablet.demo;

import java.awt.BorderLayout;
import java.awt.Container;

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
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		DemoSurface demoSurface = new DemoSurface();
		contentPane.add(demoSurface,BorderLayout.CENTER);
		contentPane.add(new DemoLogPanel(demoSurface),BorderLayout.SOUTH);
		contentPane.add(new DemoInfoPanel(demoSurface),BorderLayout.EAST);
		
		invalidate();
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
			frame.setSize(800,550);
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

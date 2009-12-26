package cello.jtablet.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * A simple sketching surface applet
 * 
 * @author marcello
 */
public class DemoApplet extends JApplet {
	
	
	public void init() {
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		


		tabbedPane.addTab("1 Component",createDrawingGroup(BorderLayout.EAST));

		JPanel panel = new JPanel(new GridLayout(1,3,5,5));
		panel.setBackground(Color.BLACK);
		panel.add(createDrawingGroup(BorderLayout.NORTH));
		panel.add(createDrawingGroup(BorderLayout.NORTH));
		panel.add(createDrawingGroup(BorderLayout.NORTH));
		
		tabbedPane.addTab("3 Components",panel);

		panel = new JPanel(new GridLayout(4,4,5,5));
//		panel.setBackground(Color.BLACK);
		for (int i=0; i<16; i++) {
			panel.add(new DemoSurface());
		}
		tabbedPane.addTab("16 Components",panel);

		tabbedPane.addTab("Screen Listener",new ScreenTabletListenerLogPanel());
		
		
		contentPane.add(tabbedPane,BorderLayout.CENTER);
		
		invalidate();
	}

	private Component createDrawingGroup(String infoPosition) {
		JPanel panel = new JPanel(new BorderLayout());
		DemoSurface demoSurface = new DemoSurface();
		panel.add(demoSurface,BorderLayout.CENTER);
		panel.add(new TabletListenerLogPanel(demoSurface),BorderLayout.SOUTH);
		panel.add(new JScrollPane(new DemoInfoPanel(demoSurface)),infoPosition);
		return panel;
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

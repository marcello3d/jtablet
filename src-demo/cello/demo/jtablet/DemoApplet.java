package cello.demo.jtablet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import cello.jtablet.installer.ExtensionLoader;
import cello.jtablet.installer.ExtensionLoader.InstallStatus;

/**
 * A simple sketching surface applet
 * 
 * @author marcello
 */
public class DemoApplet extends JApplet {
	

	/** Require JTablet version for extension installation status. */
	public static final String REQUIRED_VERSION = "1.2.0";

	public void init() {
		
		System.out.println("Installed JTablet version: " + ExtensionLoader.getInstalledVersion());
		InstallStatus installStatus = ExtensionLoader.getInstallStatus(REQUIRED_VERSION);
		
		System.out.println("install status = "+installStatus);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		
		switch (installStatus) {
			case INSTALLED:
			case NOT_INSTALLED:
				
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
		
				try {
					tabbedPane.addTab("Screen Listener",new ScreenTabletListenerLogPanel());
				} catch (UnsupportedOperationException ex) {
					// do nothing
				}
				panel = new JPanel();
				
				JButton button = new JButton("Garbage collect");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						System.gc();
						double freeMemory = Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0;
						double totalMemory = Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0;
						double usedMemory = totalMemory - freeMemory;
						double maxMemory = Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0;
		
						NumberFormat nf = NumberFormat.getNumberInstance();
						nf.setGroupingUsed(true);
						nf.setMaximumFractionDigits(0);
						nf.setMinimumFractionDigits(0);
						System.out.println("memory: " + 
							nf.format(usedMemory) + "/" +
							nf.format(totalMemory) +  
							"MB used (" +
							nf.format(maxMemory) + "MB max)" 
						);

						Window[] windows = Window.getWindows();
						System.out.println("Windows ("+windows.length+"):");
						for (Window w : windows) {
							System.out.println("window: "+w);
						}						

					}
				});
				panel.add(button);
				
				button = new JButton("Open in new window");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						makeNewFrame();
					}
				});
				panel.add(button);
				
				button = new JButton("Window benchmark");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for (int i = 0; i<50; i++) {
							makeNewFrame();
						}
					}
				});
				panel.add(button);
				
				tabbedPane.addTab("Misc",panel);
				
				tabbedPane.addTab("JTablet Status Diagnostics", new InstallStatusPanel());
				
				
				contentPane.add(tabbedPane,BorderLayout.CENTER);
				break;
			case RESTART_REQUIRED:
			case UPDATE_REQUIRED:
				contentPane.add(new InstallStatusPanel());
				break;
		}
		
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

	private void makeNewFrame() {
		JFrame frame = new JFrame("JTablet Demo");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800,550);
//		frame.setLocationByPlatform(true);
		frame.getContentPane().add(createDrawingGroup(BorderLayout.EAST), BorderLayout.CENTER);
		frame.setVisible(true);
		frame.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					Component component = e.getComponent();
					if (component instanceof Window) {
						System.out.println("Disposing "+component);
						((Window)component).dispose();
					} else {
						System.out.println("Cannot dispose "+component);
					}
				}
			}
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String ...args) throws InterruptedException {
		final JFrame frame = new JFrame("JTablet Demo");
		try {
			final DemoApplet demo = new DemoApplet();
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

package cello.demo.jtablet;

import cello.jtablet.installer.JTabletExtension;
import cello.jtablet.installer.JTabletExtension.InstallStatus;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

/**
 * A simple sketching surface applet
 * 
 * @author marcello
 */
public class DemoApplet extends JApplet {
	

	/** Require JTablet version for extension installation status. */
	public static final String REQUIRED_VERSION = "1.2.0";

	public void init() {		
		InstallStatus installStatus = JTabletExtension.getInstallStatus(REQUIRED_VERSION);
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
        contentPane.setBackground(SystemColor.control);
		
		switch (installStatus) {
			case INSTALLED:
			case NOT_INSTALLED:
				
				JTabbedPane tabbedPane = new JTabbedPane();

                tabbedPane.addTab("Status", new InstallStatusPanel());

				tabbedPane.addTab("1 Canvas",createDrawingGroup(BorderLayout.EAST));
		
				ClearPanel panel = new ClearPanel(new GridLayout(1,3,5,5));
				panel.setOpaque(false);
				panel.add(createDrawingGroup(BorderLayout.NORTH));
				panel.add(createDrawingGroup(BorderLayout.NORTH));
				panel.add(createDrawingGroup(BorderLayout.NORTH));
				
				tabbedPane.addTab("3",panel);
		
				panel = new ClearPanel(new GridLayout(4,4,5,5));
                panel.setOpaque(false);
				for (int i=0; i<16; i++) {
					panel.add(new DemoSurface());
				}
				tabbedPane.addTab("16",panel);
		
				try {
					tabbedPane.addTab("Screen",new ScreenTabletListenerLogPanel());
				} catch (UnsupportedOperationException ex) {
					// do nothing
				}
				panel = new ClearPanel();
				
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
				

				contentPane.add(tabbedPane,BorderLayout.CENTER);
				break;
			case UPDATE_REQUIRED:
				contentPane.add(new InstallStatusPanel());
				break;
		}
		
		invalidate();
	}

	private Component createDrawingGroup(String infoPosition) {
		ClearPanel panel = new ClearPanel(new BorderLayout());
		DemoSurface demoSurface = new DemoSurface();
		panel.add(demoSurface,BorderLayout.CENTER);
		panel.add(new TabletListenerLogPanel(demoSurface),BorderLayout.SOUTH);
		panel.add(new DemoInfoPanel(demoSurface),infoPosition);
		return panel;
	}

	private void makeNewFrame() {
		JFrame frame = new JFrame("JTablet Demo");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(800,550);
		frame.getContentPane().add(createDrawingGroup(BorderLayout.EAST), BorderLayout.CENTER);
		frame.setVisible(true);
		frame.addKeyListener(new KeyAdapter() {
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

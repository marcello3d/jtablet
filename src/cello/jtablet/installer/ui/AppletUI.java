package cello.jtablet.installer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;

public class AppletUI implements UI {

	private static final Color CAPTION_COLOR = new Color(0x888888);
	private static final Font CAPTION_FONT = new Font("Dialog",Font.PLAIN,10);
	private static final Font SECTION_FONT = new Font("Dialog",Font.BOLD,15);
	private static final Font LABEL_FONT = new Font("Dialog",Font.BOLD,12);
	private static final Font TITLE_FONT = new Font("Dialog",Font.BOLD,20);
	protected JTextArea area;
	protected JPanel panel;
	private JComboBox libComboBox;
	private JComboBox jarComboBox;
	private final JApplet applet;
	public AppletUI(JApplet applet) {
		
		

		

	    this.applet = applet;
		panel = new JPanel(new SpringLayout());
//	    BoxLayout boxLayout = new BoxLayout(panel,BoxLayout.Y_AXIS);
//		panel.setLayout(boxLayout);

	    addComponents(new Component []{
	    	makeLabel("JTablet 2 Plugin Installer",TITLE_FONT,Color.BLACK),
	    	makeWrappedLabel(
	    			"In a couple easy steps you will be rocking and rolling with the latest JTablet for Java Plugin.",
	    			CAPTION_FONT,CAPTION_COLOR),
	    	makeLabel("Step 1: Installation directories...",SECTION_FONT,Color.BLACK)
	    });

		JPanel logPanel = makeLogPanel();


	    applet.getContentPane().add(panel,BorderLayout.CENTER);
	    applet.getContentPane().add(logPanel,BorderLayout.SOUTH);
	    applet.invalidate();
	}

	private void addComponents(Component[] components) {
		for (Component c : components) {
	    	panel.add(c);
	    }
		updatePanel();
	}

	private JPanel makeLogPanel() {
		area = new JTextArea();
		area.setEditable(false);
		area.setFont(new Font("Dialog",Font.PLAIN,15));
		
		final JScrollPane scrollPane = new JScrollPane(area) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, 150);
			}
		};
		scrollPane.setSize(new Dimension(100,150));
		scrollPane.setVisible(false);
		JCheckBox showLogCheckbox = new JCheckBox(new AbstractAction("Show log") {
			public void actionPerformed(ActionEvent e) {
				scrollPane.setVisible(((JCheckBox)e.getSource()).isSelected());
				panel.revalidate();
			}
		});
		showLogCheckbox.setFont(LABEL_FONT);
	    JPanel logPanel = new JPanel(new BorderLayout());
	    logPanel.add(showLogCheckbox, BorderLayout.NORTH);
	    logPanel.add(scrollPane, BorderLayout.CENTER);
		return logPanel;
	}

	private void layoutSpringComponents(JComponent panel, int cols, int margin, int padding) {
		SpringUtilities.makeCompactGrid(panel,
	                                    panel.getComponentCount()/cols, cols, //rows, cols
	                                    margin, margin,        //initX, initY
	                                    padding, padding);       //xPad, yPad
	}

	private JLabel makeLabel(String text, Font font, Color color) {
		JLabel label = new JLabel(text);
		label.setFont(font);
		label.setForeground(color);
		return label;
	}
	private JTextArea makeWrappedLabel(String text, Font font, Color color) {
		JTextArea label = new JTextArea(text) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
			}
		};
		label.setFont(font);
		label.setForeground(color);
		label.setWrapStyleWord(true);
	    label.setLineWrap(true);
	    label.setEditable(false);
	    label.setBackground(panel.getBackground());
		return label;
	}

	public void finishedInstall(boolean hasToRestart) {
		
	}

	public void logMessage(String message) {
		area.insert(message+"\n", area.getText().length());
		area.setCaretPosition(area.getText().length());
	}

	NumberFormat nf = NumberFormat.getNumberInstance();
	{
		nf.setGroupingUsed(true);
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
	}
	NumberFormat nf2 = NumberFormat.getPercentInstance();
	{
		nf2.setGroupingUsed(true);
		nf2.setMaximumFractionDigits(1);
		nf2.setMinimumFractionDigits(1);
	}
	public void renderDownloads(List<Download> downloads) {
		for (final Download download : downloads) {
			final JTextArea progressText = makeWrappedLabel("",CAPTION_FONT,CAPTION_COLOR);
			final JProgressBar progressBar = new JProgressBar();
			addComponents(new Component[] {
				makeWrappedLabel(download.getSource().toString(),LABEL_FONT,Color.BLACK),
				progressText,
				progressBar
			});
			download.setListener(new DownloadListener() {

				public void downloadFailed(Download d, IOException ex) {
					progressText.setForeground(Color.RED);
					progressText.setText("Download failed - "+ex.toString());
				}

				public void downloadFinished(Download d) {
					progressText.setText("Download complete - "+progressText.getText());
					progressBar.setMaximum(1);
					progressBar.setValue(1);
				}

				public void downloadProgress(Download d) {
					long bytesDownloaded = d.getBytesDownloaded();
					long bytesTotal = d.getBytesTotal();
					String text = "Downloaded "+nf.format(bytesDownloaded);
					if (bytesTotal > 0) {
						text += " of "+nf.format(bytesTotal);
						if (bytesTotal>1) {
							text += " bytes";
						} else {
							text += " byte";
						}
						text += " ("+nf2.format((double)bytesDownloaded/bytesTotal)+")";
					} else {
						if (bytesDownloaded>1) {
							text += " bytes";
						} else {
							text += " byte";
						}
						
					}
					progressText.setText(text);
					progressBar.setMaximum((int)bytesTotal);
					progressBar.setIndeterminate(bytesTotal<=0);
					progressBar.setValue((int)bytesDownloaded);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}

				public void downloadStarted(Download d) {
					progressText.setText("Starting download...");
				}
				
			});
		}
		
	}
//	private HashMap<URL,DownloadUI> downloads = new HashMap<URL,DownloadUI>(); 
//	public void renderDownload(URL source, File destination, long bytesDownloaded, long bytesTotal) {
//		DownloadUI download = downloads.get(source);
//		if (download == null) { 
//			download = new DownloadUI(source,destination);
//			downloads.put(source,download);
//		}
//		download.update(bytesDownloaded,bytesTotal);
//		try {
//			Thread.sleep(200);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

	public void requestInstallDirectories(List<File> jarDirectories, List<File> libDirectories, final UIResponder responder) {
		jarComboBox = new JComboBox(jarDirectories.toArray());
		libComboBox = new JComboBox(libDirectories.toArray());
		
		
		final JPanel panel = new JPanel(new SpringLayout()) {

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
			}
			
		};
		panel.add(makeLabel("Jar install directory:", LABEL_FONT, Color.BLACK));
		panel.add(jarComboBox);
		panel.add(makeLabel("Library install directory:", LABEL_FONT, Color.BLACK));	
		panel.add(libComboBox);
		layoutSpringComponents(panel, 2, 0, 2);

	    addComponents(new Component []{
			makeWrappedLabel("Most users can leave these as default.", CAPTION_FONT, CAPTION_COLOR),
			panel,
			new JButton(new AbstractAction("Install now!") {
				public void actionPerformed(ActionEvent e) {
					File jarFile = (File)jarComboBox.getSelectedItem();
					File libFile = (File)libComboBox.getSelectedItem();
					layoutSpringComponents(panel, 2, 0, 2);
					panel.revalidate();
					jarComboBox.setEnabled(false);
					libComboBox.setEnabled(false);
					setEnabled(false);
	
				    addComponents(new Component []{
					    makeLabel("Step 2: Downloading and installing...",SECTION_FONT,Color.BLACK)
				    });
					responder.useDirectories(jarFile, libFile);
				}
			}),
			makeWrappedLabel("", CAPTION_FONT, CAPTION_COLOR),
	    });
		
		updatePanel();
	}

	private void updatePanel() {
		layoutSpringComponents(panel, 1, 6, 6);
		panel.revalidate();
		applet.getContentPane().invalidate();
		applet.repaint();
	}

}

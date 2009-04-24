package cello.tablet.installer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;

public class AppletUI implements UI {

	final protected JTextArea area = new JTextArea();
	private JPanel panel;
	public AppletUI(JApplet applet) {
		
		area.setEditable(false);
		area.setFont(new Font("Dialog",Font.PLAIN,15));
		
		final JScrollPane scrollPane = new JScrollPane(area);
		scrollPane.setVisible(false);

		

	    panel = new JPanel(new SpringLayout());
	    
	    JTextArea textArea = new JTextArea("In a couple easy steps you will be rocking and rolling with the latest JTablet for Java Plugin.");
		Component components[] = {
	    	makeLabel("JTablet 2 Plugin Installer",new Font("Dialog",Font.BOLD,20),Color.BLACK),
	    	makeWrappedLabel(
	    			"In a couple easy steps you will be rocking and rolling with the latest JTablet for Java Plugin.",
	    			new Font("Dialog",Font.PLAIN,10),new Color(0x888888)),
	    	makeLabel("Step 1: Install Directory",new Font("Dialog",Font.BOLD,15),Color.BLACK),
			new JCheckBox(new AbstractAction("Show log") {
				public void actionPerformed(ActionEvent e) {
					scrollPane.setVisible(((JCheckBox)e.getSource()).isSelected());
				}
			}),
	    	scrollPane
	    };
	    for (Component c : components) {
	    	panel.add(c);
	    }

	    //Lay out the panel.
	    SpringUtilities.makeCompactGrid(panel,
	                                    components.length, 1, //rows, cols
	                                    6, 6,        //initX, initY
	                                    6, 6);       //xPad, yPad

	    applet.getContentPane().add(panel,BorderLayout.CENTER);
	    panel.invalidate();
	}

	private JLabel makeLabel(String text, Font font, Color color) {
		JLabel label = new JLabel(text);
		label.setFont(font);
		label.setForeground(color);
//		label.set
		return label;
	}
	private JTextArea makeWrappedLabel(String text, Font font, Color color) {
		JTextArea label = new JTextArea(text);
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

	public void renderDownload(URL source, File destination, long bytesDownloaded, long bytesTotal) {
	}

	public void requestInstallDirectories(List<File> jarDirectories, List<File> libDirectories, UIResponder responder) {	
		
	}

}

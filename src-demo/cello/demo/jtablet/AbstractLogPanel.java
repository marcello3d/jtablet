package cello.demo.jtablet;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 * A simple log display component.
 * 
 * @author marcello
 */
public class AbstractLogPanel extends JScrollPane {

	protected JTextArea logArea = new JTextArea();

	/**
	 * Constructs a new abstract log panel
	 */
	public AbstractLogPanel() {
		setViewportView(logArea);
		logArea.setEditable(false);
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setPreferredSize(new Dimension(100,100));
	}
	protected void logMessage(String s) {
		logArea.append(s+"\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
		if (logArea.getLineCount()>500) {
			try {
				logArea.getDocument().remove(0, logArea.getLineStartOffset(logArea.getLineCount()-200));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

}
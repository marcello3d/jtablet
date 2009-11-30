package cello.jtablet.demo;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import cello.jtablet.TabletManager;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletFunneler;

public class DemoLogPanel extends JScrollPane {

	
	public DemoLogPanel(Component targetComponent) {
		setViewportView(logArea);
		logArea.setEditable(false);
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		setPreferredSize(new Dimension(100,100));
		
		TabletManager.getManager().addTabletListener(targetComponent, new TabletFunneler() {
			protected void handleEvent(TabletEvent ev) {
				logMessage(ev.toString());
			}		
		});
	}
	
	private JTextArea logArea = new JTextArea();
	
	private void logMessage(String s) {
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

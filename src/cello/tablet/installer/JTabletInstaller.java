package cello.tablet.installer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author marcello
 *
 */
public class JTabletInstaller extends JApplet {
	final protected JTextArea area = new JTextArea();
	@Override
	public void init() {
		area.setEditable(false);
		area.setFont(new Font("Dialog",Font.PLAIN,20));
		getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
		
		final OSInstaller installers[] = {
				new MacOSXInstaller()
		};
		
		Thread t = new Thread() {
			public void run() {
				
				String os = System.getProperty("os.name");
				String osVersion = System.getProperty("os.version");
				addLine("You appear to be using "+os+" ("+osVersion+").");
				
				try {
					Class c = Class.forName("jpen.provider.Utils");
					Method m = c.getMethod("getFullVersion");
					String version = "unknown";
					try {
						version = (String)m.invoke(null);
					} catch (Exception e) {}
					addLine("Detected JPen ("+version+")");
				} catch (Exception e) {
					addLine("JPen not detected.");
				}
				
				for (int i=0; i<installers.length; i++) {
					if (installers[i].isCompatible()) {
						installers[i].install(JTabletInstaller.this);
						return;
					}
				}
				addLine("Sorry, "+os+" is currently unsupported.");
			}
		};
		t.start();
	}
	void addLine(String s) {
		area.insert(s+"\n", area.getText().length());
		area.setCaretPosition(area.getText().length());
	}

	/**
	 * @param args
	 */
	public static void main(String []args) {
		
	}
}

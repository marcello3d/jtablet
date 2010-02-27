package cello.demo.jtablet;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletManager;
import cello.jtablet.installer.ExtensionLoader;

/**
 * Displays information about the current JTablet installation.
 * 
 * @author marcello
 */
public class InstallStatusPanel extends JPanel {
	
	private JLabel installVersion = new JLabel("Installed version");
	private JLabel installStatus = new JLabel("Install status (³"+DemoApplet.REQUIRED_VERSION+")");
	private JLabel driverStatus = new JLabel("Driver status");
	private JLabel driverException = new JLabel("Driver exception");
	
	private JLabel labels[] = {
		installVersion,
		installStatus,
		driverStatus,
		driverException
	};
	private Map<JLabel,JLabel> labelPrefixes = new HashMap<JLabel,JLabel>(labels.length);

	/**
	 * Fills in the install status details.
	 */
	public InstallStatusPanel() {
		super (new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx=0;
		gbc.gridy=0;
		gbc.insets = new Insets(5,5,5,5);
		
		for (JLabel label : labels) {
			gbc.gridx=0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.NONE;
			JLabel label2 = new JLabel(label.getText()+":",JLabel.LEFT);
			label2.setFont(label2.getFont().deriveFont(Font.BOLD));
			labelPrefixes.put(label, label2);
			add(label2, gbc);
			
			gbc.gridx=2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
//			label.setText("XXXXXXXXX");
//			label.setPreferredSize(label.getPreferredSize());
//			label.setMaximumSize(new Dimension(200,200));
			label.setText("");
			add(label, gbc);
			
			gbc.gridy++;
		}
		
		installVersion.setText(ExtensionLoader.getInstalledVersion());
		installStatus.setText(ExtensionLoader.getInstallStatus(DemoApplet.REQUIRED_VERSION).toString());
		DriverStatus status = TabletManager.getDefaultManager().getDriverStatus();
		driverStatus.setText(status.getState().toString());
		Throwable throwable = status.getThrowable();
		driverException.setText(throwable == null ? "null" : throwable.toString());
	}

}

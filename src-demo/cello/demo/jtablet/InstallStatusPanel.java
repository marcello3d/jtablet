package cello.demo.jtablet;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletManager;
import cello.jtablet.installer.JTabletExtension;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Displays information about the current JTablet installation.
 * 
 * @author marcello
 */
public class InstallStatusPanel extends ClearPanel {

    /**
	 * Fills in the install status details.
	 */
	public InstallStatusPanel() {
		super(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx=0;
		gbc.gridy=0;
		gbc.insets = new Insets(5,5,5,5);

        addLabel(gbc, "Installed version",
                      JTabletExtension.getInstalledVersion());
        addLabel(gbc, "Install status (" + DemoApplet.REQUIRED_VERSION + ")",
                      JTabletExtension.getInstallStatus(DemoApplet.REQUIRED_VERSION).toString());

		DriverStatus status = TabletManager.getDefaultManager().getDriverStatus();
		Throwable throwable = status.getThrowable();
        if (throwable != null) {
            throwable.printStackTrace();
        }

        addLabel(gbc, "Driver status", status.getState().toString());
        addLabel(gbc, "Driver error", throwable == null ? "none" : throwable.toString());
	}

    private void addLabel(GridBagConstraints gbc, String text, String value) {
        JLabel valueLabel = new JLabel(value);
        gbc.gridx=0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(text+":",JLabel.LEFT);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        add(label, gbc);

        gbc.gridx=2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(valueLabel, gbc);

        gbc.gridy++;
    }

}

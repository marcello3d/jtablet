package cello.demo.jtablet;

import cello.jtablet.DriverStatus;
import cello.jtablet.TabletManager;
import cello.jtablet.installer.JTabletExtension;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.Dimension;
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
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public InstallStatusPanel() {
		super(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx=0;
		gbc.gridy=0;
		gbc.insets = new Insets(5,5,5,5);

        addLabel(gbc, "Installed version",
                        new JLabel(JTabletExtension.getInstalledVersion()));
        addLabel(gbc, "Install status (" + DemoApplet.REQUIRED_VERSION + ")",
                        new JLabel(JTabletExtension.getInstallStatus(DemoApplet.REQUIRED_VERSION).toString()));

		DriverStatus status = TabletManager.getDefaultManager().getDriverStatus();
        addLabel(gbc, "Driver status",
                        new JLabel(status.getState().toString()));


        String exceptionString = "none";
		Throwable throwable = status.getThrowable();
        if (throwable != null) {
            throwable.printStackTrace();
            exceptionString = throwable.toString();
        }
        addLabel(gbc, "Driver error", exceptionString);
	}

    private void addLabel(GridBagConstraints gbc, String text, String value) {
        if (value.contains("\n")) {
            JTextArea textArea = new JTextArea(value);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setBorder(null);
            textArea.setOpaque(false);
            textArea.setPreferredSize(new Dimension(300,300));
            addLabel(gbc, text, textArea);
        } else {
            addLabel(gbc, text, new JLabel(value));
        }
    }
    private void addLabel(GridBagConstraints gbc, String text, Component value) {
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        JLabel label = new JLabel(text+":",JLabel.LEFT);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        add(label, gbc);

        gbc.gridx=2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(value, gbc);

        gbc.gridy++;
    }

}

import java.applet.Applet;
import java.awt.Label;

import cello.jtablet.installer.JTabletExtension;

public class DetectionApplet extends Applet {
    public void init() {
        // Displays alert if an incompatible version of JTablet is detected
        if (!JTabletExtension.checkCompatibility(this, "1.2.0")) {
            return;
        }
        add(new Label("Installed JTablet version: " + 
                JTabletExtension.getInstalledVersion()));
    }
}

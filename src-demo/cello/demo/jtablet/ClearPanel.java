package cello.demo.jtablet;

import javax.swing.JPanel;
import java.awt.LayoutManager;

/**
 * @author Marcello Bastea-Forte (marcello@cellosoft.com)
 * @created 2010.12.26
 */
public class ClearPanel extends JPanel {
    public ClearPanel() {}
    public ClearPanel(LayoutManager layout) {
        super(layout);
    }
    {
        // Mac OS X tweaks the colors of JTabbedPanes
        setOpaque(false);
    }
}

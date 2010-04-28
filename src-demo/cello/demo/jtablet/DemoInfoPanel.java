package cello.demo.jtablet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletFunneler;

/**
 * This panel displays tablet information for  
 * 
 * @author marcello
 */
public class DemoInfoPanel extends JPanel {

	private JLabel typeValue = new JLabel("Type");
	private JLabel xValue = new JLabel("X");
	private JLabel yValue = new JLabel("Y");
	private JLabel pressureValue = new JLabel("Pressure");
	private JLabel tiltXValue = new JLabel("Tilt X");
	private JLabel tiltYValue = new JLabel("Tilt Y");
	private JLabel sidePressureValue = new JLabel("Side Pressure");
	private JLabel rotationValue = new JLabel("Rotation");
	private JLabel ppsValue = new JLabel("Rate");
	private JLabel rawButtonValue = new JLabel("Raw Buttons");
	private JLabel labels[] = {
		ppsValue,
		typeValue,
		xValue,
		yValue,
		pressureValue,
		sidePressureValue,
		tiltXValue,
		tiltYValue,
		rotationValue,
		rawButtonValue
	};
	private Map<JLabel,JLabel> labelPrefixes = new HashMap<JLabel,JLabel>(labels.length);
	
	private NumberFormat nf = DecimalFormat.getNumberInstance();
	{ 
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(true);
	}
	
	/**
	 * Constructs a new DemoInfoPanel targetting a given component
	 * @param targetComponent
	 */
	public DemoInfoPanel(Component targetComponent) {
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
			label.setText("XXXXXXXXX");
			label.setPreferredSize(label.getPreferredSize());
			label.setText("");
			add(label, gbc);
			
			gbc.gridy++;
		}

		TabletManager.getDefaultManager().addTabletListener(targetComponent, new TabletFunneler() {
			private int eventCount;
			private long lastTime = System.currentTimeMillis();
			protected void handleEvent(TabletEvent ev) {
				eventCount++;
				long time = System.currentTimeMillis();
				if (time-lastTime > 1000) {
					ppsValue.setText(eventCount+" pps");
					lastTime = time;
					eventCount = 0;
				}
				TabletDevice device = ev.getDevice();
				setText(typeValue, 			device.getType().toString(), 			Support.YES);
				setText(xValue,				nf.format(ev.getFloatX()),				Support.YES);
				setText(yValue,				nf.format(ev.getFloatY()),				Support.YES);
				setText(pressureValue,		nf.format(ev.getPressure()),			device.getPressureSupport());
				setText(sidePressureValue,	nf.format(ev.getSidePressure()),		device.getSidePressureSupport());
				char degree = (char)186;
				setText(tiltXValue,			nf.format(Math.toDegrees(ev.getTiltX()))+degree,		device.getTiltSupport());
				setText(tiltYValue,			nf.format(Math.toDegrees(ev.getTiltY()))+degree,		device.getTiltSupport());
				setText(rotationValue,		nf.format(Math.toDegrees(ev.getRotation()))+degree,		device.getRotationSupport());
				setText(rawButtonValue, String.format("%1$#04x", ev.getRawTabletButtonMask()), device.getButtonSupport());
			}
			private void setText(JLabel label, String value, TabletDevice.Support supported) {
				if (supported != null) {
					JLabel prefix = labelPrefixes.get(label);
					switch (supported) {
					case NO:
						label.setEnabled(false);
						prefix.setEnabled(false);
						label.setForeground(new Color(0x800000));
						break;
					case YES:
						label.setEnabled(true);
						prefix.setEnabled(true);
						label.setForeground(new Color(0x008000));
						break;
					case UNKNOWN:
						label.setEnabled(true);
						prefix.setEnabled(true);
						label.setForeground(new Color(0x808000));
						break;
					}
				}
				label.setText(value);

			}
		});
	}

}

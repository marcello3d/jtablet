package cello.jtablet.demo;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.TabletDevice.Support;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletFunneler;

public class DemoInfoPanel extends JPanel {

	private JLabel typeValue = new JLabel("Type");
	private JLabel xValue = new JLabel("X");
	private JLabel yValue = new JLabel("Y");
	private JLabel pressureValue = new JLabel("Pressure");
	private JLabel tiltXValue = new JLabel("Tilt X");
	private JLabel tiltYValue = new JLabel("Tilt Y");
	private JLabel sidePressureValue = new JLabel("Side Pressure");
	private JLabel rotationValue = new JLabel("Rotation");
	private JLabel labels[] = {
		typeValue,
		xValue,
		yValue,
		pressureValue,
		tiltXValue,
		tiltYValue,
		sidePressureValue,
		rotationValue
	};
	
	private NumberFormat nf = DecimalFormat.getNumberInstance();
	{ 
		nf.setMaximumFractionDigits(4);
		nf.setGroupingUsed(true);
	}
	
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
			add(label2, gbc);
			
			gbc.gridx=2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			label.setText("XXXXXXXXXXXXX");
			label.setPreferredSize(label.getPreferredSize());
			add(label, gbc);
			
			gbc.gridy++;
		}

		TabletManager.addScreenTabletListener(new TabletFunneler() {
			protected void handleEvent(TabletEvent ev) {
				TabletDevice device = ev.getDevice();
				setText(typeValue, 			device.getType().toString(), 			Support.SUPPORTED);
				setText(xValue,				nf.format(ev.getRealX()),				Support.SUPPORTED);
				setText(yValue,				nf.format(ev.getRealY()),				Support.SUPPORTED);
				setText(pressureValue,		nf.format(ev.getPressure()),			device.supportsPressure());
				setText(sidePressureValue,	nf.format(ev.getTangentialPressure()),	device.supportsTangentialPressure());
				setText(tiltXValue,			nf.format(ev.getTiltX())+" rad",		device.supportsTilt());
				setText(tiltYValue,			nf.format(ev.getTiltY())+" rad",		device.supportsTilt());
				setText(rotationValue,		nf.format(ev.getRotation())+" rad",		device.supportsRotation());
			}
			private void setText(JLabel label, String value, TabletDevice.Support supported) {
				if (supported != null) {
					switch (supported) {
					case NONE:
						label.setForeground(new Color(0x800000));
						break;
					case SUPPORTED:
						label.setForeground(new Color(0x008000));
						break;
					case UNKNOWN:
						label.setForeground(new Color(0x800000));
						break;
					}
				}
				label.setText(value);

			}
		});
	}

}

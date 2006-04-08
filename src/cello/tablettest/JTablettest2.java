package cello.tablettest;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import java.applet.*;
import java.lang.reflect.*;

import cello.lui.*;

import cello.tablet.*;

public final class JTablettest2 extends Applet
				implements	Runnable {
	private Thread mainThread;

	private static final String DISTNAME = "JTablet";
	private static final String APPNAMEVERSION = "JTablet Test v2.0";

	lLabel labels[], labels2[];
	
	lLabel version_label, cursorNameLabel, physicalIdLabel, typeLabel;
	JTablet tablet;

	public static void main(String args[]) {
		Frame frame = new Frame(APPNAMEVERSION);
		JTablettest2 app = new JTablettest2();
		frame.add(app, BorderLayout.CENTER);
		app.init();
		app.start();
		frame.setSize(new Dimension(400,300));
		frame.show();
	}	
	
	public void init() {
		setLayout(new BorderLayout());
		
		lPanel rootPanel = new lRootPanel();
		lPanel panel = new lPanel();
		rootPanel.add(panel,BorderLayout.CENTER);
		
		try {
			tablet = new JTablet();
		} catch (JTabletException e) {
			e.printStackTrace();
		}
		
		

		String s = APPNAMEVERSION;
		s += " (plugin v.:"+tablet.getVersion()+")";

		version_label = new lLabel(s);
		rootPanel.add(version_label,BorderLayout.NORTH);
		
		panel.setLayout(new GridLayout(0,3));
		
		panel.add( new lLabel("Item"));
		panel.add( new lLabel("Value"));
		panel.add( new lLabel("Range"));
		
		panel.add( new lLabel("Name:"));
		panel.add( cursorNameLabel = new lLabel("n/a"));
		panel.add( new lLabel("n/a"));
		
		panel.add( new lLabel("Physical Id:"));
		panel.add( physicalIdLabel = new lLabel("n/a"));
		panel.add( new lLabel("n/a"));
		
		panel.add( new lLabel("Type:"));
		panel.add( typeLabel = new lLabel("n/a") );
		panel.add( new lLabel("n/a"));
		
		String strings[] = {
			"Cursor",
			"Buttons",
			"X",
			"Y",
			"Z",
			"Pressure",
			"TPressure",
			"Orient.Azi",
			"Orient.Alt",
			"Orient.Twi",
			"Rotate.Pitch",
			"Rotate.Roll",
			"Rotate.Yaw"
		};
		labels = new lLabel[strings.length];
		labels2 = new lLabel[strings.length];
		
		for (int i=0; i<strings.length; i++) {
			panel.add( new lLabel(strings[i]+":"));
			panel.add( labels[i] = new lLabel("n/a"));
			panel.add( labels2[i] = new lLabel("n/a"));
		}
		
		add(rootPanel,BorderLayout.CENTER);
	}
	public void update(Graphics g) {
		paint(g);
	}
	public void start() {
		if (mainThread==null)
			mainThread = new Thread(this);
		mainThread.start();
	}
	public void stop() {
		mainThread = null;
	}
	public void run() {
		JTabletCursor cursor = null;
		while (true) {
			try {
				tablet.poll();
			} catch (JTabletException ex) {
				ex.printStackTrace();
			}
			if (cursor != tablet.getCursor()) {
				cursor = tablet.getCursor();
				cursorNameLabel.setText(cursor.getName());
				cursorNameLabel.repaint();
				physicalIdLabel.setText(""+cursor.getPhysicalId());
				physicalIdLabel.repaint();
				String text = "";
				switch (cursor.getCursorTypeSpecific()) {
					case JTabletCursor.TYPE_STYLUS:
						text = "Stylus";
						break;
					case JTabletCursor.TYPE_AIRBRUSH:
						text = "Airbrush";
						break;
					case JTabletCursor.TYPE_4DMOUSE:
						text = "4D Mouse";
						break;
					case JTabletCursor.TYPE_LENS_CURSOR:
						text = "Lens Cursor";
						break;
					
				}
				switch (cursor.getCursorType()) {
					case JTabletCursor.TYPE_UNKNOWN:
						text = "Unknown";
						break;
					case JTabletCursor.TYPE_PEN_TIP:
						text += ": Tip";
						break;
					case JTabletCursor.TYPE_PEN_ERASER:
						text += ": Eraser";
						break;
					case JTabletCursor.TYPE_PUCK:
						text += ": Puck";
						break;

				}
				typeLabel.setText(text);
				typeLabel.repaint();
				for (int i=0; i<labels2.length; i++) {
					(labels2[i]).setText(cursor==null ? "null" : "["+cursor.getDataMinimum(i)+","+cursor.getDataMaximum(i)+"] supported="+cursor.isDataSupported(i));
					(labels2[i]).repaint();
				}
			}
			
			for (int i=0; i<labels.length; i++) {
				(labels[i]).setText(cursor==null ? "null" : ""+cursor.getData(i));
				(labels[i]).repaint();
			}
			try {
				Thread.yield();
				Thread.sleep(10);
			} catch (Exception e) {}
		}
	}

}

package cello.jtablet.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.events.TabletAdapter;
import cello.jtablet.events.TabletEvent;

/**
 * Simple demo component that handles tablet input to draw lines 
 * 
 * @author marcello
 */
public class DemoSurface extends JComponent {

	private BufferedImage bi;
	private Graphics2D g2d;
	
	/**
	 * 
	 */
	public DemoSurface() {
		createBuffer();
		TabletManager.addTabletListener(null, new TabletAdapter() {

			double lastX,lastY,lastPressure;
			

			@Override
			public void cursorDragged(TabletEvent ev) {
				double x = ev.getRealX();
				double y = ev.getRealY();
				double pressure = ev.getPressure()*20;
				if (lastPressure>0) {
					GeneralPath p = new GeneralPath();
					double angle = Math.atan2(y-lastY, x-lastX);
					double deg90 = Math.PI / 2;
					
					//    +---_______
					//               -----+
					//    +               +
					//          _____-----+
					//    +-----
					p.moveTo((float)(lastX+Math.cos(angle+deg90)*lastPressure),(float)(lastY+Math.sin(angle+deg90)*lastPressure));
					p.lineTo((float)(lastX+Math.cos(angle-deg90)*lastPressure),(float)(lastY+Math.sin(angle-deg90)*lastPressure));
					p.lineTo((float)(x+Math.cos(angle-deg90)*pressure),        (float)(y+Math.sin(angle-deg90)*pressure));
					p.lineTo((float)(x+Math.cos(angle+deg90)*pressure),        (float)(y+Math.sin(angle+deg90)*pressure));
					p.closePath();
					g2d.setColor(ev.getDevice() == TabletDevice.STYLUS_ERASER ? Color.WHITE : Color.BLACK);
					g2d.fill(new Ellipse2D.Double(x-pressure,y-pressure,2*pressure,2*pressure));
					g2d.fill(p);
					repaint();
				}
				lastX = x;
				lastY = y;
				lastPressure = pressure;
			}

			@Override
			public void cursorMoved(TabletEvent ev) {
			}

			@Override
			public void cursorPressed(TabletEvent ev) {
				lastX = ev.getRealX();
				lastY = ev.getRealY();
				lastPressure = ev.getPressure();
			}

			@Override
			public void cursorReleased(TabletEvent ev) {
			}
			
		});
	}

	private void createBuffer() {
		int width = Math.max(1,getWidth());
		int height= Math.max(1,getHeight());
		bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		g2d = bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0, width, height);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (getWidth()>bi.getWidth() || getHeight()>bi.getHeight()) {
			BufferedImage old = bi;
			createBuffer();
			g2d.drawImage(old,0,0,null);
		}
		g.drawImage(bi, 0, 0, null);
	}

	public String toString() {
		return getClass().getSimpleName();
	}
	
}

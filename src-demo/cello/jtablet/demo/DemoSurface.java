package cello.jtablet.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
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

	private static final int MAX_RADIUS = 20;
	private BufferedImage bi;
	private Graphics2D g2d;
	
	private double lastX,lastY,lastPressure;
	
	private AffineTransform at = new AffineTransform();
	private boolean onSurface = false;
	private boolean dragging = false;
	
	
	/**
	 * 
	 */
	public DemoSurface() {
		createBuffer();
		TabletManager.getManager().addTabletListener(this, new TabletAdapter() {

			boolean dragged = false;
			@Override
			public void cursorExited(TabletEvent ev) {
				onSurface = false;
				repaint();
			}
			
			
			@Override
			public synchronized void cursorDragged(TabletEvent ev) {
				onSurface = true;
				dragged = true;
				float x = ev.getRealX();
				float y = ev.getRealY();
				float pressure = ev.getPressure() * MAX_RADIUS;
				if (lastPressure>0) {
					
					try {
						g2d.setTransform(at.createInverse());
					} catch (NoninvertibleTransformException e) {
						e.printStackTrace();
					}

					g2d.setColor(ev.getDevice().getType() == TabletDevice.Type.ERASER ? Color.WHITE : Color.BLACK);
					g2d.fill(new Ellipse2D.Float(x-pressure,y-pressure,2*pressure-0.5f,2*pressure-0.5f));
					
					double angle = Math.atan2(y-lastY, x-lastX);
					
					// distance between points = c
					double c = Math.hypot(x-lastX, y-lastY);
					// radial difference = b - a
					double b = lastPressure-pressure;
					// remaining side length
					// c*c = a*a + b*b ->
					double a = Math.sqrt(c*c-b*b);
					
					double angle2 = Math.atan2(a, b);

					
					// This can happen if the endpoint circles contain each other...
					if (!Double.isNaN(angle2)) {
						
						double sin1 = Math.sin(angle-angle2);
						double cos1 = Math.cos(angle-angle2);
						double sin2 = Math.sin(angle+angle2);
						double cos2 = Math.cos(angle+angle2);
						GeneralPath p = new GeneralPath();
						p.moveTo((float)(lastX+cos1*lastPressure),(float)(lastY+sin1*lastPressure));
						p.lineTo((float)(lastX+cos2*lastPressure),(float)(lastY+sin2*lastPressure));
						p.lineTo((float)(x+cos2*pressure),        (float)(y+sin2*pressure));
						p.lineTo((float)(x+cos1*pressure),   	  (float)(y+sin1*pressure));
						p.closePath();

						g2d.fill(p);
						
					}
					repaint();
				}
				lastX = x;
				lastY = y;
				lastPressure = pressure;
			}

			@Override
			public void cursorMoved(TabletEvent ev) {
				onSurface = true;
				lastX = ev.getRealX();
				lastY = ev.getRealY();
				lastPressure = ev.getPressure();
				dragged = false;
				repaint();
			}

			@Override
			public void cursorPressed(TabletEvent ev) {
				lastX = ev.getRealX();
				lastY = ev.getRealY();
				lastPressure = ev.getPressure();
				dragged = false;
				dragging = true;
			}

			@Override
			public void cursorReleased(TabletEvent ev) {
				if (!dragged) {
					double x = ev.getRealX();
					double y = ev.getRealY();
					double pressure = ev.getPressure()*20;
					g2d.setColor(ev.getDevice().getType() == TabletDevice.Type.ERASER ? Color.WHITE : Color.BLACK);
					g2d.fill(new Ellipse2D.Double(x-pressure,y-pressure,2*pressure,2*pressure));
				}
				dragged = false;
				dragging = false;
			}
			@Override
			public void cursorScrolled(TabletEvent ev) {
				at.translate(ev.getDeltaX()*10, ev.getDeltaY()*10);
				repaint();
			}
			@Override
			public void cursorGestured(TabletEvent ev) {
				switch (ev.getType()) {
					case ZOOMED:
						float zoom = 1+ev.getZoom();
						at.translate(getWidth()/2, getHeight()/2);
						at.scale(zoom, zoom);
						at.translate(zoom*getWidth()/-2, zoom*getHeight()/-2);
						repaint();
						break;
					case ROTATED:
						at.translate(getWidth()/2, getHeight()/2);
						at.rotate(-ev.getRotation());
						at.translate(getWidth()/-2, getHeight()/-2);
						repaint();
						break;
					case SWIPED:
						at.setToIdentity();
						repaint();
						break;
				}
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
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (getWidth()>bi.getWidth() || getHeight()>bi.getHeight()) {
			BufferedImage old = bi;
			createBuffer();
			g2d.drawImage(old,0,0,null);
		}
		Graphics2D gg = (Graphics2D)g;
		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gg.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		gg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		gg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setColor(Color.GRAY);
		gg.fill(g.getClip());
		AffineTransform t = gg.getTransform();
		gg.transform(at);
		gg.drawImage(bi, 0, 0, null);
		gg.setTransform(t);
		if (onSurface || dragging) {
			gg.setColor(Color.LIGHT_GRAY);
			if (lastPressure == 0) {
				gg.draw(new Ellipse2D.Double(lastX-MAX_RADIUS,lastY-MAX_RADIUS,2*MAX_RADIUS,2*MAX_RADIUS));
			} else { 
				gg.draw(new Ellipse2D.Double(lastX-lastPressure,lastY-lastPressure,2*lastPressure,2*lastPressure));
			}
		}
	}

	public String toString() {
		return getClass().getSimpleName()+"@"+hashCode();
	}
}

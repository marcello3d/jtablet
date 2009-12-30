package cello.jtablet.demo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManagerFactory;
import cello.jtablet.event.TabletAdapter;
import cello.jtablet.event.TabletEvent;

/**
 * Simple demo component that handles tablet input to draw lines 
 * 
 * @author marcello
 */
public class DemoSurface extends JComponent {

	private static final Stroke BASIC_STROKE = new BasicStroke(1.0f);
	private static final int MAX_RADIUS = 20;
	protected static final float SCROLL_SCALE = 2;
	private BufferedImage bi;
	private Graphics2D g2d;
	
	private float lastX,lastY,lastPressure;
	
	private AffineTransform at = new AffineTransform();
	private AffineTransform atInv = new AffineTransform();
	private boolean onSurface = false;
	private boolean dragging = false;
	
	private Area drawShape = new Area(), canvasArea = new Area();
	private boolean erasing = false;
	private Stroke zoomedStroke = BASIC_STROKE;

	Ellipse2D.Double cursorShape;
	/**
	 * 
	 */
	public DemoSurface() {
		createBuffer();
		TabletManagerFactory.getManager().addTabletListener(this, new TabletAdapter() {

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
				boolean rightClick = (ev.getModifiersEx()&InputEvent.BUTTON3_DOWN_MASK) != 0;
				if (rightClick && ev.getDevice().getType() == TabletDevice.Type.MOUSE) {
					pressure = MAX_RADIUS;
				}
				if (lastPressure>0) {

					boolean erasing = rightClick || ev.getDevice().getType() == TabletDevice.Type.ERASER;
					if (erasing != DemoSurface.this.erasing) {
						DemoSurface.this.erasing = erasing;
					}
					drawRoundedLine(x, y, pressure, lastX, lastY, lastPressure);
				}
				lastX = x;
				lastY = y;
				lastPressure = pressure;
				updateCursor();
			}
			@Override
			public void cursorMoved(TabletEvent ev) {
				onSurface = true;
				lastX = ev.getRealX();
				lastY = ev.getRealY();
				lastPressure = ev.getPressure();
				dragged = false;
				updateCursor();
			}

			@Override
			public void cursorPressed(TabletEvent ev) {
				lastX = ev.getRealX();
				lastY = ev.getRealY();
				drawCurrentShape();
				lastPressure = ev.getPressure();
				dragged = false;
				dragging = true;
			}

			@Override
			public void cursorReleased(TabletEvent ev) {
				if (!dragged) {
					double x = ev.getRealX();
					double y = ev.getRealY();
					double radius = MAX_RADIUS;
					erasing = ev.getDevice().getType() == TabletDevice.Type.ERASER;
					fill(new Ellipse2D.Double(x-radius,y-radius,2*radius,2*radius));
				}
				dragged = false;
				dragging = false;
				updateCursor();
			}
			@Override
			public void cursorScrolled(TabletEvent ev) {
				
				at.preConcatenate(AffineTransform.getTranslateInstance(ev.getScrollX()*SCROLL_SCALE, ev.getScrollY()*SCROLL_SCALE));
//				Point2D p = atInv.transform(new Point2D.Float(ev.getRealX()+ev.getScrollX(),ev.getRealY()+ev.getScrollY()), null);
//				at.translate(p.getX(), p.getY());
				updateTransform();
			}


			private void updateTransform() {
				try {
					atInv = at.createInverse();
				} catch (NoninvertibleTransformException e) {
					e.printStackTrace();
				}
				// To calculate the scale independent of rotation we need Math.sqrt(m00^2 + m01^2)
				double scaleX = at.getScaleX();
				double shearX = at.getShearX();
				double scale = scaleX*scaleX+shearX*shearX;
				zoomedStroke = new BasicStroke(1/(float)Math.sqrt(scale));
				repaint();
			}
			
			public void updateCursor() {
				if (cursorShape != null) {
					repaint(cursorShape);
				}
				if (lastPressure == 0) {
					cursorShape = new Ellipse2D.Double(lastX-MAX_RADIUS,lastY-MAX_RADIUS,2*MAX_RADIUS,2*MAX_RADIUS);
				} else { 
					cursorShape = new Ellipse2D.Double(lastX-lastPressure,lastY-lastPressure,2*lastPressure,2*lastPressure);
				}
				repaint(cursorShape);
			}
			
			@Override
			public void cursorGestured(TabletEvent ev) {
				Point2D transformedPoint;
				switch (ev.getType()) {
					case ZOOMED:
						float zoom = 1+ev.getZoomFactor();
						transformedPoint = atInv.transform(ev.getPoint2D(), null);
						at.translate(transformedPoint.getX(), transformedPoint.getY());
						at.scale(zoom, zoom);
						at.translate(-transformedPoint.getX(), -transformedPoint.getY());
						updateTransform();
						break;
					case ROTATED:
						transformedPoint = atInv.transform(ev.getPoint2D(), null);
						at.translate(transformedPoint.getX(), transformedPoint.getY());
						at.rotate(-ev.getRotation());
						at.translate(-transformedPoint.getX(), -transformedPoint.getY());
						updateTransform();
						break;
					case SWIPED:
						at.setToIdentity();
						updateTransform();
						break;
				}
			}
		});
	}




	protected void drawCurrentShape() {
		g2d.setColor(erasing ? Color.WHITE : Color.BLACK);
		Shape repaintShape;
		synchronized (drawShape) {
			g2d.fill(drawShape);
			repaintShape = at.createTransformedShape(drawShape);
			drawShape.reset();
		}
		repaint(repaintShape);
	}




	/**
	 * Draws the convex hull of two circles.
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param x2
	 * @param y2
	 * @param radius2
	 */
	private void drawRoundedLine(double x, double y, double radius,
			double x2, double y2, double radius2) {
		
		// Draw the two endpoint circles
		fill(new Ellipse2D.Double(x-radius,y-radius,2*radius,2*radius));
		fill(new Ellipse2D.Double(x2-radius2,y2-radius2,2*radius2,2*radius2));
		
		// Next calculate the points where tangential lines to the two circles t 
		
		
		double angle = Math.atan2(y-y2, x-x2);
		
		// distance between points = c
		double c = Math.hypot(x-x2, y-y2);
		// radial difference = b - a
		double b = radius2-radius;
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
			
			Path2D.Double p = new Path2D.Double();
			p.moveTo((x2+cos1*radius2), (y2+sin1*radius2));
			p.lineTo((x2+cos2*radius2), (y2+sin2*radius2));
			p.lineTo((x+cos2*radius),   (y+sin2*radius));
			p.lineTo((x+cos1*radius),   (y+sin1*radius));
			p.closePath();

			fill(p);
		}
	}

	protected void repaint(Shape s) {
		Rectangle r = s.getBounds();
		r.grow(1, 1);
		repaint(r);
	}
	
	private void fill(Shape s) {
		Area a = new Area(atInv.createTransformedShape(s));
		synchronized (drawShape) {
			drawShape.add(a);
			Area a2 = new Area(simplifyShape(drawShape));
			drawShape.reset();
			drawShape.add(a2);
			drawShape.intersect(canvasArea);
		}
		repaint(s);
	}




	private Shape simplifyShape(Shape drawShape) {
		PathIterator it = 
		new FlatteningPathIterator(drawShape.getPathIterator(new AffineTransform()), 2.0, 20);
		GeneralPath gp = new GeneralPath(it.getWindingRule());
		float coords[] = new float[6];
		while (!it.isDone()) {
			switch (it.currentSegment(coords)) {
				case PathIterator.SEG_CLOSE:
					gp.closePath();
					break;
				case PathIterator.SEG_CUBICTO:
					gp.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					break;
				case PathIterator.SEG_LINETO:
					gp.lineTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_MOVETO:
					gp.moveTo(coords[0], coords[1]);
					break;
				case PathIterator.SEG_QUADTO:
					gp.quadTo(coords[0], coords[1], coords[2], coords[3]);
					break;
			}
			it.next();
		}
		return gp;
	}




	/**
	 * Creates a new buffer based on the current component size 
	 */
	private void createBuffer() {
		int width = Math.max(1,getWidth());
		int height= Math.max(1,getHeight());
		bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		canvasArea = new Area(new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
		g2d = bi.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0,0, width, height);
		setSuperSmoothRenderingHints(g2d);
		drawRoundedLine(40, 40, 100, 200, 200, 100);
		drawRoundedLine(200, 200, 100, 250, 700, 100);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		// Component size changed? update buffer size
		if (getWidth()>bi.getWidth() || getHeight()>bi.getHeight()) {
			BufferedImage old = bi;
			createBuffer();
			g2d.drawImage(old,0,0,null);
		}
		Graphics2D gg = (Graphics2D)g;
		setSuperSmoothRenderingHints(gg);
		g.setColor(Color.GRAY);
		gg.fill(g.getClip());
		AffineTransform t = gg.getTransform();
		gg.transform(at);
		gg.drawImage(bi, 0, 0, null);
		gg.setColor(erasing ? Color.WHITE : Color.DARK_GRAY);
		synchronized (drawShape) {
			gg.fill(drawShape);
			gg.setColor(Color.RED);
			gg.setStroke(zoomedStroke);
			gg.draw(drawShape);
		}
		gg.setTransform(t);
		gg.setStroke(BASIC_STROKE);
		if (onSurface || dragging) {
			gg.setColor(Color.LIGHT_GRAY);
			gg.draw(cursorShape);

		}
	}




	private void setSuperSmoothRenderingHints(Graphics2D gg) {
		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gg.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		gg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		gg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	public String toString() {
		return getClass().getSimpleName()+"@"+hashCode();
	}
}

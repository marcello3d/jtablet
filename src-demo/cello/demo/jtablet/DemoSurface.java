package cello.demo.jtablet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;

import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.event.TabletAdapter;
import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletListener;

/**
 * Simple demo component that handles tablet input to draw lines 
 * 
 * @author marcello
 */
public class DemoSurface extends JComponent {

	private static final Stroke BASIC_STROKE = new BasicStroke(1.0f);
	private static final int MAX_RADIUS = 20;
	private static final float SCROLL_SCALE = 2;
	
	private BufferedImage bufferedSurface;
	private Graphics2D bufferedSurfaceGraphics;
	
	private float lastX,lastY,lastPressure;
	
	private AffineTransform at = new AffineTransform();
	private AffineTransform atInv = new AffineTransform();
	private boolean onSurface = false;
	private boolean dragging = false;
	
	private boolean erasing = false;

	private Ellipse2D.Double cursorShape;
	
	private TabletEventQueue eventQueue = new TabletEventQueue();
	
	private boolean pollForEvents = false;
	/**
	 * 
	 */
	public DemoSurface() {
		createBackgroundBuffer();

        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                                                  SystemColor.control.darker(),
                                                  SystemColor.control.darker().darker()));
		
		// Add listener that sucks tablet events into a queue
		TabletManager.getDefaultManager().addTabletListener(this, eventQueue);	
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				requestFocus();
			}
		});
	}
	
	/**
	 * This is called when the component is added to a parent.
	 * @see JComponent#addNotify()
	 */
	@Override
	public void addNotify() {
		startPollingForEvents();
		
		super.addNotify();
	}

	private synchronized void startPollingForEvents() {
		pollForEvents = true;
		
		eventPollThread = new Thread() {
			public void run() {
				try {
					while (pollForEvents) {
						eventQueue.take().fireEvent(eventHandler);
					}
				} catch (InterruptedException e) {
					// Done!
				}
			}
		};
		eventPollThread.setDaemon(true);
		eventPollThread.start();
	}
	/**
	 * This is called when the component is removed from a parent.
	 * @see JComponent#removeNotify()
	 */
	@Override
	public void removeNotify() {
		stopPollingForEvents();
		
		super.removeNotify();
	}

	private synchronized void stopPollingForEvents() {
		if (eventPollThread != null) {
			pollForEvents = false;
			eventPollThread.interrupt();
			eventPollThread = null;
		}
	}
	
	/**
	 * This class receives the tablet events from the event queue.
	 */
	private TabletListener eventHandler = new TabletAdapter() {

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
			float x = ev.getFloatX();
			float y = ev.getFloatY();
			float pressure = ev.getPressure() * MAX_RADIUS;
			boolean rightClick = (ev.getModifiersEx()&InputEvent.BUTTON3_DOWN_MASK) != 0;
			if (ev.getDevice().getPressureSupport() != TabletDevice.Support.YES) {
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
			lastX = ev.getFloatX();
			lastY = ev.getFloatY();
			lastPressure = ev.getPressure();
			dragged = false;
			updateCursor();
		}

		@Override
		public void cursorPressed(TabletEvent ev) {
			lastX = ev.getFloatX();
			lastY = ev.getFloatY();
			lastPressure = ev.getPressure();
			dragged = false;
			dragging = true;
		}

		@Override
		public void cursorReleased(TabletEvent ev) {
			if (!dragged) {
				double x = ev.getFloatX();
				double y = ev.getFloatY();
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
			updateTransform();
		}


		private void updateTransform() {
			try {
				atInv = at.createInverse();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
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
	};
	private Thread eventPollThread;


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
		
		// Draw the two circles at either end of the line
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
			
			// Build a quad that's tangential to the two circles 
			Path2D.Double p = new Path2D.Double();
			p.moveTo((x2+cos1*radius2), (y2+sin1*radius2));
			p.lineTo((x2+cos2*radius2), (y2+sin2*radius2));
			p.lineTo((x+cos2*radius),   (y+sin2*radius));
			p.lineTo((x+cos1*radius),   (y+sin1*radius));
			p.closePath();

			// fill it in
			fill(p);
		}
	}

	/**
	 * Repaint an anti-aliased shape 
	 * @param shape
	 */
	protected void repaint(Shape shape) {
		repaint(shape,1);
	}
	/**
	 * Repaint a shape with a given amount of margin
	 * @param shape
	 * @param margin
	 */
	protected void repaint(Shape shape, int margin) {
		Rectangle r = shape.getBounds();
		r.grow(margin, margin);
		repaint(r);
	}
	
	/**
	 * Draw filled shape to the buffered surface independent of the viewport 
	 * @param shape
	 */
	private void fill(Shape shape) {
		bufferedSurfaceGraphics.setColor(erasing ? Color.WHITE : Color.BLACK);
		Shape repaintShape = atInv.createTransformedShape(shape);
		bufferedSurfaceGraphics.fill(repaintShape);

		// To calculate the scale independent of rotation we need Math.sqrt(m00^2 + m01^2)
		double scaleX = at.getScaleX();
		double shearX = at.getShearX();
		double scale = scaleX*scaleX+shearX*shearX;
		
		repaint(shape,(int)scale);
	}




	/**
	 * Creates a new buffer based on the current component size 
	 */
	private void createBackgroundBuffer() {
		int width = Math.max(1,getWidth());
		int height= Math.max(1,getHeight());
		bufferedSurface = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		bufferedSurfaceGraphics = bufferedSurface.createGraphics();
		bufferedSurfaceGraphics.setColor(Color.WHITE);
		bufferedSurfaceGraphics.fillRect(0,0, width, height);
		setSuperSmoothRenderingHints(bufferedSurfaceGraphics);
	}
	
	@Override
	protected void paintComponent(Graphics gg) {
		// Component size changed? update buffer size
		if (getWidth()>bufferedSurface.getWidth() || getHeight()>bufferedSurface.getHeight()) {
			BufferedImage old = bufferedSurface;
			createBackgroundBuffer();
			bufferedSurfaceGraphics.drawImage(old,0,0,null);
		}
		Graphics2D g = (Graphics2D)gg;
        RenderingHints hints = g.getRenderingHints();

        setSuperSmoothRenderingHints(g);

        gg.setColor(Color.GRAY);
		g.fill(gg.getClip());
		AffineTransform t = g.getTransform();
		g.transform(at);
		g.drawImage(bufferedSurface, 0, 0, null);

		g.setTransform(t);
		g.setStroke(BASIC_STROKE);
		if (onSurface || dragging) {
			g.setColor(Color.LIGHT_GRAY);
			g.draw(cursorShape);
		}

        g.setRenderingHints(hints);
	}

	private void setSuperSmoothRenderingHints(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	public String toString() {
		return getClass().getSimpleName()+"@"+hashCode()+"["+getBounds()+"]";
	}
}

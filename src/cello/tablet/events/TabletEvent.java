package cello.tablet.events;

import java.awt.Component;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.Serializable;


/**
 * Stores input event data.
 * 
 * @author Marcello
 */
public class TabletEvent implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private final transient Component source;
	private transient Point2D point;
	private final Point2D originalPoint;
	private final float pressure;
	
	private final long when;
	private final Type type;
	private final Button button;
	private final TabletDevice device;

	private transient boolean consumed = false;
	

	/**
	 * @param source
	 * @param type
	 * @param when
	 * @param device
	 * @param point
	 * @param button
	 * @param pressure
	 */
	private TabletEvent(Component source, Type type, long when,
			TabletDevice device, Point2D point, Button button, float pressure) {
		super();
		this.source = source;
		this.type = type;
		this.when = when;
		this.device = device;
		this.point = point;
		this.originalPoint = point;
		this.button = button;
		this.pressure = pressure;
	}
	
	@Override
	public String toString() {
		return "InputEvent["+
				"source="+source+
				",type="+type+
				",when="+when+
				",device="+device+
				",point="+point+
				",button="+button+
				",pressure="+pressure+
				"]";
	}

	/**
	 * Type of input event this is
	 */
	public static enum Type {
		/** button/stylus tip pressed */
		PRESSED,
		/** button/stylus tip released */
		RELEASED,
		/** cursor moved */
		MOVED,
		/** cursor dragged */
		DRAGGED,
		/** new device */
		NEW_DEVICE,
		/** level changed */
		LEVEL_CHANGED
	}
	
	/**
	 * Represents a mouse button
	 */
	public static enum Button {
		/** left mouse button */
		LEFT,
		/** center mouse button */
		CENTER,
		/** right mouse button */
		RIGHT,
		/** no mouse button */
		NONE
	}
	
	/**
	 * @param l
	 */
	public void fireEvent(TabletListener l) {
		switch (type) {
		case PRESSED:
			l.cursorPressed(this);
			break;
		case RELEASED:
			l.cursorReleased(this);
			break;
		case DRAGGED:
			l.cursorDragged(this);
			break;
		case LEVEL_CHANGED:
			l.levelChanged(this);
			break;
		case MOVED:
			l.cursorMoved(this);
			break;
		case NEW_DEVICE:
			l.newDevice(this);
			break;
		}
	}
	
	/**
	 * Constructs a new InputEvent
	 * 
	 * @param source
	 * @param type 
	 * @param when
	 * @param button
	 * @param point
	 */
	public TabletEvent(Component source, Type type, long when, 
			Point2D point, Button button) {
		this(source,type,when,null,point,button,0);
	}
	/**
	 * Constructs a new InputEvent
	 * 
	 * @param source
	 * @param type 
	 * @param when
	 * @param point 
	 * @param pressure 
	 */
	public TabletEvent(Component source, Type type, long when, 
			Point2D point, float pressure) {
		this(source,type,when,null,point,null,pressure);
	}
	/**
	 * Constructs a new InputEvent
	 * 
	 * @param source
	 * @param type 
	 * @param when
	 * @param device
	 */
	public TabletEvent(Component source, Type type, long when, TabletDevice device) {
		this(source,type,when,device,null,null,0);
	}
	
	

	
	/**
	 * @return the point
	 */
	public Point2D getPoint() {
		return point;
	}

	/**
	 * @return the when
	 */
	public long getWhen() {
		return when;
	}

	/**
	 * @return the button
	 */
	public Button getButton() {
		return button;
	}

	/**
	 * 
	 */
	public void consume() {
		consumed = true;
	}
	/**
	 * @return whether this event was consumed
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * @return the source
	 */
	public Component getSource() {
		return source;
	}

	/**
	 * @return the pressure
	 */
	public float getPressure() {
		return pressure;
	}

	/**
	 * @return the device
	 */
	public TabletDevice getDevice() {
		return device;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Transforms the point by the given transform. 
	 * @param transform
	 */
	public void transform(AffineTransform transform) {
		if (point != null) {
			// don't want to modify original point
			if (point==originalPoint)
				point = transform.transform(point, null);
			else 
				transform.transform(point, point);
		}
	}

	/**
	 * Resets the (potentially transformed) point to the original point.
	 */
	public void resetTransform() {
		point = originalPoint;
	}

	/**
	 * Inverse transforms the point by given transform.
	 * @param transform
	 */
	public void inverseTransform(AffineTransform transform) {
		if (point!=null)
			try {
				// don't want to modify original point
				if (point==originalPoint)
					point = transform.inverseTransform(point, null);
				else
					transform.inverseTransform(point, point);
			} catch (NoninvertibleTransformException e) {
				// XXX: this a problem?
				e.printStackTrace();
			}
	}
}

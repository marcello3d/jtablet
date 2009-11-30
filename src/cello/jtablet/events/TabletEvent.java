package cello.jtablet.events;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import cello.jtablet.TabletDevice;


/**
 * Stores input event data.
 * 
 * @author Marcello
 */
public class TabletEvent extends MouseEvent implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private final float x,y;
	private final float pressure;
	private final float tangentialPressure;
	
	private final float tiltX,tiltY;
	private final float rotation;
	
	private final float deltaX,deltaY;
	private final float zoom;
	
	
	private final Type type;
	private final TabletDevice device;


	/**
	 * @param source
	 * @param type
	 * @param when
	 * @param device
	 * @param x 
	 * @param y 
	 * @param modifiers
	 * @param button 
	 * @param pressure
	 * @param tiltX 
	 * @param tiltY 
	 * @param tangentialPressure 
	 * @param rotation 
	 * @param deltaX 
	 * @param deltaY 
	 * @param zoom 
	 */
	public TabletEvent(Component source, Type type, long when, int modifiers, 
						TabletDevice device, float x, float y, float pressure,
						float tiltX, float tiltY, float tangentialPressure,
						float rotation, float deltaX, float deltaY, float zoom,
						int button) {
		
		super(source, type.getId(), when, modifiers,
				(int)x, (int)y, 0, false, button);

		this.type = type;
		this.device = device;
		this.x = x;
		this.y = y;
		this.pressure = pressure;
		this.tiltX = tiltX;
		this.tiltY = tiltY;
		this.tangentialPressure = tangentialPressure;
		this.rotation = rotation;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.zoom = zoom;
	}
	/**
	 * Wrap a mouseevent as a TabletEvent
	 * @param e
	 * @param type 
	 * @param device 
	 */
	public TabletEvent(MouseEvent e, Type type, TabletDevice device) {
		super(e.getComponent(),e.getID(), e.getWhen(), e.getModifiersEx(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
		this.x = e.getX();
		this.y = e.getY();
		this.type = type;
		this.pressure = (e.getModifiersEx() & BUTTON1_DOWN_MASK) != 0 ? 1.0f : 0;
		this.tiltX = 0;
		this.tiltY = 0;
		this.tangentialPressure = 0;
		this.rotation = 0;
		this.device = device;
		this.deltaX = 0;
		this.deltaY = 0;
		this.zoom = 0;
	} 

	


	public TabletEvent(Component source, Type type, long when, int modifiers, TabletDevice device, 
			float x, float y, 
			float pressure, 
			float tiltX, float tiltY,
			float tangentialPressure, 
			float rotation, 
			int button) {
		this(source,type,when,modifiers,device,x,y,pressure,tiltX,tiltY,tangentialPressure,rotation,0,0,0,button);
	}
	public TabletEvent(Component source, Type type, long when, int modifiers, TabletDevice device, 
			float x, float y) {
		this(source,type,when,modifiers,device,x,y,0,0,0,0,0,0,0,0,NOBUTTON);
	}
	public TabletEvent(Component source, Type type, long when, int modifiers, TabletDevice device,
			float x, float y, 
			float rotation, 
			float deltaX, float deltaY,
			float zoom) {
		this(source,type,when,modifiers,device,x,y,0,0,0,0,rotation,deltaX,deltaY,zoom,NOBUTTON);
	}
	public TabletEvent(Component source, Type type, long when, TabletDevice device, int modifiers, 
			float x, float y,
			int button) {
		this(source,type,when,modifiers,device,x,y,0,0,0,0,0,0,0,0,button);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getClass().getSimpleName());
		sb.append("[");
		sb.append("").append(type);
		sb.append(",when=").append(getWhen());
		sb.append(",device=").append(device);
		sb.append(",x=").append(x);
		sb.append(",y=").append(y);
		
        if (getModifiersEx() != 0) {
            sb.append(",modifiers=").append(getModifiersExText(getModifiersEx()));
        }
        if (pressure != 0) {
        	sb.append(",pressure=").append(pressure);
        }
        if (tangentialPressure != 0) {
            sb.append(",tanPressure=").append(tangentialPressure);
        }
        if (tiltX != 0 || tiltY != 0) {
            sb.append(",tilt=").append(tiltX).append(',').append(tiltY);
        }
        if (rotation != 0) {
        	sb.append(",rotation=").append(rotation);
        }
        if (deltaX != 0 || deltaY != 0) {
        	sb.append(",delta=").append(deltaX).append(',').append(deltaY);
        }
        if (zoom != 0) {
        	sb.append(",zoom=").append(zoom);
        }
        sb.append("] on ").append(source);
        
		return sb.toString();
	}

	private static final int ID_START = RESERVED_ID_MAX + 1200;
	/**
	 * Type of input event this is
	 */
	public static enum Type {
		/** button/stylus tip pressed */
		PRESSED			( MOUSE_PRESSED ),
		/** button/stylus tip released */
		RELEASED		( MOUSE_RELEASED ),
		/** cursor enters proximity and/or component */
		ENTERED			( MOUSE_ENTERED ),
		/** cursor exits proximity and/or component*/
		EXITED			( MOUSE_EXITED ),
		/** cursor moved */
		MOVED			( MOUSE_MOVED ),
		/** cursor dragged */
		DRAGGED			( MOUSE_DRAGGED ),
		/** scrolled */
		SCROLLED		( MOUSE_WHEEL ),
		/** new device */
		NEW_DEVICE		( ID_START ),
		/** level changed */
		LEVEL_CHANGED	( ID_START+1 ),
		/** gestured */
		ZOOMED			( ID_START+2 ),
		/** gestured */
		ROTATED			( ID_START+3 ),
		/** gestured */
		SWIPED			( ID_START+4 )
		;
		
		private final int id;
		
		private Type(int id) {
			this.id = id;
		}
		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}
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
		case ENTERED:
			l.cursorEntered(this);
			break;
		case EXITED:
			l.cursorExited(this);
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
		case SCROLLED:
			l.cursorScrolled(this);
			break;
		case ZOOMED:
		case ROTATED:
		case SWIPED:
			l.cursorGestured(this);
			break;
		}
	}
	
	

	
	/**
	 * @return the (possibly) fractional x coordinate
	 */
	public float getRealX() {
		return x;
	}
	/**
	 * @return the (possibly) fractional y coordinate
	 */
	public float getRealY() {
		return y;
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
	 * @return tangential pressure from -1 to 1.
	 */
	public float getTangentialPressure() {
		return tangentialPressure;
	}


	/**
	 * @return tiltX in radians
	 */
	public float getTiltX() {
		return tiltX;
	}


	/**
	 * @return tilt Y in radians
	 */
	public float getTiltY() {
		return tiltY;
	}
	
	/**
	 * @return rotation in radians
	 */
	public float getRotation() {
		return rotation;
	}

	public TabletEvent withPoint(Component c, float x, float y) {
		return new TabletEvent(c, type, getWhen(), getModifiersEx(), 
				device, x, y, pressure,
				tiltX, tiltY, tangentialPressure,
				rotation,this.deltaX,this.deltaY,zoom,
				getButton());
	}

	public TabletEvent withPoint(float x, float y) {
		return withPoint(getComponent(), x, y);
	}

	/**
	 * Returns a translated version of this TabletEvent
	 * @param c new component for translated event
	 * @param deltaX
	 * @param deltaY
	 * @return the new TabletEvent
	 */
	public TabletEvent translated(Component c, float deltaX, float deltaY) {
		return withPoint(c, x+deltaX, y+deltaY);
	}
	/**
	 * @param type
	 * @return a new TabletEvent with the given type
	 */
	public TabletEvent withType(Type type) {
		return new TabletEvent((Component)source, type, getWhen(), getModifiersEx(), 
				device, x + deltaX, y + deltaY, pressure,
				tiltX, tiltY, tangentialPressure,
				rotation,this.deltaX,this.deltaY,zoom,
				getButton());
	}

	/**
	 * Returns a translated version of this TabletEvent
	 * @param deltaX
	 * @param deltaY
	 * @return the new TabletEvent
	 */
	public TabletEvent translated(float deltaX, float deltaY) {
		return translated(getComponent(), deltaX, deltaY);
	}

	/**
	 * @return the deltaX
	 */
	public float getDeltaX() {
		return deltaX;
	}

	/**
	 * @return the deltaY
	 */
	public float getDeltaY() {
		return deltaY;
	}

	/**
	 * @return the zoom
	 */
	public float getZoom() {
		return zoom;
	}
}

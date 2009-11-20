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
	 */
	public TabletEvent(Component source, Type type, long when, int modifiers, 
						TabletDevice device, float x, float y, float pressure,
						float tiltX, float tiltY, float tangentialPressure,
						float rotation,
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
		this.pressure = (e.getModifiersEx() & (BUTTON1_DOWN_MASK|BUTTON2_DOWN_MASK|BUTTON3_DOWN_MASK)) != 0 ? 1.0f : 0;
		this.tiltX = 0;
		this.tiltY = 0;
		this.tangentialPressure = 0;
		this.rotation = 0;
		this.device = device;
	} 


	/**
	 * Constructs a new InputEvent
	 * 
	 * @param source
	 * @param type 
	 * @param when
	 * @param device 
	 * @param x
	 * @param y 
	 * @param modifiers 
	 * @param button 
	 */
	public TabletEvent(Component source, Type type, long when, TabletDevice device, int modifiers, float x, float y, int button) {
		this(source,type,when,modifiers,
				device,x,y,0,
				0,0,0,0,
				button);
	}
	/**
	 * Constructs a new InputEvent
	 * 
	 * @param source
	 * @param type 
	 * @param when
	 * @param device 
	 * @param modifiers 
	 * @param x 
	 * @param y  
	 * @param pressure 
	 */
	public TabletEvent(Component source, Type type, long when, TabletDevice device, int modifiers, float x, float y, float pressure) {
		this(source,type,when,modifiers,
				device,x,y,pressure,
				0,0,0,0,
				NOBUTTON);
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
		this(source,type,when,0,
				device,0,0,0,
				0,0,0,0,
				NOBUTTON);
	}
	
	

	@Override
	public String toString() {
		return "InputEvent["+
				"source="+source+
				",type="+type+
				",when="+getWhen()+
				",device="+device+
				",x="+x+
				",y="+y+
				",modifiers="+MouseEvent.getMouseModifiersText(getModifiers())+
				",pressure="+pressure+
				"]";
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
		/** button/stylus tip pressed */
		ENTERED			( MOUSE_ENTERED ),
		/** button/stylus tip released */
		EXITED			( MOUSE_EXITED ),
//		/** button/stylus tip pressed */
//		PRESSURED		( ID_START ),
//		/** button/stylus tip released */
//		UNPRESSURED		( ID_START+1 ),
		/** cursor moved */
		MOVED			( MOUSE_MOVED ),
		/** cursor dragged */
		DRAGGED			( MOUSE_DRAGGED ),
		/** new device */
		NEW_DEVICE		( ID_START+2 ),
		/** level changed */
		LEVEL_CHANGED	( ID_START+3 );
		
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

	/**
	 * Returns a translated version of this TabletEvent
	 * @param c new component for translated event
	 * @param deltaX
	 * @param deltaY
	 * @return the new TabletEvent
	 */
	public TabletEvent translated(Component c, float deltaX, float deltaY) {
		return new TabletEvent(c, type, getWhen(), getModifiersEx(), 
				device, x + deltaX, y + deltaY, pressure,
				tiltX, tiltY, tangentialPressure,
				rotation,
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

}

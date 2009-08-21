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
	 */
	protected TabletEvent(Component source, Type type, long when, int modifiers, 
						TabletDevice device, float x, float y, float pressure,
						float tiltX, float tiltY, float tangentialPressure,
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
				0,0,0,
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
				0,0,0,
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
				0,0,0,
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
		/** cursor moved */
		MOVED			( MOUSE_MOVED ),
		/** cursor dragged */
		DRAGGED			( MOUSE_DRAGGED ),
		/** new device */
		NEW_DEVICE		( ID_START ),
		/** level changed */
		LEVEL_CHANGED	( ID_START+1 );
		
		private final int id;
		Type(int id) {
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
	 * @return the tangentialPressure
	 */
	public float getTangentialPressure() {
		return tangentialPressure;
	}


	/**
	 * @return the tiltX
	 */
	public float getTiltX() {
		return tiltX;
	}


	/**
	 * @return the tiltY
	 */
	public float getTiltY() {
		return tiltY;
	}
}

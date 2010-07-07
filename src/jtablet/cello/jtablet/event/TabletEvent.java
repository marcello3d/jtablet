/*!
 * Copyright (c) 2009 Marcello Bastéa-Forte (marcello@cellosoft.com)
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 * 
 *     2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 * 
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */

package cello.jtablet.event;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.Serializable;

import cello.jtablet.TabletDevice;


/**
 * An event that indicates cursor input occurred on the given component. This
 * class extends {@link MouseEvent} and  provides a similar and extended API
 * for accessing tablet events.
 * 
 * <p>You likely will not be constructing this class yourself.</p>
 * 
 * <p><b>Implementation Note:</b> {@link #isPopupTrigger()} is unsupported.</p>
 * 
 * @author Marcello
 * @since 1.2.5
 */
public class TabletEvent extends MouseEvent implements Serializable {
	
	private static final long serialVersionUID = 1;
	
	private final float x,y;
	private final float pressure;
	private final float sidePressure;
	
	private final float tiltX,tiltY;
	private final float rotation;
	
	private final float scrollX,scrollY;
	private final float zoomFactor;
	
	private final int rawTabletButtonMask;
	
	
	private final Type type;
	private final TabletDevice device;


	/**
	 * Constructs a new {@linkplain TabletEvent} with all the trimmings... 
	 * yourself.
	 *
	 * @param source the component which "sensed" (if creating for adding to the {@link TabletManager} event queue) or is "targeted at" (if creating for reception by a {@link TabletListener})
	 * @param type the specific kind of event this is
	 * @param when the time this event occurred
	 * @param device the {@link TabletDevice} which generated this event
	 * @param x the horizontal location of this event relative to the {@code source} component
	 * @param y the vertical location of this event relative to the {@code source} component
	 * @param modifiers the modifier keys down during event (e.g. shift, ctrl, alt, meta) Either extended {@code _DOWN_MASK} or old {@code _MASK} modifiers should be used, but both models should not be mixed in one event. Use of the extended modifiers is preferred.
	 * @param button which of the Java-defined buttons have changed state. {@code NOBUTTON}, {@code BUTTON1}, {@code BUTTON2} or {@code BUTTON3}.
	 * @param pressure the amount of pressure sensed by the {@code device}
	 * @param tiltX the tilt angle (in radians) along the x-axis sensed by the {@code device}
	 * @param tiltY the tilt angle (in radians) along the y-axis sensed by the {@code device}
	 * @param sidePressure the amount of side pressure sensed by the {@code device}
	 * @param rotation the amount of barrel rotation (in radians) sensed by the {@code device}
	 * @param deltaX the amount of horizontal scrolling sensed by the {@code device} (from e.g. a mousewheel or multitouch)
	 * @param deltaY the amount of vertical scrolling sensed by the {@code device} (from e.g. a mousewheel or multitouch)
	 * @param zoom the degree of zoom factor sensed by the {@code device} (from e.g. a multitouch gesture)
	 * @param rawTabletButtonMask the state of all the {@code device}'s buttons
	 */
	public TabletEvent(Component source, Type type, long when, int modifiers, 
						TabletDevice device, float x, float y, float pressure,
						float tiltX, float tiltY, float sidePressure,
						float rotation, float deltaX, float deltaY, float zoom,
						int button, int rawTabletButtonMask) {
		
		super(source, type.getId(), when, modifiers,
				(int)x, (int)y, 0, false, button);

		this.type = type;
		this.device = device;
		this.x = x;
		this.y = y;
		this.pressure = pressure;
		this.tiltX = tiltX;
		this.tiltY = tiltY;
		this.sidePressure = sidePressure;
		this.rotation = rotation;
		this.scrollX = deltaX;
		this.scrollY = deltaY;
		this.zoomFactor = zoom;
		this.rawTabletButtonMask = rawTabletButtonMask;
	}
	
	/**
	 * Wrap a {@link MouseEvent} as a TabletEvent.
	 *
	 * @param e the source event to base this off of
	 * @param type the more-precise type of tablet event this is
	 * @param device the device which generated this event
	 */
	public TabletEvent(MouseEvent e, Type type, TabletDevice device) {
		super(e.getComponent(),e.getID(), e.getWhen(), e.getModifiersEx(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
		this.x = e.getX();
		this.y = e.getY();
		this.type = type;
		this.pressure = (e.getModifiersEx() & BUTTON1_DOWN_MASK) != 0 ? 1.0f : 0;
		this.tiltX = 0;
		this.tiltY = 0;
		this.sidePressure = 0;
		this.rotation = 0;
		this.device = device;
		this.scrollX = 0;
		this.scrollY = 0;
		this.zoomFactor = 0;
		this.rawTabletButtonMask = 0;
	} 


	/**
	 * Wrap a {@link MouseEvent} as a TabletEvent.
	 *
	 * @param e the source event to base this off of
	 * @param type the more-precise type of tablet event this is
	 * @param device the device which generated this event
	 * @param c the component which "sensed" (if creating for adding to the {@link TabletManager} event queue) or is "targeted at" (if creating for reception by a {@link TabletListener})
	 * @param x the horizontal location of this event relative to the {@code source} component
	 * @param y the vertical location of this event relative to the {@code source} component
	 */
	public TabletEvent(MouseEvent e, Type type, TabletDevice device, Component c, float x, float y) {
		super(c, e.getID(), e.getWhen(), e.getModifiersEx(), (int)x, (int)y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
		this.x = x;
		this.y = y;
		this.type = type;
		this.pressure = (e.getModifiersEx() & BUTTON1_DOWN_MASK) != 0 ? 1.0f : 0;
		this.tiltX = 0;
		this.tiltY = 0;
		this.sidePressure = 0;
		this.rotation = 0;
		this.device = device;
		this.scrollX = 0;
		this.scrollY = 0;
		this.zoomFactor = 0;
		this.rawTabletButtonMask = 0;
	} 



	/**
	 * Constructs a new {@linkplain TabletEvent} with all the trimmings...
	 * @param source the component which "sensed" (if creating for adding to the {@link TabletManager} event queue) or is "targeted at" (if creating for reception by a {@link TabletListener})
	 * @param type the specific kind of event this is
	 * @param when the time this event occurred
	 * @param modifiers the modifier keys down during event (e.g. shift, ctrl, alt, meta) Either extended {@code _DOWN_MASK} or old {@code _MASK} modifiers should be used, but both models should not be mixed in one event. Use of the extended modifiers is preferred.
	 * @param rawTabletButtonMask the state of all the {@code device}'s buttons
	 * @param device the {@link TabletDevice} which generated this event
	 * @param x the horizontal location of this event relative to the {@code source} component
	 * @param y the vertical location of this event relative to the {@code source} component
	 * @param pressure the amount of pressure sensed by the {@code device}
	 * @param tiltX the tilt angle (in radians) along the x-axis sensed by the {@code device}
	 * @param tiltY the tilt angle (in radians) along the y-axis sensed by the {@code device}
	 * @param sidePressure the amount of side pressure sensed by the {@code device}
	 * @param rotation the amount of barrel rotation (in radians) sensed by the {@code device}
	 * @param button which of the Java-defined buttons have changed state. {@code NOBUTTON}, {@code BUTTON1}, {@code BUTTON2} or {@code BUTTON3}.
	 */
	public TabletEvent(Component source, Type type, long when, int modifiers, int rawTabletButtonMask, 
			TabletDevice device, 
			float x, float y, 
			float pressure, 
			float tiltX, float tiltY,
			float sidePressure, 
			float rotation, 
			int button) {
		this(source,type,when,modifiers,device,x,y,pressure,tiltX,tiltY,sidePressure,rotation,0,0,0,button,rawTabletButtonMask);
	}
	/**
	 * Constructs a new {@linkplain TabletEvent} with some of the trimmings...
	 *
	 * @param source the component which "sensed" (if creating for adding to the {@link TabletManager} event queue) or is "targeted at" (if creating for reception by a {@link TabletListener})
	 * @param type the specific kind of event this is
	 * @param when the time this event occurred
	 * @param modifiers the modifier keys down during event (e.g. shift, ctrl, alt, meta) Either extended {@code _DOWN_MASK} or old {@code _MASK} modifiers should be used, but both models should not be mixed in one event. Use of the extended modifiers is preferred.
	 * @param rawTabletButtonMask the state of all the {@code device}'s buttons
	 * @param device the {@link TabletDevice} which generated this event
	 * @param x the horizontal location of this event relative to the {@code source} component
	 * @param y the vertical location of this event relative to the {@code source} component
	 */
	public TabletEvent(Component source, Type type, long when, int modifiers, int rawTabletButtonMask, 
			TabletDevice device, float x, float y) {
		this(source,type,when,modifiers,device,x,y,0,0,0,0,0,0,0,0,NOBUTTON,rawTabletButtonMask);
	}
	/**
	 * Constructs a new {@linkplain TabletEvent} with some of the trimmings...
	 *
	 * @param source the component which "sensed" (if creating for adding to the {@link TabletManager} event queue) or is "targeted at" (if creating for reception by a {@link TabletListener})
	 * @param type the specific kind of event this is
	 * @param when the time this event occurred
	 * @param modifiers the modifier keys down during event (e.g. shift, ctrl, alt, meta) Either extended {@code _DOWN_MASK} or old {@code _MASK} modifiers should be used, but both models should not be mixed in one event. Use of the extended modifiers is preferred.
	 * @param rawTabletButtonMask the state of all the {@code device}'s buttons
	 * @param device the {@link TabletDevice} which generated this event
	 * @param x the horizontal location of this event relative to the {@code source} component
	 * @param y the vertical location of this event relative to the {@code source} component
	 * @param rotation the amount of barrel rotation (in radians) sensed by the {@code device}
	 * @param deltaX the amount of horizontal scrolling sensed by the {@code device} (from e.g. a mousewheel or multitouch)
	 * @param deltaY the amount of vertical scrolling sensed by the {@code device} (from e.g. a mousewheel or multitouch)
	 * @param zoom the degree of zoom factor sensed by the {@code device} (from e.g. a multitouch gesture)
	 */
	public TabletEvent(Component source, Type type, long when, int modifiers, TabletDevice device,
			float x, float y, 
			float rotation, 
			float deltaX, float deltaY,
			float zoom) {
		this(source,type,when,modifiers,device,x,y,0,0,0,0,rotation,deltaX,deltaY,zoom,NOBUTTON,0);
	}
	/**
	 * Constructs a new {@linkplain TabletEvent} with some of the trimmings...
	 *
	 * @param source the component which "sensed" (if creating for adding to the {@link TabletManager} event queue) or is "targeted at" (if creating for reception by a {@link TabletListener})
	 * @param type the specific kind of event this is
	 * @param when the time this event occurred
	 * @param device the {@link TabletDevice} which generated this event
	 * @param modifiers the modifier keys down during event (e.g. shift, ctrl, alt, meta) Either extended {@code _DOWN_MASK} or old {@code _MASK} modifiers should be used, but both models should not be mixed in one event. Use of the extended modifiers is preferred.
	 * @param x the horizontal location of this event relative to the {@code source} component
	 * @param y the vertical location of this event relative to the {@code source} component
	 * @param button which of the Java-defined buttons have changed state. {@code NOBUTTON}, {@code BUTTON1}, {@code BUTTON2} or {@code BUTTON3}.
	 */
	public TabletEvent(Component source, Type type, long when, TabletDevice device, int modifiers, 
			float x, float y,
			int button) {
		this(source,type,when,modifiers,device,x,y,0,0,0,0,0,0,0,0,button,0);
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getClass().getName());
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
        if (sidePressure != 0) {
            sb.append(",sidePressure=").append(sidePressure);
        }
        if (tiltX != 0 || tiltY != 0) {
            sb.append(",tilt=").append(tiltX).append(',').append(tiltY);
        }
        if (rotation != 0) {
        	sb.append(",rotation=").append(rotation);
        }
        if (scrollX != 0 || scrollY != 0) {
        	sb.append(",scroll=").append(scrollX).append(',').append(scrollY);
        }
        if (zoomFactor != 0) {
        	sb.append(",zoom=").append(zoomFactor);
        }
        if (rawTabletButtonMask != 0) {
        	sb.append(",rawTabletButtons=");
        	boolean first = true;
        	for (int i=0; i<31; i++) {
        		if ((rawTabletButtonMask&(1<<i)) != 0) {
        			if (first) {
        				first = false;
        			} else {
        				sb.append(',');
        			}
        			sb.append(Integer.toString(i+1));
        		}
        	}
        }
        sb.append("] on ").append(source);
        
		return sb.toString();
	}

	private static final int ID_START = RESERVED_ID_MAX + 1200;
	/**
	 * This enum is used to specify the possible types of a
	 * {@link TabletEvent}.
	 *
	 * @since 1.2.5
	 */
	public static enum Type {
		/** 
		 * Button or stylus tip pressed.
		 *
		 * @since 1.2.5
		 */
		PRESSED			( MOUSE_PRESSED ),
		
		/** 
		 * Button or stylus tip released.
		 *
		 * @since 1.2.5
		 */
		RELEASED		( MOUSE_RELEASED ),
		
		/** 
		 * Cursor enters proximity and/or component.
		 *
		 * @since 1.2.5
		 */
		ENTERED			( MOUSE_ENTERED ),
		
		/** 
		 * Cursor exits proximity and/or component.
		 *
		 * @since 1.2.5
		 */
		EXITED			( MOUSE_EXITED ),
		
		/** 
		 * Cursor moved with no button pressed.
		 *
		 * @since 1.2.5
		 */
		MOVED			( MOUSE_MOVED ),
		
		/** 
		 * Cursor moved with button pressed.
		 *
		 * @since 1.2.5
		 */
		DRAGGED			( MOUSE_DRAGGED ),
		
		/** 
		 * Level changed. This occurs when the stylus changes
		 * pressure or tilt, but does not move.
		 *
		 * @since 1.2.5
		 */
		LEVEL_CHANGED	( ID_START ),
		
		/** 
		 * Mouse wheel scroll or multi-touch scroll (on Mac OS X).
		 * @see TabletEvent#getScrollX()
		 * @see TabletEvent#getScrollY()
		 *
		 * @since 1.2.5
		 */
		SCROLLED		( ID_START+1 ),
		
		// gesture events...
		
		/** 
		 * Zoom gesture (pinching on a Mac OS X multi-touch device).
		 * Use {@link TabletEvent#getZoomFactor()} to get the zoom
		 * factor amount.
		 *
		 * @see TabletEvent#getZoomFactor()
		 * @since 1.2.5
		 */
		ZOOMED			( ID_START+2 ),
		
		/** 
		 * Rotate gesture (rotating two fingers on a Mac OS X
		 * multi-touch device). Use {@link TabletEvent#getRotation()}
		 * to get the rotation amount.
		 *
		 * @see TabletEvent#getRotation()
		 * @since 1.2.5
		 */
		ROTATED			( ID_START+3 ),
		
		/** 
		 * Swipe gesture (moving three fingers on a Mac OS X
		 * multi-touch device). Use {@link TabletEvent#getScrollX()}
		 * and {@link TabletEvent#getScrollY()} to get the swipe
		 * direction.
		 *
		 * @see TabletEvent#getScrollX()
		 * @see TabletEvent#getScrollY()
		 * @since 1.2.5
		 */
		SWIPED			( ID_START+4 );
		
		private final int id;
		
		private Type(int id) {
			this.id = id;
		}
		/**
		 * Returns the AWTEvent id associated with this event type.
		 *
		 * @return the id
		 * @since 1.2.5
		 */
		public int getId() {
			return id;
		}
	}
	
	/**
	 * Triggers this event on the given {@link TabletListener}.
	 * 
	 * @param listener the listener to trigger on
	 * @since 1.2.5
	 */
	public void fireEvent(TabletListener listener) {
		if (type == null) {
			return;
		}
		switch (type) {
		case PRESSED:
			listener.cursorPressed(this);
			break;
		case RELEASED:
			listener.cursorReleased(this);
			break;
		case ENTERED:
			listener.cursorEntered(this);
			break;
		case EXITED:
			listener.cursorExited(this);
			break;
		case DRAGGED:
			listener.cursorDragged(this);
			break;
		case LEVEL_CHANGED:
			listener.levelChanged(this);
			break;
		case MOVED:
			listener.cursorMoved(this);
			break;
		case SCROLLED:
			listener.cursorScrolled(this);
			break;
		case ZOOMED:
		case ROTATED:
		case SWIPED:
			listener.cursorGestured(this);
			break;
		}
	}
	
	/**
	 * Returns the fractional {@link Point2D} of the mouse/tablet
	 * cursor (if available).
	 * 
	 * @return the (possibly) fractional point
	 * @since 1.2.5
	 */
	public Point2D.Float getPoint2D() {
		return new Point2D.Float(x,y);
	}

	
	/**
	 * Returns the fractional x position of the mouse/tablet
	 * cursor (if available).
	 *
	 * <p>You can determine if the current device supports fractional
	 * points with {@link TabletDevice#getFloatSupport()}
	 * via {@link #getDevice()}.</p>
	 * 
	 * @return the (possibly) fractional x coordinate
	 * @since 1.2.5
	 */
	public float getFloatX() {
		return x;
	}
	/**
	 * Returns the fractional y position of the mouse/tablet cursor
	 * (if available).
	 *
	 * <p>You can determine if the current device supports fractional
	 * points with {@link TabletDevice#getFloatSupport()}
	 * via {@link #getDevice()}.</p>
	 * 
	 * @return the (possibly) fractional y coordinate
	 * @since 1.2.5
	 */
	public float getFloatY() {
		return y;
	}

	/**
	 * Returns the current pressure value of the tablet pressure. 
	 * 
	 * <p>You can determine if the current device supports pressure
	 * with {@link TabletDevice#getPressureSupport()} via
	 * {@link #getDevice()}.</p>
	 *
	 * @see TabletDevice#getPressureSupport()
	 * @return the pressure from 0 to 1
	 * @since 1.2.5
	 */
	public float getPressure() {
		return pressure;
	}

	/**
	 * Returns the tablet device associated with this event. You can
	 * determine capabilities available by inspecting the device object.
	 * 
	 * @see TabletDevice 
	 * @return the device
	 * @since 1.2.5
	 */
	public TabletDevice getDevice() {
		return device;
	}

	/**
	 * Returns the {@link Type} for this event.
	 *
	 * @see Type
	 * @return the event type
	 * @since 1.2.5
	 */
	public Type getType() {
		return type;
	}


	/**
	 * Returns the current tablet side pressure. (E.g. the side
	 * wheel on a Wacom airbrush tool.)
	 * 
	 * <p>You can determine if the current device supports side
	 * pressure with  {@link TabletDevice#getSidePressureSupport()}
	 * via {@link #getDevice()}.</p>
	 * 
	 * @return side pressure from 0 to 1.
	 * @since 1.2.5
	 */
	public float getSidePressure() {
		return sidePressure;
	}


	/**
	 * Returns the current horizontal tilt angle in radians.
	 * 
	 * <p>You can determine if the current device supports side
	 * pressure with {@link TabletDevice#getTiltSupport()} via
	 * {@link #getDevice()}.</p>
	 * 
	 * @return tilt X in radians
	 * @since 1.2.5
	 */
	public float getTiltX() {
		return tiltX;
	}


	/**
	 * Returns the current horizontal tilt angle in radians.
	 * 
	 * <p>You can determine if the current device supports side
	 * pressure with  {@link TabletDevice#getTiltSupport()} via
	 * {@link #getDevice()}.</p>
	 * 
	 * @return tilt Y in radians
	 * @since 1.2.5
	 */
	public float getTiltY() {
		return tiltY;
	}
	
	/**
	 * Returns either the rotational position of a tablet, or the
	 * rotational amount for a {@link Type#ROTATED} gesture event.
	 * 
	 * <p>You can determine if the current device supports rotation
	 * with {@link TabletDevice#getRotationSupport()} via
	 * {@link #getDevice()}.
	 * 
	 * @return rotation in radians
	 * @since 1.2.5
	 */
	public float getRotation() {
		return rotation;
	}

	/**
	 * Returns the raw tablet button mask independent of any user
	 * mapping with regards to "left" or "right." This may return
	 * zero for emulated devices.
	 *  
	 * @return a bitset given by the tablet driver
	 * @since 1.2.5
	 */
	public int getRawTabletButtonMask() {
		return rawTabletButtonMask;
	}


	/**
	 * Returns the amount scrolled in the x direction for a
	 * {@link Type#SCROLLED} gesture event.
	 *
	 * @return the amount scrolled in the x direction
	 * @since 1.2.5
	 */
	public float getScrollX() {
		return scrollX;
	}

	/**
	 * Returns the amount scrolled in the y direction for a
	 * {@link Type#SCROLLED} gesture event.
	 *
	 * @return the amount scrolled in the y direction
	 * @since 1.2.5
	 */
	public float getScrollY() {
		return scrollY;
	}

	/**
	 * Returns the magnification amount for a {@link Type#ZOOMED}
	 * gesture event.
	 *
	 * @return the zoom
	 * @since 1.2.5
	 */
	public float getZoomFactor() {
		return zoomFactor;
	}
	

	/**
	 * Returns a copy of this {@linkplain TabletEvent} with the given
	 * component and coordinates.
	 * 
	 * @see #withPoint(float, float)
	 * @param component component for the new event
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return a new {@linkplain TabletEvent} with the new component and coordinates
	 * @since 1.2.5
	 */
	public TabletEvent withPoint(Component component, float x, float y) {
		return new TabletEvent(component, type, getWhen(), getModifiersEx(), 
				device, x, y, pressure,
				tiltX, tiltY, sidePressure,
				rotation,scrollX,scrollY,zoomFactor,
				getButton(),rawTabletButtonMask);
	}

	/**
	 * Returns a copy of this {@linkplain TabletEvent} with the given
	 * coordinates.
	 *
	 * @see #withPoint(Component, float, float)
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return a new {@linkplain TabletEvent} with the new coordinates
	 * @since 1.2.5
	 */
	public TabletEvent withPoint(float x, float y) {
		return withPoint(getComponent(), x, y);
	}

	/**
	 * Returns a copy of this {@linkplain TabletEvent} with translated
	 * coordinates.
	 * 
	 * @see #translated(float, float)
	 * @param c component for translated event
	 * @param deltaX the amount to translate the x-coordinate
	 * @param deltaY the amount to translate the y-coordinate
	 * @return a new {@linkplain TabletEvent} with the new component and coordinates
	 * @since 1.2.5
	 */
	public TabletEvent translated(Component c, float deltaX, float deltaY) {
		return withPoint(c, x+deltaX, y+deltaY);
	}

	/**
	 * Returns a copy of this {@linkplain TabletEvent} with translated
	 * coordinates.
	 *
	 * @see #translated(Component, float, float)
	 * @param deltaX the amount to translate the x-coordinate
	 * @param deltaY the amount to translate the y-coordinate
	 * @return a new {@linkplain TabletEvent} with the new coordinates
	 * @since 1.2.5
	 */
	public TabletEvent translated(float deltaX, float deltaY) {
		return translated(getComponent(), deltaX, deltaY);
	}
	/**
	 * Returns a copy of this {@linkplain TabletEvent} with a new
	 * event {@link Type}.
	 *
	 * @param type the new event type
	 * @return a new {@linkplain TabletEvent} with the given type
	 * @since 1.2.5
	 */
	public TabletEvent withType(Type type) {
		return new TabletEvent((Component)source, type, getWhen(), getModifiersEx(), 
				device, x, y, pressure,
				tiltX, tiltY, sidePressure,
				rotation,scrollX,scrollY,zoomFactor,
				getButton(), rawTabletButtonMask);
	}

	/**
	 * Constructs a {@linkplain TabletEvent} based on another with a
	 * new component and coordinates.
	 * 
	 * @see #withPoint(float, float)
	 * @param original the original tablet event to base the new one on
	 * @param newComponent the component for the new event
	 * @param newX x-coordinate
	 * @param newY y-coordinate
	 * @since 1.2.5
	 */
	public TabletEvent(TabletEvent original, Component newComponent, float newX, float newY) {
		this(newComponent, original.type, original.getWhen(), original.getModifiersEx(), 
				original.device, newX, newY, original.pressure,
				original.tiltX, original.tiltY, original.sidePressure,
				original.rotation,original.scrollX,original.scrollY,original.zoomFactor,
				original.getButton(), original.rawTabletButtonMask);
	}

	/**
	 * Constructs a {@linkplain TabletEvent} based on another with
	 * new coordinates.
	 *
	 * @see #withPoint(Component, float, float)
	 * @param original the original tablet event to base the new one on
	 * @param newX x-coordinate
	 * @param newY y-coordinate
	 * @since 1.2.5
	 */
	public TabletEvent(TabletEvent original, float newX, float newY) {
		this(original, original.getComponent(), newX, newY);
	}
	/**
	 * Constructs a {@linkplain TabletEvent} based on another with
	 * a new event {@link Type}.
	 *
	 * @param original the original tablet event to base the new one on
	 * @param newType the new event type
	 * @since 1.2.5
	 */
	public TabletEvent(TabletEvent original, Type newType) {
		this(original.getComponent(), newType, original.getWhen(), original.getModifiersEx(), 
				original.device, original.x, original.y, original.pressure,
				original.tiltX, original.tiltY, original.sidePressure,
				original.rotation,original.scrollX,original.scrollY,original.zoomFactor,
				original.getButton(), original.rawTabletButtonMask);
	}

}

package cello.jtablet.impl.jpen;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import jpen.PButtonEvent;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import jpen.owner.ScreenPenOwner;
import cello.jtablet.TabletDevice;
import cello.jtablet.TabletManager;
import cello.jtablet.events.TabletEvent;
import cello.jtablet.events.TabletListener;
import cello.jtablet.events.TabletEvent.Type;

/**
 * 
 * @author marcello
 */

public class JPenTabletManager extends TabletManager {

	private PenManager penManager;
	private JPenListener listener;
	private final Component component,eventSource;
	private final List<TabletListener> listeners = new ArrayList<TabletListener>();
	
	private class ScreenComponent extends Component {
		
	}

	/**
	 * @param component
	 */
	public JPenTabletManager(Component component) {
		this.component = component;
		this.eventSource = component == null ? new ScreenComponent() : component;
	}
	
	/**
	 * Add a listener to this component
	 * @param l
	 */
	public void addTabletListener(TabletListener l) {
		synchronized (listeners) {
			if (listeners.isEmpty()) {
				if (penManager==null) {
					penManager = component == null ? new PenManager(new ScreenPenOwner()) : new PenManager(component);
				}
				listener = new JPenListener();
				penManager.pen.addListener(listener);
			}
			listeners.add(l);
		}
	}
	/**
	 * @param l
	 */
	public void removeTabletListener(TabletListener l) {
		synchronized (listeners) {
			listeners.remove(l);
			if (listeners.isEmpty()) {
				penManager.pen.removeListener(listener);
				listener = null;
			}
		}
	}
	

	/**
	 * A wrapper that for JPen into our TabletEvent structure.  Makes it much
	 * more digestable and adds support for consuming events.
	 * 
	 * @author Marcello
	 */
	protected class JPenListener extends PenAdapter {
		private float x=0,y=0,pressure=0,tangentialPressure=0,tiltX=0,tiltY=0,rotation=0;
		private TabletDevice device = TabletDevice.BASIC_MOUSE;
		private int buttonMask = 0;

		@Override
		public void penButtonEvent(PButtonEvent ev) {
			// Translate the button
			int mask = 0;
			int button = MouseEvent.NOBUTTON;
			switch (ev.button.typeNumber) {
			case 0: // LEFT
				button = MouseEvent.BUTTON1;
				mask = MouseEvent.BUTTON1_DOWN_MASK;
				break;
			case 1: // CENTER
				button = MouseEvent.BUTTON2;
				mask = MouseEvent.BUTTON2_DOWN_MASK;
				break;
			case 2: // RIGHT
				button = MouseEvent.BUTTON3;
				mask = MouseEvent.BUTTON3_DOWN_MASK;
				break;
			case 3: // CUSTOM
			default:
				break;
			}
			boolean pressed = ev.button.value;
			if (pressed) {
				// flip mask bit on
				buttonMask |= mask;
			} else {
				// flip mask bit off
				buttonMask &= ~mask;
			}
			
			Type type = pressed ? TabletEvent.Type.PRESSED : TabletEvent.Type.RELEASED;
			fireTabletEvent(new TabletEvent(eventSource, type, ev.getTime(), device, buttonMask,
											x,y,
											button));
		}
		@Override
		public void penKindEvent(PKindEvent ev) {
			// Translate the device
			switch (ev.pen.getKind().getType()) {
				case CURSOR:
					device = TabletDevice.BASIC_MOUSE;
					break;
				case ERASER:
//					device = TabletDevice.STYLUS_ERASER;
					break;
				case STYLUS:
//					device = TabletDevice.STYLUS_TIP;
					break;
			}
			fireTabletEvent(new TabletEvent(eventSource, TabletEvent.Type.NEW_DEVICE, ev.getTime(), buttonMask, device, x, y));
		}
		@Override
		public void penLevelEvent(PLevelEvent ev) {
			boolean moved = false;
			boolean levelChanged = false;
			for (PLevel level : ev.levels) {
				switch (level.getType()) {
				case X:
					if (x != level.value) {
						x = level.value;
						moved = true;
					}
					break;
				case Y:
					if (y != level.value) {
						y = level.value;
						moved = true;
					}
					break;
				case PRESSURE:
					if (pressure != level.value) {
						pressure = level.value;
						levelChanged = true;
					}
					break;
				case TANGENTIAL_PRESSURE:
					if (tangentialPressure != level.value) {
						tangentialPressure = level.value;
						levelChanged = true;
					}
					break;
				case ROTATION:
					if (rotation != level.value) {
						rotation = level.value;
						levelChanged = true;
					}
					break;
				case TILT_X:
					if (tiltX != level.value) {
						tiltX = level.value;
						levelChanged = true;
					}
					break;
				case TILT_Y:
					if (tiltY != level.value) {
						tiltY = level.value;
						levelChanged = true;
					}
					break;
				}
			}

//			if (device == TabletDevice.MOUSE) {
//				if (!pressured && buttonMask != 0) {
//					pressure = 1;
//				} 
//				if (buttonMask == 0){
//					pressure = 0;
//				}
//			}
			if (moved) {
				// Dragging?
				if (pressure > 0) {
					fireTabletEvent(new TabletEvent(eventSource, TabletEvent.Type.DRAGGED, ev.getTime(), buttonMask, device, 
									x,y, pressure, tiltX,tiltY, tangentialPressure, rotation, TabletEvent.NOBUTTON));
				} else {
					fireTabletEvent(new TabletEvent(eventSource, TabletEvent.Type.MOVED, ev.getTime(), buttonMask, device,
							x,y, pressure, tiltX,tiltY, tangentialPressure, rotation, TabletEvent.NOBUTTON));
				}
			} else if (levelChanged) {
				fireTabletEvent(new TabletEvent(eventSource, TabletEvent.Type.LEVEL_CHANGED, ev.getTime(), buttonMask, device,
						x,y,pressure, tiltX,tiltY, tangentialPressure, rotation, TabletEvent.NOBUTTON));
			}
		}
	}
	
	protected void fireTabletEvent(TabletEvent ev) {
		synchronized (listeners) {
			for (TabletListener l : listeners) {
				ev.fireEvent(l);
				if (ev.isConsumed())
					break;
			}
		}
	}
}

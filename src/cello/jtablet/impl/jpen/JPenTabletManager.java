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
	private final Component component;
	private final List<TabletListener> listeners = new ArrayList<TabletListener>();
	

	/**
	 * @param component
	 */
	public JPenTabletManager(Component component) {
		this.component = component;
	}
	
	/**
	 * Add a listener to this component
	 * @param l
	 */
	public void addTabletListener(TabletListener l) {
		synchronized (listeners) {
			if (listeners.isEmpty()) {
				if (penManager==null) {
					penManager = new PenManager(component);
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
		private float x=0,y=0,pressure=0;
		private TabletDevice device = null;
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
			fireTabletEvent(new TabletEvent(component, type, ev.getTime(), device, buttonMask,
											x,y,
											button));
		}
		@Override
		public void penKindEvent(PKindEvent ev) {
			// Translate the device
			switch (ev.pen.getKind().getType()) {
				case CURSOR:
					device = TabletDevice.MOUSE;
					break;
				case ERASER:
					device = TabletDevice.STYLUS_ERASER;
					break;
				case STYLUS:
					device = TabletDevice.STYLUS;
					break;
			}
			fireTabletEvent(new TabletEvent(component, TabletEvent.Type.NEW_DEVICE, ev.getTime(), device));
		}
		@Override
		public void penLevelEvent(PLevelEvent ev) {
			boolean moved = false;
			boolean pressured = false;
			for (PLevel level : ev.levels) {
				switch (level.getType()) {
				case X:
					x = level.value;
					moved = true;
					break;
				case Y:
					y = level.value;
					moved = true;
					break;
				case PRESSURE:
					pressure = level.value;
					pressured = true;
					break;
				}
			}

			if (device == TabletDevice.MOUSE && !pressured) {
				pressure = 1;
			}
			if (moved) {
				// Dragging?
				if (buttonMask != 0) {
					fireTabletEvent(new TabletEvent(component, TabletEvent.Type.DRAGGED, ev.getTime(), device, buttonMask,
									x,y, pressure));
				} else {
					fireTabletEvent(new TabletEvent(component, TabletEvent.Type.MOVED, ev.getTime(), device, buttonMask,
							x,y, pressure));
				}
			} else if (pressured) {
				fireTabletEvent(new TabletEvent(component, TabletEvent.Type.LEVEL_CHANGED, ev.getTime(), device, buttonMask,
						x,y,pressure));
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

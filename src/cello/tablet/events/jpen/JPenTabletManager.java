package cello.tablet.events.jpen;

import java.awt.Component;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import jpen.PButtonEvent;
import jpen.PKindEvent;
import jpen.PLevel;
import jpen.PLevelEvent;
import jpen.PenManager;
import jpen.event.PenAdapter;
import cello.tablet.events.TabletDevice;
import cello.tablet.events.TabletEvent;
import cello.tablet.events.TabletListener;
import cello.tablet.events.TabletManager;

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
		private TabletDevice.Device device = TabletDevice.Device.MOUSE;
		private int buttonsPressed = 0;

		@Override
		public void penButtonEvent(PButtonEvent ev) {
			// Translate the button
			TabletEvent.Button button = TabletEvent.Button.values()[ev.button.typeNumber];

			boolean pressed = ev.button.value;

			// Count current buttons being held
			if (pressed)
				buttonsPressed++;
			else
				buttonsPressed--;
			
			fireTabletEvent(new TabletEvent(component,
					pressed ? TabletEvent.Type.PRESSED : TabletEvent.Type.RELEASED,
					ev.getTime(),
					new Point2D.Float(x,y),
					button));
		}
		@Override
		public void penKindEvent(PKindEvent ev) {
			// Translate the device
			switch (ev.pen.getKind().getType()) {
				case CURSOR:
					device = TabletDevice.Device.MOUSE;
					break;
				case ERASER:
					device = TabletDevice.Device.STYLUS_ERASER;
					break;
				case STYLUS:
					device = TabletDevice.Device.STYLUS;
					break;
			}
			fireTabletEvent(new TabletEvent(component,
					TabletEvent.Type.NEW_DEVICE, 
					ev.getTime(),
					new TabletDevice(device)));
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
			if (moved) {
				// Dragging?
				if (buttonsPressed > 0) {
					fireTabletEvent(new TabletEvent(component,
							TabletEvent.Type.DRAGGED,
							ev.getTime(),
							new Point2D.Float(x,y),
							pressure));
				} else {
					fireTabletEvent(new TabletEvent(component,
							TabletEvent.Type.MOVED,
							ev.getTime(),
							new Point2D.Float(x,y),
							pressure));
				}
			} else if (pressured) {
				fireTabletEvent(new TabletEvent(component,
						TabletEvent.Type.LEVEL_CHANGED,
						ev.getTime(),
						new Point2D.Float(x,y),
						pressure));
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
